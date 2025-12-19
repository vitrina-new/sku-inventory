package com.retailer.sku.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product dimensions in inches")
public class DimensionsDto {

    @Schema(description = "Length in inches", example = "12.5")
    @DecimalMin(value = "0.0", inclusive = false, message = "Length must be positive")
    private BigDecimal length;

    @Schema(description = "Width in inches", example = "8.0")
    @DecimalMin(value = "0.0", inclusive = false, message = "Width must be positive")
    private BigDecimal width;

    @Schema(description = "Height in inches", example = "4.0")
    @DecimalMin(value = "0.0", inclusive = false, message = "Height must be positive")
    private BigDecimal height;
}
