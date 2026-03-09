# Marketplace Microservices Architecture

Este es un proyecto de comercio electrónico basado en una arquitectura de microservicios usando Spring Boot, Spring Cloud, Kafka, Redis y PostgreSQL.

## 🏗️ Arquitectura

El proyecto consta de los siguientes componentes principales:

### Microservicios
- **API Gateway (`:8080`)**: Punto de entrada único para todos los clientes construido con Spring Cloud Gateway.
- **Inventory Service (`:8081`)**: Gestiona el inventario y stock de productos.
- **Invoice Service (`:8082`)**: Encargado de la facturación.
- **Notification Service (`:8083`)**: Gestión del envío de notificaciones y alertas.
- **Order Service (`:8084`)**: Gestión del ciclo de vida de los pedidos.
- **Payment Service (`:8085`)**: Procesamiento y validación de pagos.

### Infraestructura (Docker)
- **PostgreSQL**: Base de datos relacional general par almacenar el estado de los servicios (`localhost:5432`).
- **Redis**: Base de datos en memoria para caché (`localhost:6379`).
- **Apache Kafka**: Bus de mensajes de eventos asíncronos para la comunicación entre microservicios (`localhost:9092`).
- **Zookeeper**: Servicio necesario para la coordinación del clúster de Kafka (`localhost:2181`).

## 🛠️ Requisitos Previos

- **Java 17** (JDK).
- **Docker y Docker Compose** (para levantar la infraestructura de dependencias).
- **Gradle** (incluido como Gradle Wrapper `gradlew.bat` en cada proyecto).

## 🚀 Cómo ejecutar el proyecto localmente

### 1. Iniciar la Infraestructura

Es fundamental iniciar las dependencias de datos antes de arrancar los servicios de Spring Boot. Asegurate de tener Docker Desktop / Docker Engine corriendo y utilizá el archivo `docker-compose.yml` provisto:

```bash
cd infra
docker-compose up -d
```
> Esto iniciará en segundo plano PostgreSQL, Redis, Zookeeper y Kafka.

### 2. Ejecutar los Microservicios automáticamente (Windows)

En la raíz del proyecto tenés un script preparado que abrirá una ventana de CMD por cada servicio e iniciará su proceso de build y ejecución en paralelo:

```bat
.\run-all.bat
```
*(Para detener todos los servicios basta con cerrar las ventanas emergentes).*

### Alternativa manual:

Si querés levantar un proyecto en particular desde tu terminal:

```bash
cd api-gateway
.\gradlew.bat bootRun
```

*(Repetir lo mismo para los demás servicios en sus respectivos directorios).*

## 📝 Notas de Configuración
- **Base de datos Postgres:**
  - Database: `marketplace`
  - User: `postgres`
  - Password: `postgres`
- Todos los componentes y puertos en desarrollo están pensados para correr sobre `localhost` por default, permitiéndote editar, testear y debuggear el código fuente usando tu entorno local de la forma más rápida y amigable posible.
