package com.retailer.sku.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for batch creating SKUs")
public class BatchSkuRequest {

    @Schema(description = "List of SKUs to create", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "At least one SKU is required")
    @Size(max = 100, message = "Maximum 100 SKUs per batch")
    @Valid
    private List<SkuRequest> skus;
}
