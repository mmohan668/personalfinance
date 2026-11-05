package com.pf.common.enums;

import com.pf.common.constants.FieldConstants;
import com.pf.common.entity.categoryManagement.UserCategory;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.settings.ReferenceObject;
import com.pf.common.entity.settings.UserReferenceValue;
import com.pf.common.entity.userManagement.User;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SuggestionField {

    REFERENCE_OBJECT(
            EntityName.REFERENCE_OBJECT.getValue(),
            ReferenceObject.class,
            QueryType.ENTITY,
            null
    ),
    REFERENCE_VALUE(
            EntityName.REFERENCE_VALUE.getValue(),
            UserReferenceValue.class,
            QueryType.ENTITY,
            FieldConstants.USER_ADMIN_ID
    ),
    USER_CATEGORY(
            EntityName.USER_CATEGORY.getValue(),
            UserCategory.class,
            QueryType.ENTITY,
            FieldConstants.USER_ADMIN_ID
    ),
    USER_SUBCATEGORY(
            EntityName.USER_SUBCATEGORY.getValue(),
            UserSubcategory.class,
            QueryType.ENTITY,
            FieldConstants.CATEGORY_USER_ADMIN_ID
    ),
    USER(
            EntityName.USER.getValue(),
            User.class,
            QueryType.ENTITY,
            FieldConstants.ADMIN_USER__ID
    );

    private final String entityName;
    private final Class<?> entityClass;
    private final QueryType queryType;
    private final String userField;

    SuggestionField(
            String entityName,
            Class<?> entityClass,
            QueryType queryType,
            String userField
    ) {
        this.entityName = entityName;
        this.entityClass = entityClass;
        this.queryType = queryType;
        this.userField = userField;
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

