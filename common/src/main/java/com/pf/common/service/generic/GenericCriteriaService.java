package com.pf.common.service.generic;

import com.pf.common.dto.gp.GridFilter;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.enums.FilterOperator;
import com.pf.common.exception.NullFilterValueException;
import com.pf.common.exception.UnsupportedFilterFieldTypeException;
import com.pf.common.exception.UnsupportedFilterOperatorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.HibernateHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.pf.common.constants.CommonConstants.*;
import static com.pf.common.enums.SortOrder.*;

@Slf4j
@Repository
public class GenericCriteriaService {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> long getCountBySearchCriteria(
            Class<T> entity,
            SearchCriteria searchCriteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery =
                criteriaBuilder.createQuery(Long.class);

        Root<T> root = criteriaQuery.from(entity);

        applyFilters(
                criteriaBuilder,
                criteriaQuery,
                root,
                searchCriteria
        );

        criteriaQuery.select(criteriaBuilder.count(root));

        return entityManager
                .createQuery(criteriaQuery)
                .getSingleResult();
    }

    @Transactional
    public <T> List<T> getDataBySearchCriteria(
            Class<T> entity,
            SearchCriteria searchCriteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery =
                criteriaBuilder.createQuery(entity);

        Root<T> root = criteriaQuery.from(entity);

        applyFetches(root, searchCriteria);

        criteriaQuery.select(root);

        applyFilters(
                criteriaBuilder,
                criteriaQuery,
                root,
                searchCriteria
        );

        applySorting(
                criteriaBuilder,
                criteriaQuery,
                root,
                searchCriteria
        );

        TypedQuery<T> typedQuery =
                entityManager.createQuery(criteriaQuery);

        if (!searchCriteria.isLoadAllData()) {
            typedQuery.setFirstResult(searchCriteria.getSkip());
            typedQuery.setMaxResults(searchCriteria.getTake());
            return typedQuery.getResultList();
        }

        typedQuery.setHint(HibernateHints.HINT_FETCH_SIZE, 1000);

        try (Stream<T> stream = typedQuery.getResultStream()) {
            return stream.toList();
        }
    }

    @Transactional
    public <T> Stream<T> streamDataBySearchCriteria(
            Class<T> entity,
            SearchCriteria searchCriteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entity);
        Root<T> root = criteriaQuery.from(entity);

        applyFetches(root, searchCriteria);

        criteriaQuery.select(root);

        applyFilters(
                criteriaBuilder,
                criteriaQuery,
                root,
                searchCriteria
        );
        applySorting(
                criteriaBuilder,
                criteriaQuery,
                root,
                searchCriteria
        );

        TypedQuery<T> typedQuery =
                entityManager.createQuery(criteriaQuery);

        typedQuery.setHint(HibernateHints.HINT_FETCH_SIZE, 1000);
        typedQuery.setHint(HibernateHints.HINT_READ_ONLY, true);

