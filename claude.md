# SKU Management API Service

A cloud-native RESTful API service for creating and managing Stock Keeping Units (SKUs) for retail operations. Built with Java 20+, Spring Boot 3.x, containerized with Docker, and instrumented with OpenTelemetry.

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 20+ (prefer 21 LTS) |
| Framework | Spring Boot | 3.2+ |
| Build Tool | Gradle (Kotlin DSL) | 8.5+ |
| Container | Docker | Multi-stage build |
| Observability | OpenTelemetry | 1.30+ |
| API Docs | SpringDoc OpenAPI | 2.3+ |
| Database | PostgreSQL | 15+ |
| Testing | JUnit 5, Testcontainers | Latest |

## Project Structure

```
sku-service/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/com/retailer/sku/
│   │   │   ├── SkuServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── OpenTelemetryConfig.java
│   │   │   ├── controller/
│   │   │   │   └── SkuController.java
│   │   │   ├── service/
│   │   │   │   ├── SkuService.java
│   │   │   │   └── SkuServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── SkuRepository.java
│   │   │   ├── model/
│   │   │   │   ├── entity/
│   │   │   │   │   └── Sku.java
│   │   │   │   └── dto/
│   │   │   │       ├── SkuRequest.java
│   │   │   │       ├── SkuResponse.java
│   │   │   │       └── SkuUpdateRequest.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── SkuNotFoundException.java
│   │   │       └── DuplicateSkuException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/retailer/sku/
│           ├── controller/
│           ├── service/
│           └── integration/
└── docs/
    └── openapi/
```

## SKU Domain Model

### Core Entity Fields

```java
// Sku.java - Core entity representing a retail SKU
public class Sku {
    private UUID id;                    // Internal unique identifier
    private String skuCode;             // External SKU code (e.g., "THD-1234567890")
    private String upc;                 // Universal Product Code (12-digit)
    private String name;                // Product name
    private String description;         // Detailed description
    private String brand;               // Brand name
    private String category;            // Product category (e.g., "LUMBER", "PLUMBING")
    private String subcategory;         // Subcategory (e.g., "PRESSURE_TREATED")
    private BigDecimal price;           // Current retail price
    private BigDecimal cost;            // Wholesale cost
    private String unitOfMeasure;       // Unit (EACH, SQFT, LINEAR_FT, etc.)
    private Integer quantityPerUnit;    // Quantity in package
    private BigDecimal weight;          // Weight in pounds
    private Dimensions dimensions;      // L x W x H in inches
    private String status;              // ACTIVE, DISCONTINUED, SEASONAL
    private List<String> tags;          // Searchable tags
    private Map<String, String> attributes; // Flexible key-value attributes
    private Instant createdAt;
    private Instant updatedAt;
}
```

### SKU Code Generation Strategy

Generate SKU codes using a prefix + category + sequence pattern:

```
Format: {RETAILER}-{CATEGORY_CODE}-{SEQUENCE}
Example: THD-LBR-0001234 (Home Depot Lumber item #1234)

Category Codes:
- LBR: Lumber & Building Materials
- PLB: Plumbing
- ELC: Electrical
- HRD: Hardware
- PNT: Paint
- GAR: Garden & Outdoor
- APL: Appliances
- FLR: Flooring
- KIT: Kitchen & Bath
- TOL: Tools
```

## API Endpoints

