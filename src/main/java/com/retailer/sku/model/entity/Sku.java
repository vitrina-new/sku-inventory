package com.retailer.sku.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "skus", indexes = {
        @Index(name = "idx_sku_code", columnList = "skuCode", unique = true),
        @Index(name = "idx_sku_upc", columnList = "upc", unique = true),
        @Index(name = "idx_sku_category", columnList = "category"),
        @Index(name = "idx_sku_status", columnList = "status"),
        @Index(name = "idx_sku_brand", columnList = "brand")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String skuCode;

    @Column(unique = true, length = 12)
    private String upc;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 50)
    private String subcategory;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(length = 20)
    private String unitOfMeasure;

    private Integer quantityPerUnit;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "length", column = @Column(name = "dimension_length")),
            @AttributeOverride(name = "width", column = @Column(name = "dimension_width")),
            @AttributeOverride(name = "height", column = @Column(name = "dimension_height"))
    })
    private Dimensions dimensions;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> attributes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
