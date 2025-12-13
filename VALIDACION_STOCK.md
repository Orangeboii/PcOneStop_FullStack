# 🛒 Validación de Stock - Backend y Frontend

Este documento explica cómo funciona la validación de stock cuando un producto se agota y cómo implementarla en el frontend.

## ✅ Validación en el Backend (Ya Implementada)

### 1. Validación al Crear Pedido

El backend **SIEMPRE valida el stock** antes de crear un pedido. Esto es **obligatorio** porque:
- El backend es la fuente de verdad
- Previene compras de productos agotados
- Funciona incluso si el frontend tiene bugs

**Ubicación**: `Pagos/src/main/java/com/Pedidos/Pagos/service/OrderService.java`

**Cómo funciona**:
1. Cuando se intenta crear un pedido, se valida cada producto
2. Si `stock = 0`: Retorna error **"Producto agotado. No hay unidades disponibles."**
3. Si `stock < cantidad solicitada`: Retorna error **"Stock insuficiente"**
4. Si todo está bien: Crea el pedido y reduce el stock

### 2. Respuestas del Backend

#### Producto Agotado (stock = 0)
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "No se puede completar la compra: uno o más productos están agotados. Producto agotado. No hay unidades disponibles. (Producto: GeForce RTX 4070).",
  "data": null,
  "count": 0
}
```

#### Stock Insuficiente (stock > 0 pero menor a lo solicitado)
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "Stock insuficiente para uno o más productos. Solo hay 2 unidad(es) disponible(s) (solicitaste 5) (Producto: AMD Ryzen 7 7800X3D).",
  "data": null,
  "count": 0
}
```

### 3. Endpoint GET de Producto

El endpoint `GET /api/v1/products/{id}` siempre retorna el campo `stock`:

```json
{
  "ok": true,
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "GeForce RTX 4070",
    "stock": 0,  // ← El frontend debe verificar esto
    ...
  }
}
```

## 🎨 Implementación en el Frontend (Recomendado)

Aunque el backend valida, el frontend debe **mejorar la experiencia del usuario** mostrando/ocultando botones y mensajes.

### 1. Verificar Stock al Cargar Producto

```javascript
// Al obtener un producto
const product = await fetchProduct(productId);

// Verificar si está disponible
if (product.stock === 0) {
  // Ocultar botón de compra
  // Mostrar mensaje "Producto agotado"
  // Deshabilitar selector de cantidad
}
```

### 2. Ocultar/Deshabilitar Botón de Compra

```jsx
// React/Next.js ejemplo
function ProductCard({ product }) {
  const isOutOfStock = product.stock === 0;
  
  return (
    <div>
      <h3>{product.name}</h3>
      <p>Stock: {product.stock}</p>
      
      {isOutOfStock ? (
        <button disabled className="out-of-stock">
          Producto Agotado
        </button>
      ) : (
        <button onClick={handleAddToCart}>
          Agregar al Carrito
        </button>
      )}
    </div>
  );
}
```

### 3. Mostrar Mensaje de Stock

```jsx
function StockIndicator({ stock }) {
  if (stock === 0) {
    return <span className="badge badge-danger">Agotado</span>;
  } else if (stock <= 5) {
    return <span className="badge badge-warning">Últimas {stock} unidades</span>;
  } else {
    return <span className="badge badge-success">En stock</span>;
  }
}
```

### 4. Validar Antes de Enviar al Backend

```javascript
async function createOrder(productIds) {
  // Validar stock antes de enviar (opcional, pero mejora UX)
  for (const productId of productIds) {
    const product = await fetchProduct(productId);
    if (product.stock === 0) {
      alert(`El producto ${product.name} está agotado`);
      return;
    }
  }
  
  // Si todo está bien, enviar al backend
  // El backend validará de nuevo (seguridad)
  try {
    const response = await fetch('/api/v1/orders', {
      method: 'POST',
      body: JSON.stringify({ productIds: productIds.join(',') })
    });
    
    if (!response.ok) {
      const error = await response.json();
      // Mostrar error del backend
      alert(error.message);
    }
  } catch (error) {
    console.error('Error al crear pedido:', error);
  }
}
```

### 5. Manejar Errores del Backend

```javascript
async function handleCheckout() {
  try {
    const response = await fetch('/api/v1/orders', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        productIds: cartItems.map(item => item.id).join(','),
        totalAmount: total
      })
    });
    
    const result = await response.json();
    
    if (!result.ok) {
      // Verificar tipo de error
      if (result.message.includes('agotado') || result.message.includes('agotados')) {
        // Producto agotado
        showError('Uno o más productos están agotados. Por favor, elimínalos del carrito.');
      } else if (result.message.includes('Stock insuficiente')) {
        // Stock insuficiente
        showError(result.message);
      } else {
        // Otro error
        showError('Error al procesar la compra. Por favor, intenta de nuevo.');
      }
      return;
    }
    
    // Éxito
    showSuccess('Pedido creado exitosamente');
    clearCart();
  } catch (error) {
    console.error('Error:', error);
    showError('Error de conexión. Por favor, intenta de nuevo.');
  }
}
```

## 📋 Checklist de Implementación Frontend

- [ ] Verificar `stock` al cargar producto individual
- [ ] Verificar `stock` al cargar lista de productos
- [ ] Ocultar/deshabilitar botón de compra si `stock === 0`
- [ ] Mostrar badge/mensaje "Agotado" si `stock === 0`
- [ ] Mostrar badge "Últimas X unidades" si `stock <= 5`
- [ ] Validar stock antes de agregar al carrito (opcional)
- [ ] Manejar errores del backend cuando se intenta comprar
- [ ] Actualizar stock después de compras exitosas
- [ ] Refrescar datos del producto periódicamente (opcional)

## 🔄 Flujo Completo

```
1. Usuario ve producto
   ↓
2. Frontend verifica stock (mejora UX)
   ↓
3. Si stock = 0: Ocultar botón / Mostrar "Agotado"
   ↓
4. Usuario intenta comprar
   ↓
5. Frontend valida stock (opcional, mejora UX)
   ↓
6. Envía request al backend
   ↓
7. Backend valida stock (OBLIGATORIO, seguridad)
   ↓
8. Si stock = 0: Retorna error 400
   ↓
9. Frontend muestra error al usuario
   ↓
10. Si stock > 0: Crea pedido y reduce stock
```

## ⚠️ Importante

- **El backend SIEMPRE valida** - No confíes solo en el frontend
- **El frontend mejora la UX** - Pero no es suficiente para seguridad
- **Mensajes claros** - Ayudan al usuario a entender qué pasó
- **Actualizar UI** - Después de compras, actualiza el stock mostrado

## 🧪 Pruebas

### Probar Producto Agotado

1. Busca un producto con `stock = 0`
2. Intenta crear un pedido con ese producto
3. Debe retornar error 400 con mensaje "Producto agotado"

### Probar Stock Insuficiente

1. Busca un producto con `stock = 2`
2. Intenta comprar 5 unidades
3. Debe retornar error 400 con mensaje "Stock insuficiente"

## 📝 Notas

- El backend ya está implementado y funcionando
- El frontend debe implementar las mejoras de UX
- La validación del backend es suficiente para seguridad
- Las mejoras del frontend son para mejor experiencia de usuario

## 📚 Documentación Adicional

Para una guía más detallada con ejemplos de código listos para usar, ver:
- **`GUIA_FRONTEND_VALIDACION_STOCK.md`** - Guía completa con ejemplos de código para React/JSX