### RESTful Resource Design

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/skus` | Create a new SKU |
| GET | `/api/v1/skus` | List SKUs (paginated, filterable) |
| GET | `/api/v1/skus/{id}` | Get SKU by ID |
| GET | `/api/v1/skus/code/{skuCode}` | Get SKU by code |
| GET | `/api/v1/skus/upc/{upc}` | Get SKU by UPC |
| PUT | `/api/v1/skus/{id}` | Full update of SKU |
| PATCH | `/api/v1/skus/{id}` | Partial update of SKU |
| DELETE | `/api/v1/skus/{id}` | Soft delete SKU |
| POST | `/api/v1/skus/batch` | Bulk create SKUs |
| GET | `/api/v1/skus/search` | Search SKUs by criteria |

### Query Parameters for List/Search

```
GET /api/v1/skus?category=LUMBER&status=ACTIVE&minPrice=10&maxPrice=100&page=0&size=20&sort=name,asc
```

## Gradle Build Configuration

### build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

group = "com.retailer"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // OpenTelemetry
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.34.1"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.1.0")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    
    // Micrometer for metrics bridge
    implementation("io.micrometer:micrometer-registry-otlp")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // Utilities
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Generate OpenAPI spec at build time
tasks.register("generateOpenApiSpec") {
    dependsOn("bootRun")
    doLast {
        // Fetch spec from running application
        exec {
            commandLine("curl", "-o", "docs/openapi/openapi.json", 
                "http://localhost:8080/v3/api-docs")
        }
    }
}
```

## OpenAPI Configuration

### OpenApiConfig.java

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SKU Management API")
                .version("1.0.0")
                .description("API for managing Stock Keeping Units (SKUs) for retail operations")
                .contact(new Contact()
                    .name("Retail Platform Team")
                    .email("platform@retailer.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://retailer.com/terms")))
            .externalDocs(new ExternalDocumentation()
                .description("SKU Service Documentation")
                .url("https://docs.retailer.com/sku-service"))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local"),
                new Server().url("https://api.retailer.com").description("Production")));
    }
}
```

### Controller Annotations for OpenAPI

```java
@RestController
@RequestMapping("/api/v1/skus")
@Tag(name = "SKU Management", description = "Operations for managing Stock Keeping Units")
public class SkuController {
    
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
        // Implementation
    }
}
```

## OpenTelemetry Configuration

### application.yml

```yaml
spring:
  application:
    name: sku-service

otel:
  exporter:
    otlp:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
      protocol: grpc
  resource:
    attributes:
      service.name: sku-service
      service.namespace: retail-platform
      deployment.environment: ${SPRING_PROFILES_ACTIVE:local}
  instrumentation:
    spring-web:
      enabled: true
    jdbc:
      enabled: true
    logback-appender:
      enabled: true
  metrics:
    exporter: otlp
  traces:
    exporter: otlp

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  tracing:
    sampling:
      probability: 1.0
  otlp:
    metrics:
      export:
        enabled: true
        endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
    tracing:
      endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}
```

### Custom Span Instrumentation

```java
@Service
public class SkuServiceImpl implements SkuService {
    
    private final Tracer tracer;
    
