@echo off
echo Levantando todos los microservicios...

start "API Gateway (8080)" cmd /k "cd api-gateway && .\gradlew bootRun"
start "Inventory Service (8081)" cmd /k "cd inventory-service && .\gradlew bootRun"
start "Invoice Service (8082)" cmd /k "cd invoice-service && .\gradlew bootRun"
start "Notification Service (8083)" cmd /k "cd notification-service && .\gradlew bootRun"
start "Order Service (8084)" cmd /k "cd order-service && .\gradlew bootRun"
start "Payment Service (8085)" cmd /k "cd payment-service && .\gradlew bootRun"

echo ¡Consolas abiertas! Cerrá cada ventana para detener el servicio correspondiente.
