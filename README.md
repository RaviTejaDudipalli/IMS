# Inventory Management System (IMS)

Production-grade Spring Boot 3 backend for managing products, suppliers, categories, users, and stock movements with full audit history.

## Tech Stack

| Layer        | Choice                                            |
|--------------|---------------------------------------------------|
| Language     | Java 17                                           |
| Framework    | Spring Boot 3.3                                   |
| Persistence  | Spring Data JPA / Hibernate                       |
| Database     | MySQL 8 (prod) / H2 in-memory (dev)               |
| Security     | Spring Security 6 + JWT (HS256)                   |
| Validation   | Jakarta Validation (Hibernate Validator)          |
| Docs         | springdoc-openapi (Swagger UI)                    |
| Build        | Maven                                             |
| Boilerplate  | Lombok                                            |

## Features

- Product / Category / Supplier / User CRUD
- **Stock IN / OUT** operations with full transaction audit history (`InventoryTransaction`)
- Pessimistic locking on stock updates -> safe under concurrent writes
- `@Version` optimistic lock on Product as a second line of defence
- **Low-stock alerts** (per-product threshold, queryable, surfaced in product responses)
- **JWT authentication** with `ADMIN` / `STAFF` roles
- Method-level role authorization (`@PreAuthorize`)
- Pagination, sorting, and rich filtering on products (search, price range, category, supplier, low-stock-only, in-stock-only)
- Global exception handler returning consistent JSON error responses
- Correlation-ID + per-request access logging
- Inventory summary report (top-selling products, totals, value)
- OpenAPI / Swagger UI out of the box

## Project Structure

```
c:\IMS
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/com/ims
    │   │   ├── InventoryManagementApplication.java
    │   │   ├── config
    │   │   │   ├── DataSeeder.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   ├── RequestLoggingFilterConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller
    │   │   │   ├── AuthController.java
    │   │   │   ├── CategoryController.java
    │   │   │   ├── InventoryController.java
    │   │   │   ├── ProductController.java
    │   │   │   ├── ReportController.java
    │   │   │   ├── SupplierController.java
    │   │   │   └── UserController.java
    │   │   ├── dto
    │   │   │   ├── request   (CategoryRequest, SupplierRequest, ProductRequest,
    │   │   │   │             StockMovementRequest, LoginRequest, RegisterRequest)
    │   │   │   └── response  (ApiResponse, PageResponse, ErrorResponse, *Response,
    │   │   │                  AuthResponse, InventorySummaryResponse)
    │   │   ├── entity
    │   │   │   ├── BaseEntity.java
    │   │   │   ├── Category.java
    │   │   │   ├── InventoryTransaction.java
    │   │   │   ├── Product.java
    │   │   │   ├── Role.java
    │   │   │   ├── Supplier.java
    │   │   │   ├── TransactionType.java
    │   │   │   └── User.java
    │   │   ├── exception
    │   │   │   ├── BusinessException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── InsufficientStockException.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── mapper
    │   │   │   ├── CategoryMapper.java
    │   │   │   ├── ProductMapper.java
    │   │   │   ├── SupplierMapper.java
    │   │   │   ├── TransactionMapper.java
    │   │   │   └── UserMapper.java
    │   │   ├── repository
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── InventoryTransactionRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── SupplierRepository.java
    │   │   │   ├── UserRepository.java
    │   │   │   └── specification/ProductSpecifications.java
    │   │   ├── security
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   ├── JwtProperties.java
    │   │   │   ├── JwtService.java
    │   │   │   ├── RestAccessDeniedHandler.java
    │   │   │   └── RestAuthenticationEntryPoint.java
    │   │   └── service
    │   │       ├── AuthService.java
    │   │       ├── CategoryService.java
    │   │       ├── InventoryService.java
    │   │       ├── ProductService.java
    │   │       ├── ReportService.java
    │   │       ├── SupplierService.java
    │   │       └── UserService.java
    │   └── resources
    │       ├── application.properties
    │       ├── application-dev.properties
    │       └── application-mysql.properties
    └── test/java/com/ims
        └── InventoryManagementApplicationTests.java
```

