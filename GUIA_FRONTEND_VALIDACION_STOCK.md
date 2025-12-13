# 🎨 Guía Frontend - Validación de Stock

Esta guía explica **exactamente qué implementar en el frontend** para manejar productos agotados y mejorar la experiencia del usuario.

## 📋 Resumen Rápido

**El backend ya está funcionando** y bloquea compras de productos agotados. El frontend debe:
1. ✅ Mostrar el estado del stock (agotado/disponible)
2. ✅ Ocultar/deshabilitar botones cuando `stock === 0`
3. ✅ Manejar errores del backend cuando se intenta comprar

---

## 🔍 1. Verificar Stock en Respuestas del Backend

### Endpoint: `GET /api/v1/products/{id}`

**Respuesta del Backend:**
```json
{
  "ok": true,
  "statusCode": 200,
  "data": {
    "id": 1,
    "name": "GeForce RTX 4070",
    "stock": 0,  // ← ESTE ES EL CAMPO CLAVE
    "price": 899990.00,
    ...
  }
}
```

**El campo `stock` siempre está presente:**
- `stock: 0` → Producto agotado
- `stock: 1-5` → Últimas unidades
- `stock: > 5` → En stock

---

## 🎯 2. Implementación en Componentes

### Ejemplo 1: Tarjeta de Producto (ProductCard)

```jsx
// components/ProductCard.jsx
import React from 'react';

function ProductCard({ product }) {
  const isOutOfStock = product.stock === 0;
  const isLowStock = product.stock > 0 && product.stock <= 5;
  
  return (
    <div className="product-card">
      <h3>{product.name}</h3>
      <p className="price">${product.price.toLocaleString()}</p>
      
      {/* Indicador de Stock */}
      <div className="stock-indicator">
        {isOutOfStock ? (
          <span className="badge badge-danger">Agotado</span>
        ) : isLowStock ? (
          <span className="badge badge-warning">
            Últimas {product.stock} unidades
          </span>
        ) : (
          <span className="badge badge-success">En stock</span>
        )}
      </div>
      
      {/* Botón de Compra */}
      {isOutOfStock ? (
        <button disabled className="btn btn-disabled">
          Producto Agotado
        </button>
      ) : (
        <button 
          className="btn btn-primary"
          onClick={() => handleAddToCart(product)}
        >
          Agregar al Carrito
        </button>
      )}
    </div>
  );
}

export default ProductCard;
```

### Ejemplo 2: Página de Detalles del Producto

```jsx
// pages/ProductDetail.jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  
  useEffect(() => {
    fetchProduct();
  }, [id]);
  
  const fetchProduct = async () => {
    try {
      const response = await fetch(`http://localhost:8082/api/v1/products/${id}`);
      const data = await response.json();
      
      if (data.ok) {
        setProduct(data.data);
        // Limitar cantidad máxima al stock disponible
        if (data.data.stock > 0 && quantity > data.data.stock) {
          setQuantity(data.data.stock);
        }
      }
    } catch (error) {
      console.error('Error al cargar producto:', error);
    } finally {
      setLoading(false);
    }
  };
  
  const isOutOfStock = product?.stock === 0;
  const maxQuantity = product?.stock || 0;
  
  if (loading) return <div>Cargando...</div>;
  if (!product) return <div>Producto no encontrado</div>;
  
  return (
    <div className="product-detail">
      <h1>{product.name}</h1>
      <p className="price">${product.price.toLocaleString()}</p>
      
      {/* Indicador de Stock */}
      {isOutOfStock ? (
        <div className="alert alert-danger">
          ⚠️ Este producto está agotado
        </div>
      ) : (
        <div className="stock-info">
          <p>Stock disponible: <strong>{product.stock} unidades</strong></p>
          {product.stock <= 5 && (
            <p className="text-warning">
              ⚠️ Quedan pocas unidades disponibles
            </p>
          )}
        </div>
      )}
      
      {/* Selector de Cantidad */}
      {!isOutOfStock && (
        <div className="quantity-selector">
          <label>Cantidad:</label>
          <input
            type="number"
            min="1"
            max={maxQuantity}
            value={quantity}
            onChange={(e) => {
              const val = parseInt(e.target.value);
              if (val >= 1 && val <= maxQuantity) {
                setQuantity(val);
              }
            }}
          />
          <span>Máximo: {maxQuantity}</span>
        </div>
      )}
      
      {/* Botón de Compra */}
      {isOutOfStock ? (
        <button disabled className="btn btn-disabled btn-large">
          Producto Agotado
        </button>
      ) : (
        <button 
          className="btn btn-primary btn-large"
          onClick={() => handleAddToCart(product, quantity)}
        >
          Agregar al Carrito - ${(product.price * quantity).toLocaleString()}
        </button>
      )}
    </div>
  );
}

