package com.retailer.sku.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SKU response payload")
public class SkuResponse {

    @Schema(description = "Internal unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "External SKU code", example = "THD-LBR-0001234")
    private String skuCode;

    @Schema(description = "Universal Product Code", example = "012345678901")
    private String upc;

    @Schema(description = "Product name", example = "2x4x8 Pressure Treated Lumber")
    private String name;

    @Schema(description = "Detailed product description", example = "Premium pressure treated lumber suitable for outdoor use")
    private String description;

    @Schema(description = "Brand name", example = "WeatherShield")
    private String brand;

    @Schema(description = "Product category", example = "LBR")
    private String category;

    @Schema(description = "Product subcategory", example = "PRESSURE_TREATED")
    private String subcategory;

    @Schema(description = "Retail price", example = "8.99")
    private BigDecimal price;

    @Schema(description = "Wholesale cost", example = "5.50")
    private BigDecimal cost;

    @Schema(description = "Unit of measure", example = "EACH")
    private String unitOfMeasure;

    @Schema(description = "Quantity per unit/package", example = "1")
    private Integer quantityPerUnit;

    @Schema(description = "Weight in pounds", example = "12.5")
    private BigDecimal weight;

    @Schema(description = "Product dimensions")
    private DimensionsDto dimensions;

    @Schema(description = "SKU status", example = "ACTIVE")
    private String status;

    @Schema(description = "Searchable tags", example = "[\"outdoor\", \"treated\", \"lumber\"]")
    private List<String> tags;

    @Schema(description = "Custom attributes", example = "{\"treatment_type\": \"ACQ\", \"grade\": \"#2\"}")
    private Map<String, String> attributes;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Instant updatedAt;
}
