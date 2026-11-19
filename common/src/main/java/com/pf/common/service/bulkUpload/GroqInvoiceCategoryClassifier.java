package com.pf.common.service.bulkUpload;

import com.pf.common.projection.InvoiceCategoryOption;
import com.pf.common.repository.categoryManagement.UserSubcategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqInvoiceCategoryClassifier {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_RATE_LIMIT_ATTEMPTS = 3;
    private static final Pattern RETRY_DELAY = Pattern.compile("(?i)try again in\\s+([0-9]+(?:\\.[0-9]+)?)\\s*s");
    private static final String SYSTEM_PROMPT = """
            ITEM_SECTION may contain multiple invoice sections and TABLE HEADER blocks. Read each header to
            identify its columns and extract product line items from every section; do not stop after the first
            invoice or table. For Amount, use the product's final payable line total
            after item discounts, including tax when it is included in that final total. Do not use a pre-tax
            taxable value. For example, if a row shows values 52.00, 0.00 discount, 49.52 taxable value,
            1.24 CGST, 1.24 SGST, and 52.00 final row total, Amount is 52.00. Do not calculate amounts from
            quantity, unit rate, tax/GST columns, or add those columns together. A standalone 7 immediately
            before a monetary value may be a corrupted rupee symbol; treat it as currency notation, not a
            quantity or amount. Ignore every item, fee, or adjustment row whose final payable amount is zero,
            including rows fully offset by discounts. Ignore invoice subtotal and total rows. Include every
            product row and every separately charged fee/adjustment row with a positive final payable amount
            exactly once when it contributes to the invoice total. This includes positive shipping, handling,
            marketplace, convenience, and gift-wrap fees. Do not omit a positive row because it is inexpensive,
            unfamiliar, or hard to classify; use the applicable Miscellaneous pair.
            Before responding, verify that the sum of all returned Amount values equals the sum of final payable
            amounts for every positive product and separately charged fee/adjustment row in ITEM_SECTION.
            For Remarks, return only the complete product description as printed, preserving every description
            word. For a fee or adjustment row, return its concise row description (for example, "Marketplace Fees").
            Remove quantity, unit rate, tax/GST values, line amounts, SKU/ASIN/product codes, and table column
            values; do not truncate product descriptions.
            ALLOWED_CATEGORIES maps each category to subcategories with a name and description. Use each
            subcategory description as classification guidance, but return only the exact category and
            subcategory names provided. Classify with an exact pair from ALLOWED_CATEGORIES. If the item cannot be classified to a suitable
            exact pair, use an available "Miscellaneous" subcategory under its known category. If the category
            is unknown or that pair is unavailable, use the exact "Miscellaneous" / "Miscellaneous" pair when
            present in ALLOWED_CATEGORIES. Use "Unidentified" for both values only when no applicable
            Miscellaneous pair exists. Never invent a category or subcategory.
            Preserve all description words. Combine same valid non-Miscellaneous pairs by summing Amount and
            joining distinct descriptions; keep Miscellaneous and Unidentified items separate. Return itemsTotal
            as the exact sum of the returned transaction Amount values. For every Unidentified item, add an
            object to miscellaneousSuggestions
            with itemIndex, full Remarks, SuggestedCategory, and SuggestedSubcategory. Suggest the best-fit
            category and subcategory even if the suggested pair is not currently available; never replace the
            transaction's Unidentified values with the suggestion. Return JSON matching the requested schema.
            """;

    private final UserSubcategoryRepository userSubcategoryRepository;
    private final ObjectMapper objectMapper;

    @Value("${groq.api-key:${GROQ_API_KEY:}}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-20b}")
    private String model;

    @Value("${groq.max-completion-tokens:8192}")
    private int maxCompletionTokens;

    public InvoiceAnalysis analyze(String itemsText, Long adminUserId) {
        if (itemsText == null || itemsText.isBlank()) {
            throw new IllegalArgumentException("Invoice items section is empty");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is not configured; set groq.api-key or GROQ_API_KEY on the server");
        }

        userSubcategoryRepository.ensureMiscellaneousExpenseCategory(adminUserId, adminUserId);
        userSubcategoryRepository.ensureMiscellaneousSubcategoryForExpenseCategories(adminUserId, adminUserId);
        List<InvoiceCategoryOption> options = userSubcategoryRepository.findActiveExpenseCategoryOptions(adminUserId);
        if (options.isEmpty()) {
            throw new IllegalStateException("No active Expense category and subcategory combinations are configured");
        }
        JsonNode response = requestAnalysis(itemsText, options);
        JsonNode rows = response.path("transactions");
        if (!rows.isArray()) throw new IllegalStateException("Groq returned no invoice transaction list");

        Map<String, Map<String, InvoiceCategoryOption>> optionsByCategory = new HashMap<>();
        for (InvoiceCategoryOption option : options) {
            optionsByCategory.computeIfAbsent(key(option.getCategoryName()), ignored -> new HashMap<>())
                    .put(key(option.getSubcategoryName()), option);
        }

        List<ClassifiedInvoiceItem> transactions = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            JsonNode row = rows.get(i);
            JsonNode amountNode = row.get("Amount");
            if (amountNode == null || !amountNode.isNumber()) {
                throw new IllegalStateException("Groq returned an invalid amount for invoice item " + (i + 1));
            }
            BigDecimal amount = amountNode.decimalValue();
            String remarks = row.path("Remarks").asString("").trim();
            if (amount.signum() <= 0 || remarks.isBlank()) {
                throw new IllegalStateException("Groq returned an invalid amount or blank description for invoice item " + (i + 1));
            }
            String categoryName = row.path("Category").asString("").trim();
            String subcategoryName = row.path("Subcategory").asString("").trim();
            Map<String, InvoiceCategoryOption> categoryOptions = optionsByCategory.get(key(categoryName));
            InvoiceCategoryOption match = categoryOptions == null ? null : categoryOptions.get(key(subcategoryName));
            boolean groqUnidentified = "Unidentified".equalsIgnoreCase(categoryName)
                    || "Unidentified".equalsIgnoreCase(subcategoryName);
            if (match == null || groqUnidentified) {
                match = miscellaneousFallback(optionsByCategory, categoryName);
                if (match != null) {
                    log.info("Applying available Miscellaneous category fallback for invoice item: remarks={}, category={}, subcategory={}",
                            remarks, match.getCategoryName(), match.getSubcategoryName());
                }
            }
            boolean unidentified = match == null;
            transactions.add(new ClassifiedInvoiceItem(amount, remarks,
                    unidentified ? null : match.getCategoryId(),
                    unidentified ? null : match.getSubcategoryId(),
                    unidentified ? "Unidentified" : match.getCategoryName(),
                    unidentified ? "Unidentified" : match.getSubcategoryName(),
                    unidentified));
        }

        JsonNode totalNode = response.get("itemsTotal");
        if (totalNode == null || !totalNode.isNumber()) {
            throw new IllegalStateException("Groq returned an invalid itemsTotal");
        }
        JsonNode suggestions = response.path("miscellaneousSuggestions");
        if (!suggestions.isArray()) {
            throw new IllegalStateException("Groq returned invalid category/subcategory suggestions");
        }
        return new InvoiceAnalysis(totalNode.decimalValue(), List.copyOf(transactions), serialize(suggestions));
    }

    private InvoiceCategoryOption miscellaneousFallback(
            Map<String, Map<String, InvoiceCategoryOption>> optionsByCategory,
            String requestedCategory
    ) {
        Map<String, InvoiceCategoryOption> requestedCategoryOptions = optionsByCategory.get(key(requestedCategory));
        if (requestedCategoryOptions != null && !"Unidentified".equalsIgnoreCase(requestedCategory)) {
            InvoiceCategoryOption categoryFallback = requestedCategoryOptions.get(key("Miscellaneous"));
            if (categoryFallback != null) return categoryFallback;
        }
        Map<String, InvoiceCategoryOption> miscellaneousCategory = optionsByCategory.get(key("Miscellaneous"));
        return miscellaneousCategory == null ? null : miscellaneousCategory.get(key("Miscellaneous"));
    }

    private String key(String value) { return value.trim().toLowerCase(Locale.ROOT); }

    private JsonNode requestAnalysis(String itemsText, List<InvoiceCategoryOption> options) {
        Map<String, List<Map<String, String>>> allowedCategories = new LinkedHashMap<>();
        for (InvoiceCategoryOption option : options) {
            allowedCategories.computeIfAbsent(option.getCategoryName(), ignored -> new ArrayList<>())
                    .add(Map.of(
                            "name", option.getSubcategoryName(),
                            "description", option.getSubcategoryDescription() == null
                                    ? "" : option.getSubcategoryDescription()));
        }
        allowedCategories.replaceAll((category, subcategories) -> subcategories.stream().distinct().toList());
        String serializedPayload = serialize(Map.of(
                "ITEM_SECTION", itemsText,
                "ALLOWED_CATEGORIES", allowedCategories));
        log.info("Sending invoice item extraction and classification request to Groq: model={}, itemSectionLength={}, categoryCount={}, payload={}",
                model, itemsText.length(), allowedCategories.size(), serializedPayload);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", serializedPayload)));
        requestBody.put("response_format", strictInvoiceResponseFormat());
        requestBody.put("max_completion_tokens", maxCompletionTokens);
        if (model.startsWith("openai/gpt-oss-")) requestBody.put("reasoning_effort", "low");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(90));
        RestTemplate restTemplate = new RestTemplate(factory);
        String serializedRequest = serialize(requestBody);
        for (int attempt = 1; ; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(API_URL,
                        new HttpEntity<>(serializedRequest, headers), String.class);
                if (response.getBody() == null) throw new IllegalStateException("Groq returned an empty response");
                log.info("Groq invoice extraction and classification response: status={}, body={}", response.getStatusCode(), response.getBody());
                JsonNode apiResponse = objectMapper.readTree(response.getBody());
                String content = apiResponse.path("choices").path(0).path("message").path("content").asString("");
                if (content.isBlank()) throw new IllegalStateException("Groq returned no invoice analysis content");
                return objectMapper.readTree(content);
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() == 429 && attempt < MAX_RATE_LIMIT_ATTEMPTS) {
                    long delayMillis = retryDelayMillis(e, attempt);
                    log.warn("Groq rate limit reached; retrying invoice analysis in {} ms (attempt {}/{})",
                            delayMillis, attempt + 1, MAX_RATE_LIMIT_ATTEMPTS);
                    try { Thread.sleep(delayMillis); }
                    catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while waiting to retry Groq invoice analysis", interrupted);
                    }
                    continue;
                }
                log.error("Groq invoice extraction and classification error response: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
                throw new IllegalStateException("Groq invoice analysis request failed", e);
            } catch (RestClientException e) {
                throw new IllegalStateException("Groq invoice analysis request failed", e);
            }
        }
    }

    private long retryDelayMillis(RestClientResponseException error, int attempt) {
        String retryAfter = error.getResponseHeaders() == null ? null : error.getResponseHeaders().getFirst("Retry-After");
        if (retryAfter != null) {
            try { return Math.min(30_000L, Math.max(1_000L, (long) (Double.parseDouble(retryAfter.trim()) * 1_000))); }
            catch (NumberFormatException ignored) { }
        }
        Matcher matcher = RETRY_DELAY.matcher(error.getResponseBodyAsString());
        if (matcher.find()) {
            try { return Math.min(30_000L, Math.max(1_000L, (long) (Double.parseDouble(matcher.group(1)) * 1_000) + 250)); }
            catch (NumberFormatException ignored) { }
        }
        return Math.min(8_000L * attempt, 30_000L);
    }

    private Map<String, Object> strictInvoiceResponseFormat() {
        Map<String, Object> transactionProperties = Map.of(
                "Amount", Map.of("type", "number"),
                "Category", Map.of("type", "string"),
                "Subcategory", Map.of("type", "string"),
                "Remarks", Map.of("type", "string"));
        Map<String, Object> transactionSchema = Map.of("type", "object", "properties", transactionProperties,
                "required", List.of("Amount", "Category", "Subcategory", "Remarks"), "additionalProperties", false);
        Map<String, Object> suggestionProperties = Map.of(
                "itemIndex", Map.of("type", "integer"), "Remarks", Map.of("type", "string"),
                "SuggestedCategory", Map.of("type", "string"), "SuggestedSubcategory", Map.of("type", "string"));
        Map<String, Object> suggestionSchema = Map.of("type", "object", "properties", suggestionProperties,
                "required", List.of("itemIndex", "Remarks", "SuggestedCategory", "SuggestedSubcategory"),
                "additionalProperties", false);
        Map<String, Object> invoiceProperties = Map.of(
                "itemsTotal", Map.of("type", "number"),
                "transactions", Map.of("type", "array", "items", transactionSchema),
                "miscellaneousSuggestions", Map.of("type", "array", "items", suggestionSchema));
        Map<String, Object> invoiceSchema = Map.of("type", "object", "properties", invoiceProperties,
                "required", List.of("itemsTotal", "transactions", "miscellaneousSuggestions"), "additionalProperties", false);
        return Map.of("type", "json_schema", "json_schema", Map.of(
                "name", "invoice_items_analysis", "strict", true, "schema", invoiceSchema));
    }

    private String serialize(Object value) { return objectMapper.writeValueAsString(value); }

    public record ClassifiedInvoiceItem(BigDecimal amount, String remarks, Long categoryId, Long subcategoryId,
                                        String categoryName, String subcategoryName, boolean unidentified) { }
    public record InvoiceAnalysis(BigDecimal itemsTotal, List<ClassifiedInvoiceItem> transactions,
                                  String categorySubcategorySuggestion) { }
}
