package com.pf.common.service.criteria;

import com.pf.common.dto.gp.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GenericCriteriaRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public <T> long getCountBySearchCriteria(Class<T> entity, SearchCriteria searchCriteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entity);
        // select count(*)
        cq.select(cb.count(root));
        // later you can add filters here if needed
        // e.g. cq.where(cb.equal(root.get("category"), searchCriteria.getCategory()));
        return entityManager.createQuery(cq).getSingleResult();
    }


    public <T> List<T> getDataBySearchCriteria(Class<T> entity, SearchCriteria searchCriteria) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entity);
        Root<T> root = criteriaQuery.from(entity);
        criteriaQuery.select(root);
        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(searchCriteria.getSkip());
        typedQuery.setMaxResults(searchCriteria.getTake());
        return typedQuery.getResultList();
    }
}
