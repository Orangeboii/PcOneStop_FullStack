# 📋 Resumen de Modificaciones para el Frontend

Este documento es un resumen rápido de todas las modificaciones que el frontend debe implementar para funcionar correctamente con el backend actualizado.

---

## 🎯 Modificaciones Principales

### 1. ✅ Validación de Sesión (NUEVO)

**Endpoint:** `GET /api/v1/auth/validate`

**¿Qué hacer?**
- Crear función que valide el token al cargar la aplicación
- Restaurar sesión automáticamente si el token es válido
- Redirigir a login si el token expirado

**Código mínimo:**
```javascript
export async function validateSession() {
  const token = localStorage.getItem('token');
  if (!token) return null;
  
  const response = await fetch('http://localhost:8081/api/v1/auth/validate', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const result = await response.json();
  return result.ok ? result.data : null;
}
```

---

### 2. ✅ Obtener Todos los Productos (PC Builder) - CORREGIDO

**Endpoint:** `GET /api/v1/products`

**Cambio importante:** Ahora cualquier usuario autenticado puede ver todos los productos (antes solo ADMIN).

**¿Qué hacer?**
- Crear función `getAllProducts()` con token JWT
- Usar en el PC Builder para mostrar todos los componentes
- Implementar filtros por categoría

**Código mínimo:**
```javascript
export async function getAllProducts() {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8082/api/v1/products', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const result = await response.json();
  return result.data || [];
}
```

---

### 3. ✅ Validación de Stock

**¿Qué hacer?**
- Verificar campo `stock` en respuestas de productos
- Mostrar badge "Agotado" cuando `stock === 0`
- Deshabilitar botón de compra cuando `stock === 0`
- Manejar errores del backend cuando se intenta comprar producto agotado

**Código mínimo:**
```jsx
const isOutOfStock = product.stock === 0;

{isOutOfStock ? (
  <button disabled>Producto Agotado</button>
) : (
  <button onClick={handleAddToCart}>Agregar al Carrito</button>
)}
```

---

### 4. ✅ Pedidos con Cantidades (NUEVO)

**Endpoint:** `POST /api/v1/orders`

**Cambio importante:** Ahora se debe enviar `items` con `quantity` en lugar de solo `productIds`.

**Formato antiguo (ya no recomendado):**
```json
{
  "totalAmount": 15999.99,
  "productIds": "1,5,8"
}
```

**Formato nuevo (RECOMENDADO):**
```json
{
  "totalAmount": 2599980.00,
  "items": [
    {"productId": 1, "quantity": 2},
    {"productId": 5, "quantity": 3}
  ]
}
```

**Código mínimo:**
```javascript
export async function createOrderWithQuantities(cartItems, totalAmount) {
  const items = cartItems.map(item => ({
    productId: item.id,
    quantity: item.quantity
  }));
  
  const response = await fetch('http://localhost:8083/api/v1/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      totalAmount: totalAmount,
      items: items
    })
  });
  
  const result = await response.json();
  return result.data;
}
```

---

### 5. ✅ Actualizar Productos (Admin) - NUEVO

**Endpoint:** `PUT /api/v1/products/{id}`

**¿Qué hacer?**
- Crear función `updateProduct()` para actualizar productos
- Implementar formulario de edición
- Permitir poner/quitar productos en oferta

**Código mínimo:**
```javascript
export async function updateProduct(productId, productData) {
  const token = localStorage.getItem('token');
  const response = await fetch(`http://localhost:8082/api/v1/products/${productId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(productData)
  });
  
  const result = await response.json();
  return result.data;
}

