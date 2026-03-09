# Task: Backend Marketplace Practice - 10 Phases Roadmap

## Fase 1: Repositorio e Infraestructura Base
- [x] Crear estructura de directorios base (`backend-marketplace-practice`, `infra`, `*-service`)
- [x] Configurar `infra/docker-compose.yml` (Postgres, Kafka, Redis)
- [x] Crear base template para microservicios (Java 21, Spring Boot, Gradle, `src/main/java`)
- [x] Implementar estructura base de paquetes (`controllers`, `services`, `repositories`, `config`, `models/dto`)
- [x] Implementar Server, conexión a Postgres, Kafka, y logging en el template
- [x] Implementar API Gateway simple (Spring Cloud Gateway) con rutas `/orders` y `/payments`

## Fase 2: Order Service
- [ ] Crear esquema de BD (tabla `orders`: id, user_id, status, total, created_at)
- [ ] Implementar endpoint POST `/orders` (validar, guardar, publicar evento `OrderCreated`)
- [ ] Implementar endpoint GET `/orders/{id}`
- [ ] Agregar soporte de `Idempotency-Key` (tabla `idempotency_keys`)

## Fase 3: Inventory Service
- [ ] Crear tabla `products` (id, name, stock)
- [ ] Consumir evento `OrderCreated`
- [ ] Implementar reserva de stock con Optimistic Locking (columna `version`)
- [ ] Emitir evento `StockReserved` o `StockFailed`

## Fase 4: Payment Service
- [ ] Crear tabla `payments` (id, order_id, status, amount, created_at)
- [ ] Consumir evento `StockReserved`
- [ ] Simular pago (latencia aleatoria, fallos aleatorios) -> estados APPROVED / REJECTED
- [ ] Emitir evento `PaymentApproved` o `PaymentRejected`
- [ ] Implementar idempotencia de pagos (evitar doble pago para el mismo `order_id`)

## Fase 5: Invoice Service
- [ ] Crear tabla `invoices` (id, order_id, invoice_number, total, issued_at) con constraint `unique(order_id)`
- [ ] Consumir evento `PaymentApproved`
- [ ] Generar número de factura (ej. INV-2026-000001) y guardar

## Fase 6: Notification Service
- [ ] Consumir eventos `InvoiceGenerated` y `PaymentRejected`
- [ ] Simular envío (email, webhook, push) y guardar logs

## Fase 7: Saga / Compensaciones
- [ ] Consumir evento `StockFailed` en Order Service -> actualizar orden a `FAILED`
- [ ] Consumir evento `PaymentRejected` -> Acciones: ReleaseStock y CancelOrder

## Fase 8: Outbox Pattern
- [ ] Modificar servicios para usar tabla `outbox_events`
- [ ] Guardar eventos en la misma transacción de BD (insert entidad + insert outbox_event)
- [ ] Crear worker que lea `outbox_events` y publique en Kafka

## Fase 9: Observabilidad
- [ ] Implementar Correlation ID mediante header `X-Request-ID` en todos los servicios
- [ ] Exponer endpoint `/metrics` para Prometheus
- [ ] Agregar Distributed Tracing (OpenTelemetry + Jaeger)

## Fase 10: Test de Concurrencia
- [ ] Crear script de carga con `k6`
- [ ] Ejecutar escenario de 1000 orders/min
- [ ] Medir latencia, errores y throughput
