package com.retailer.sku.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new SKU")
public class SkuRequest {

    @Schema(description = "Universal Product Code (12 digits)", example = "012345678901")
    @Pattern(regexp = "^\\d{12}$", message = "UPC must be exactly 12 digits")
    private String upc;

    @Schema(description = "Product name", example = "2x4x8 Pressure Treated Lumber", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Schema(description = "Detailed product description", example = "Premium pressure treated lumber suitable for outdoor use")
    @Size(max = 4000, message = "Description must not exceed 4000 characters")
    private String description;

    @Schema(description = "Brand name", example = "WeatherShield")
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Schema(description = "Product category code", example = "LBR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Category must be a 3-letter uppercase code")
    private String category;

    @Schema(description = "Product subcategory", example = "PRESSURE_TREATED")
    @Size(max = 50, message = "Subcategory must not exceed 50 characters")
    private String subcategory;

    @Schema(description = "Retail price", example = "8.99")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    @Schema(description = "Wholesale cost", example = "5.50")
    @DecimalMin(value = "0.0", inclusive = false, message = "Cost must be positive")
    @Digits(integer = 8, fraction = 2, message = "Cost must have at most 8 integer digits and 2 decimal places")
    private BigDecimal cost;

    @Schema(description = "Unit of measure", example = "EACH", allowableValues = {"EACH", "SQFT", "LINEAR_FT", "CUBIC_FT", "LB", "GAL"})
    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String unitOfMeasure;

    @Schema(description = "Quantity per unit/package", example = "1")
    @Min(value = 1, message = "Quantity per unit must be at least 1")
    private Integer quantityPerUnit;

    @Schema(description = "Weight in pounds", example = "12.5")
    @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be positive")
    private BigDecimal weight;

    @Schema(description = "Product dimensions")
    @Valid
    private DimensionsDto dimensions;

    @Schema(description = "Searchable tags", example = "[\"outdoor\", \"treated\", \"lumber\"]")
    private List<@Size(max = 50, message = "Each tag must not exceed 50 characters") String> tags;

    @Schema(description = "Custom attributes as key-value pairs", example = "{\"treatment_type\": \"ACQ\", \"grade\": \"#2\"}")
    private Map<String, String> attributes;
}
