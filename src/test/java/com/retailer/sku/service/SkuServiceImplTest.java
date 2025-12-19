package com.retailer.sku.service;

import com.retailer.sku.exception.DuplicateSkuException;
import com.retailer.sku.exception.SkuNotFoundException;
import com.retailer.sku.model.dto.SkuRequest;
import com.retailer.sku.model.dto.SkuResponse;
import com.retailer.sku.model.dto.SkuUpdateRequest;
import com.retailer.sku.model.entity.Sku;
import com.retailer.sku.repository.SkuRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkuServiceImplTest {

    @Mock
    private SkuRepository skuRepository;

    @Mock
    private SkuMapper skuMapper;

    @Mock
    private Tracer tracer;

    @Mock
    private SpanBuilder spanBuilder;

    @Mock
    private Span span;

    @Mock
    private Scope scope;

    @InjectMocks
    private SkuServiceImpl skuService;

    private SkuRequest validRequest;
    private Sku sku;
    private SkuResponse skuResponse;
    private UUID skuId;

    @BeforeEach
    void setUp() {
        skuId = UUID.randomUUID();

        validRequest = SkuRequest.builder()
                .name("Test Product")
                .category("LBR")
                .brand("TestBrand")
                .price(new BigDecimal("10.99"))
                .upc("012345678901")
                .build();

        sku = Sku.builder()
                .id(skuId)
                .skuCode("THD-LBR-0000001")
                .name("Test Product")
                .category("LBR")
                .brand("TestBrand")
                .price(new BigDecimal("10.99"))
                .upc("012345678901")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
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

        // Setup tracer mocks
        lenient().when(tracer.spanBuilder(anyString())).thenReturn(spanBuilder);
        lenient().when(spanBuilder.setAttribute(anyString(), anyString())).thenReturn(spanBuilder);
        lenient().when(spanBuilder.startSpan()).thenReturn(span);
        lenient().when(span.makeCurrent()).thenReturn(scope);
    }

    @Test
    void createSku_ValidRequest_ReturnsSkuResponse() {
        when(skuRepository.existsByUpc(anyString())).thenReturn(false);
        when(skuMapper.toEntity(any(SkuRequest.class))).thenReturn(sku);
        when(skuRepository.findMaxSequenceByPrefix(anyString())).thenReturn(0);
        when(skuRepository.save(any(Sku.class))).thenReturn(sku);
        when(skuMapper.toResponse(any(Sku.class))).thenReturn(skuResponse);

        SkuResponse response = skuService.createSku(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getCategory()).isEqualTo("LBR");
        verify(skuRepository).save(any(Sku.class));
    }

    @Test
    void createSku_DuplicateUpc_ThrowsException() {
        when(skuRepository.existsByUpc(anyString())).thenReturn(true);

        assertThatThrownBy(() -> skuService.createSku(validRequest))
                .isInstanceOf(DuplicateSkuException.class)
                .hasMessageContaining("UPC");
    }

    @Test
    void getSkuById_ExistingSku_ReturnsSkuResponse() {
        when(skuRepository.findById(skuId)).thenReturn(Optional.of(sku));
        when(skuMapper.toResponse(sku)).thenReturn(skuResponse);

        SkuResponse response = skuService.getSkuById(skuId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(skuId);
    }

    @Test
    void getSkuById_NonExistingSku_ThrowsException() {
        when(skuRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skuService.getSkuById(skuId))
                .isInstanceOf(SkuNotFoundException.class)
                .hasMessageContaining(skuId.toString());
    }

    @Test
    void getSkuByCode_ExistingSku_ReturnsSkuResponse() {
        String skuCode = "THD-LBR-0000001";
        when(skuRepository.findBySkuCode(skuCode)).thenReturn(Optional.of(sku));
        when(skuMapper.toResponse(sku)).thenReturn(skuResponse);

        SkuResponse response = skuService.getSkuByCode(skuCode);

        assertThat(response).isNotNull();
        assertThat(response.getSkuCode()).isEqualTo(skuCode);
    }

    @Test
    void getSkuByUpc_ExistingSku_ReturnsSkuResponse() {
        String upc = "012345678901";
        when(skuRepository.findByUpc(upc)).thenReturn(Optional.of(sku));
        when(skuMapper.toResponse(sku)).thenReturn(skuResponse);

        SkuResponse response = skuService.getSkuByUpc(upc);

        assertThat(response).isNotNull();
        assertThat(response.getUpc()).isEqualTo(upc);
    }

    @Test
    void deleteSku_ExistingSku_SoftDeletes() {
        when(skuRepository.findById(skuId)).thenReturn(Optional.of(sku));
        when(skuRepository.save(any(Sku.class))).thenReturn(sku);

        skuService.deleteSku(skuId);

        verify(skuRepository).save(argThat(s -> "DISCONTINUED".equals(s.getStatus())));
    }

    @Test
    void deleteSku_NonExistingSku_ThrowsException() {
        when(skuRepository.findById(skuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skuService.deleteSku(skuId))
                .isInstanceOf(SkuNotFoundException.class);
    }

    @Test
    void partialUpdateSku_ValidRequest_UpdatesOnlyProvidedFields() {
        SkuUpdateRequest updateRequest = SkuUpdateRequest.builder()
                .name("Updated Product Name")
                .build();

        when(skuRepository.findById(skuId)).thenReturn(Optional.of(sku));
        when(skuRepository.save(any(Sku.class))).thenReturn(sku);
        when(skuMapper.toResponse(any(Sku.class))).thenReturn(skuResponse);

        SkuResponse response = skuService.partialUpdateSku(skuId, updateRequest);

        assertThat(response).isNotNull();
        verify(skuMapper).updateEntityFromRequest(eq(updateRequest), any(Sku.class));
        verify(skuRepository).save(any(Sku.class));
    }
}
