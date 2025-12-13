# 🔗 URLs de Swagger UI - PC OneStop (Actualizado)

## 🚀 Acceso Rápido a Swagger

Copia y pega estas URLs en tu navegador:

```
🔵 Usuarios:        http://localhost:8081/swagger-ui.html
🟢 Inventario:      http://localhost:8082/swagger-ui.html
🟡 Pagos:           http://localhost:8083/swagger-ui.html
🟣 Calificaciones:  http://localhost:8084/swagger-ui.html
```

---

## 📋 URLs Completas por Microservicio

### 1. 🔵 Microservicio Usuarios (Autenticación)
- **Puerto:** `8081`
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs (JSON):** http://localhost:8081/v3/api-docs
- **Descripción:** Gestión de usuarios, registro, login y validación de sesión

### 2. 🟢 Microservicio Inventario (Productos)
- **Puerto:** `8082`
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **API Docs (JSON):** http://localhost:8082/v3/api-docs
- **Descripción:** Gestión de productos del catálogo, stock y ofertas

### 3. 🟡 Microservicio Pagos (Pedidos)
- **Puerto:** `8083`
- **Swagger UI:** http://localhost:8083/swagger-ui.html
- **API Docs (JSON):** http://localhost:8083/v3/api-docs
- **Descripción:** Gestión de pedidos y órdenes con validación de stock

### 4. 🟣 Microservicio Calificaciones (Reseñas)
- **Puerto:** `8084`
- **Swagger UI:** http://localhost:8084/swagger-ui.html
- **API Docs (JSON):** http://localhost:8084/v3/api-docs
- **Descripción:** Gestión de reseñas y calificaciones de productos

---

## 🆕 Endpoints Nuevos/Actualizados

### ✅ Usuarios - Nuevo Endpoint

**`GET /api/v1/auth/validate`**
- **Descripción:** Valida token JWT y restaura sesión del usuario
- **Autenticación:** Requerida (token en header `Authorization: Bearer <token>`)
- **Respuestas:**
  - `200`: Token válido, retorna datos del usuario
  - `401`: Token inválido o expirado
  - `403`: Token no proporcionado

### ✅ Inventario - Cambios

**`GET /api/v1/products`**
- **Cambio:** Ahora disponible para cualquier usuario autenticado (antes solo ADMIN)
- **Uso:** Permite que el PC Builder muestre todos los componentes disponibles

**`PUT /api/v1/products/{id}`**
- **Nuevo:** Endpoint completo para actualizar productos
- **Incluye:** Actualización de campos de oferta (isOnSale, discount, fechas)

### ✅ Pagos - Cambios

**`POST /api/v1/orders`**
- **Nuevo formato:** Soporta `items` con `quantity` para cantidades variables
- **Validación:** Valida stock automáticamente antes de crear pedido
- **Formato recomendado:**
  ```json
  {
    "totalAmount": 2599980.00,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 5, "quantity": 3}
    ]
  }
  ```

---

## 🔐 Cómo Usar Swagger con Autenticación JWT

### Paso 1: Obtener Token JWT

1. Abre Swagger de **Usuarios**: http://localhost:8081/swagger-ui.html
2. Busca el endpoint `POST /api/v1/auth/login`
3. Haz clic en "Try it out"
4. Ingresa las credenciales (usuarios precargados):
   ```json
   {
     "email": "admin@pconestop.com",
     "password": "admin123"
   }
   ```
   O para cliente:
   ```json
   {
     "email": "cliente@pconestop.com",
     "password": "cliente123"
   }
   ```
5. Ejecuta el request (Execute)
6. Copia el `token` de la respuesta

### Paso 2: Autenticarse en Swagger

1. En cualquier microservicio (Inventario, Pagos), busca el botón **"Authorize"** 🔓 (arriba a la derecha)
2. Haz clic en "Authorize"
3. En el campo "Value", pega el token JWT (sin "Bearer ")
4. Haz clic en "Authorize"
5. Cierra el diálogo
6. Ahora todos los endpoints protegidos estarán autenticados ✅

### Paso 3: Probar Endpoints Protegidos

- Los endpoints que requieren autenticación mostrarán un candado 🔒
- Puedes probarlos directamente desde Swagger
- El token se enviará automáticamente en el header `Authorization: Bearer <token>`

---

## 📝 Endpoints Principales Actualizados

### 🔵 Usuarios (Puerto 8081)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/auth/register` | POST | Registrar nuevo usuario | ❌ Público |
| `/api/v1/auth/login` | POST | Iniciar sesión | ❌ Público |
| `/api/v1/auth/validate` | GET | **NUEVO** - Validar token y restaurar sesión | ✅ Requerida |
| `/api/v1/auth` | GET | Listar todos los usuarios | ✅ Requerida |
| `/api/v1/auth/{id}` | GET | Obtener usuario por ID | ✅ Requerida |
| `/api/v1/auth/{id}` | PUT | Actualizar usuario | ✅ Requerida |
| `/api/v1/auth/{id}/password` | PUT | Actualizar contraseña | ✅ Requerida |
| `/api/v1/auth/{id}` | DELETE | Eliminar usuario | ✅ Requerida |

**Swagger:** http://localhost:8081/swagger-ui.html

---

### 🟢 Inventario (Puerto 8082)

