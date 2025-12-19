package com.retailer.sku.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.sku.exception.GlobalExceptionHandler;
import com.retailer.sku.exception.SkuNotFoundException;
import com.retailer.sku.model.dto.SkuRequest;
import com.retailer.sku.model.dto.SkuResponse;
import com.retailer.sku.service.SkuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SkuControllerTest {

    @Mock
    private SkuService skuService;

    @InjectMocks
    private SkuController skuController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private SkuRequest validRequest;
    private SkuResponse skuResponse;
    private UUID skuId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(skuController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        skuId = UUID.randomUUID();

        validRequest = SkuRequest.builder()
                .name("Test Product")
                .category("LBR")
                .brand("TestBrand")
                .price(new BigDecimal("10.99"))
                .upc("012345678901")
                .build();

        skuResponse = SkuResponse.builder()
                .id(skuId)
                .skuCode("THD-LBR-0000001")
                .name("Test Product")
                .category("LBR")
                .brand("TestBrand")
                .price(new BigDecimal("10.99"))
                .upc("012345678901")
                .status("ACTIVE")
                .build();
    }

    @Test
    void createSku_ValidRequest_ReturnsCreated() throws Exception {
        when(skuService.createSku(any(SkuRequest.class))).thenReturn(skuResponse);

        mockMvc.perform(post("/api/v1/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skuCode").value("THD-LBR-0000001"))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(skuService).createSku(any(SkuRequest.class));
    }

    @Test
    void createSku_InvalidRequest_ReturnsBadRequest() throws Exception {
        SkuRequest invalidRequest = SkuRequest.builder()
                .name("") // Invalid: blank name
                .category("INVALID") // Invalid: not 3 uppercase letters
                .build();

        mockMvc.perform(post("/api/v1/skus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSkuById_ExistingSku_ReturnsOk() throws Exception {
        when(skuService.getSkuById(skuId)).thenReturn(skuResponse);

        mockMvc.perform(get("/api/v1/skus/{id}", skuId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(skuId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getSkuById_NonExistingSku_ReturnsNotFound() throws Exception {
        when(skuService.getSkuById(skuId)).thenThrow(new SkuNotFoundException("SKU not found"));

        mockMvc.perform(get("/api/v1/skus/{id}", skuId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("SKU Not Found"));
    }

    @Test
    void getSkuByCode_ExistingSku_ReturnsOk() throws Exception {
        String skuCode = "THD-LBR-0000001";
        when(skuService.getSkuByCode(skuCode)).thenReturn(skuResponse);

        mockMvc.perform(get("/api/v1/skus/code/{skuCode}", skuCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skuCode").value(skuCode));
    }

    @Test
    void getSkuByUpc_ExistingSku_ReturnsOk() throws Exception {
        String upc = "012345678901";
        when(skuService.getSkuByUpc(upc)).thenReturn(skuResponse);

        mockMvc.perform(get("/api/v1/skus/upc/{upc}", upc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upc").value(upc));
    }

    @Test
    void deleteSku_ExistingSku_ReturnsNoContent() throws Exception {
        doNothing().when(skuService).deleteSku(skuId);

        mockMvc.perform(delete("/api/v1/skus/{id}", skuId))
                .andExpect(status().isNoContent());

        verify(skuService).deleteSku(skuId);
    }

    @Test
    void deleteSku_NonExistingSku_ReturnsNotFound() throws Exception {
        doThrow(new SkuNotFoundException("SKU not found")).when(skuService).deleteSku(skuId);

        mockMvc.perform(delete("/api/v1/skus/{id}", skuId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSku_ValidRequest_ReturnsOk() throws Exception {
        when(skuService.updateSku(eq(skuId), any(SkuRequest.class))).thenReturn(skuResponse);

        mockMvc.perform(put("/api/v1/skus/{id}", skuId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }
}
