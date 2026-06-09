# E-Commerce Microservices

A Spring Boot microservices project demonstrating an e-commerce backend with independent services for users, products, and orders communicating over HTTP.

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│                      Client / API Consumer                │
└───────────────┬──────────────────┬───────────────────────┘
                │                  │
     ┌──────────▼──────┐  ┌────────▼────────┐
     │   User Service  │  │ Product Service  │
     │   :8082         │  │   :8083          │
     └──────────▲──────┘  └────────▲─────────┘
                │                  │
     ┌──────────┴──────────────────┴──────────┐
     │              Order Service             │
     │                 :8084                  │
     └────────────────────────────────────────┘
```

The Order Service orchestrates the other two — it calls User Service to validate the buyer and Product Service to validate/look up items.

## Services

| Service | Port | Description |
|---|---|---|
| `user` | 8082 | User profiles, roles, and addresses |
| `product` | 8083 | Product catalog with soft-delete and search |
| `order` | 8084 | Cart management and order creation |
| `product-client` | — | Shared HTTP client library (published to GitHub Packages) |

## Tech Stack

- **Java 17**, **Spring Boot 4.0.6**
- **Spring Data JPA** + **H2** (in-memory, per-service)
- **Spring WebMVC** (`RestClient` + `@HttpExchange` for inter-service calls)
- **Lombok**, **Maven**

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run services

Each service is a standalone Spring Boot app. Start them in this order so the Order Service can reach the others:

```bash
# Terminal 1 — User Service
cd user && ./mvnw spring-boot:run

# Terminal 2 — Product Service
cd product && ./mvnw spring-boot:run

# Terminal 3 — Order Service
cd order && ./mvnw spring-boot:run
```

H2 consoles are available at `http://localhost:{port}/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`).

## API Reference

### User Service — `http://localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `POST` | `/api/users` | Create user |
| `PUT` | `/api/users/{id}` | Update user |

**Create user request body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "phone": "555-0100",
  "role": "CUSTOMER",
  "address": {
    "street": "1 Main St",
    "city": "Springfield",
    "state": "IL",
    "country": "US",
    "zipcode": "62701"
  }
}
```

User roles: `CUSTOMER`, `ADMIN`

---

### Product Service — `http://localhost:8083`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | List all active products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/products` | Create product |
| `PUT` | `/api/products/{id}` | Update product |
| `DELETE` | `/api/products/{id}` | Soft-delete product |
| `GET` | `/api/products/search?keyword={kw}` | Search active products |

**Create product request body:**
```json
{
  "name": "Wireless Headphones",
  "description": "Noise-cancelling over-ear headphones",
  "price": 79.99,
  "stockQuantity": 50,
  "category": "Electronics",
  "imageUrl": "https://example.com/img.jpg"
}
```

> Deletion is a soft-delete — it sets `active = false` rather than removing the row.

---

### Order Service — `http://localhost:8084`

All endpoints require an `X-User-Id` header containing the user's ID.

**Cart**

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/cart/add` | Add item to cart |
| `DELETE` | `/api/cart/items/{productId}` | Remove item from cart |
| `GET` | `/api/cart/usercart` | Get current user's cart |

**Add to cart request body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Orders**

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders` | Create order from cart (clears cart on success) |

Order statuses: `PENDING`, `ACTIVE`, `CONFIRMED`, `SHIPPED`, `DELIVERED`

## Product Client Library

`product-client` is a small shared JAR that exposes a declarative `@HttpExchange` interface for calling the Product Service. It is published to GitHub Packages and consumed by the Order Service.

```xml
<dependency>
  <groupId>com.ecommerce</groupId>
  <artifactId>product-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

To publish a new version:
```bash
cd product-client
./mvnw deploy
```

## Project Structure

```
microServicess/
├── user/                  # User Service
├── product/               # Product Service
├── order/                 # Order Service
└── product-client/        # Shared HTTP client library
```

Each service follows the same internal layout:

```
src/main/java/com/ecommerce/{service}/
├── {Service}Application.java
├── controllers/
├── services/
├── models/        # JPA entities + enums
├── dtos/          # Request / response DTOs
├── repositories/
└── config/        # HTTP client beans (Order Service)
```
