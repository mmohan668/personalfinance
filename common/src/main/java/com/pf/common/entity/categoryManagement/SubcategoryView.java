package com.pf.common.entity.categoryManagement;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vw_subcategories_view")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryView {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_category_id")
    private Long userCategoryId;

    @Column(name = "transaction_type_id")
    private Long transactionTypeId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_description")
    private String categoryDescription;

    @Column(name = "admin_user_id")
    private Long adminUserId;

    @Column(name = "subcategory_name")
    private String subcategoryName;

    @Column(name = "subcategory_description")
    private String subcategoryDescription;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
