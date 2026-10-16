package com.pf.common.service.suggestion;

import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.dto.suggestion.SuggestionRequest;
import com.pf.common.enums.QueryType;
import com.pf.common.enums.SortOrder;
import com.pf.common.enums.SuggestionField;
import com.pf.common.service.generic.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.pf.common.constants.CommonConstants.*;
import static com.pf.common.enums.FilterOperator.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SuggestionService extends BaseService {

    public List<?> fetchSuggestions(SuggestionRequest suggestionRequest) {
        SuggestionField suggestionField = SuggestionField.resolve(suggestionRequest.getEntity());
        SearchCriteria searchCriteria = createSearchCriteria(suggestionRequest);
        if (suggestionField.getQueryType() == QueryType.ENTITY) {
            return getSuggestionBySearchCriteria(suggestionField.getEntityClass(), searchCriteria, suggestionRequest.getField());
        }
        throw new UnsupportedOperationException("Query type not yet supported: " + suggestionField.getQueryType());
    }

    private static @NonNull SearchCriteria createSearchCriteria(SuggestionRequest suggestionRequest) {
        SearchCriteria searchCriteria = new SearchCriteria();
        addNonnullFilter(suggestionRequest, searchCriteria);
        addSuggestionFilter(searchCriteria, suggestionRequest.getField(), suggestionRequest.getValue());
        if (suggestionRequest.getDependentField() != null) {
            addSuggestionFilter(searchCriteria, suggestionRequest.getDependentField(), suggestionRequest.getDependentValue());
        }
        searchCriteria.addSort(suggestionRequest.getField(), SortOrder.ASC.getValue());
        searchCriteria.setTake(suggestionRequest.getLimit());
        return searchCriteria;
    }

    private static void addNonnullFilter(SuggestionRequest suggestionRequest, SearchCriteria searchCriteria) {
        searchCriteria.addFilter(suggestionRequest.getField(), IS_NOT_NULL.getValue());
    }

    private static void addSuggestionFilter(SearchCriteria searchCriteria, String field, String input) {
        if (input == null || input.isBlank()) {
            return;
        }
        String value = input.trim().replace(ASTERISK_CHAR, LIKE_CHAR).replaceAll("%+", LIKE_STR);
        // Ignore input containing only wildcards.
        if (value.chars().allMatch(c -> c == LIKE_CHAR)) {
            return;
        }
        if (!value.contains(LIKE_STR)) {
            searchCriteria.addFilter(field, EQUALS.getValue(), value);
            return;
        }
        String operator = getOperator(value);
        searchCriteria.addFilter(field, operator, value);
    }


    private static String getOperator(String pattern) {
        boolean startsWithWildcard = pattern.startsWith(LIKE_STR);
        boolean endsWithWildcard = pattern.endsWith(LIKE_STR);
        if (startsWithWildcard && endsWithWildcard) {
            return CONTAINS.getValue();
        }
        if (startsWithWildcard) {
            return ENDS_WITH.getValue();
        }
        if (endsWithWildcard) {
            return STARTS_WITH.getValue();
        }
        return CONTAINS.getValue();
    }

}
