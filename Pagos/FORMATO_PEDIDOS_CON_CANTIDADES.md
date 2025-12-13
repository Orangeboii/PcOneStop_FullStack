# 🛒 Formato de Pedidos con Cantidades

Este documento explica cómo enviar pedidos con cantidades específicas para cada producto.

## 📋 Formatos Soportados

El backend ahora soporta **dos formatos** para crear pedidos:

### 1. Formato Antiguo (Compatibilidad)
**Cantidad fija: 1 unidad por producto**

```json
{
  "totalAmount": 15999.99,
  "productIds": "1,5,8"
}
```

### 2. Formato Nuevo (Recomendado) ⭐
**Cantidad variable: especifica cuántas unidades de cada producto**

```json
{
  "totalAmount": 15999.99,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 899990.00
    },
    {
      "productId": 5,
      "quantity": 1,
      "price": 699990.00
    }
  ]
}
```

---

## 🎯 Formato Nuevo - Estructura Detallada

### Request Body

```json
{
  "totalAmount": 2599980.00,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 899990.00
    },
    {
      "productId": 5,
      "quantity": 3,
      "price": 266663.33
    }
  ]
}
```

### Campos

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `totalAmount` | Number | ✅ Sí | Monto total de la compra |
| `items` | Array | ✅ Sí | Lista de productos con cantidades |
| `items[].productId` | Number | ✅ Sí | ID del producto |
| `items[].quantity` | Number | ✅ Sí | Cantidad a comprar (debe ser > 0) |
| `items[].price` | Number | ❌ No | Precio unitario (opcional, para referencia) |
| `userId` | Number | ❌ No | Se extrae del token JWT automáticamente |

---

## 💻 Ejemplos de Código Frontend

### Ejemplo 1: Crear Pedido con Cantidades (JavaScript/React)

```javascript
// services/orderService.js
const API_BASE_URL = 'http://localhost:8083/api/v1';

export async function createOrderWithQuantities(cartItems, totalAmount) {
  const token = localStorage.getItem('token');
  
  if (!token) {
    throw new Error('No estás autenticado. Por favor, inicia sesión.');
  }
  
  // Preparar items con cantidades
  const items = cartItems.map(item => ({
    productId: item.id,
    quantity: item.quantity, // ← Cantidad específica de cada producto
    price: item.price // Opcional, para referencia
  }));
  
  const orderData = {
    totalAmount: totalAmount,
    items: items
  };
  
  try {
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(orderData)
    });
    
    const result = await response.json();
    
    if (!result.ok) {
      throw new Error(result.message || 'Error al crear el pedido');
    }
    
    return result.data;
  } catch (error) {
    console.error('Error al crear pedido:', error);
    throw error;
  }
}
```

### Ejemplo 2: Componente de Carrito con Cantidades

```jsx
// components/Cart.jsx
import React, { useState } from 'react';
import { createOrderWithQuantities } from '../services/orderService';

function Cart({ cartItems, onUpdateQuantity, onRemoveItem }) {
  const [loading, setLoading] = useState(false);
  
  // Calcular total
  const totalAmount = cartItems.reduce((sum, item) => {
    return sum + (item.price * item.quantity);
  }, 0);
  
  const handleCheckout = async () => {
    setLoading(true);
    try {
      // Validar que todos los items tengan stock suficiente
      for (const item of cartItems) {
        if (item.stock < item.quantity) {
          alert(`No hay suficiente stock para ${item.name}. Stock disponible: ${item.stock}`);
          setLoading(false);
          return;
        }
      }
      
      // Crear pedido con cantidades
      const order = await createOrderWithQuantities(cartItems, totalAmount);
      
      alert('Pedido creado exitosamente!');
      // Limpiar carrito o redirigir
      
    } catch (error) {
      alert('Error al crear pedido: ' + error.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="cart">
      <h2>Carrito de Compras</h2>
      
      {cartItems.map(item => (
        <div key={item.id} className="cart-item">
          <h3>{item.name}</h3>
          <p>Precio unitario: ${item.price.toLocaleString()}</p>
          
          {/* Selector de Cantidad */}
          <div className="quantity-controls">
            <button 
              onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
              disabled={item.quantity <= 1}
            >
              -
            </button>
            <span>{item.quantity}</span>
            <button 
              onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
              disabled={item.quantity >= item.stock}
            >
              +
            </button>
            <span>Máximo: {item.stock}</span>
          </div>
          
          <p>Subtotal: ${(item.price * item.quantity).toLocaleString()}</p>
          
          {item.stock < item.quantity && (
            <p className="text-danger">
              ⚠️ Stock insuficiente (disponible: {item.stock})
            </p>
          )}
          
          <button onClick={() => onRemoveItem(item.id)}>Eliminar</button>
        </div>
      ))}
      
      <div className="cart-total">
        <h3>Total: ${totalAmount.toLocaleString()}</h3>
      </div>
      
      <button 
        className="btn btn-primary"
        onClick={handleCheckout}
        disabled={loading || cartItems.length === 0}
      >
        {loading ? 'Procesando...' : 'Completar Compra'}
      </button>
    </div>
  );
}

export default Cart;
```

### Ejemplo 3: Agregar Producto al Carrito con Cantidad

