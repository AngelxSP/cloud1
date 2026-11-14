# Ferretería Progresol Charito — Sistema de Ventas y Almacén

Sistema web full‑stack para una ferretería: punto de venta, inventario, caja, abastecimiento,
devoluciones, reportes y administración de usuarios.

- **Backend:** Spring Boot 3.3.5 · Java 17 · Spring Data JPA · Spring Security + JWT · MySQL
- **Frontend:** Angular 21 (standalone components) · ngx-echarts (c-charts)
- **Sin dependencias extra:** solo se usan las indicadas (Security, JPA, JWT, ECharts).

---

## 1. Requisitos

| Herramienta | Versión |
|-------------|---------|
| JDK         | 17 (obligatorio, el backend ya no compila con Java 11) |
| Maven       | 3.9+ (o el wrapper `./mvnw`) |
| Node.js     | 20+ |
| Angular CLI | 21 (`npm i -g @angular/cli`) |
| MySQL       | 8.x |

## 2. Base de datos

```sql
-- En MySQL, ejecuta el script completo:
SOURCE fase2Ferreteria.sql;
```

El script crea la base `tienda_db` con todas las tablas, incluyendo las nuevas:
`caja`, `movimientos_caja`, `devoluciones`, `detalle_devolucion`.

> Las credenciales de prueba quedan con la contraseña **admin123** (hash BCrypt incluido al final del script).

Ajusta usuario/clave de MySQL en `BACKEND/src/main/resources/application.properties`
(o vía variables de entorno `DB_URL`, `DB_USER`, `DB_PASS`).

## 3. Backend

```bash
cd BACKEND
./mvnw spring-boot:run        # o: mvn spring-boot:run
# API en http://localhost:8080
```

## 4. Frontend

```bash
cd FRONTEND
npm install
ng serve
# App en http://localhost:4200
```

## 5. Credenciales de prueba

| Rol           | Email                  | Contraseña |
|---------------|------------------------|------------|
| Administrador | admin@tienda.com       | admin123   |
| Recepcionista | recepcion@tienda.com   | admin123   |
| Almacenero    | almacen@tienda.com     | admin123   |

---

## 6. Módulos

| Menú | Ruta | Estado |
|------|------|--------|
| Dashboard | `/dashboard` | Métricas + gráficos (ECharts) |
| Punto de Venta | `/punto-de-venta` | Carrito, IGV 18%, registra venta y descuenta stock |
| Historial de Ventas | `/historial-ventas` | Lista y detalle |
| Devoluciones | `/devoluciones` | Notas de crédito, reingresa stock |
| Caja | `/caja` | Apertura/cierre, movimientos, historial |
| Productos | `/productos` | CRUD |
| Categorías | `/categorias` | CRUD |
| Stock | `/stock` | Stock actual + movimientos + ajuste |
| Compras | `/compras` | CRUD, suma stock al recibir |
| Proveedores | `/proveedores` | CRUD |
| Clientes | `/clientes` | CRUD |
| Reportes | `/reportes` | Libro Mayor y Auditoría → Excel (CSV) y PDF (impresión) |
| Usuarios | `/usuarios` | CRUD con roles y BCrypt |

## 7. Cambios clave realizados

- **Build:** `pom.xml` corregido (artefactos válidos `spring-boot-starter-web` / `-test`; Spring Boot
  4.0.6 (preview) → **3.3.5 LTS estable** para compilar de forma confiable con Java 17).
- **Seguridad:** `DaoAuthenticationProvider` compatible con Spring Security 6.3; `passwordHash`
  ya **no** se expone en las respuestas JSON; usuarios nuevos se guardan con BCrypt.
- **Layout único:** un solo `LayoutComponent` con el sidebar (antes cada página duplicaba el suyo).
- **Backend nuevo:** módulos Caja, Devoluciones y Reportes (entidades, repos, services, controllers).
- **Ventas:** autogeneran número de comprobante y, si hay caja abierta, suman a su total.

## 8. Dónde integrar IA (pendiente, recomendado)

Tres apartados, de más fácil a más ambicioso:

1. **Reportes → resumen ejecutivo automático (lo más fácil).** Ya tienes el endpoint
   `/api/reportes/libro-mayor` devolviendo JSON. Un botón "Análisis IA" envía esos totales a un
   modelo y devuelve un párrafo en lenguaje natural ("las ventas subieron 12%…, revisa el stock de…").
   No toca la base de datos ni la lógica existente.
2. **Inventario → predicción de reposición.** Con los movimientos de `inventario` (ya registrados)
   se puede estimar cuándo cada producto llegará a su stock mínimo y sugerir cantidades de compra.
3. **Punto de Venta → buscador inteligente / recomendaciones.** Sugerir productos relacionados al
   armar el carrito a partir del historial de `detalle_venta`.

**Recomendación:** empezar por el **(1) en Reportes** — el endpoint ya entrega datos limpios, es un
único punto de integración y el impacto visual es alto sin riesgo para el resto del sistema.