        return typedQuery.getResultStream();
    }

    private <T> void applyFetches(
            Root<T> root,
            SearchCriteria searchCriteria
    ) {
        if (searchCriteria.getFetchPaths() == null
                || searchCriteria.getFetchPaths().isEmpty()) {
            return;
        }

        for (String fetchPath : searchCriteria.getFetchPaths()) {
            if (fetchPath == null || fetchPath.isBlank()) {
                continue;
            }

            root.fetch(fetchPath, JoinType.LEFT);
        }
    }

    private void applySorting(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root,
            SearchCriteria searchCriteria
    ) {
        if (searchCriteria.getSortList() == null
                || searchCriteria.getSortList().isEmpty()) {
            return;
        }

        List<Order> orders = new ArrayList<>();

        for (GridSort sort : searchCriteria.getSortList()) {
            if (sort == null
                    || sort.getField() == null
                    || sort.getField().isBlank()) {
                continue;
            }

            Path<?> path = resolvePath(
                    root,
                    sort.getField(),
                    searchCriteria.getFIELD_MAPPINGS()
            );

            boolean descending =
                    DESC.getValue().equalsIgnoreCase(sort.getOrder());

            Expression<?> sortExpression = path;

            if (String.class.equals(path.getJavaType())) {
                sortExpression = criteriaBuilder.lower(
                        path.as(String.class)
                );
            }

            orders.add(
                    descending
                            ? criteriaBuilder.desc(sortExpression)
                            : criteriaBuilder.asc(sortExpression)
            );
        }

        if (!orders.isEmpty()) {
            criteriaQuery.orderBy(orders);
        }
    }

    private void applyFilters(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root,
            SearchCriteria searchCriteria
    ) {
        if (searchCriteria.getFilterList() == null
                || searchCriteria.getFilterList().isEmpty()) {
            return;
        }

        List<Predicate> predicates = new ArrayList<>();

        for (GridFilter filter : searchCriteria.getFilterList()) {
            if (filter == null) {
                continue;
            }

            if (filter.getField() == null
                    || filter.getField().isBlank()) {
                throw new IllegalArgumentException(
                        "Filter field cannot be null or blank"
                );
            }

            FilterOperator filterOperator = normalizeOperator(filter.getOperator());

            Path<?> path = resolvePath(
                    root,
                    filter.getField(),
                    searchCriteria.getFIELD_MAPPINGS()
            );

            switch (filterOperator) {
                case IS_NULL -> predicates.add(
                        criteriaBuilder.isNull(path)
                );

                case IS_NOT_NULL -> predicates.add(
                        criteriaBuilder.isNotNull(path)
                );

                case IN -> addInPredicate(
                        criteriaBuilder,
                        predicates,
                        path,
                        filter.getValues()
                );

                default -> {
                    validateFilterValue(filter, filterOperator.getValue());

                    addSingleValuePredicate(
                            criteriaBuilder,
                            predicates,
                            path,
                            filter
                    );
                }
            }
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.where(
                    predicates.toArray(new Predicate[0])
            );
        }
    }

    private void addInPredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            Collection<?> values
    ) {
        if (values == null || values.isEmpty()) {
            throw new NullFilterValueException(
                    "Filter values cannot be null or empty for operator: in"
            );
        }

        Class<?> fieldType = path.getJavaType();

        if (String.class.equals(fieldType)) {
            Expression<String> field =
                    criteriaBuilder.lower(
                            path.as(String.class)
                    );

            List<String> convertedValues = values.stream()
                    .map(this::toStringValue)
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .toList();

            predicates.add(field.in(convertedValues));
            return;
        }

        List<?> convertedValues = convertValues(
                values,
                fieldType
        );

        addTypedInPredicate(
                predicates,
                path,
                convertedValues
        );
    }

    private void addTypedInPredicate(
            List<Predicate> predicates,
            Path<?> path,
            List<?> values
    ) {
        predicates.add(path.in(values));
    }

    private List<?> convertValues(
            Collection<?> values,
            Class<?> fieldType
    ) {
        Function<String, ?> converter =
                getValueConverter(fieldType);

        return values.stream()
                .map(this::toStringValue)
                .map(converter)
                .toList();
    }

    private Function<String, ?> getValueConverter(
            Class<?> fieldType
    ) {
        if (Long.class.equals(fieldType)
                || long.class.equals(fieldType)) {
            return Long::valueOf;
        }

        if (Integer.class.equals(fieldType)
                || int.class.equals(fieldType)) {
            return Integer::valueOf;
        }

        if (Double.class.equals(fieldType)
                || double.class.equals(fieldType)) {
            return Double::valueOf;
        }

        if (BigDecimal.class.equals(fieldType)) {
            return BigDecimal::new;
        }

        if (LocalDate.class.equals(fieldType)) {
            return LocalDate::parse;
        }

        if (LocalDateTime.class.equals(fieldType)) {
            return this::parseLocalDateTime;
        }

        if (Boolean.class.equals(fieldType)
                || boolean.class.equals(fieldType)) {
            return this::parseBoolean;
        }

        throw new UnsupportedFilterFieldTypeException(
                "Unsupported filter field type for IN operator: "
                        + fieldType.getName()
        );
    }

    private void addSingleValuePredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            GridFilter filter
    ) {
        Class<?> fieldType = path.getJavaType();

        if (String.class.equals(fieldType)) {
            addStringPredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter
            );
            return;
        }

        if (BigDecimal.class.equals(fieldType)) {
            addComparablePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter,
                    BigDecimal::new
            );
            return;
        }

        if (Integer.class.equals(fieldType)
                || int.class.equals(fieldType)) {
            addComparablePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter,
                    Integer::valueOf
            );
            return;
        }

        if (Long.class.equals(fieldType)
                || long.class.equals(fieldType)) {
            addComparablePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter,
                    Long::valueOf
            );
            return;
        }

        if (Double.class.equals(fieldType)
                || double.class.equals(fieldType)) {
            addComparablePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter,
                    Double::valueOf
            );
            return;
        }

        if (Boolean.class.equals(fieldType)
                || boolean.class.equals(fieldType)) {
            addBooleanPredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter
            );
            return;
        }

        if (LocalDate.class.equals(fieldType)) {
            addComparablePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter,
                    LocalDate::parse
            );
            return;
        }

        if (LocalDateTime.class.equals(fieldType)) {
            addLocalDateTimePredicate(
                    criteriaBuilder,
                    predicates,
                    path,
                    filter
            );
            return;
        }

        throw new UnsupportedFilterFieldTypeException(
                "Unsupported filter field type: "
                        + fieldType.getName()
        );
    }

    private void addStringPredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            GridFilter filter
    ) {
        String value =
                filter.getValue().toLowerCase(Locale.ROOT);

        Expression<String> field =
                criteriaBuilder.lower(
                        path.as(String.class)
                );
        FilterOperator filterOperator = normalizeOperator(filter.getOperator());

        switch (filterOperator) {
            case EQUALS -> predicates.add(
                    criteriaBuilder.equal(field, value)
            );

            case NOT_EQUALS -> predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.notEqual(field, value),
                            criteriaBuilder.isNull(path)
                    )
            );

            case CONTAINS -> predicates.add(
                    criteriaBuilder.like(
                            field,
                            LIKE_STR + value + LIKE_STR
                    )
            );

            case NOT_CONTAINS -> predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.notLike(
                                    field,
                                    LIKE_STR + value + LIKE_STR
                            ),
                            criteriaBuilder.isNull(path)
                    )
            );

            case STARTS_WITH -> predicates.add(
                    criteriaBuilder.like(
                            field,
                            value + LIKE_STR
                    )
            );

            case ENDS_WITH -> predicates.add(
                    criteriaBuilder.like(
                            field,
                            LIKE_STR + value
                    )
            );

            default -> throw unsupportedOperator(
                    filter.getOperator()
            );
        }
    }

    private <T extends Comparable<? super T>> void addComparablePredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            GridFilter filter,
            Function<String, T> converter
    ) {
        T value = converter.apply(filter.getValue());

        Expression<T> field = typedExpression(path);
        FilterOperator filterOperator = normalizeOperator(filter.getOperator());

        switch (filterOperator) {
            case EQUALS -> predicates.add(
                    criteriaBuilder.equal(field, value)
            );

            case NOT_EQUALS -> predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.notEqual(field, value),
                            criteriaBuilder.isNull(path)
                    )
            );

            case GREATER_THAN -> predicates.add(
                    criteriaBuilder.greaterThan(field, value)
            );

            case GREATER_THAN_OR_EQUALS -> predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(
                            field,
                            value
                    )
            );

            case LESS_THAN -> predicates.add(
                    criteriaBuilder.lessThan(field, value)
            );

            case LESS_THAN_OR_EQUALS -> predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(
                            field,
                            value
                    )
            );

            case BETWEEN -> {
                String valueTo = filter.getValueTo();

                if (valueTo == null || valueTo.isBlank()) {
                    throw new NullFilterValueException(
                            "Second filter value cannot be null for operator: between"
                    );
                }

                T upperValue = converter.apply(valueTo);

                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                ),
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        upperValue
                                )
                        )
                );
            }

            default -> throw unsupportedOperator(
                    filter.getOperator()
            );
        }
    }

    private void addBooleanPredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            GridFilter filter
    ) {
        Boolean value = parseBoolean(filter.getValue());

        Expression<Boolean> field =
                typedExpression(path);
        FilterOperator filterOperator = normalizeOperator(filter.getOperator());
        switch (filterOperator) {
            case EQUALS -> predicates.add(
                    criteriaBuilder.equal(field, value)
            );

            case NOT_EQUALS -> predicates.add(
                    criteriaBuilder.or(
                            criteriaBuilder.notEqual(field, value),
                            criteriaBuilder.isNull(path)
                    )
            );

            default -> throw unsupportedOperator(
                    filter.getOperator()
            );
        }
    }

    private void addLocalDateTimePredicate(
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Path<?> path,
            GridFilter filter
    ) {
        LocalDateTime value =
                parseLocalDateTime(filter.getValue());

        Expression<LocalDateTime> field =
                typedExpression(path);
        FilterOperator filterOperator = normalizeOperator(filter.getOperator());
        switch (filterOperator) {
            case EQUALS -> {
                LocalDateTime startOfDay =
                        value.toLocalDate().atStartOfDay();

                LocalDateTime nextDay =
                        value.toLocalDate().plusDays(1).atStartOfDay();

                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        startOfDay
                                ),
                                criteriaBuilder.lessThan(
                                        field,
                                        nextDay
                                )
                        )
                );
            }

            case NOT_EQUALS -> {
                LocalDateTime startOfDay =
                        value.toLocalDate().atStartOfDay();

                LocalDateTime nextDay =
                        value.toLocalDate().plusDays(1).atStartOfDay();

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(path),
                                criteriaBuilder.lessThan(
                                        field,
                                        startOfDay
                                ),
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        nextDay
                                )
                        )
                );
            }

            case GREATER_THAN -> {
                LocalDateTime nextDay =
                        value.toLocalDate().plusDays(1).atStartOfDay();

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                field,
                                nextDay
                        )
                );
            }

            case GREATER_THAN_OR_EQUALS -> {
                LocalDateTime startOfDay =
                        value.toLocalDate().atStartOfDay();

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                field,
                                startOfDay
                        )
                );
            }

            case LESS_THAN -> {
                LocalDateTime startOfDay =
                        value.toLocalDate().atStartOfDay();
                predicates.add(
                        criteriaBuilder.lessThan(field, startOfDay)
                );
            }

            case LESS_THAN_OR_EQUALS -> {
                LocalDateTime nextDay =
                        value.toLocalDate().plusDays(1).atStartOfDay();

                predicates.add(
                        criteriaBuilder.lessThan(
                                field,
                                nextDay
                        )
                );
            }

            case BETWEEN -> {
                String valueTo = filter.getValueTo();

                if (valueTo == null || valueTo.isBlank()) {
                    throw new NullFilterValueException(
                            "Second filter value cannot be null for operator: between"
                    );
                }

                LocalDateTime upperValue =
                        parseLocalDateTimeForUpperBound(valueTo);

                LocalDateTime startOfDay = value.toLocalDate().atStartOfDay();
                LocalDateTime nextDay = upperValue.toLocalDate().plusDays(1).atStartOfDay();

                predicates.add(
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        startOfDay
                                ),
                                criteriaBuilder.lessThan(
                                        field,
                                        nextDay
                                )
                        )
                );
            }

            default -> throw unsupportedOperator(
                    filter.getOperator()
            );
        }
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isBlank()) {
            throw new NullFilterValueException(
                    "Date-time filter value cannot be null or blank"
            );
        }

        if (value.length() == 10) {
            return LocalDate.parse(
                            value,
                            DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    .atStartOfDay();
        }

        return LocalDateTime.parse(
                value,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }

    private LocalDateTime parseLocalDateTimeForUpperBound(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw new NullFilterValueException(
                    "Date-time filter value cannot be null or blank"
            );
        }

        if (value.length() == 10) {
            return LocalDate.parse(
                            value,
                            DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    .atTime(LocalTime.MAX);
        }

        return LocalDateTime.parse(
                value,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            throw new NullFilterValueException(
                    "Boolean filter value cannot be null or blank"
            );
        }

        if (!TRUE_STR.equalsIgnoreCase(value)
                && !FALSE_STR.equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(
                    "Invalid boolean filter value: " + value
            );
        }

        return Boolean.parseBoolean(value);
    }

    @SuppressWarnings(UNCHECKED_STR)
    private <T> Expression<T> typedExpression(
            Path<?> path
    ) {
        return (Expression<T>) path;
    }

    private FilterOperator normalizeOperator(String operator) {
        if (operator == null || operator.isBlank()) {
            throw new UnsupportedFilterOperatorException(
                    "Filter operator cannot be null or blank"
            );
        }

        return Arrays.stream(FilterOperator.values())
                .filter(op -> op.getValue().equals(operator))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown filter operator: " + operator
                ));
    }

    private void validateFilterValue(
            GridFilter filter,
            String operator
    ) {
        if (filter.getValue() == null
                || filter.getValue().isBlank()) {
            throw new NullFilterValueException(
                    "Filter value cannot be null or blank for operator: "
                            + operator
            );
        }
    }

    private String toStringValue(Object value) {
        if (value == null) {
            throw new NullFilterValueException(
                    "Filter value cannot contain null values"
            );
        }

        return value.toString();
    }

    private UnsupportedFilterOperatorException unsupportedOperator(
            String operator
    ) {
        return new UnsupportedFilterOperatorException(
                "Unsupported filter operator: " + operator
        );
    }

    private Path<?> resolvePath(
            Root<?> root,
            String field,
            Map<String, String> fieldMap
    ) {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException(
                    "Field cannot be null or blank"
            );
        }

        String mappedField =
                fieldMap != null
                        ? fieldMap.getOrDefault(field, field)
                        : field;

        String[] parts =
                mappedField.split(DOT_SPLIT_STR);

        Path<?> path = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (part == null || part.isBlank()) {
                throw new IllegalArgumentException(
                        "Invalid field path: " + mappedField
                );
            }

            if (i < parts.length - 1) {
                path = ((From<?, ?>) path)
                        .join(part, JoinType.LEFT);
            } else {
                path = path.get(part);
            }
        }

        return path;
    }
}