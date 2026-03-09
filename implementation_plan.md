# Plan de Implementación: Backend Marketplace Practice

Arquitectura de microservicios orientada a eventos, dividida en 10 fases de implementación, desarrollada en **Java con Spring Boot**.

## Fases de Desarrollo

### Fase 1: Repositorio e Infraestructura Base
- **Docker Compose (`infra/docker-compose.yml`)**: PostgreSQL, Kafka + KRaft/Zookeeper, Redis.
- **Template de Microservicio (Spring Boot)**: Creación de un proyecto base o arquetipo con la estructura estándar de Java:
  - `src/main/java/com/marketplace/{service}/`
    - `api/` (Controllers HTTP)
    - `service/` (Lógica de negocio / Casos de uso)
    - `repository/` (Interfaces Spring Data JPA)
    - `model/` o `entity/` (Entidades de dominio)
    - `config/` (Configuraciones de Beans, Kafka, Redis)
    - `exception/` (Manejo global de errores)
- **API Gateway**: Instancia de Spring Cloud Gateway ruteando `/orders` al Order Service y `/payments` al Payment Service.

### Fase 2: Order Service
- **Entidades**: `orders` y `idempotency_keys`.
- **Lógica**: Exponer POST `/orders` que guarda la orden y emite `OrderCreated`. Se usará `Idempotency-Key` en headers para evitar crear la misma orden múltiples veces. Exponer GET `/orders/{id}`.

### Fase 3: Inventory Service
- **Entidades**: `products` con control de concurrencia usando *Optimistic Locking* (columna `version`).
- **Lógica**: Reacciona a `OrderCreated`. Si hay stock suficiente -> resta stock y emite `StockReserved`. Si no -> emite `StockFailed`.

### Fase 4: Payment Service
- **Entidades**: `payments`.
- **Lógica**: Reacciona a `StockReserved`. Simula pagos agregando latencia aleatoria y ratios de fallo predefinidos. Emite `PaymentApproved` o `PaymentRejected`. Implementa chequeo de idempotencia por `order_id` para no cobrar duplicado.

### Fase 5: Invoice Service
- **Entidades**: `invoices` (con constraint `unique(order_id)`).
- **Lógica**: Reacciona a `PaymentApproved`. Genera secuencia de factura única (e.g. `INV-YYYY-XXXXXX`).

### Fase 6: Notification Service
- **Lógica**: Servicio consumista/sink que reacciona a `InvoiceGenerated` y `PaymentRejected`. Solo guarda logs simulando envíos por email, webhooks o push.

### Fase 7: Saga y Compensaciones (Resiliencia)
- **Order Service**: Reacciona a `StockFailed` marcando la orden como `FAILED`.
- **Inventario/Otros**: Reacción a `PaymentRejected` provocando la anulación de reservas de stock (`ReleaseStock`) y la respectiva cancelación de orden (`CancelOrder`).

### Fase 8: Outbox Pattern (Transactional Outbox)
- **Implementación generalizada**: Los servicios que emiten eventos ya no escribirán directo a Kafka tras hacer un guardado local en su BD. Guardarán un registro en la tabla local `outbox_events` dentro de la **misma transacción** de base de datos.
- **Worker**: Un proceso asíncrono (o routine) sondeará `outbox_events` localmente y lo publicará a Kafka asegurando entrega al-menos-una-vez.

### Fase 9: Observabilidad
- **Correlation ID**: Todos los logs incluirán el header asíncrono `X-Request-ID`.
- **Métricas**: Endpoints en `/metrics` parseables por Prometheus instalados en el template base.
- **Tracing**: Instrumentación de llamadas HTTP y consumo/producción en Kafka vía OpenTelemetry enviando la telemetría a un contenedor Jaeger.

### Fase 10: Test de Concurrencia
- **Herramienta**: Script en `k6`.
- **Escenario**: Simular picos de carga de 1000 orders/min hacia el API Gateway. Monitorizar y ajustar infraestructura en contenedores según métricas de latencia, tasa de errores y el throughput general del flujo de compra completo.