| Endpoint | Método | Descripción | Autenticación | Rol Requerido |
|----------|--------|-------------|---------------|---------------|
| `/api/v1/products` | GET | Listar todos los productos (PC Builder) | ✅ | **Cualquier usuario autenticado** |
| `/api/v1/products/{id}` | GET | Obtener producto por ID | ❌ | Público |
| `/api/v1/products/offers` | GET | Productos en oferta | ❌ | Público |
| `/api/v1/products` | POST | Crear nuevo producto | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}` | PUT | **ACTUALIZADO** - Actualizar producto (incluye ofertas) | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}` | DELETE | Eliminar producto | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}/stock` | PUT | Reducir stock | ✅ | Requerida |

**Swagger:** http://localhost:8082/swagger-ui.html

**Nota importante:** `GET /api/v1/products` ahora está disponible para cualquier usuario autenticado (no solo ADMIN) para permitir el uso del PC Builder.

---

### 🟡 Pagos (Puerto 8083)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/orders` | GET | Listar todos los pedidos | ✅ Requerida |
| `/api/v1/orders` | POST | **ACTUALIZADO** - Crear pedido con validación de stock | ✅ Requerida |
| `/api/v1/orders/{id}` | GET | Obtener pedido por ID | ✅ Requerida |
| `/api/v1/orders/user/{userId}` | GET | Pedidos de un usuario | ✅ Requerida |
| `/api/v1/orders/{id}/status` | PUT | Actualizar estado | ✅ Requerida |

**Swagger:** http://localhost:8083/swagger-ui.html

**Formato de pedido actualizado:**

**Formato antiguo (compatible):**
```json
{
  "totalAmount": 15999.99,
  "productIds": "1,5,8"
}
```

**Formato nuevo (recomendado):**
```json
{
  "totalAmount": 2599980.00,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 5, "quantity": 3}
  ]
}
```

---

### 🟣 Calificaciones (Puerto 8084)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/reviews` | GET | Listar todas las reseñas | ❌ Público |
| `/api/v1/reviews` | POST | Crear nueva reseña | ❌ Público |
| `/api/v1/reviews/product/{productId}` | GET | Reseñas de un producto | ❌ Público |
| `/api/v1/products/{productId}/reviews` | POST | Crear reseña para producto | ❌ Público |

**Swagger:** http://localhost:8084/swagger-ui.html

---

## 🎯 Ejemplos de Uso en Swagger

### Ejemplo 1: Validar Sesión (Nuevo)

1. **Obtener Token:**
   - Ve a http://localhost:8081/swagger-ui.html
   - Usa `POST /api/v1/auth/login`
   - Copia el token

2. **Validar Sesión:**
   - En el mismo Swagger, busca `GET /api/v1/auth/validate`
   - Haz clic en "Authorize" y pega el token
   - Ejecuta el request
   - Deberías recibir los datos del usuario (200 OK)

### Ejemplo 2: Actualizar Producto con Oferta (Nuevo)

1. **Obtener Token de ADMIN:**
   - Login con `admin@pconestop.com` / `admin123`

2. **Autenticarse en Inventario:**
   - Ve a http://localhost:8082/swagger-ui.html
   - Haz clic en "Authorize" y pega el token

3. **Actualizar Producto:**
   - Busca `PUT /api/v1/products/{id}`
   - Ingresa el ID del producto
   - En el body, envía:
     ```json
     {
       "isOnSale": true,
       "discount": 20,
       "offerStartDate": "2024-01-01",
       "offerEndDate": "2024-12-31"
     }
     ```
   - Ejecuta el request

### Ejemplo 3: Crear Pedido con Cantidades (Actualizado)

1. **Obtener Token:**
   - Login en Usuarios

2. **Autenticarse en Pagos:**
   - Ve a http://localhost:8083/swagger-ui.html
   - Haz clic en "Authorize" y pega el token

3. **Crear Pedido:**
   - Busca `POST /api/v1/orders`
   - En el body, envía:
     ```json
     {
       "totalAmount": 2599980.00,
       "items": [
         {"productId": 1, "quantity": 2},
         {"productId": 5, "quantity": 3}
       ]
     }
     ```
   - Ejecuta el request
   - El backend validará el stock automáticamente

---

## ⚠️ Notas Importantes

1. **Todos los microservicios deben estar ejecutándose** para acceder a Swagger
2. **El token JWT expira después de 24 horas** (configurable)
3. **El botón "Authorize" solo aparece** si hay configuración de seguridad en OpenAPI
4. **Los endpoints públicos** no requieren token (ej: `GET /api/v1/products/{id}`)
5. **Los endpoints protegidos** muestran un candado 🔒 en Swagger
6. **Usuarios precargados:**
   - Admin: `admin@pconestop.com` / `admin123`
   - Cliente: `cliente@pconestop.com` / `cliente123`

---

## 📋 Checklist para Probar en Swagger

- [ ] Acceder a Swagger de Usuarios
- [ ] Hacer login y obtener token
- [ ] Probar `GET /api/v1/auth/validate` (nuevo)
- [ ] Acceder a Swagger de Inventario
- [ ] Hacer clic en "Authorize" y pegar token
- [ ] Probar `GET /api/v1/products` (ahora disponible para usuarios autenticados)
- [ ] Probar `PUT /api/v1/products/{id}` para actualizar producto con oferta
- [ ] Acceder a Swagger de Pagos
- [ ] Probar `POST /api/v1/orders` con formato nuevo (items con quantity)
- [ ] Verificar que funciona con token válido
- [ ] Verificar que rechaza sin token (401/403)

---

## 🚀 URLs de Acceso Directo

Copia y pega en tu navegador:

```
🔵 Usuarios:        http://localhost:8081/swagger-ui.html
🟢 Inventario:      http://localhost:8082/swagger-ui.html
🟡 Pagos:           http://localhost:8083/swagger-ui.html
🟣 Calificaciones:  http://localhost:8084/swagger-ui.html
```

---

¡Listo para probar! 🎉