## Entity Relationships

```
Category 1 ── * Product * ── 1 Supplier
                   │
                   └── 1 ── * InventoryTransaction ── 0..1 User (performedBy)

User (ADMIN | STAFF)
```

- `Product` has FK `category_id` (NOT NULL) and `supplier_id` (NOT NULL).
- `InventoryTransaction` records every IN/OUT with `quantityAfter` snapshot for audit.
- `User` is unique on `username` and `email`.

## Build & Run

### Prerequisites
- JDK 17+
- Maven 3.9+
- (Optional) MySQL 8 if running with the `mysql` profile

### Run with H2 (default, zero setup)

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080/api`.

- Swagger UI:   http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v3/api-docs
- H2 console:   http://localhost:8080/api/h2-console  (JDBC URL `jdbc:h2:mem:imsdb`, user `sa`, blank password)

### Run with MySQL

```bash
export DB_URL='jdbc:mysql://localhost:3306/ims_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true'
export DB_USERNAME=root
export DB_PASSWORD=secret
export IMS_JWT_SECRET=$(openssl rand -base64 48)

mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Build a runnable JAR

```bash
mvn clean package
java -jar target/inventory-management-system-1.0.0.jar
```

### Default seeded credentials

| Username | Password     | Role  |
|----------|--------------|-------|
| `admin`  | `password123`| ADMIN |
| `staff`  | `password123`| STAFF |

> Change these in production by setting up real users via `POST /auth/register` (admin only) and disabling/deleting the seeded ones, or by setting `ims.seed.enabled=false`.

## Authorization Matrix

| Endpoint                            | Public | STAFF | ADMIN |
|-------------------------------------|:------:|:-----:|:-----:|
| `POST /auth/login`                  |   ✅   |  ✅   |  ✅   |
| `POST /auth/register`               |        |       |  ✅   |
| `GET /products`, `GET /products/*`  |        |  ✅   |  ✅   |
| `POST/PUT /products`                |        |  ✅   |  ✅   |
| `DELETE /products/{id}`             |        |       |  ✅   |
| `GET /products/low-stock`           |        |  ✅   |  ✅   |
| `POST /inventory/stock-in`          |        |  ✅   |  ✅   |
| `POST /inventory/stock-out`         |        |  ✅   |  ✅   |
| `GET /inventory/transactions`       |        |  ✅   |  ✅   |
| `GET/POST/PUT/DELETE /categories`   |  R*    |  R    |  ✅   |
| `GET/POST/PUT/DELETE /suppliers`    |  R*    |  R    |  ✅   |
| `GET /users/me`                     |        |  ✅   |  ✅   |
| `GET/PATCH/DELETE /users/**`        |        |       |  ✅   |
| `GET /reports/summary`              |        |  ✅   |  ✅   |

`R` = read; `R*` = still requires authentication, but any role may read.

## Sample API Requests

All examples assume the `/api` context path. Replace `<TOKEN>` with the JWT returned by `/auth/login`.

### 1. Login

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"password123"}'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInMs": 3600000,
    "username": "admin",
    "role": "ADMIN"
  },
  "timestamp": "2026-04-26T10:00:00Z"
}
```

### 2. Register a new STAFF user (ADMIN only)

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "alice",
    "email": "alice@ims.local",
    "fullName": "Alice Storekeeper",
    "password": "Str0ngPass!",
    "role": "STAFF"
  }'
```

### 3. Create a category

```bash
curl -s -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Hardware","description":"Tools and hardware"}'
```

### 4. Create a supplier

```bash
curl -s -X POST http://localhost:8080/api/suppliers \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Bolt & Co",
    "contactPerson": "Mike B.",
    "email": "mike@boltco.example",
    "phone": "+1-555-0400",
    "address": "1 Workshop Ln"
  }'
```

### 5. Create a product

```bash
curl -s -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Cordless Drill",
    "sku": "HW-DRILL-001",
    "description": "18V cordless drill",
    "price": 129.99,
    "quantity": 30,
    "lowStockThreshold": 5,
    "categoryId": 1,
    "supplierId": 1
  }'
```

