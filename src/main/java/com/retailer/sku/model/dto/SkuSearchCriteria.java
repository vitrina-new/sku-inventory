package com.retailer.sku.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criteria for filtering SKUs")
public class SkuSearchCriteria {

    @Schema(description = "Search query for name or description", example = "lumber")
    private String query;

    @Schema(description = "Filter by category", example = "LBR")
    private String category;

    @Schema(description = "Filter by subcategory", example = "PRESSURE_TREATED")
    private String subcategory;

    @Schema(description = "Filter by brand", example = "WeatherShield")
    private String brand;

    @Schema(description = "Filter by status", example = "ACTIVE")
    private String status;

    @Schema(description = "Minimum price filter", example = "5.00")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter", example = "50.00")
    private BigDecimal maxPrice;

    @Schema(description = "Filter by tags (any match)", example = "[\"outdoor\", \"treated\"]")
    private List<String> tags;
}
