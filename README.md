# Recipe and Meal Planning (P20)

[![CI Build](https://github.com/JavierLianoRioz/recipe-meal-planning/actions/workflows/ci.yml/badge.svg)](https://github.com/JavierLianoRioz/recipe-meal-planning/actions)

> **Asignatura:** Rapid Development of Java Applications Using Programming Frameworks  
> **Institución:** Algebra Bernays University — Academic Year 2026/27  
> **Estudiante:** Javier Liaño Rioz  
> **Proyecto:** P20 — Recipe and Meal Planning  

---

## Índice

1. [Descripción General](#1-descripción-general)
2. [Despliegue y Acceso Público](#2-despliegue-y-acceso-público)
3. [Credenciales Demo por Rol](#3-credenciales-demo-por-rol)
4. [Stack Tecnológico y Decisiones de Arquitectura](#4-stack-tecnológico-y-decisiones-de-arquitectura)
5. [Instrucciones de Ejecución Local](#5-instrucciones-de-ejecución-local)
   - [Requisitos Previos](#51-requisitos-previos)
   - [Ejecución con Docker Compose](#52-ejecución-con-docker-compose)
   - [Ejecución con Maven Wrapper](#53-ejecución-con-maven-wrapper)
6. [Mapeo de Prototipo Figma a Vistas Thymeleaf](#6-mapeo-de-prototipo-figma-a-vistas-thymeleaf)
7. [Integración de Inteligencia Artificial (LLM)](#7-integración-de-inteligencia-artificial-llm)
8. [Limitaciones Actuales y Siguientes Pasos](#8-limitaciones-actuales-y-siguientes-pasos)

---

## 1. Descripción General

> [!NOTE] ¿Qué debe contener esta sección?
> Explica de forma concisa qué problema resuelve la aplicación en el dominio culinario y de planificación nutricional.
> - Detalla el modelo de agregados principal: `Recipe` $\rightarrow$ `RecipeIngredient` $\rightarrow$ `Ingredient` y `MealPlan` $\rightarrow$ `PlannedMeal` $\rightarrow$ `Recipe`.
> - Menciona los tres requisitos avanzados específicos:
>   1. Escalado de raciones y conversión bidireccional masa/volumen.
>   2. Agregación nutricional acumulada en el plan semanal.
>   3. Generación y consolidación de la lista de la compra deduplicada.

<!-- TODO: Rellenar la descripción del producto, el flujo de usuario y el valor diferencial. -->

---

## 2. Despliegue y Acceso Público

> [!NOTE] ¿Qué debe contener esta sección?
> Es obligatorio según la especificación del curso para la defensa final.
> - **URL pública en producción:** Enlace HTTPS accesible por internet (ej. Render/Koyeb).
> - **Base de datos gestionada:** Proveedor en la nube utilizado (ej. Neon PostgreSQL).
> - **Comportamiento Cold-Start:** Advertencia honesta sobre el tiempo de arranque en frío si el contenedor gratuito entra en reposo (ej. ~50-60s tras 15 min de inactividad).

- **URL Pública:** `https://<tu-app-en-render>.onrender.com`
- **Documentación OpenAPI (Swagger):** `https://<tu-app-en-render>.onrender.com/swagger-ui.html`
- **Actuator Health:** `https://<tu-app-en-render>.onrender.com/actuator/health`

---

## 3. Credenciales Demo por Rol

> [!NOTE] ¿Qué debe contener esta sección?
> La especificación exige al menos 3 roles con capacidades operativas claramente diferenciadas (ej. `ROLE_ADMIN`, `ROLE_PLANNER`, `ROLE_VIEWER`).
> Proporciona usuarios y contraseñas de demostración sembrados automáticamente por Flyway en la base de datos de producción para que el examinador pueda iniciar sesión.

| Rol | Usuario | Contraseña | Capacidades y Permisos |
| :--- | :--- | :--- | :--- |
| `ROLE_ADMIN` | `admin` | *(definir contraseña demo)* | Gestión global de ingredientes del catálogo, auditoría y tareas programadas. |
| `ROLE_PLANNER` | `planner` | *(definir contraseña demo)* | Creación de recetas, planificación semanal de comidas y normalización con IA. |
| `ROLE_VIEWER` | `viewer` | *(definir contraseña demo)* | Solo lectura de recetas públicas y listas generadas. |

---

## 4. Stack Tecnológico y Decisiones de Arquitectura

> [!NOTE] ¿Qué debe contener esta sección?
> Resume las decisiones tecnológicas y enlaza los Architecture Decision Records (ADRs) ubicados en `docs/adr/`.
> - **Java 25** & **Spring Boot 4.1**.
> - **Spring Security 7** (BCrypt, CSRF, `@PreAuthorize`).
> - **Spring Data JPA** sobre **Hibernate 7** con PostgreSQL y Flyway.
> - **Thymeleaf** con Layout Dialect y Security Dialect.
> - **Springdoc OpenAPI 3.x** y RFC 7807 (`ProblemDetail`).
> - Referencia a los 3 ADRs:
>   - `docs/adr/001-persistence-association-entity.md` (Por qué `RecipeIngredient` lleva atributos).
>   - `docs/adr/002-ai-structured-ingredient-normalizer.md` (Integración LLM / Jev / Spring AI).
>   - `docs/adr/003-deployment-render-neon.md` (Infraestructura y variables de entorno).

<!-- TODO: Completar justificación del stack y enlaces a los ADRs -->

---

## 5. Instrucciones de Ejecución Local

### 5.1 Requisitos Previos

> [!NOTE] ¿Qué debe contener esta sección?
> Lista el software necesario para compilar y ejecutar en local (JDK 25, Docker Desktop o Podman).

- Java Development Kit (JDK) 25 o superior.
- Docker & Docker Compose (para base de datos PostgreSQL local).

### 5.2 Ejecución con Docker Compose

> [!NOTE] ¿Qué debe contener esta sección?
> El comando único que levanta la base de datos y la aplicación conjuntamente.

```bash
docker compose up -d
```

### 5.3 Ejecución con Maven Wrapper

> [!NOTE] ¿Qué debe contener esta sección?
> Comandos para compilar, pasar la suite de tests (con cobertura JaCoCo) y arrancar en perfil local.

```bash
# Compilar y ejecutar tests unitarios e integrados
./mvnw clean verify

# Arrancar la aplicación con perfil de desarrollo
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 6. Mapeo de Prototipo Figma a Vistas Thymeleaf

> [!NOTE] ¿Qué debe contener esta sección?
> Requisito obligatorio de la sección 4 de la especificación:
> - Enlace público del archivo Figma (modo solo lectura para cualquier usuario con el enlace).
> - Referencia al PDF exportado en `docs/prototype.pdf`.
> - Tabla exhaustiva relacionando cada frame de Figma con su plantilla `.html` correspondiente y la justificación de cualquier desviación de diseño producida durante el desarrollo.

- **Enlace al Figma:** [Figma Project Link](https://www.figma.com/...)
- **Exportación Offline:** `docs/prototype.pdf`

| Frame en Figma (Desktop 1440 / Mobile 390) | Plantilla Thymeleaf (`src/main/resources/templates/`) | Estado / Desviaciones Justificadas |
| :--- | :--- | :--- |
| `01-recipe-list` | `recipes/list.html` | Coincidencia exacta. |
| `02-recipe-empty` | `recipes/list.html` (bloque `th:if="${#lists.isEmpty(recipes)}"`) | Coincidencia exacta. |
| `03-recipe-form` | `recipes/form.html` | Coincidencia exacta. |
| `04-recipe-form-errors` | `recipes/form.html` (bloque `th:errors`) | Coincidencia exacta con mensajes de validación i18n. |
| `05-recipe-detail` | `recipes/detail.html` | Coincidencia exacta con escalador de raciones. |
| `06-ai-ingredient-parser` | `recipes/ai-normalize.html` | Coincidencia exacta con panel interactivo. |

---

## 7. Integración de Inteligencia Artificial (LLM)

> [!NOTE] ¿Qué debe contener esta sección?
> Describe cómo opera la funcionalidad de normalización de ingredientes con IA.
> - Explica la arquitectura Hexagonal (puerto `IngredientNormalizer` y adaptadores: implementación real vs. `StubIngredientNormalizer` para tests sin coste).
> - Cita los documentos `docs/ai-usage.md` (registro de desarrollo) y `docs/ai-evaluation.md` (los 10 casos de evaluación empírica con entradas en croata e inglés).

<!-- TODO: Detallar prompt, estructura JSON devuelta y política de contingencia ante fallos del modelo -->

---

## 8. Limitaciones Actuales y Siguientes Pasos

> [!NOTE] ¿Qué debe contener esta sección?
> Demuestra madurez ingenieril ante el examinador indicando qué aspectos quedaron fuera del alcance de la entrega o qué optimizaciones futuras están planificadas (ej. soporte para dietas personalizadas, sincronización con proveedores de supermercados o cálculo de huella de carbono).

<!-- TODO: Enumerar limitaciones conocidas y deuda técnica asumida conscientemente -->
