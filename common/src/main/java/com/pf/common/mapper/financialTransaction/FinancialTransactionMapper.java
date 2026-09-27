package com.pf.common.mapper.financialTransaction;

import com.pf.common.dto.financialTransaction.FinancialTransactionDto;
import com.pf.common.entity.categoryManagement.UserSubcategory;
import com.pf.common.entity.financialTransaction.FinancialTransaction;
import com.pf.common.entity.settings.ReferenceValue;
import com.pf.common.entity.userManagement.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class FinancialTransactionMapper {
    @Mapping(
            target = "subcategory",
            source = "subcategoryId",
            qualifiedByName = "userSubcategory"
    )
    @Mapping(
            target = "location",
            source = "locationId",
            qualifiedByName = "referenceValue"
    )
    @Mapping(
            target = "adminUser",
            source = "adminUserId",
            qualifiedByName = "user"
    )
    @Mapping(
            target = "createdBy",
            source = "createdBy",
            qualifiedByName = "user"
    )
    @Mapping(
            target = "updatedBy",
            source = "updatedBy",
            qualifiedByName = "user"
    )
    public abstract FinancialTransaction toEntity(
            FinancialTransactionDto dto
    );

    @Mapping(
            target = "subcategoryId",
            source = "subcategory.id"
    )
    @Mapping(
            target = "locationId",
            source = "location.id"
    )
    @Mapping(
            target = "adminUserId",
            source = "adminUser.id"
    )
    @Mapping(
            target = "createdBy",
            source = "createdBy.id"
    )
    @Mapping(
            target = "updatedBy",
            source = "updatedBy.id"
    )
    public abstract FinancialTransactionDto toDto(
            FinancialTransaction entity
    );

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(
            target = "subcategory",
            source = "subcategoryId",
            qualifiedByName = "userSubcategory"
    )
    @Mapping(
            target = "location",
            source = "locationId",
            qualifiedByName = "referenceValue"
    )
    @Mapping(
            target = "adminUser",
            source = "adminUserId",
            qualifiedByName = "user"
    )
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(
            FinancialTransactionDto dto,
            @MappingTarget FinancialTransaction entity
    );

    @Named("referenceValue")
    protected ReferenceValue mapReferenceValue(Long id) {
        if (id == null) {
            return null;
        }

        return entityManager.getReference(
                ReferenceValue.class,
                id
        );
    }

    @Named("userSubcategory")
    protected UserSubcategory mapUserSubcategory(Long id) {
        if (id == null) {
            return null;
        }

        return entityManager.getReference(
                UserSubcategory.class,
                id
        );
    }

    @Named("user")
    protected User mapUser(Long id) {
        if (id == null) {
            return null;
        }

        return entityManager.getReference(
                User.class,
                id
        );
    }

    @PersistenceContext
    private EntityManager entityManager;
}
