package com.retailer.sku.repository;

import com.retailer.sku.model.dto.SkuSearchCriteria;
import com.retailer.sku.model.entity.Sku;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SkuSpecifications {

    public static Specification<Sku> withSearchCriteria(SkuSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getQuery() != null && !criteria.getQuery().isBlank()) {
                String searchPattern = "%" + criteria.getQuery().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, descPredicate));
            }

            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), criteria.getCategory()));
            }

            if (criteria.getSubcategory() != null && !criteria.getSubcategory().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("subcategory"), criteria.getSubcategory()));
            }

            if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), criteria.getBrand()));
            }

            if (criteria.getStatus() != null && !criteria.getStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
