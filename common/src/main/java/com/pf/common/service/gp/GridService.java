package com.pf.common.service.gp;

import com.pf.common.dto.gp.*;
import com.pf.common.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GridService {
    private final List<ProductDTO> products = List.of(
            new ProductDTO(1L, "Laptop", "Electronics", BigDecimal.valueOf(75000), 10),
            new ProductDTO(2L, "Mobile Phone", "Electronics", BigDecimal.valueOf(35000), 25),
            new ProductDTO(3L, "Keyboard", "Accessories", BigDecimal.valueOf(2500), 50),
            new ProductDTO(4L, "Monitor", "Electronics", BigDecimal.valueOf(18000), 15),
            new ProductDTO(5L, "Mouse", "Accessories", BigDecimal.valueOf(1200), 100),
            new ProductDTO(5L, "Mobile", "Electronics", BigDecimal.valueOf(25000), 15)
    );

    public List<ProductDTO> getProducts(SearchCriteria searchCriteria) {
        Stream<ProductDTO> stream = products.stream();

        stream = applyFilters(stream, searchCriteria.getFilterList());

        stream = applySorting(
                stream,
                searchCriteria.getSort()
        );

        return stream.toList();

    }

    public List<GridColumnDTO> getGridColumns() {
        List<GridColumnDTO> list = new ArrayList<>();
        GridColumnDTO id = new GridColumnDTO("id", "ID", "number", false, null, false, "number", "equals", "80px", "right", true);
        GridColumnDTO name = new GridColumnDTO("name", "Name", "text", true, "asc", true, "text", "contains", null, null, true);
        GridColumnDTO category = new GridColumnDTO("category", "Category", "text", true, null, true, "text", "contains", null, null, true);
        GridColumnDTO price = new GridColumnDTO("price", "Price (₹)", "number", true, null, true, "number", "equals", null, "right", true);
        GridColumnDTO quantity = new GridColumnDTO("quantity", "Quantity", "number", true, null, true, "number", "equals", null, "right", true);
        list.add(id);
        list.add(name);
        list.add(category);
        list.add(price);
        list.add(quantity);
        return list;
    }

    private Stream<ProductDTO> applyFilters(
            Stream<ProductDTO> stream,
            List<GridFilter> filterList
    ) {
        if (filterList == null || filterList.isEmpty()) {
            return stream;
        }

        for (GridFilter filter : filterList) {

            if (filter == null) {
                continue;
            }
            String field = filter.getField();
            String value = filter.getValue();
            String operator = filter.getOperator();

            if (field == null || field.isEmpty()) {
                continue;
            }

            // No filter value = don't filter
            if ((value == null || value.isBlank())
                    && !"isNull".equals(operator)
                    && !"isNotNull".equals(operator)) {
                continue;
            }

            stream = stream.filter(product ->
                    matches(product, field, operator, value)
            );
        }

        return stream;
    }

    private boolean matches(
            ProductDTO product,
            String field,
            String operator,
            String value
    ) {
        return switch (field) {
            case "name" -> matchesText(product.getName(), operator, value);
            case "category" -> matchesText(product.getCategory(), operator, value);
            case "price" -> matchesNumber(product.getPrice(), operator, value);
            case "quantity" -> matchesNumber(
                    BigDecimal.valueOf(product.getQuantity()),
                    operator,
                    value
            );
            default -> true;
        };
    }

    private boolean matchesText(
            String fieldValue,
            String operator,
            String filterValue
    ) {
        if ("isNull".equals(operator)) {
            return fieldValue == null;
        }
        if ("isNotNull".equals(operator)) {
            return fieldValue != null;
        }
        if (fieldValue == null) {
            return false;
        }
        String actual = fieldValue.toLowerCase();
        String expected = filterValue == null
                ? ""
                : filterValue.toLowerCase();
        return switch (operator) {
            case "contains" -> actual.contains(expected);
            case "notContains" -> !actual.contains(expected);
            case "startsWith" -> actual.startsWith(expected);
            case "endsWith" -> actual.endsWith(expected);
            case "equals" -> actual.equals(expected);
            case "notEquals" -> !actual.equals(expected);
            default -> true;
        };
    }

    private boolean matchesNumber(
            BigDecimal fieldValue,
            String operator,
            String filterValue
    ) {
        if ("isNull".equals(operator)) {
            return fieldValue == null;
        }
        if ("isNotNull".equals(operator)) {
            return fieldValue != null;
        }
        if (fieldValue == null || filterValue == null || filterValue.isBlank()) {
            return false;
        }
        BigDecimal expected = new BigDecimal(filterValue);
        int comparison = fieldValue.compareTo(expected);
        return switch (operator) {
            case "equals" -> comparison == 0;
            case "notEquals" -> comparison != 0;
            case "gt" -> comparison > 0;
            case "gte" -> comparison >= 0;
            case "lt" -> comparison < 0;
            case "lte" -> comparison <= 0;
            default -> true;
        };
    }

    private Stream<ProductDTO> applySorting(
            Stream<ProductDTO> stream,
            GridSort sort
    ) {
        if (sort == null || sort.getField() == null || sort.getField().isBlank()) {
            return stream;
        }

        Comparator<ProductDTO> comparator = switch (sort.getField()) {
            case "id" -> Comparator.comparing(ProductDTO::getId);
            case "name" -> Comparator.comparing(
                    ProductDTO::getName,
                    String.CASE_INSENSITIVE_ORDER
            );
            case "category" -> Comparator.comparing(
                    ProductDTO::getCategory,
                    String.CASE_INSENSITIVE_ORDER
            );
            case "price" -> Comparator.comparing(ProductDTO::getPrice);
            case "quantity" -> Comparator.comparing(ProductDTO::getQuantity);
            default -> null;
        };
        if (comparator == null) {
            return stream;
        }
        if ("desc".equalsIgnoreCase(sort.getOrder())) {
            comparator = comparator.reversed();
        }
        return stream.sorted(comparator);
    }

}
