package com.pf.common.enums;

import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.userManagement.User;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SuggestionField {

    REFERENCE_VALUE(
            EntityName.REFERENCE_VALUE.getValue(),
            ReferenceValue.class,
            QueryType.ENTITY
    ),
    USER_CATEGORY(
            EntityName.USER_CATEGORY.getValue(),
            UserCategory.class,
            QueryType.ENTITY
    ),
    USER_SUBCATEGORY(
            EntityName.USER_SUBCATEGORY.getValue(),
            UserSubcategory.class,
            QueryType.ENTITY
    ),
    USER(
            EntityName.USER.getValue(),
            User.class,
            QueryType.ENTITY
    );

    private final String entityName;
    private final Class<?> entityClass;
    private final QueryType queryType;

    SuggestionField(
            String entityName,
            Class<?> entityClass,
            QueryType queryType
    ) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.queryType = queryType;
    }

    public static SuggestionField resolve(String entityName) {
        return Arrays.stream(values())
                .filter(field ->
                        field.entityName.equals(entityName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported suggestion field: "
                                        + entityName
                        ));
    }
}