// Poner producto en oferta
const productData = {
  isOnSale: true,
  discount: 20,
  offerStartDate: "2024-01-01",
  offerEndDate: "2024-12-31"
};
await updateProduct(productId, productData);
```

---

## 📝 Checklist Rápido

### Funcionalidades Básicas
- [ ] Validar sesión al cargar la app (`GET /api/v1/auth/validate`)
- [ ] Obtener todos los productos para PC Builder (`GET /api/v1/products`)
- [ ] Mostrar estado de stock (agotado/disponible)
- [ ] Deshabilitar botones cuando `stock === 0`

### Carrito y Pedidos
- [ ] Implementar selector de cantidad en carrito
- [ ] Enviar pedidos con formato `items` y `quantity`
- [ ] Manejar errores de stock insuficiente

### Admin
- [ ] Implementar actualización de productos (`PUT /api/v1/products/{id}`)
- [ ] Permitir poner/quitar productos en oferta
- [ ] Validar campos antes de enviar

### Manejo de Errores
- [ ] Manejar error 400 (producto agotado, stock insuficiente)
- [ ] Manejar error 401 (token expirado) → Redirigir a login
- [ ] Manejar error 403 (sin permisos)
- [ ] Mostrar mensajes claros al usuario

---

## 🔗 URLs de los Servicios

| Servicio | Puerto | URL Base |
|----------|--------|----------|
| Usuarios (Auth) | 8081 | `http://localhost:8081/api/v1` |
| Productos | 8082 | `http://localhost:8082/api/v1` |
| Pedidos | 8083 | `http://localhost:8083/api/v1` |
| Calificaciones | 8084 | `http://localhost:8084/api/v1` |

---

## 📚 Documentación Completa

Para ejemplos de código completos y detallados, consulta:
- **`GUIA_IMPLEMENTACION_FRONTEND.md`** - Guía completa con todos los ejemplos de código

---

## 🚀 Cambios Críticos que DEBES Implementar

### 1. Cambiar formato de pedidos
**ANTES:**
```javascript
productIds: "1,5,8"  // Cantidad fija: 1 por producto
```

**AHORA:**
```javascript
items: [
  {productId: 1, quantity: 2},
  {productId: 5, quantity: 3}
]  // Cantidad variable
```

### 2. Agregar validación de sesión
```javascript
// Al cargar la app
useEffect(() => {
  validateSession().then(user => {
    if (user) setUser(user);
  });
}, []);
```

### 3. Obtener productos con autenticación
```javascript
// PC Builder necesita token JWT
const products = await getAllProducts(); // Requiere autenticación
```

### 4. Mostrar estado de stock
```jsx
{product.stock === 0 && <span>Agotado</span>}
{product.stock > 0 && product.stock <= 5 && <span>Últimas {product.stock} unidades</span>}
```

---

## ⚠️ Errores Comunes a Evitar

1. ❌ **No enviar token JWT** → Error 401
   - ✅ Siempre incluir `Authorization: Bearer ${token}`

2. ❌ **Usar formato antiguo de pedidos** → Funciona pero cantidad fija a 1
   - ✅ Usar formato nuevo con `items` y `quantity`

3. ❌ **No validar stock antes de mostrar** → Mala UX
   - ✅ Verificar `stock === 0` y deshabilitar botones

4. ❌ **No manejar error 401** → Usuario no sabe qué pasó
   - ✅ Redirigir a login cuando token expirado

---

## 🎯 Prioridades de Implementación

### Alta Prioridad (Crítico)
1. ✅ Cambiar formato de pedidos a `items` con `quantity`
2. ✅ Validar sesión al cargar la app
3. ✅ Obtener productos con autenticación para PC Builder

### Media Prioridad (Importante)
4. ✅ Mostrar estado de stock
5. ✅ Manejar errores de stock insuficiente
6. ✅ Implementar actualización de productos (Admin)

### Baja Prioridad (Mejoras UX)
7. ✅ Filtros por categoría en PC Builder
8. ✅ Búsqueda de productos
9. ✅ Badges de "Últimas unidades"

---

## 📞 Soporte

Si tienes dudas sobre alguna implementación, consulta:
- `GUIA_IMPLEMENTACION_FRONTEND.md` - Ejemplos completos de código
- `FORMATO_PEDIDOS_CON_CANTIDADES.md` - Detalles de pedidos con cantidades
- `GUIA_FRONTEND_VALIDACION_STOCK.md` - Detalles de validación de stock

---

**¡El backend está listo! Solo necesitas implementar estos cambios en el frontend.** 🚀