    @Override
    public SkuResponse createSku(SkuRequest request) {
        Span span = tracer.spanBuilder("sku.create")
            .setAttribute("sku.category", request.getCategory())
            .setAttribute("sku.brand", request.getBrand())
            .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // Business logic
            String skuCode = generateSkuCode(request.getCategory());
            span.setAttribute("sku.code", skuCode);
            
            Sku sku = skuRepository.save(mapToEntity(request, skuCode));
            span.addEvent("sku.persisted");
            
            return mapToResponse(sku);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

## Docker Configuration

### Dockerfile (Multi-stage)

```dockerfile
# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle files first for dependency caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon

# Copy source and build
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Extract layered JAR for optimized Docker layers
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Security: Run as non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy layers in order of change frequency
COPY --from=builder /app/extracted/dependencies/ ./
COPY --from=builder /app/extracted/spring-boot-loader/ ./
COPY --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/extracted/application/ ./

# OpenTelemetry Java Agent
ADD --chown=spring:spring https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.1.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/app/opentelemetry-javaagent.jar org.springframework.boot.loader.launch.JarLauncher"]
```

### docker-compose.yml

```yaml
version: '3.8'

services:
  sku-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/skudb
      SPRING_DATASOURCE_USERNAME: sku_user
      SPRING_DATASOURCE_PASSWORD: sku_password
      OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4317
      OTEL_SERVICE_NAME: sku-service
      OTEL_RESOURCE_ATTRIBUTES: "service.namespace=retail,deployment.environment=local"
    depends_on:
      postgres:
        condition: service_healthy
      otel-collector:
        condition: service_started
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 60s

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: skudb
      POSTGRES_USER: sku_user
      POSTGRES_PASSWORD: sku_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sku_user -d skudb"]
      interval: 5s
      timeout: 5s
      retries: 5

  otel-collector:
    image: otel/opentelemetry-collector-contrib:0.94.0
    command: ["--config=/etc/otel-collector-config.yml"]
    volumes:
      - ./otel-collector-config.yml:/etc/otel-collector-config.yml
    ports:
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
      - "8889:8889"   # Prometheus metrics
    depends_on:
      - jaeger
      - prometheus

  jaeger:
    image: jaegertracing/all-in-one:1.54
    ports:
      - "16686:16686"  # Jaeger UI
      - "14250:14250"  # gRPC

  prometheus:
    image: prom/prometheus:v2.49.1
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"

  grafana:
    image: grafana/grafana:10.3.1
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  postgres_data:
  grafana_data:
```

## Testing Strategy

### Unit Tests

Test service layer with mocked repositories:

```java
@ExtendWith(MockitoExtension.class)
class SkuServiceImplTest {
    @Mock private SkuRepository skuRepository;
    @InjectMocks private SkuServiceImpl skuService;
    
    @Test
    void createSku_ValidRequest_ReturnsSkuResponse() {
        // Given
        SkuRequest request = createValidRequest();
        when(skuRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        
        // When
        SkuResponse response = skuService.createSku(request);
        
        // Then
        assertThat(response.getSkuCode()).startsWith("THD-");
        verify(skuRepository).save(any());
    }
}
```

### Integration Tests with Testcontainers

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SkuControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createAndRetrieveSku_Success() {
        // Create
        SkuRequest request = new SkuRequest("Test Product", "LBR", "TestBrand");
        ResponseEntity<SkuResponse> createResponse = restTemplate.postForEntity(
            "/api/v1/skus", request, SkuResponse.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Retrieve
        String skuCode = createResponse.getBody().getSkuCode();
        ResponseEntity<SkuResponse> getResponse = restTemplate.getForEntity(
            "/api/v1/skus/code/{code}", SkuResponse.class, skuCode);
        
        assertThat(getResponse.getBody().getName()).isEqualTo("Test Product");
    }
}
```

## Build and Run Commands

```bash
# Build the application
./gradlew build

# Run tests
./gradlew test

# Run locally with Gradle
./gradlew bootRun --args='--spring.profiles.active=local'

# Build Docker image
docker build -t sku-service:latest .

# Run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f sku-service

# Access OpenAPI UI
# http://localhost:8080/swagger-ui.html

# Download OpenAPI spec
curl http://localhost:8080/v3/api-docs -o openapi.json
curl http://localhost:8080/v3/api-docs.yaml -o openapi.yaml

# Access Jaeger UI for traces
# http://localhost:16686

# Access Grafana for dashboards
# http://localhost:3000 (admin/admin)
```

## Best Practices

### API Design
- Use consistent resource naming (plural nouns)
- Version APIs in URL path (`/api/v1/`)
- Return appropriate HTTP status codes
- Implement HATEOAS links for discoverability
- Use ETags for caching and optimistic locking

### Error Handling
- Return RFC 7807 Problem Details format
- Include correlation IDs in error responses
- Log errors with trace context

### Performance
- Implement pagination for list endpoints
- Use database indexes on frequently queried fields
- Enable response compression
- Configure connection pooling (HikariCP)

### Security
- Validate all input with Bean Validation
- Sanitize output to prevent XSS
- Use parameterized queries (JPA handles this)
- Implement rate limiting
- Add authentication/authorization (Spring Security + OAuth2)

### Observability
- Emit custom metrics for business KPIs
- Add semantic conventions to spans
- Include baggage for cross-service context
- Configure log correlation with trace IDs