export default ProductDetail;
```

---

## 🛒 3. Manejo de Errores al Crear Pedido

### Endpoint: `POST /api/v1/orders`

**Errores del Backend:**

#### Error 400 - Producto Agotado
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "No se puede completar la compra: uno o más productos están agotados. Producto agotado. No hay unidades disponibles. (Producto: GeForce RTX 4070).",
  "data": null,
  "count": 0
}
```

#### Error 400 - Stock Insuficiente
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "Stock insuficiente para uno o más productos. Solo hay 2 unidad(es) disponible(s) (solicitaste 5) (Producto: AMD Ryzen 7 7800X3D).",
  "data": null,
  "count": 0
}
```

### Ejemplo: Función para Crear Pedido

```javascript
// services/orderService.js
const API_BASE_URL = 'http://localhost:8083/api/v1';

export async function createOrder(cartItems, totalAmount) {
  const token = localStorage.getItem('token');
  
  if (!token) {
    throw new Error('No estás autenticado. Por favor, inicia sesión.');
  }
  
  // Preparar datos del pedido
  const productIds = cartItems.map(item => item.id).join(',');
  
  const orderData = {
    productIds: productIds,
    totalAmount: totalAmount
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
    
    // Manejar errores
    if (!result.ok) {
      // Detectar tipo de error
      const message = result.message || 'Error al procesar la compra';
      
      if (message.includes('agotado') || message.includes('agotados')) {
        throw new Error('Uno o más productos están agotados. Por favor, elimínalos del carrito e intenta de nuevo.');
      } else if (message.includes('Stock insuficiente')) {
        throw new Error(message);
      } else {
        throw new Error('Error al procesar la compra. Por favor, intenta de nuevo.');
      }
    }
    
    // Éxito
    return result.data;
    
  } catch (error) {
    // Re-lanzar errores para que el componente los maneje
    throw error;
  }
}
```

### Ejemplo: Componente de Checkout

```jsx
// components/Checkout.jsx
import React, { useState } from 'react';
import { createOrder } from '../services/orderService';

function Checkout({ cartItems, totalAmount, onSuccess, onCancel }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const handleCheckout = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Validación previa (opcional, mejora UX)
      for (const item of cartItems) {
        if (item.stock === 0) {
          setError(`El producto "${item.name}" está agotado. Por favor, elimínalo del carrito.`);
          setLoading(false);
          return;
        }
      }
      
      // Crear pedido
      const order = await createOrder(cartItems, totalAmount);
      
      // Éxito
      onSuccess(order);
      
    } catch (err) {
      // Mostrar error al usuario
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="checkout">
      <h2>Confirmar Compra</h2>
      
      {/* Mostrar error si hay */}
      {error && (
        <div className="alert alert-danger">
          <strong>Error:</strong> {error}
        </div>
      )}
      
      {/* Lista de productos */}
      <div className="cart-items">
        {cartItems.map(item => (
          <div key={item.id} className="cart-item">
            <span>{item.name}</span>
            <span>${item.price.toLocaleString()}</span>
            {item.stock === 0 && (
              <span className="text-danger">⚠️ Agotado</span>
            )}
          </div>
        ))}
      </div>
      
      <div className="total">
        <strong>Total: ${totalAmount.toLocaleString()}</strong>
      </div>
      
      <div className="checkout-actions">
        <button 
          className="btn btn-secondary"
          onClick={onCancel}
          disabled={loading}
        >
          Cancelar
        </button>
        <button 
          className="btn btn-primary"
          onClick={handleCheckout}
          disabled={loading || cartItems.some(item => item.stock === 0)}
        >
          {loading ? 'Procesando...' : 'Confirmar Compra'}
        </button>
      </div>
    </div>
  );
}