```javascript
// hooks/useCart.js
import { useState } from 'react';

export function useCart() {
  const [cartItems, setCartItems] = useState([]);
  
  const addToCart = (product, quantity = 1) => {
    // Validar stock
    if (product.stock < quantity) {
      throw new Error(`No hay suficiente stock. Disponible: ${product.stock}`);
    }
    
    // Buscar si el producto ya está en el carrito
    const existingItem = cartItems.find(item => item.id === product.id);
    
    if (existingItem) {
      // Actualizar cantidad
      const newQuantity = existingItem.quantity + quantity;
      if (newQuantity > product.stock) {
        throw new Error(`No puedes agregar más. Stock disponible: ${product.stock}`);
      }
      
      setCartItems(cartItems.map(item =>
        item.id === product.id
          ? { ...item, quantity: newQuantity }
          : item
      ));
    } else {
      // Agregar nuevo item
      setCartItems([...cartItems, {
        id: product.id,
        name: product.name,
        price: product.price,
        quantity: quantity,
        stock: product.stock
      }]);
    }
  };
  
  const updateQuantity = (productId, newQuantity) => {
    if (newQuantity <= 0) {
      removeFromCart(productId);
      return;
    }
    
    setCartItems(cartItems.map(item => {
      if (item.id === productId) {
        if (newQuantity > item.stock) {
          throw new Error(`No puedes comprar más de ${item.stock} unidades`);
        }
        return { ...item, quantity: newQuantity };
      }
      return item;
    }));
  };
  
  const removeFromCart = (productId) => {
    setCartItems(cartItems.filter(item => item.id !== productId));
  };
  
  return {
    cartItems,
    addToCart,
    updateQuantity,
    removeFromCart
  };
}
```

---

## 🔄 Flujo Completo

```
1. Usuario selecciona productos y cantidades
   ↓
2. Frontend valida stock (mejora UX)
   ↓
3. Usuario hace checkout
   ↓
4. Frontend envía request con items y cantidades:
   {
     "totalAmount": 2599980.00,
     "items": [
       {"productId": 1, "quantity": 2},
       {"productId": 5, "quantity": 3}
     ]
   }
   ↓
5. Backend valida stock para cada item
   ↓
6. Backend reduce stock con la cantidad exacta:
   - Producto 1: stock -= 2
   - Producto 5: stock -= 3
   ↓
7. Backend crea pedido
   ↓
8. Frontend muestra confirmación
```

---

## ✅ Validaciones del Backend

El backend valida automáticamente:

1. **Existencia de productos**: Todos los `productId` deben existir
2. **Stock suficiente**: Para cada item, `stock >= quantity`
3. **Cantidad válida**: `quantity > 0`

### Errores Posibles

#### Error 400 - Stock Insuficiente
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "Stock insuficiente para GeForce RTX 4070. Solo hay 1 unidad(es) disponible(s) (solicitaste 2)",
  "data": null,
  "count": 0
}
```

#### Error 400 - Producto Agotado
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "Producto agotado: AMD Ryzen 7 7800X3D. No hay unidades disponibles.",
  "data": null,
  "count": 0
}
```

---

## 📝 Ejemplo Completo de Request

### Request
```http
POST /api/v1/orders
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "totalAmount": 2599980.00,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "price": 899990.00
    },
    {
      "productId": 5,
      "quantity": 1,
      "price": 799990.00
    }
  ]
}
```

### Response (Éxito)
```json
{
  "ok": true,
  "statusCode": 201,
  "message": "Pedido creado exitosamente",
  "data": {
    "id": 1,
    "userId": 10,
    "totalAmount": 2599980.00,
    "status": "PENDIENTE",
    "productIds": "1,5",
    "createdAt": "2024-01-01T12:00:00"
  },
  "count": 1
}
```

---

## 🔄 Migración del Formato Antiguo

Si estás usando el formato antiguo (`productIds`), puedes seguir usándolo. El backend lo soporta y asume cantidad 1 por producto.

**Recomendación**: Migra al formato nuevo (`items`) para poder especificar cantidades.

---

## 🧪 Pruebas

### Probar con Cantidades

1. **Crear pedido con cantidad 2:**
```json
{
  "totalAmount": 1799980.00,
  "items": [
    {"productId": 1, "quantity": 2}
  ]
}
```

2. **Verificar que el stock se redujo en 2 unidades:**
   - Antes: stock = 10
   - Después: stock = 8

3. **Probar con cantidad mayor al stock:**
```json
{
  "totalAmount": 8999900.00,
  "items": [
    {"productId": 1, "quantity": 15}
  ]
}
```
   - Debe retornar error 400: "Stock insuficiente"

---

## 📌 Notas Importantes

- ✅ El backend valida y reduce el stock con la cantidad exacta
- ✅ Si falla la validación, no se crea el pedido (rollback automático)
- ✅ El formato antiguo (`productIds`) sigue funcionando (compatibilidad)
- ✅ El formato nuevo (`items`) es recomendado para especificar cantidades
- ✅ El campo `price` en items es opcional (solo para referencia)

---

## 🚀 Listo para Usar

El backend ya está implementado y funcionando. Solo necesitas:

1. Enviar el request con el formato nuevo (`items`)
2. Incluir `quantity` para cada producto
3. El backend se encargará de validar y reducir el stock correctamente

¡El sistema ahora descuenta la cantidad exacta que se compra! 🎉
