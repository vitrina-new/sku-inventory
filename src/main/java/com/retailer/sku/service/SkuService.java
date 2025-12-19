package com.retailer.sku.service;

import com.retailer.sku.model.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SkuService {

    SkuResponse createSku(SkuRequest request);

    List<SkuResponse> createSkusBatch(List<SkuRequest> requests);

    SkuResponse getSkuById(UUID id);

    SkuResponse getSkuByCode(String skuCode);

    SkuResponse getSkuByUpc(String upc);

    Page<SkuResponse> getAllSkus(Pageable pageable);

    Page<SkuResponse> getSkusByFilters(
            String category,
            String status,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable);

    Page<SkuResponse> searchSkus(SkuSearchCriteria criteria, Pageable pageable);

    SkuResponse updateSku(UUID id, SkuRequest request);

    SkuResponse partialUpdateSku(UUID id, SkuUpdateRequest request);

    void deleteSku(UUID id);
}
