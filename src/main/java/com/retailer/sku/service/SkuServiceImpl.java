package com.retailer.sku.service;

import com.retailer.sku.exception.DuplicateSkuException;
import com.retailer.sku.exception.SkuNotFoundException;
import com.retailer.sku.model.dto.*;
import com.retailer.sku.model.entity.Sku;
import com.retailer.sku.repository.SkuRepository;
import com.retailer.sku.repository.SkuSpecifications;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SkuServiceImpl implements SkuService {

    private static final String RETAILER_PREFIX = "THD";
    private static final Map<String, String> CATEGORY_CODES = Map.of(
            "LBR", "Lumber & Building Materials",
            "PLB", "Plumbing",
            "ELC", "Electrical",
            "HRD", "Hardware",
            "PNT", "Paint",
            "GAR", "Garden & Outdoor",
            "APL", "Appliances",
            "FLR", "Flooring",
            "KIT", "Kitchen & Bath",
            "TOL", "Tools"
    );

    private final SkuRepository skuRepository;
    private final SkuMapper skuMapper;
    private final Tracer tracer;

    private final Map<String, AtomicInteger> sequenceCounters = new ConcurrentHashMap<>();

    @Override
    public SkuResponse createSku(SkuRequest request) {
        Span span = tracer.spanBuilder("sku.create")
                .setAttribute("sku.category", request.getCategory())
                .setAttribute("sku.brand", request.getBrand() != null ? request.getBrand() : "")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            validateUpcUniqueness(request.getUpc());

            Sku sku = skuMapper.toEntity(request);
            String skuCode = generateSkuCode(request.getCategory());
            sku.setSkuCode(skuCode);

            span.setAttribute("sku.code", skuCode);

            Sku savedSku = skuRepository.save(sku);
            span.addEvent("sku.persisted");

            log.info("Created SKU with code: {}", skuCode);
            return skuMapper.toResponse(savedSku);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public List<SkuResponse> createSkusBatch(List<SkuRequest> requests) {
        Span span = tracer.spanBuilder("sku.batch.create")
                .setAttribute(AttributeKey.longKey("batch.size"), (long) requests.size())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            List<Sku> skus = requests.stream()
                    .map(request -> {
                        validateUpcUniqueness(request.getUpc());
                        Sku sku = skuMapper.toEntity(request);
                        sku.setSkuCode(generateSkuCode(request.getCategory()));
                        return sku;
                    })
                    .toList();

            List<Sku> savedSkus = skuRepository.saveAll(skus);
            span.addEvent("batch.persisted");

            log.info("Created {} SKUs in batch", savedSkus.size());
            return skuMapper.toResponseList(savedSkus);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SkuResponse getSkuById(UUID id) {
        return skuRepository.findById(id)
                .map(skuMapper::toResponse)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public SkuResponse getSkuByCode(String skuCode) {
        return skuRepository.findBySkuCode(skuCode)
                .map(skuMapper::toResponse)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found with code: " + skuCode));
    }

    @Override
    @Transactional(readOnly = true)
    public SkuResponse getSkuByUpc(String upc) {
        return skuRepository.findByUpc(upc)
                .map(skuMapper::toResponse)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found with UPC: " + upc));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkuResponse> getAllSkus(Pageable pageable) {
        return skuRepository.findAll(pageable)
                .map(skuMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkuResponse> getSkusByFilters(
            String category,
            String status,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {
        return skuRepository.findByFilters(category, status, brand, minPrice, maxPrice, pageable)
                .map(skuMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkuResponse> searchSkus(SkuSearchCriteria criteria, Pageable pageable) {
        return skuRepository.findAll(SkuSpecifications.withSearchCriteria(criteria), pageable)
                .map(skuMapper::toResponse);
    }

    @Override
    public SkuResponse updateSku(UUID id, SkuRequest request) {
        Span span = tracer.spanBuilder("sku.update")
                .setAttribute("sku.id", id.toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Sku existingSku = skuRepository.findById(id)
                    .orElseThrow(() -> new SkuNotFoundException("SKU not found with id: " + id));

            if (request.getUpc() != null && !request.getUpc().equals(existingSku.getUpc())) {
                validateUpcUniqueness(request.getUpc());
            }

            Sku updatedSku = skuMapper.toEntity(request);
            updatedSku.setId(existingSku.getId());
            updatedSku.setSkuCode(existingSku.getSkuCode());
            updatedSku.setVersion(existingSku.getVersion());
            updatedSku.setCreatedAt(existingSku.getCreatedAt());

            Sku savedSku = skuRepository.save(updatedSku);
            span.addEvent("sku.updated");

            log.info("Updated SKU: {}", savedSku.getSkuCode());
            return skuMapper.toResponse(savedSku);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public SkuResponse partialUpdateSku(UUID id, SkuUpdateRequest request) {
        Span span = tracer.spanBuilder("sku.partial.update")
                .setAttribute("sku.id", id.toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Sku existingSku = skuRepository.findById(id)
                    .orElseThrow(() -> new SkuNotFoundException("SKU not found with id: " + id));

            if (request.getUpc() != null && !request.getUpc().equals(existingSku.getUpc())) {
                validateUpcUniqueness(request.getUpc());
            }

            skuMapper.updateEntityFromRequest(request, existingSku);
            Sku savedSku = skuRepository.save(existingSku);
            span.addEvent("sku.partially.updated");

            log.info("Partially updated SKU: {}", savedSku.getSkuCode());
            return skuMapper.toResponse(savedSku);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public void deleteSku(UUID id) {
        Span span = tracer.spanBuilder("sku.delete")
                .setAttribute("sku.id", id.toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            Sku sku = skuRepository.findById(id)
                    .orElseThrow(() -> new SkuNotFoundException("SKU not found with id: " + id));

            sku.setStatus("DISCONTINUED");
            skuRepository.save(sku);
            span.addEvent("sku.soft.deleted");

            log.info("Soft deleted SKU: {}", sku.getSkuCode());
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private String generateSkuCode(String category) {
        String prefix = RETAILER_PREFIX + "-" + category;

        AtomicInteger counter = sequenceCounters.computeIfAbsent(prefix, k -> {
            Integer maxSequence = skuRepository.findMaxSequenceByPrefix(prefix);
            return new AtomicInteger(maxSequence != null ? maxSequence : 0);
        });

        int sequence = counter.incrementAndGet();
        return String.format("%s-%07d", prefix, sequence);
    }

    private void validateUpcUniqueness(String upc) {
        if (upc != null && skuRepository.existsByUpc(upc)) {
            throw new DuplicateSkuException("SKU with UPC " + upc + " already exists");
        }
    }
}
