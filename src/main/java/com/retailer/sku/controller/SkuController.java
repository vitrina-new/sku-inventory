package com.retailer.sku.controller;

import com.retailer.sku.model.dto.*;
import com.retailer.sku.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skus")
@Tag(name = "SKU Management", description = "Operations for managing Stock Keeping Units")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @Operation(
            summary = "Create a new SKU",
            description = "Creates a new Stock Keeping Unit with auto-generated SKU code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SKU created successfully",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "SKU with UPC already exists")
    })
    @PostMapping
    public ResponseEntity<SkuResponse> createSku(
            @Valid @RequestBody SkuRequest request) {
        SkuResponse response = skuService.createSku(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Batch create SKUs",
            description = "Creates multiple SKUs in a single request"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SKUs created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "One or more SKUs have duplicate UPCs")
    })
    @PostMapping("/batch")
    public ResponseEntity<List<SkuResponse>> createSkusBatch(
            @Valid @RequestBody BatchSkuRequest request) {
        List<SkuResponse> responses = skuService.createSkusBatch(request.getSkus());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(
            summary = "Get all SKUs",
            description = "Retrieves a paginated list of all SKUs with optional filtering"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SKUs")
    })
    @GetMapping
    public ResponseEntity<Page<SkuResponse>> getAllSkus(
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand,
            @Parameter(description = "Minimum price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if (category != null || status != null || brand != null || minPrice != null || maxPrice != null) {
            return ResponseEntity.ok(skuService.getSkusByFilters(category, status, brand, minPrice, maxPrice, pageable));
        }
        return ResponseEntity.ok(skuService.getAllSkus(pageable));
    }

    @Operation(
            summary = "Get SKU by ID",
            description = "Retrieves a single SKU by its internal UUID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SKU",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "404", description = "SKU not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SkuResponse> getSkuById(
            @Parameter(description = "SKU UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(skuService.getSkuById(id));
    }

    @Operation(
            summary = "Get SKU by code",
            description = "Retrieves a single SKU by its external SKU code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SKU",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "404", description = "SKU not found")
    })
    @GetMapping("/code/{skuCode}")
    public ResponseEntity<SkuResponse> getSkuByCode(
            @Parameter(description = "External SKU code") @PathVariable String skuCode) {
        return ResponseEntity.ok(skuService.getSkuByCode(skuCode));
    }

    @Operation(
            summary = "Get SKU by UPC",
            description = "Retrieves a single SKU by its Universal Product Code"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SKU",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "404", description = "SKU not found")
    })
    @GetMapping("/upc/{upc}")
    public ResponseEntity<SkuResponse> getSkuByUpc(
            @Parameter(description = "Universal Product Code") @PathVariable String upc) {
        return ResponseEntity.ok(skuService.getSkuByUpc(upc));
    }

    @Operation(
            summary = "Search SKUs",
            description = "Search SKUs by various criteria"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matching SKUs")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<SkuResponse>> searchSkus(
            @Parameter(description = "Search query for name or description") @RequestParam(required = false) String query,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by subcategory") @RequestParam(required = false) String subcategory,
            @Parameter(description = "Filter by brand") @RequestParam(required = false) String brand,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        SkuSearchCriteria criteria = SkuSearchCriteria.builder()
                .query(query)
                .category(category)
                .subcategory(subcategory)
                .brand(brand)
                .status(status)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        return ResponseEntity.ok(skuService.searchSkus(criteria, pageable));
    }

    @Operation(
            summary = "Full update of SKU",
            description = "Replaces all fields of an existing SKU"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SKU updated successfully",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "409", description = "UPC already exists for another SKU")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SkuResponse> updateSku(
            @Parameter(description = "SKU UUID") @PathVariable UUID id,
            @Valid @RequestBody SkuRequest request) {
        return ResponseEntity.ok(skuService.updateSku(id, request));
    }

    @Operation(
            summary = "Partial update of SKU",
            description = "Updates only the provided fields of an existing SKU"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SKU updated successfully",
                    content = @Content(schema = @Schema(implementation = SkuResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "SKU not found"),
            @ApiResponse(responseCode = "409", description = "UPC already exists for another SKU")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<SkuResponse> partialUpdateSku(
            @Parameter(description = "SKU UUID") @PathVariable UUID id,
            @Valid @RequestBody SkuUpdateRequest request) {
        return ResponseEntity.ok(skuService.partialUpdateSku(id, request));
    }

    @Operation(
            summary = "Soft delete SKU",
            description = "Marks a SKU as discontinued (soft delete)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SKU deleted successfully"),
            @ApiResponse(responseCode = "404", description = "SKU not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSku(
            @Parameter(description = "SKU UUID") @PathVariable UUID id) {
        skuService.deleteSku(id);
        return ResponseEntity.noContent().build();
    }
}
