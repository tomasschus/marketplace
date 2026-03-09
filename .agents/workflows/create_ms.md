---
description: Create a new Spring Boot microservice using Spring Initializr
---
# Create Spring Boot Microservice

This workflow generates a new Spring Boot 4.0.3 microservice using Gradle and Java 25 via Spring Initializr.

## Prerequisites
- The target microservice name
- `curl` installed on the system
- `tar` or `unzip` to extract the generated project

## Steps

1. Configure the `curl` command with the desired microservice name. In the command below, replace `[MS_NAME]` with the actual name of your microservice (e.g., `order-service`).

```bash
curl "https://start.spring.io/starter.zip?type=gradle-project&language=java&bootVersion=4.0.3&baseDir=[MS_NAME]&groupId=com.schusterapps&artifactId=[MS_NAME]&name=[MS_NAME]&description=marketplace%20ms&packageName=com.schusterapps.[MS_NAME]&packaging=jar&javaVersion=21&configurationFileFormat=properties" -o "[MS_NAME].zip"
```

// turbo
2. Extract the downloaded zip file:
```bash
tar -xf [MS_NAME].zip
```

// turbo
3. Clean up the zip file:
```bash
del [MS_NAME].zip
```

4. You can now navigate to the created directory:
```bash
cd [MS_NAME]
```
