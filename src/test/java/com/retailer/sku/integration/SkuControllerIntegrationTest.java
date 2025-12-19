package com.retailer.sku.integration;

import com.retailer.sku.model.dto.SkuRequest;
import com.retailer.sku.model.dto.SkuResponse;
import com.retailer.sku.model.entity.Sku;
import com.retailer.sku.repository.SkuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class SkuControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("skudb_test")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SkuRepository skuRepository;

    @BeforeEach
    void setUp() {
        skuRepository.deleteAll();
    }

    @Test
    void createAndRetrieveSku_Success() {
        // Create
        SkuRequest request = SkuRequest.builder()
                .name("Test Product")
                .category("LBR")
                .brand("TestBrand")
                .price(new BigDecimal("10.99"))
                .upc("012345678901")
                .build();

        ResponseEntity<SkuResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/skus", request, SkuResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getSkuCode()).startsWith("THD-LBR-");
        assertThat(createResponse.getBody().getName()).isEqualTo("Test Product");

        // Retrieve by ID
        String skuCode = createResponse.getBody().getSkuCode();
        ResponseEntity<SkuResponse> getResponse = restTemplate.getForEntity(
                "/api/v1/skus/code/{code}", SkuResponse.class, skuCode);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Test Product");
    }

    @Test
    void createSku_DuplicateUpc_ReturnsConflict() {
        // Create first SKU
        SkuRequest request1 = SkuRequest.builder()
                .name("Product 1")
                .category("LBR")
                .upc("012345678901")
                .build();

        restTemplate.postForEntity("/api/v1/skus", request1, SkuResponse.class);

        // Attempt to create second SKU with same UPC
        SkuRequest request2 = SkuRequest.builder()
                .name("Product 2")
                .category("LBR")
                .upc("012345678901")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/skus", request2, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getSkuById_NotFound_ReturnsNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/skus/{id}", String.class, "550e8400-e29b-41d4-a716-446655440000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteSku_SoftDeletes() {
        // Create SKU
        SkuRequest request = SkuRequest.builder()
                .name("Product to Delete")
                .category("LBR")
                .build();

        ResponseEntity<SkuResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/skus", request, SkuResponse.class);

        String skuId = createResponse.getBody().getId().toString();

        // Delete SKU
        restTemplate.delete("/api/v1/skus/{id}", skuId);

        // Verify soft delete
        Sku deletedSku = skuRepository.findById(createResponse.getBody().getId()).orElse(null);
        assertThat(deletedSku).isNotNull();
        assertThat(deletedSku.getStatus()).isEqualTo("DISCONTINUED");
    }

    @Test
    void updateSku_FullUpdate_Success() {
        // Create SKU
        SkuRequest createRequest = SkuRequest.builder()
                .name("Original Product")
                .category("LBR")
                .brand("OriginalBrand")
                .price(new BigDecimal("10.00"))
                .build();

        ResponseEntity<SkuResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/skus", createRequest, SkuResponse.class);

        String skuId = createResponse.getBody().getId().toString();

        // Update SKU
        SkuRequest updateRequest = SkuRequest.builder()
                .name("Updated Product")
                .category("LBR")
                .brand("UpdatedBrand")
                .price(new BigDecimal("15.00"))
                .build();

        ResponseEntity<SkuResponse> updateResponse = restTemplate.exchange(
                "/api/v1/skus/{id}", HttpMethod.PUT,
                new HttpEntity<>(updateRequest), SkuResponse.class, skuId);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getName()).isEqualTo("Updated Product");
        assertThat(updateResponse.getBody().getBrand()).isEqualTo("UpdatedBrand");
        assertThat(updateResponse.getBody().getPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void searchSkus_ByCategory_ReturnsFilteredResults() {
        // Create SKUs in different categories
        SkuRequest lumberRequest = SkuRequest.builder()
                .name("Lumber Product")
                .category("LBR")
                .build();

        SkuRequest plumbingRequest = SkuRequest.builder()
                .name("Plumbing Product")
                .category("PLB")
                .build();

        restTemplate.postForEntity("/api/v1/skus", lumberRequest, SkuResponse.class);
        restTemplate.postForEntity("/api/v1/skus", plumbingRequest, SkuResponse.class);

        // Search by category
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
                "/api/v1/skus?category=LBR", String.class);

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).contains("Lumber Product");
        assertThat(searchResponse.getBody()).doesNotContain("Plumbing Product");
    }
}
