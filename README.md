# 🏦 Devsu Banking App

Aplicación bancaria fullstack desarrollada como prueba técnica. Permite gestionar clientes, cuentas y movimientos bancarios, con generación de reportes en PDF.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura](#arquitectura)
- [Patrones de Diseño](#patrones-de-diseño)
- [Funcionalidades](#funcionalidades)
- [Cómo correr con Docker](#cómo-correr-con-docker)
- [Endpoints de la API](#endpoints-de-la-api)
- [Tests](#tests)

---

## Descripción General

Sistema bancario que expone una API REST (Spring Boot) consumida por una SPA (Angular 21). Soporta operaciones CRUD sobre clientes, cuentas corrientes/de ahorro y movimientos. El módulo de reportes genera un PDF con el historial de movimientos filtrado por rango de fechas y cliente.

```
┌──────────────┐       HTTP/REST       ┌───────────────────┐
│   Angular 21 │ ─────────────────────▶│  Spring Boot API  │
│   (Nginx)    │◀───────────────────── │   (Puerto 8080)   │
└──────────────┘        JSON           └────────┬──────────┘
   Puerto 4200                                  │ JPA/Hibernate
                                       ┌────────▼──────────┐
                                       │   PostgreSQL 16   │
                                       │  (Puerto 5432)    │
                                       └───────────────────┘
```

---

## Stack Tecnológico

### Backend

| Tecnología        | Versión | Uso                                 |
| ----------------- | ------- | ----------------------------------- |
| Java              | 17      | Lenguaje principal                  |
| Spring Boot       | 3.2.4   | Framework web y DI                  |
| Spring Data JPA   | 3.2.4   | Acceso a datos ORM                  |
| PostgreSQL Driver | —       | Conector base de datos              |
| iText 7           | 7.x     | Generación de reportes PDF          |
| Lombok            | 1.18.38 | Reducción de boilerplate            |
| JUnit 5 + Mockito | —       | Testing unitario e integración      |
| H2                | —       | Base de datos en memoria para tests |

### Frontend

| Tecnología      | Versión | Uso                          |
| --------------- | ------- | ---------------------------- |
| Angular         | 21.0.0  | Framework SPA                |
| TypeScript      | 5.9     | Lenguaje principal           |
| Angular Signals | —       | Gestión de estado reactivo   |
| RxJS            | 7.8.0   | Programación reactiva / HTTP |
| Jest            | 30.0.0  | Testing unitario             |

### Infraestructura

| Tecnología     | Uso                            |
| -------------- | ------------------------------ |
| Docker         | Contenedores de cada servicio  |
| Docker Compose | Orquestación multi-servicio    |
| Nginx          | Servidor estático para Angular |
| PostgreSQL 16  | Base de datos relacional       |

---

## Arquitectura

El backend sigue una arquitectura en capas clásica con separación clara de responsabilidades:

```
┌─────────────────────────────────────────────┐
│               Controllers (REST)            │  ← Reciben peticiones HTTP, validan entrada
├─────────────────────────────────────────────┤
│               Services (Business Logic)     │  ← Reglas de negocio (límites, balances)
├─────────────────────────────────────────────┤
│               Repositories (Data Access)    │  ← Queries JPA / JPQL personalizadas
├─────────────────────────────────────────────┤
│               Entities / DTOs               │  ← Modelo de dominio y transferencia
├─────────────────────────────────────────────┤
│               PostgreSQL                    │  ← Persistencia
└─────────────────────────────────────────────┘
```

### Herencia de Entidades

Se utiliza herencia `JOINED` en JPA:

```
Persona (tabla: personas)
└── Cliente (tabla: clientes)  ← extiende Persona con contraseña y estado
```

---

## Patrones de Diseño

### Backend

#### 1. Layered Architecture (Arquitectura en Capas)

Todo el backend está organizado en capas — Controller → Service → Repository — donde cada capa solo conoce a la inmediatamente inferior. Esto garantiza bajo acoplamiento y facilita el testing aislado.

#### 2. Repository Pattern

Los repositorios (`ClienteRepository`, `CuentaRepository`, `MovimientoRepository`) encapsulan toda la lógica de acceso a datos. Los servicios no escriben queries directamente; solo invocan métodos del repositorio.

#### 3. DTO (Data Transfer Object)

Se usan DTOs diferenciados (`*Request` / `*Response`) para separar el modelo de dominio interno de la representación pública de la API. Evita exponer entidades JPA directamente y permite controlar qué campos entran y salen.

```
ClienteRequest  ──▶  Service  ──▶  ClienteEntity  ──▶  DB
ClienteResponse ◀──  Service  ◀──  ClienteEntity  ◀──  DB
```

#### 4. Service Layer Pattern

Toda la lógica de negocio vive en la capa de servicio. Ejemplos concretos:

- Cálculo automático del saldo disponible antes de cada movimiento.
- Validación del límite diario de retiros (máximo $1.000 por día).
- Generación y codificación en Base64 del reporte PDF.

#### 5. Global Exception Handler (Chain of Responsibility aplicado a errores)

`GlobalExceptionHandler` centraliza el manejo de excepciones con `@RestControllerAdvice`. Cada excepción de dominio (`SaldoInsuficienteException`, `CupoDiarioExcedidoException`, `RecursoNoEncontradoException`, `DuplicadoException`) se mapea a un código HTTP y un cuerpo de error consistente (`ApiError`).

#### 6. Template Method (iText PDF)

El servicio de reportes sigue una secuencia fija para construir el PDF — inicializar documento → crear cabecera → iterar movimientos → cerrar — delegando el contenido variable a los datos recibidos.

#### 7. Dependency Injection (Spring IoC)

Toda la inyección de dependencias se realiza vía constructor (`@RequiredArgsConstructor` de Lombok), lo que facilita el testing con mocks sin necesidad de reflection.

---

### Frontend

#### 8. Signals-based Reactive State

Cada componente gestiona su propio estado con `signal()` y `computed()` de Angular. No hay estado global ni librerías externas de state management, lo que simplifica el flujo de datos.

```typescript
clientes = signal<Cliente[]>([]);
filtro = signal('');
filtrados = computed(() =>
  this.clientes().filter((c) => c.nombre.includes(this.filtro())),
);
```

#### 9. Service Layer (Frontend)

Los servicios Angular (`ClienteService`, `CuentaService`, etc.) encapsulan todas las llamadas HTTP. Los componentes nunca usan `HttpClient` directamente, solo consumen los métodos del servicio correspondiente.

#### 10. Lazy Loading (Router)

Todas las rutas cargan sus componentes de forma diferida con `loadComponent`, reduciendo el bundle inicial de la aplicación.

---

## Funcionalidades

### Clientes

- Crear, editar, eliminar y listar clientes
- Búsqueda en tiempo real por nombre
- Validación de identificación única

### Cuentas

- Cuentas corrientes y de ahorro
- Saldo inicial configurable
- Asociación a un cliente existente

### Movimientos

- Registro de depósitos y retiros
- Cálculo automático de saldo tras cada movimiento
- Límite diario de retiros: **$1.000**
- Validación de saldo suficiente antes de debitar

### Reportes

- Filtrado por rango de fechas y cliente
- Exportación a PDF generado en el servidor
- PDF entregado codificado en Base64

---

## Cómo correr con Docker

### Prerrequisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y corriendo
- Puertos **4200**, **8080** y **5432** disponibles en tu máquina

### Pasos

**1. Clonar el repositorio**

```bash
git clone https://github.com/blilllly/technical-assessment-java-angular.git
cd BillyAlvearDevsu
```

**2. Levantar todos los servicios**

```bash
docker compose up --build
```

> La primera vez puede tardar varios minutos mientras descarga imágenes base y compila el backend con Maven.

**3. Verificar que los servicios estén corriendo**

```bash
docker compose ps
```

Deberías ver los tres contenedores en estado `running` / `healthy`:

| Contenedor     | Puerto | Estado esperado |
| -------------- | ------ | --------------- |
| devsu-db       | 5432   | healthy         |
| devsu-backend  | 8080   | running         |
| devsu-frontend | 4200   | running         |

**4. Abrir la aplicación**

| Servicio               | URL                        |
| ---------------------- | -------------------------- |
| Frontend (Angular)     | http://localhost:4200      |
| API REST (Spring Boot) | http://localhost:8080      |
| Base de datos          | localhost:5432 / devsubank |

### Detener los servicios

```bash
docker compose down
```

Para detener **y eliminar los volúmenes** (borra los datos de la base de datos):

```bash
docker compose down -v
```

### Variables de entorno (opcionales)

Las siguientes variables pueden sobreescribirse en el `docker-compose.yml` o en un archivo `.env`:

| Variable                     | Valor por defecto                     | Descripción                  |
| ---------------------------- | ------------------------------------- | ---------------------------- |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://db:5432/devsubank` | URL de conexión a PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `devsu`                               | Usuario de base de datos     |
| `SPRING_DATASOURCE_PASSWORD` | `devsu123`                            | Contraseña de base de datos  |

---

## Endpoints de la API

### Clientes — `/clientes`

| Método   | Ruta             | Descripción                     |
| -------- | ---------------- | ------------------------------- |
| `GET`    | `/clientes`      | Listar todos los clientes       |
| `GET`    | `/clientes/{id}` | Obtener cliente por ID          |
| `POST`   | `/clientes`      | Crear cliente                   |
| `PUT`    | `/clientes/{id}` | Actualizar cliente completo     |
| `PATCH`  | `/clientes/{id}` | Actualizar cliente parcialmente |
| `DELETE` | `/clientes/{id}` | Eliminar cliente                |

### Cuentas — `/cuentas`

| Método   | Ruta            | Descripción                    |
| -------- | --------------- | ------------------------------ |
| `GET`    | `/cuentas`      | Listar todas las cuentas       |
| `GET`    | `/cuentas/{id}` | Obtener cuenta por ID          |
| `POST`   | `/cuentas`      | Crear cuenta                   |
| `PUT`    | `/cuentas/{id}` | Actualizar cuenta completa     |
| `PATCH`  | `/cuentas/{id}` | Actualizar cuenta parcialmente |
| `DELETE` | `/cuentas/{id}` | Eliminar cuenta                |

### Movimientos — `/movimientos`

| Método   | Ruta                | Descripción                        |
| -------- | ------------------- | ---------------------------------- |
| `GET`    | `/movimientos`      | Listar todos los movimientos       |
| `GET`    | `/movimientos/{id}` | Obtener movimiento por ID          |
| `POST`   | `/movimientos`      | Registrar movimiento               |
| `PUT`    | `/movimientos/{id}` | Actualizar movimiento              |
| `PATCH`  | `/movimientos/{id}` | Actualizar movimiento parcialmente |
| `DELETE` | `/movimientos/{id}` | Eliminar movimiento                |

### Reportes — `/reportes`

| Método | Ruta        | Parámetros                           | Descripción                              |
| ------ | ----------- | ------------------------------------ | ---------------------------------------- |
| `GET`  | `/reportes` | `fechaInicio`, `fechaFin`, `cliente` | Reporte de movimientos con PDF en Base64 |

---

## Tests

### Backend

```bash
cd backend
./mvnw test
```

Cubre:

- `ClienteControllerTest` — creación, listado, búsqueda por ID, errores de validación
- `MovimientoServiceTest` — retiros, depósitos, saldo insuficiente, límite diario excedido

### Frontend

```bash
cd frontend
npm test
```

Cubre:

- `ClienteService` — listar, crear, actualizar, eliminar (con `HttpTestingController`)

---

## Autor

**Billy Alvear** — Prueba Técnica Devsu
