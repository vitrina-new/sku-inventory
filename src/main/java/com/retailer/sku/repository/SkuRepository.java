package com.retailer.sku.repository;

import com.retailer.sku.model.entity.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkuRepository extends JpaRepository<Sku, UUID>, JpaSpecificationExecutor<Sku> {

    Optional<Sku> findBySkuCode(String skuCode);

    Optional<Sku> findByUpc(String upc);

    boolean existsBySkuCode(String skuCode);

    boolean existsByUpc(String upc);

    Page<Sku> findByCategory(String category, Pageable pageable);

    Page<Sku> findByStatus(String status, Pageable pageable);

    Page<Sku> findByCategoryAndStatus(String category, String status, Pageable pageable);

    Page<Sku> findByBrand(String brand, Pageable pageable);

    @Query("SELECT s FROM Sku s WHERE " +
            "(:category IS NULL OR s.category = :category) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:brand IS NULL OR s.brand = :brand) AND " +
            "(:minPrice IS NULL OR s.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR s.price <= :maxPrice)")
    Page<Sku> findByFilters(
            @Param("category") String category,
            @Param("status") String status,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("SELECT s FROM Sku s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Sku> searchByNameOrDescription(@Param("query") String query, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.skuCode, LENGTH(:prefix) + 2) AS integer)), 0) FROM Sku s WHERE s.skuCode LIKE :prefix || '-%'")
    Integer findMaxSequenceByPrefix(@Param("prefix") String prefix);
}
