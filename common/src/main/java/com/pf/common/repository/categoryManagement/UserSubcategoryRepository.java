package com.pf.common.repository.categoryManagement;

import com.pf.common.entity.categoryManagement.UserSubcategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubcategoryRepository extends CrudRepository<UserSubcategory, Long> {

    @Query("""
            SELECT COUNT(usc)
            FROM UserSubcategory usc
            WHERE usc.userCategory.id IN (:ids)
            """)
    long countByCategoryId(@Param("ids") List<Long> ids);
}
