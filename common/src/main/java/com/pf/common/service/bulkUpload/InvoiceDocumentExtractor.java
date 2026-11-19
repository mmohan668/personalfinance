package com.pf.common.service.bulkUpload;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InvoiceDocumentExtractor {
    private static final Pattern INVOICE_NUMBER = Pattern.compile(
            "(?i)\\binvoice\\s*(?:number|no\\.?|#)\\s*[:#]?\\s*([A-Z0-9][A-Z0-9./-]{2,})");
    private static final Pattern DATE_VALUE = Pattern.compile(
            "(?i)(?:invoice\\s*date|date\\s+of\\s+invoice)\\s*[:#-]?\\s*(\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}|\\d{1,2}\\s+[A-Za-z]{3,9}\\s+\\d{4})");
    private static final Pattern MONEY = Pattern.compile("(?:Rs\\.?\\s*)?\\s*([0-9]+(?:,[0-9]{3})*\\.[0-9]{2})");
    private static final Pattern ITEM_ROW = Pattern.compile("^\\s*\\d{1,3}[.)]?\\s+(?=.*\\p{L}).{3,}");
    private static final Pattern ITEM_HEADER = Pattern.compile("(?i)\\b(description|product|particulars|item)\\b");
    private static final Pattern ITEM_END = Pattern.compile(
            "(?i)^\\s*(?:grand\\s+total|sub\\s*total|total\\s*:|total\\s+(?:payable|amount)|amount\\s+payable|sold\\s+by|billing\\s+address|shipping\\s+address|amount\\s+in\\s+words|payment\\s+transaction|whether\\s+tax)");
    private static final Set<String> TABLE_HEADER_LINES = Set.of(
            "description", "product", "item", "particulars", "sl.", "sl", "sl. no", "sl no", "no", "unit", "unit price",
            "qty", "quantity", "net amount", "tax", "tax rate", "tax type", "tax amount", "total amount",
            "net", "total", "amount", "price", "hsn", "hsn/sac", "rate");

    private final String tesseractCommand;

    public InvoiceDocumentExtractor(@Value("${invoice.ocr.tesseract-command:tesseract}") String tesseractCommand) {
        this.tesseractCommand = tesseractCommand;
    }

    public ExtractedInvoice extract(Path file) throws IOException {
        String extension = extension(file);
        List<String> pages = switch (extension) {
            case "pdf" -> extractPdfPages(file);
            case "png", "jpg", "jpeg" -> List.of(ocr(Files.readAllBytes(file)));
            default -> throw new IllegalArgumentException("Supported invoice formats are PDF, PNG, JPG and JPEG");
        };
        String fullText = String.join("\n", pages);
        LocalDate invoiceDate = findInvoiceDate(fullText);
        List<String> invoiceNumbers = findInvoiceNumbers(fullText);
        java.math.BigDecimal invoiceTotal = findInvoiceTotal(fullText);
        String itemsText = extractItemsSection(pages);
        Path extractedTextFile = saveExtractedText(file, itemsText);
        log.info("Saved extracted invoice text for Groq to {}", extractedTextFile);
        if (invoiceDate == null) throw new IllegalArgumentException("Invoice date was not found");
        if (invoiceNumbers.isEmpty()) throw new IllegalArgumentException("Invoice number was not found");
        if (invoiceTotal == null) throw new IllegalArgumentException("Invoice total was not found");
        if (itemsText.isBlank()) throw new IllegalArgumentException("Invoice items section was not found");
        return new ExtractedInvoice(invoiceDate, invoiceNumbers, invoiceTotal, itemsText);
    }

    private Path saveExtractedText(Path invoiceFile, String itemsText) throws IOException {
        String fileName = invoiceFile.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        String baseName = extensionStart > 0 ? fileName.substring(0, extensionStart) : fileName;
        Path textFile = invoiceFile.resolveSibling(baseName + ".extracted.txt");
        Files.writeString(textFile, itemsText, java.nio.charset.StandardCharsets.UTF_8);
        return textFile;
    }

    private List<String> extractPdfPages(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            List<String> pages = new ArrayList<>();
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                stripper.setStartPage(page + 1);
                stripper.setEndPage(page + 1);
                String pageText = stripper.getText(document);
                if (pageText != null) pageText = normalizePdfCurrencyGlyph(pageText);
                pages.add(pageText == null ? "" : pageText);
            }
            return pages;
        }
    }

    private String ocr(byte[] imageBytes) throws IOException {
        Process process;
        try {
            process = new ProcessBuilder(resolveTesseractCommand(), "stdin", "stdout", "-l", "eng", "--psm", "6").start();
        } catch (IOException e) {
            throw new IllegalStateException("Tesseract OCR is unavailable. Install Tesseract or set invoice.ocr.tesseract-command", e);
        }
        CompletableFuture<byte[]> stdout = CompletableFuture.supplyAsync(() -> readAll(process.getInputStream()));
        CompletableFuture<byte[]> stderr = CompletableFuture.supplyAsync(() -> readAll(process.getErrorStream()));
        try {
            process.getOutputStream().write(imageBytes);
            process.getOutputStream().close();
            if (!process.waitFor(90, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("Tesseract OCR exceeded the 90 second time limit");
            }
            String errorOutput = new String(stderr.get(5, TimeUnit.SECONDS), java.nio.charset.StandardCharsets.UTF_8);
            String text = new String(stdout.get(5, TimeUnit.SECONDS), java.nio.charset.StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Tesseract OCR failed: " + errorOutput.trim());
            }
            return text;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running Tesseract OCR", e);
        } catch (java.util.concurrent.ExecutionException | java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException("Unable to collect Tesseract OCR output", e);
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
    }

    private String resolveTesseractCommand() {
        if (!"tesseract".equalsIgnoreCase(tesseractCommand)) return tesseractCommand;

        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null) {
            Path standardInstall = Path.of(programFiles, "Tesseract-OCR", "tesseract.exe");
            if (Files.isRegularFile(standardInstall)) return standardInstall.toString();
        }
        return tesseractCommand;
    }

    private byte[] readAll(java.io.InputStream input) {
        try (input) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Tesseract output", e);
        }
    }

    private String extension(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private LocalDate findInvoiceDate(String text) {
        Matcher matcher = DATE_VALUE.matcher(text);
        while (matcher.find()) {
            LocalDate parsed = parseDate(matcher.group(1));
            if (parsed != null) return parsed;
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ofPattern("d-M-uuuu"),
                DateTimeFormatter.ofPattern("d.M.uuuu"), DateTimeFormatter.ofPattern("d/M/uu"),
                DateTimeFormatter.ofPattern("d-M-uu"), DateTimeFormatter.ofPattern("d.M.uu"),
                DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH))) {
            try { return LocalDate.parse(value.trim(), formatter); }
            catch (DateTimeParseException ignored) { }
        }
        return null;
    }

    private List<String> findInvoiceNumbers(String text) {
        Matcher matcher = INVOICE_NUMBER.matcher(text);
        LinkedHashSet<String> numbers = new LinkedHashSet<>();
        while (matcher.find()) numbers.add(matcher.group(1).trim());
        return List.copyOf(numbers);
    }

    private java.math.BigDecimal findInvoiceTotal(String text) {
        Matcher invoiceMatcher = INVOICE_NUMBER.matcher(text);
        List<InvoiceSegment> segments = new ArrayList<>();
        while (invoiceMatcher.find()) {
            segments.add(new InvoiceSegment(invoiceMatcher.group(1).trim(), invoiceMatcher.start()));
        }
        if (segments.isEmpty()) return findPayableTotal(text);

        Map<String, java.math.BigDecimal> totalsByInvoice = new LinkedHashMap<>();
        for (int i = 0; i < segments.size(); i++) {
            InvoiceSegment segment = segments.get(i);
            int end = i + 1 < segments.size() ? segments.get(i + 1).position() : text.length();
            java.math.BigDecimal amount = findPayableTotal(text.substring(segment.position(), end));
            if (amount != null) totalsByInvoice.put(segment.number(), amount);
        }
        if (totalsByInvoice.isEmpty()) return null;
        return totalsByInvoice.values().stream().reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    private java.math.BigDecimal findPayableTotal(String text) {
        java.math.BigDecimal invoiceValue = null;
        Matcher invoiceValueMatcher = Pattern.compile("(?i)invoice\\s+value\\s*[:\\-]?\\s*(?:Rs\\.?\\s*)?([0-9,]+\\.\\d{2})").matcher(text);
        while (invoiceValueMatcher.find()) invoiceValue = money(invoiceValueMatcher.group(1));
        if (invoiceValue != null) return invoiceValue;

        java.math.BigDecimal payable = null;
        Matcher payableMatcher = Pattern.compile("(?i)(?:grand\\s+total|invoice\\s+total|total\\s+payable|amount\\s+payable|total\\s+amount)\\s*[:\\-]?[^\\r\\n]*").matcher(text);
        while (payableMatcher.find()) {
            java.math.BigDecimal value = lastMoney(payableMatcher.group());
            if (value != null) payable = value;
        }
        if (payable != null) return payable;

        java.math.BigDecimal total = null;
        Matcher totalMatcher = Pattern.compile("(?im)^\\s*total\\s*:[^\\r\\n]*").matcher(text);
        while (totalMatcher.find()) {
            java.math.BigDecimal value = lastMoney(totalMatcher.group());
            if (value != null) total = value;
        }
        return total;
    }

    private java.math.BigDecimal lastMoney(String line) {
        Matcher matcher = MONEY.matcher(line);
        java.math.BigDecimal amount = null;
        while (matcher.find()) amount = money(matcher.group(1));
        return amount;
    }

    private java.math.BigDecimal money(String value) {
        try { return new java.math.BigDecimal(value.replace(",", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private String extractItemsSection(List<String> pages) {
        List<String> itemLines = new ArrayList<>();
        String previousTableHeader = null;
        for (String page : pages) {
            List<String> lines = page.lines().map(String::trim).toList();
            int cursor = 0;
            while (cursor < lines.size()) {
                int header = findItemHeader(lines, cursor);
                int unheadedItemLine = findFirstUnheadedItemLine(lines, cursor);
                if (header >= 0 && unheadedItemLine >= 0 && unheadedItemLine < header) header = -1;
                int firstItemLine = header >= 0 ? findFirstItemLine(lines, header + 1) : -1;
                // Start at the table header so no early product row is dropped merely because its
                // amount is on a separate line or PDFBox exposes fewer than two money values.
                int start = header >= 0 ? header : unheadedItemLine;
                if (start < 0 || start >= lines.size()) break;

                if (header >= 0) {
                    int headerEnd = firstItemLine >= 0 ? firstItemLine : Math.min(lines.size(), header + 12);
                    String tableHeader = lines.subList(header, headerEnd).stream()
                            .filter(line -> !line.isBlank())
                            .collect(Collectors.joining(" | "));
                    if (!tableHeader.isBlank()) {
                        previousTableHeader = tableHeader;
                        itemLines.add("TABLE HEADER: " + tableHeader);
                    }
                } else if (previousTableHeader != null) {
                    itemLines.add("TABLE HEADER (continued): " + previousTableHeader);
                }

                boolean sectionEnded = false;
                for (int i = start; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (ITEM_END.matcher(line).find()) {
                        cursor = i + 1;
                        sectionEnded = true;
                        break;
                    }
                    if (INVOICE_NUMBER.matcher(line).find()) {
                        cursor = i;
                        sectionEnded = true;
                        break;
                    }
                    if (!line.isBlank() && !isBoilerplateLine(line)) itemLines.add(stripHsnLabel(line));
                }
                if (!sectionEnded) break;
            }
        }
        return itemLines.stream().collect(Collectors.joining("\n")).trim();
    }

    private int findFirstItemLine(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || ITEM_END.matcher(line).find()
                    || INVOICE_NUMBER.matcher(line).find()
                    || line.matches("(?i)^\\s*(?:invoice|order|date|payment|billing|shipping|sold\\s+by)\\b.*")
                    || !line.matches(".*\\p{L}.*")) continue;
            Matcher moneyMatcher = MONEY.matcher(line);
            if (moneyMatcher.find()) return i;
        }
        return -1;
    }

    private String normalizePdfCurrencyGlyph(String text) {
        // Some invoice PDF fonts expose the rupee glyph as a digit 7, often glued to the amount.
        return text.replaceAll("(?<![\\p{L}\\d])7(?=\\s+[0-9]+(?:,[0-9]{3})*\\.\\d{2}\\b|\\d{2,}(?:,[0-9]{3})*\\.\\d{2}\\b)", "₹");
    }

    private int findFirstUnheadedItemLine(List<String> lines, int start) {
        int itemLine = findFirstItemLine(lines, start);
        return itemLine >= 0 ? itemLine : findFirstItemRow(lines, start);
    }

    private int findItemHeader(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            if (!ITEM_HEADER.matcher(lines.get(i)).find()) continue;
            String window = lines.subList(i, Math.min(lines.size(), i + 24)).stream()
                    .collect(Collectors.joining(" ")).toLowerCase(Locale.ROOT);
            if ((window.contains("qty") || window.contains("quantity"))
                    && (window.contains("amount") || window.contains("price") || window.contains("total"))) return i;
        }
        return -1;
    }

    private int findFirstItemRow(List<String> lines) {
        return findFirstItemRow(lines, 0);
    }

    private int findFirstItemRow(List<String> lines, int start) {
        for (int i = start; i < lines.size(); i++) {
            if (ITEM_ROW.matcher(lines.get(i)).matches()) return i;
        }
        return -1;
    }

    private boolean isBoilerplateLine(String line) {
        String normalized = line.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        boolean itemColumnHeader = ITEM_HEADER.matcher(normalized).find()
                && Pattern.compile("(?i)\\b(qty|quantity|amount|price|rate|total|tax|hsn)\\b").matcher(normalized).find()
                && !MONEY.matcher(normalized).find();
        boolean hsnOnlyLine = (normalized.startsWith("hsn:") || normalized.startsWith("hsn/sac:"))
                && !MONEY.matcher(normalized).find();
        return TABLE_HEADER_LINES.contains(normalized)
                || itemColumnHeader
                || hsnOnlyLine
                || normalized.matches("\\d+(?:\\.\\d+)?%\\s+(?:cgst|sgst|igst|gst).*" )
                || normalized.matches("(?:cgst|sgst|igst|gst)\\s+\\d+(?:\\.\\d+)?%.*");
    }

    private String stripHsnLabel(String line) {
        return line.replaceFirst("(?i)^\\s*hsn(?:/sac)?\\s*:\\s*[A-Z0-9-]+\\s*", "").trim();
    }

    public record ExtractedInvoice(LocalDate invoiceDate, List<String> invoiceNumbers,
                                   java.math.BigDecimal invoiceTotal, String itemsText) { }

    private record InvoiceSegment(String number, int position) { }
}
