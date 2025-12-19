package com.retailer.sku.service;

import com.retailer.sku.model.dto.DimensionsDto;
import com.retailer.sku.model.dto.SkuRequest;
import com.retailer.sku.model.dto.SkuResponse;
import com.retailer.sku.model.dto.SkuUpdateRequest;
import com.retailer.sku.model.entity.Dimensions;
import com.retailer.sku.model.entity.Sku;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkuMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skuCode", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Sku toEntity(SkuRequest request);

    SkuResponse toResponse(Sku sku);

    List<SkuResponse> toResponseList(List<Sku> skus);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skuCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(SkuUpdateRequest request, @MappingTarget Sku sku);

    DimensionsDto toDimensionsDto(Dimensions dimensions);

    Dimensions toDimensions(DimensionsDto dto);
}