### 6. Search & paginate products

```bash
# ?search=mouse&minPrice=10&maxPrice=50&inStockOnly=true&page=0&size=10&sort=price,asc
curl -s -G http://localhost:8080/api/products \
  -H "Authorization: Bearer <TOKEN>" \
  --data-urlencode 'search=mouse' \
  --data-urlencode 'minPrice=10' \
  --data-urlencode 'maxPrice=50' \
  --data-urlencode 'inStockOnly=true' \
  --data-urlencode 'page=0' \
  --data-urlencode 'size=10' \
  --data-urlencode 'sort=price,asc'
```

### 7. Stock IN (record a purchase)

```bash
curl -s -X POST http://localhost:8080/api/inventory/stock-in \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "productId": 1,
    "quantity": 50,
    "note": "Restock from Acme",
    "referenceCode": "PO-2026-0001"
  }'
```

### 8. Stock OUT (record a sale / issue)

```bash
curl -s -X POST http://localhost:8080/api/inventory/stock-out \
  -H "Authorization: Bearer <TOKEN>" \
  -H 'Content-Type: application/json' \
  -d '{
    "productId": 1,
    "quantity": 3,
    "note": "Sold at front desk",
    "referenceCode": "SO-2026-0001"
  }'
```

> If quantity > available, the API returns HTTP `422 Unprocessable Entity` with an `InsufficientStockException` message.

### 9. Low-stock listing

```bash
curl -s http://localhost:8080/api/products/low-stock \
  -H "Authorization: Bearer <TOKEN>"
```

### 10. Transaction history (filterable)

```bash
# All OUT transactions for product 1 in the last 30 days
curl -s -G http://localhost:8080/api/inventory/transactions \
  -H "Authorization: Bearer <TOKEN>" \
  --data-urlencode 'type=OUT' \
  --data-urlencode 'productId=1'

# History for a specific product
curl -s "http://localhost:8080/api/inventory/products/1/transactions?page=0&size=20" \
  -H "Authorization: Bearer <TOKEN>"
```

### 11. Inventory summary report

```bash
curl -s "http://localhost:8080/api/reports/summary?days=30&topN=5" \
  -H "Authorization: Bearer <TOKEN>"
```

### 12. Error response shape

Any handled error returns a consistent payload:

```json
{
  "success": false,
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Insufficient stock for product 'ELEC-MOUSE-001'. Requested: 100, Available: 50",
  "path": "/api/inventory/stock-out",
  "timestamp": "2026-04-26T10:01:23Z"
}
```

Validation errors include the offending fields:

```json
{
  "success": false,
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation",
  "path": "/api/products",
  "timestamp": "2026-04-26T10:02:45Z",
  "fieldErrors": [
    { "field": "sku",   "rejectedValue": "",   "message": "SKU is required" },
    { "field": "price", "rejectedValue": -5.0, "message": "Price must be >= 0" }
  ]
}
```

## Concurrency Safety

`InventoryService.stockIn` / `stockOut` load the product with `LockModeType.PESSIMISTIC_WRITE` so two simultaneous stock-out requests for the same product cannot both observe the same pre-decrement quantity. `Product.@Version` adds optimistic locking against any other code path that mutates a product outside of `findByIdForUpdate`. A concurrency conflict surfaces as HTTP `409`.

## Logging

- Console + rolling file (`logs/ims.log`, 10MB / 14 days retained).
- Every HTTP request logs method, path, status, and duration.
- An `X-Correlation-Id` header is propagated (or generated) and put into MDC so all log lines for one request can be correlated.

## Notes for Production

1. Replace `IMS_JWT_SECRET` with a random base64 value (`openssl rand -base64 48`).
2. Set `ims.seed.enabled=false` after first boot, then change/remove the default users.
3. Switch `spring.jpa.hibernate.ddl-auto` from `update` to managed migrations (Flyway / Liquibase).
4. Lock down CORS to your real frontend origins (`SecurityConfig.corsConfigurationSource`).
5. Put the service behind HTTPS termination and add rate limiting at the gateway.