export default Checkout;
```

---

## 🔄 4. Actualizar Stock Después de Compras

```javascript
// Después de una compra exitosa, actualizar el stock en el carrito
function handlePurchaseSuccess(order) {
  // Actualizar stock de productos en el carrito
  setCartItems(prevItems => 
    prevItems.map(item => {
      // Buscar si este producto está en el pedido
      const productIds = order.productIds.split(',');
      if (productIds.includes(item.id.toString())) {
        // Reducir stock en 1 (o la cantidad comprada)
        return {
          ...item,
          stock: Math.max(0, item.stock - 1)
        };
      }
      return item;
    })
  );
  
  // Eliminar productos agotados del carrito
  setCartItems(prevItems => 
    prevItems.filter(item => item.stock > 0)
  );
}
```

---

## 📝 5. Checklist de Implementación

### Componentes a Modificar:

- [ ] **ProductCard.jsx** - Mostrar badge de stock y deshabilitar botón si `stock === 0`
- [ ] **ProductDetail.jsx** - Mostrar stock disponible y limitar cantidad máxima
- [ ] **Cart.jsx** - Mostrar advertencia si algún producto está agotado
- [ ] **Checkout.jsx** - Validar stock antes de comprar y manejar errores
- [ ] **OrderService.js** - Manejar errores del backend (400, 404, etc.)

### Funcionalidades a Agregar:

- [ ] Verificar `product.stock` al cargar productos
- [ ] Mostrar badge "Agotado" cuando `stock === 0`
- [ ] Mostrar badge "Últimas X unidades" cuando `stock <= 5`
- [ ] Deshabilitar botón "Agregar al Carrito" si `stock === 0`
- [ ] Limitar cantidad máxima al stock disponible
- [ ] Mostrar mensaje de error cuando el backend rechaza la compra
- [ ] Actualizar stock en el carrito después de compras exitosas
- [ ] Eliminar productos agotados del carrito automáticamente

---

## 🎨 6. Estilos CSS (Opcional)

```css
/* Estilos para indicadores de stock */
.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.badge-danger {
  background-color: #dc3545;
  color: white;
}

.badge-warning {
  background-color: #ffc107;
  color: #000;
}

.badge-success {
  background-color: #28a745;
  color: white;
}

/* Botón deshabilitado */
.btn-disabled {
  background-color: #6c757d;
  color: white;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-disabled:hover {
  background-color: #6c757d;
}

/* Alerta de stock */
.alert {
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.alert-danger {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.alert-warning {
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  color: #856404;
}
```

---

## 🧪 7. Pruebas

### Probar Producto Agotado:

1. Buscar un producto con `stock = 0`
2. Verificar que:
   - ✅ Muestra badge "Agotado"
   - ✅ Botón está deshabilitado
   - ✅ No permite agregar al carrito

### Probar Compra con Producto Agotado:

1. Agregar producto al carrito cuando tiene stock
2. Esperar a que se agote (o cambiar stock manualmente en BD)
3. Intentar comprar
4. Verificar que:
   - ✅ Backend retorna error 400
   - ✅ Frontend muestra mensaje de error claro
   - ✅ No se crea el pedido

---

## 📌 Resumen

**Lo que el Backend hace (ya implementado):**
- ✅ Valida stock antes de crear pedido
- ✅ Bloquea compras de productos agotados
- ✅ Retorna errores claros (400)

**Lo que el Frontend debe hacer:**
- ✅ Mostrar estado del stock (agotado/disponible)
- ✅ Ocultar/deshabilitar botones cuando `stock === 0`
- ✅ Manejar errores del backend y mostrar mensajes claros
- ✅ Actualizar UI después de compras

**El backend ya protege la seguridad, el frontend mejora la experiencia del usuario.**

---

## 🚀 ¿Listo para Implementar?

1. Copia los ejemplos de código de arriba
2. Adapta a tu framework (React, Vue, Angular, etc.)
3. Prueba con productos que tengan `stock = 0`
4. Verifica que los errores se muestren correctamente

¡El backend ya está funcionando! Solo necesitas mejorar la UI del frontend. 🎉
