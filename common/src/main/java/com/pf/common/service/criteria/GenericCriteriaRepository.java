package com.pf.common.service.criteria;

import com.pf.common.dto.gp.GridFilter;
import com.pf.common.dto.gp.GridSort;
import com.pf.common.dto.gp.SearchCriteria;
import com.pf.common.exception.NullFilterValueException;
import com.pf.common.exception.UnsupportedFilterFieldTypeException;
import com.pf.common.exception.UnsupportedFilterOperatorException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Repository
public class GenericCriteriaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> long getCountBySearchCriteria(
            Class<T> entity,
            SearchCriteria searchCriteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteriaQuery.from(entity);
        applyFilters(criteriaBuilder, criteriaQuery, root, searchCriteria);
        // select count(*)
        criteriaQuery.select(criteriaBuilder.count(root));

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    public <T> List<T> getDataBySearchCriteria(
            Class<T> entity,
            SearchCriteria searchCriteria
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entity);
        Root<T> root = criteriaQuery.from(entity);

        criteriaQuery.select(root);
        applyFilters(criteriaBuilder, criteriaQuery, root, searchCriteria);
        applySorting(criteriaBuilder, criteriaQuery, root, searchCriteria
        );

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        
        if(!searchCriteria.isLoadAllData()) {
            typedQuery.setFirstResult(searchCriteria.getSkip());
            typedQuery.setMaxResults(searchCriteria.getTake());
        }

        return typedQuery.getResultList();
    }

    private void applySorting(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root,
            SearchCriteria searchCriteria
    ) {
        if (searchCriteria.getSortList() != null
                && !searchCriteria.getSortList().isEmpty()) {

            List<Order> orders = new ArrayList<>();

            for (GridSort sort : searchCriteria.getSortList()) {
                if (sort.getField() == null || sort.getField().isBlank()) {
                    continue;
                }

                Path<?> path = root.get(sort.getField());

                boolean descending = "desc".equalsIgnoreCase(sort.getOrder());

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
    }

    private void applyFilters(
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root,
            SearchCriteria searchCriteria
    ) {
        if (searchCriteria.getFilterList() != null
                && !searchCriteria.getFilterList().isEmpty()) {

            List<Predicate> predicates = new ArrayList<>();

            for (GridFilter filter : searchCriteria.getFilterList()) {
                Path<?> path = root.get(filter.getField());
                String operator = filter.getOperator();

                if ("isNull".equals(operator)) {
                    predicates.add(criteriaBuilder.isNull(path));

                } else if ("isNotNull".equals(operator)) {
                    predicates.add(criteriaBuilder.isNotNull(path));

                } else if (filter.getValue() == null) {
                    throw new NullFilterValueException(
                            "Filter value cannot be null for operator: " + operator
                    );

                } else if (String.class.equals(path.getJavaType())) {
                    String value = filter.getValue().toLowerCase(Locale.ROOT);

                    Expression<String> field = criteriaBuilder.lower(path.as(String.class));

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "contains" -> predicates.add(
                                criteriaBuilder.like(
                                        field,
                                        "%" + value + "%"
                                )
                        );

                        case "notContains" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notLike(
                                                field,
                                                "%" + value + "%"
                                        ),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "startsWith" -> predicates.add(
                                criteriaBuilder.like(
                                        field,
                                        value + "%"
                                )
                        );

                        case "endsWith" -> predicates.add(
                                criteriaBuilder.like(
                                        field,
                                        "%" + value
                                )
                        );

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (BigDecimal.class.equals(path.getJavaType())) {
                    BigDecimal value = new BigDecimal(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<BigDecimal> field = (Expression<BigDecimal>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            BigDecimal valueTo = new BigDecimal(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (Integer.class.equals(path.getJavaType())
                        || int.class.equals(path.getJavaType())) {

                    Integer value = Integer.parseInt(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<Integer> field = (Expression<Integer>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            Integer valueTo = Integer.parseInt(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (Long.class.equals(path.getJavaType())
                        || long.class.equals(path.getJavaType())) {

                    Long value = Long.parseLong(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<Long> field = (Expression<Long>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            Long valueTo = Long.parseLong(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (Double.class.equals(path.getJavaType())
                        || double.class.equals(path.getJavaType())) {

                    Double value = Double.parseDouble(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<Double> field = (Expression<Double>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            Double valueTo = Double.parseDouble(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (Boolean.class.equals(path.getJavaType())
                        || boolean.class.equals(path.getJavaType())) {

                    boolean value = Boolean.parseBoolean(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<Boolean> field = (Expression<Boolean>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (LocalDate.class.equals(path.getJavaType())) {
                    LocalDate value = LocalDate.parse(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<LocalDate> field = (Expression<LocalDate>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            LocalDate valueTo = LocalDate.parse(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else if (LocalDateTime.class.equals(path.getJavaType())) {
                    LocalDateTime value = LocalDateTime.parse(filter.getValue());

                    @SuppressWarnings("unchecked")
                    Expression<LocalDateTime> field = (Expression<LocalDateTime>) path;

                    switch (operator) {
                        case "equals" -> predicates.add(
                                criteriaBuilder.equal(field, value)
                        );

                        case "notEquals" -> predicates.add(
                                criteriaBuilder.or(
                                        criteriaBuilder.notEqual(field, value),
                                        criteriaBuilder.isNull(path)
                                )
                        );

                        case "gt" -> predicates.add(
                                criteriaBuilder.greaterThan(field, value)
                        );

                        case "gte" -> predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "lt" -> predicates.add(
                                criteriaBuilder.lessThan(field, value)
                        );

                        case "lte" -> predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        field,
                                        value
                                )
                        );

                        case "between" -> {
                            if (filter.getValueTo() == null || filter.getValueTo().isBlank()) {
                                throw new NullFilterValueException(
                                        "Second filter value cannot be null for operator: between"
                                );
                            }

                            LocalDateTime valueTo = LocalDateTime.parse(filter.getValueTo());

                            predicates.add(
                                    criteriaBuilder.and(
                                            criteriaBuilder.greaterThanOrEqualTo(field, value),
                                            criteriaBuilder.lessThanOrEqualTo(field, valueTo)
                                    )
                            );
                        }

                        default -> throw new UnsupportedFilterOperatorException(
                                "Unsupported filter operator: " + operator
                        );
                    }

                } else {
                    throw new UnsupportedFilterFieldTypeException(
                            "Unsupported filter field type: "
                                    + path.getJavaType().getName()
                    );
                }
            }

            if (!predicates.isEmpty()) {
                criteriaQuery.where(
                        predicates.toArray(new Predicate[0])
                );
            }
        }
    }
}
