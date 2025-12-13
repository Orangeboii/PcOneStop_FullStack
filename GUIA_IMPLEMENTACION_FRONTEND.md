# 🎨 Guía de Implementación Frontend - PC OneStop

Esta guía contiene **todo lo que el frontend debe implementar** para funcionar correctamente con el backend actualizado.

---

## 📋 Índice

1. [Validación de Sesión](#1-validación-de-sesión)
2. [Obtener Todos los Productos (PC Builder)](#2-obtener-todos-los-productos-pc-builder)
3. [Validación de Stock](#3-validación-de-stock)
4. [Pedidos con Cantidades](#4-pedidos-con-cantidades)
5. [Manejo de Errores](#5-manejo-de-errores)

---

## 1. Validación de Sesión

### Endpoint Nuevo: `GET /api/v1/auth/validate`

**¿Para qué sirve?**  
Permite verificar si el token JWT del usuario sigue siendo válido y restaurar la sesión sin hacer login de nuevo.

### Implementación

```javascript
// services/authService.js
const API_BASE_URL = 'http://localhost:8081/api/v1';

export async function validateSession() {
  const token = localStorage.getItem('token');
  
  if (!token) {
    return null; // No hay token, usuario no autenticado
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/auth/validate`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.ok && result.data) {
      // Token válido, retornar datos del usuario
      return result.data;
    } else {
      // Token inválido o expirado
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return null;
    }
  } catch (error) {
    console.error('Error al validar sesión:', error);
    return null;
  }
}
```

### Uso en la Aplicación

```javascript
// App.jsx o main.jsx
import { useEffect, useState } from 'react';
import { validateSession } from './services/authService';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    // Al cargar la app, validar si hay sesión activa
    async function checkSession() {
      const userData = await validateSession();
      if (userData) {
        setUser(userData);
        // Guardar usuario en estado global o contexto
      }
      setLoading(false);
    }
    
    checkSession();
  }, []);
  
  if (loading) {
    return <div>Cargando...</div>;
  }
  
  // Resto de la aplicación
  return (
    <div>
      {user ? (
        <p>Bienvenido, {user.firstName} {user.lastName}</p>
      ) : (
        <p>Por favor, inicia sesión</p>
      )}
    </div>
  );
}
```

### Respuestas del Backend

**✅ Token Válido (200):**
```json
{
  "ok": true,
  "statusCode": 200,
  "message": "Token válido",
  "data": {
    "id": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "role": "CLIENTE"
  },
  "count": 1
}
```

**❌ Token Inválido (401):**
```json
{
  "ok": false,
  "statusCode": 401,
  "message": "Token inválido o expirado",
  "data": null,
  "count": 0
}
```

**❌ Token No Proporcionado (403):**
```json
{
  "ok": false,
  "statusCode": 403,
  "message": "Token no proporcionado",
  "data": null,
  "count": 0
}
```

---

## 2. Obtener Todos los Productos (PC Builder)

### Endpoint: `GET /api/v1/products`

**¿Para qué sirve?**  
Obtiene el listado completo de todos los componentes disponibles en PcOneStop. Esencial para el PC Builder donde los usuarios necesitan ver todos los productos disponibles para construir su PC.

**⚠️ IMPORTANTE:** Este endpoint requiere autenticación (token JWT). Cualquier usuario autenticado puede acceder.

### Implementación

```javascript
// services/productService.js
const API_BASE_URL = 'http://localhost:8082/api/v1';

export async function getAllProducts() {
  const token = localStorage.getItem('token');
  
  if (!token) {
    throw new Error('No estás autenticado. Por favor, inicia sesión.');
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/products`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (!result.ok) {
      if (response.status === 401) {
        // Token inválido, redirigir a login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw new Error('Sesión expirada. Por favor, inicia sesión de nuevo.');
      }
      throw new Error(result.message || 'Error al obtener productos');
    }
    
    return result.data || []; // Retorna array de productos
  } catch (error) {
    console.error('Error al obtener productos:', error);
    throw error;
  }
}
```

### Uso en PC Builder

```jsx
// components/PCBuilder.jsx
import React, { useState, useEffect } from 'react';
import { getAllProducts } from '../services/productService';

function PCBuilder() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('all');
  
  useEffect(() => {
    loadProducts();
  }, []);
  
  const loadProducts = async () => {
    try {
      setLoading(true);
      const allProducts = await getAllProducts();
      setProducts(allProducts);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  // Filtrar productos por categoría
  const filteredProducts = selectedCategory === 'all'
    ? products
    : products.filter(p => p.category === selectedCategory);
  
  // Obtener categorías únicas
  const categories = ['all', ...new Set(products.map(p => p.category))];
  
  if (loading) {
    return <div>Cargando productos...</div>;
  }
  
  if (error) {
    return <div className="error">Error: {error}</div>;
  }
  
  return (
    <div className="pc-builder">
      <h1>PC Builder</h1>
      
      {/* Filtro por categoría */}
      <div className="category-filter">
        <label>Filtrar por categoría:</label>
        <select 
          value={selectedCategory} 
          onChange={(e) => setSelectedCategory(e.target.value)}
        >
          {categories.map(cat => (
            <option key={cat} value={cat}>
              {cat === 'all' ? 'Todas las categorías' : cat}
            </option>
          ))}
        </select>
      </div>
      
      {/* Lista de productos */}
      <div className="products-grid">
        {filteredProducts.map(product => (
          <div key={product.id} className="product-card">
            <h3>{product.name}</h3>
            <p>{product.brand} {product.model}</p>
            <p className="category">{product.category}</p>
            <p className="price">${product.price.toLocaleString()}</p>
            <p className="stock">Stock: {product.stock}</p>
            
            {product.stock > 0 ? (
              <button onClick={() => handleAddToBuild(product)}>
                Agregar al Build
              </button>
            ) : (
              <button disabled>Agotado</button>
            )}
          </div>
        ))}
      </div>
      
      {filteredProducts.length === 0 && (
        <p>No hay productos disponibles en esta categoría.</p>
      )}
    </div>
  );
}

export default PCBuilder;
```

### Respuesta del Backend

**✅ Éxito (200):**
```json
{
  "ok": true,
  "statusCode": 200,
  "message": "Productos obtenidos",
  "data": [
    {
      "id": 1,
      "name": "GeForce RTX 4070",
      "brand": "MSI",
      "model": "Ventus 3X",
      "category": "GPU",
      "price": 899990.00,
      "stock": 10,
      "description": "Tarjeta gráfica de alto rendimiento",
      "image": "url_de_imagen"
    },
    {
      "id": 2,
      "name": "AMD Ryzen 7 7800X3D",
      "brand": "AMD",
      "model": "Ryzen 7 7800X3D",
      "category": "CPU",
      "price": 599990.00,
      "stock": 5,
      "description": "Procesador de alto rendimiento",
      "image": "url_de_imagen"
    }
    // ... más productos
  ],
  "count": 40
}
```

**❌ No Autenticado (401):**
```json
{
  "ok": false,
  "statusCode": 401,
  "message": "Token inválido o expirado",
  "data": null,
  "count": 0
}
```

**❌ Sin Productos (204):**
```json
{
  "ok": false,
  "statusCode": 204,
  "message": "No hay productos registrados",
  "data": null,
  "count": 0
}
```

### Ejemplo con Filtrado por Categoría

```javascript
// Filtrar productos por categoría para el PC Builder
export function getProductsByCategory(products, category) {
  if (category === 'all') {
    return products;
  }
  return products.filter(p => p.category === category);
}

// Categorías comunes:
// - GPU (Tarjetas gráficas)
// - CPU (Procesadores)
// - RAM (Memoria)
// - SSD (Almacenamiento)
// - Motherboard (Placas base)
// - PSU (Fuentes de poder)
// - Case (Gabinetes)
// - Cooler (Refrigeración)
// - Peripheral (Periféricos)
```

### Componente Completo de PC Builder

```jsx
// components/PCBuilder.jsx
import React, { useState, useEffect } from 'react';
import { getAllProducts } from '../services/productService';

const CATEGORIES = {
  all: 'Todas las categorías',
  GPU: 'Tarjetas Gráficas',
  CPU: 'Procesadores',
  RAM: 'Memoria RAM',
  SSD: 'Almacenamiento',
  Motherboard: 'Placas Base',
  PSU: 'Fuentes de Poder',
  Case: 'Gabinetes',
  Cooler: 'Refrigeración',
  Peripheral: 'Periféricos'
};

function PCBuilder() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [build, setBuild] = useState([]); // Componentes seleccionados para el build
  
  useEffect(() => {
    loadProducts();
  }, []);
  
  const loadProducts = async () => {
    try {
      setLoading(true);
      const allProducts = await getAllProducts();
      setProducts(allProducts);
    } catch (err) {
      setError(err.message);
      if (err.message.includes('Sesión expirada')) {
        // Redirigir a login después de 2 segundos
        setTimeout(() => {
          window.location.href = '/login';
        }, 2000);
      }
    } finally {
      setLoading(false);
    }
  };
  
  // Filtrar productos
  const filteredProducts = products.filter(product => {
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.brand.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });
  
  const handleAddToBuild = (product) => {
    // Verificar si ya está en el build
    const existing = build.find(item => item.id === product.id);
    if (existing) {
      alert('Este producto ya está en tu build');
      return;
    }
    
    // Verificar stock
    if (product.stock === 0) {
      alert('Este producto está agotado');
      return;
    }
    
    setBuild([...build, product]);
  };
  
  const handleRemoveFromBuild = (productId) => {
    setBuild(build.filter(item => item.id !== productId));
  };
  
  const calculateTotal = () => {
    return build.reduce((sum, item) => sum + item.price, 0);
  };
  
  if (loading) {
    return <div className="loading">Cargando productos...</div>;
  }
  
  if (error) {
    return <div className="error">Error: {error}</div>;
  }
  
  return (
    <div className="pc-builder">
      <h1>PC Builder</h1>
      
      {/* Filtros */}
      <div className="filters">
        <div className="filter-group">
          <label>Categoría:</label>
          <select 
            value={selectedCategory} 
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            {Object.entries(CATEGORIES).map(([key, label]) => (
              <option key={key} value={key}>{label}</option>
            ))}
          </select>
        </div>
        
        <div className="filter-group">
          <label>Buscar:</label>
          <input
            type="text"
            placeholder="Buscar producto..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>
      
      <div className="builder-layout">
        {/* Lista de productos disponibles */}
        <div className="products-section">
          <h2>Componentes Disponibles ({filteredProducts.length})</h2>
          <div className="products-grid">
            {filteredProducts.map(product => (
              <div key={product.id} className="product-card">
                <h3>{product.name}</h3>
                <p>{product.brand} {product.model}</p>
                <p className="category">{product.category}</p>
                <p className="price">${product.price.toLocaleString()}</p>
                <p className={`stock ${product.stock === 0 ? 'out-of-stock' : ''}`}>
                  Stock: {product.stock}
                </p>
                
                {product.stock > 0 ? (
                  <button 
                    onClick={() => handleAddToBuild(product)}
                    disabled={build.some(item => item.id === product.id)}
                  >
                    {build.some(item => item.id === product.id) 
                      ? 'Ya en Build' 
                      : 'Agregar al Build'}
                  </button>
                ) : (
                  <button disabled>Agotado</button>
                )}
              </div>
            ))}
          </div>
        </div>
        
        {/* Build actual */}
        <div className="build-section">
          <h2>Tu Build</h2>
          {build.length === 0 ? (
            <p>No has agregado componentes aún</p>
          ) : (
            <>
              {build.map(product => (
                <div key={product.id} className="build-item">
                  <h4>{product.name}</h4>
                  <p>${product.price.toLocaleString()}</p>
                  <button onClick={() => handleRemoveFromBuild(product.id)}>
                    Eliminar
                  </button>
                </div>
              ))}
              <div className="build-total">
                <h3>Total: ${calculateTotal().toLocaleString()}</h3>
                <button className="btn-primary">
                  Completar Compra
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default PCBuilder;
```

---

## 3. Validación de Stock

### 2.1 Verificar Stock en Productos

**Endpoint:** `GET /api/v1/products/{id}`

El campo `stock` siempre está presente en la respuesta:

```json
{
  "ok": true,
  "data": {
    "id": 1,
    "name": "GeForce RTX 4070",
    "stock": 0,  // ← VERIFICAR ESTE CAMPO
    "price": 899990.00
  }
}
```

### 2.2 Mostrar Estado de Stock

```jsx
// components/ProductCard.jsx
function ProductCard({ product }) {
  const isOutOfStock = product.stock === 0;
  const isLowStock = product.stock > 0 && product.stock <= 5;
  
  return (
    <div className="product-card">
      <h3>{product.name}</h3>
      <p>${product.price.toLocaleString()}</p>
      
      {/* Indicador de Stock */}
      {isOutOfStock ? (
        <span className="badge badge-danger">Agotado</span>
      ) : isLowStock ? (
        <span className="badge badge-warning">
          Últimas {product.stock} unidades
        </span>
      ) : (
        <span className="badge badge-success">En stock</span>
      )}
      
      {/* Botón de Compra */}
      {isOutOfStock ? (
        <button disabled className="btn-disabled">
          Producto Agotado
        </button>
      ) : (
        <button onClick={() => handleAddToCart(product)}>
          Agregar al Carrito
        </button>
      )}
    </div>
  );
}
```

### 2.3 Manejar Errores de Stock al Comprar

**Endpoint:** `POST /api/v1/orders`

Cuando intentas comprar un producto agotado, el backend retorna:

```json
{
  "ok": false,
  "statusCode": 400,
  "message": "No se puede completar la compra: uno o más productos están agotados. Producto agotado. No hay unidades disponibles. (Producto: GeForce RTX 4070).",
  "data": null,
  "count": 0
}
```

**Implementación:**

```javascript
// services/orderService.js
export async function createOrder(cartItems, totalAmount) {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8083/api/v1/orders', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      totalAmount: totalAmount,
      items: cartItems.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }))
    })
  });
  
  const result = await response.json();
  
  if (!result.ok) {
    // Detectar tipo de error
    if (result.message.includes('agotado') || result.message.includes('agotados')) {
      throw new Error('Uno o más productos están agotados. Por favor, elimínalos del carrito.');
    } else if (result.message.includes('Stock insuficiente')) {
      throw new Error(result.message);
    } else {
      throw new Error('Error al procesar la compra. Por favor, intenta de nuevo.');
    }
  }
  
  return result.data;
}
```

---

## 3. Pedidos con Cantidades

### 3.1 Formato del Request (NUEVO - Recomendado)

**Endpoint:** `POST /api/v1/orders`

**Formato con Cantidades:**

```json
{
  "totalAmount": 2599980.00,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 5,
      "quantity": 3
    }
  ]
}
```

### 3.2 Implementación Completa

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
    quantity: item.quantity  // ← Cantidad específica de cada producto
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
      // Manejar errores
      if (result.message.includes('agotado') || result.message.includes('agotados')) {
        throw new Error('Uno o más productos están agotados. Por favor, elimínalos del carrito.');
      } else if (result.message.includes('Stock insuficiente')) {
        throw new Error(result.message);
      } else {
        throw new Error(result.message || 'Error al crear el pedido');
      }
    }
    
    return result.data;
  } catch (error) {
    console.error('Error al crear pedido:', error);
    throw error;
  }
}
```

### 3.3 Componente de Carrito con Cantidades

```jsx
// components/Cart.jsx
import React, { useState } from 'react';
import { createOrderWithQuantities } from '../services/orderService';

function Cart({ cartItems, onUpdateQuantity, onRemoveItem }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Calcular total
  const totalAmount = cartItems.reduce((sum, item) => {
    return sum + (item.price * item.quantity);
  }, 0);
  
  const handleCheckout = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // Validación previa (opcional, mejora UX)
      for (const item of cartItems) {
        if (item.stock < item.quantity) {
          setError(`No hay suficiente stock para ${item.name}. Stock disponible: ${item.stock}`);
          setLoading(false);
          return;
        }
      }
      
      // Crear pedido con cantidades
      const order = await createOrderWithQuantities(cartItems, totalAmount);
      
      // Éxito
      alert('Pedido creado exitosamente!');
      // Limpiar carrito o redirigir
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="cart">
      <h2>Carrito de Compras</h2>
      
      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}
      
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

### 3.4 Hook para Manejar Carrito

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
  
  const clearCart = () => {
    setCartItems([]);
  };
  
  const getTotal = () => {
    return cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  };
  
  return {
    cartItems,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    getTotal
  };
}
```

---

## 4. Manejo de Errores

### 4.1 Errores Comunes del Backend

| Código | Situación | Mensaje del Backend |
|--------|-----------|---------------------|
| **400** | Producto agotado | "No se puede completar la compra: uno o más productos están agotados..." |
| **400** | Stock insuficiente | "Stock insuficiente para uno o más productos. Solo hay X unidad(es) disponible(s)..." |
| **400** | Producto no encontrado | "Uno o más productos no fueron encontrados" |
| **401** | Token inválido/expirado | "Token inválido o expirado" |
| **403** | Token no proporcionado | "Token no proporcionado" |
| **404** | Recurso no encontrado | "Producto no encontrado con ID: X" |

### 4.2 Función Helper para Manejar Errores

```javascript
// utils/errorHandler.js
export function handleApiError(error, result) {
  if (!result || !result.ok) {
    const message = result?.message || error.message || 'Error desconocido';
    const statusCode = result?.statusCode || 500;
    
    // Errores de stock
    if (message.includes('agotado') || message.includes('agotados')) {
      return {
        type: 'out_of_stock',
        message: 'Uno o más productos están agotados. Por favor, elimínalos del carrito.',
        userFriendly: 'Algunos productos ya no están disponibles. Por favor, actualiza tu carrito.'
      };
    }
    
    if (message.includes('Stock insuficiente')) {
      return {
        type: 'insufficient_stock',
        message: message,
        userFriendly: message
      };
    }
    
    // Errores de autenticación
    if (statusCode === 401) {
      return {
        type: 'unauthorized',
        message: 'Tu sesión ha expirado. Por favor, inicia sesión de nuevo.',
        userFriendly: 'Sesión expirada. Redirigiendo al login...'
      };
    }
    
    if (statusCode === 403) {
      return {
        type: 'forbidden',
        message: 'No tienes permisos para realizar esta acción.',
        userFriendly: 'Acceso denegado.'
      };
    }
    
    // Error genérico
    return {
      type: 'generic',
      message: message,
      userFriendly: 'Ocurrió un error. Por favor, intenta de nuevo.'
    };
  }
  
  return null;
}
```

### 4.3 Uso del Error Handler

```javascript
// En un componente
import { handleApiError } from '../utils/errorHandler';

async function handleCheckout() {
  try {
    const order = await createOrderWithQuantities(cartItems, totalAmount);
    showSuccess('Pedido creado exitosamente');
  } catch (error) {
    const errorInfo = handleApiError(error, error.response?.data);
    
    if (errorInfo) {
      // Mostrar mensaje amigable al usuario
      showError(errorInfo.userFriendly);
      
      // Si es error de autenticación, redirigir a login
      if (errorInfo.type === 'unauthorized') {
        setTimeout(() => {
          window.location.href = '/login';
        }, 2000);
      }
      
      // Si es error de stock, actualizar carrito
      if (errorInfo.type === 'out_of_stock' || errorInfo.type === 'insufficient_stock') {
        // Refrescar productos del carrito para actualizar stock
        refreshCartItems();
      }
    }
  }
}
```

---

## 📋 Checklist de Implementación

### Validación de Sesión
- [ ] Crear función `validateSession()` que llame a `GET /api/v1/auth/validate`
- [ ] Validar sesión al cargar la aplicación
- [ ] Guardar datos del usuario si el token es válido
- [ ] Redirigir a login si el token es inválido

### Obtener Todos los Productos (PC Builder)
- [ ] Crear función `getAllProducts()` que llame a `GET /api/v1/products`
- [ ] Incluir token JWT en el header Authorization
- [ ] Manejar error 401 (redirigir a login si token expirado)
- [ ] Mostrar todos los productos en el PC Builder
- [ ] Implementar filtros por categoría
- [ ] Implementar búsqueda de productos

### Actualizar Productos (Admin)
- [ ] Crear función `updateProduct()` que llame a `PUT /api/v1/products/{id}`
- [ ] Incluir token JWT en el header Authorization
- [ ] Manejar error 401 (redirigir a login si token expirado)
- [ ] Manejar error 403 (sin permisos de ADMIN)
- [ ] Implementar formulario de edición de productos
- [ ] Implementar funcionalidad para poner/quitar productos en oferta
- [ ] Validar campos antes de enviar (precio >= 0, stock >= 0, discount 0-100)

### Validación de Stock
- [ ] Verificar campo `stock` al cargar productos
- [ ] Mostrar badge "Agotado" cuando `stock === 0`
- [ ] Mostrar badge "Últimas X unidades" cuando `stock <= 5`
- [ ] Deshabilitar botón de compra cuando `stock === 0`
- [ ] Limitar cantidad máxima al stock disponible

### Pedidos con Cantidades
- [ ] Implementar selector de cantidad en carrito
- [ ] Enviar pedidos con formato `items` (no solo `productIds`)
- [ ] Incluir `quantity` para cada producto
- [ ] Validar que `quantity <= stock` antes de enviar
- [ ] Calcular total considerando cantidades

### Manejo de Errores
- [ ] Manejar error 400 (producto agotado)
- [ ] Manejar error 400 (stock insuficiente)
- [ ] Manejar error 401 (token inválido) → Redirigir a login
- [ ] Manejar error 403 (sin permisos)
- [ ] Mostrar mensajes de error claros al usuario

---

## 🔗 URLs de los Servicios

| Servicio | Puerto | URL Base |
|----------|--------|----------|
| Usuarios (Auth) | 8081 | `http://localhost:8081/api/v1` |
| Productos | 8082 | `http://localhost:8082/api/v1` |
| Pedidos | 8083 | `http://localhost:8083/api/v1` |
| Calificaciones | 8084 | `http://localhost:8084/api/v1` |

---

## 📝 Ejemplo Completo de Flujo

```javascript
// 1. Validar sesión al cargar app
const user = await validateSession();
if (user) {
  setUser(user);
}

// 2. Cargar productos
const products = await fetchProducts();
// products[0].stock = 5

// 3. Usuario agrega 3 unidades al carrito
addToCart(products[0], 3);

// 4. Usuario hace checkout
const order = await createOrderWithQuantities([
  { id: 1, quantity: 3, price: 899990.00 }
], 2699970.00);

// 5. Backend valida: stock (5) >= quantity (3) ✅
// 6. Backend reduce stock: 5 - 3 = 2 ✅
// 7. Backend crea pedido ✅

// 8. Si otro usuario intenta comprar 3 unidades:
// Backend valida: stock (2) < quantity (3) ❌
// Backend retorna error 400: "Stock insuficiente"
```

---

## 🎯 Resumen Rápido

### Lo que DEBES implementar:

1. ✅ **Validación de sesión** - Llamar a `/auth/validate` al cargar la app
2. ✅ **Mostrar estado de stock** - Badges y botones deshabilitados cuando `stock === 0`
3. ✅ **Enviar cantidades** - Usar formato `items` con `quantity` en lugar de solo `productIds`
4. ✅ **Manejar errores** - Mostrar mensajes claros cuando el backend rechaza la compra

### Lo que el Backend ya hace:

- ✅ Valida stock automáticamente
- ✅ Bloquea compras de productos agotados
- ✅ Descuenta la cantidad exacta comprada
- ✅ Retorna errores claros y descriptivos

---

## 📚 Documentación Adicional

Para más detalles, consulta:
- `FORMATO_PEDIDOS_CON_CANTIDADES.md` - Guía detallada de pedidos con cantidades
- `GUIA_FRONTEND_VALIDACION_STOCK.md` - Guía detallada de validación de stock
- `VALIDACION_STOCK.md` - Documentación técnica de validación

---

---

## 6. Actualizar Productos (Admin)

### Endpoint: `PUT /api/v1/products/{id}`

**¿Para qué sirve?**  
Permite a los administradores actualizar la información de un producto existente, incluyendo poner productos en oferta.

**⚠️ IMPORTANTE:** Este endpoint requiere autenticación JWT con rol ADMIN.

### Implementación

```javascript
// services/adminService.js o productService.js
const API_BASE_URL = 'http://localhost:8082/api/v1';

export async function updateProduct(productId, productData) {
  const token = localStorage.getItem('token');
  
  if (!token) {
    throw new Error('No estás autenticado. Por favor, inicia sesión.');
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(productData)
    });
    
    const result = await response.json();
    
    if (!result.ok) {
      if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
        throw new Error('Sesión expirada. Por favor, inicia sesión de nuevo.');
      }
      throw new Error(result.message || 'Error al actualizar producto');
    }
    
    return result.data;
  } catch (error) {
    console.error('Error al actualizar producto:', error);
    throw error;
  }
}
```

### Ejemplo: Actualizar Producto con Oferta

```javascript
// Poner un producto en oferta
async function putProductOnSale(productId) {
  try {
    const productData = {
      isOnSale: true,
      discount: 20, // 20% de descuento
      offerStartDate: "2024-01-01",
      offerEndDate: "2024-12-31"
    };
    
    const updatedProduct = await updateProduct(productId, productData);
    console.log('Producto actualizado:', updatedProduct);
    return updatedProduct;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// O usando el objeto 'offer' (formato alternativo)
async function putProductOnSaleWithOffer(productId) {
  try {
    const productData = {
      offer: {
        discount: 25,
        startDate: "2024-06-01",
        endDate: "2024-06-30"
      }
    };
    
    const updatedProduct = await updateProduct(productId, productData);
    return updatedProduct;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Actualizar solo algunos campos
async function updateProductPrice(productId, newPrice) {
  try {
    const productData = {
      price: newPrice
    };
    
    const updatedProduct = await updateProduct(productId, productData);
    return updatedProduct;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}

// Quitar producto de oferta
async function removeProductFromSale(productId) {
  try {
    const productData = {
      isOnSale: false,
      discount: 0,
      offerStartDate: null,
      offerEndDate: null
    };
    
    const updatedProduct = await updateProduct(productId, productData);
    return updatedProduct;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}
```

### Componente de Edición de Producto

```jsx
// components/AdminProductEdit.jsx
import React, { useState, useEffect } from 'react';
import { updateProduct } from '../services/adminService';

function AdminProductEdit({ productId, onSuccess, onCancel }) {
  const [product, setProduct] = useState({
    name: '',
    brand: '',
    model: '',
    category: '',
    price: 0,
    stock: 0,
    description: '',
    image: '',
    isOnSale: false,
    discount: 0,
    offerStartDate: '',
    offerEndDate: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    
    try {
      // Preparar datos para enviar (solo campos que han cambiado)
      const productData = {
        name: product.name,
        brand: product.brand,
        model: product.model,
        category: product.category,
        price: product.price,
        stock: product.stock,
        description: product.description,
        image: product.image,
        isOnSale: product.isOnSale,
        discount: product.discount,
        offerStartDate: product.offerStartDate || null,
        offerEndDate: product.offerEndDate || null
      };
      
      const updatedProduct = await updateProduct(productId, productData);
      onSuccess(updatedProduct);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <form onSubmit={handleSubmit} className="product-edit-form">
      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}
      
      <div className="form-group">
        <label>Nombre:</label>
        <input
          type="text"
          value={product.name}
          onChange={(e) => setProduct({...product, name: e.target.value})}
          required
        />
      </div>
      
      <div className="form-group">
        <label>Precio:</label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={product.price}
          onChange={(e) => setProduct({...product, price: parseFloat(e.target.value)})}
          required
        />
      </div>
      
      <div className="form-group">
        <label>Stock:</label>
        <input
          type="number"
          min="0"
          value={product.stock}
          onChange={(e) => setProduct({...product, stock: parseInt(e.target.value)})}
          required
        />
      </div>
      
      {/* Campos de Oferta */}
      <div className="form-group">
        <label>
          <input
            type="checkbox"
            checked={product.isOnSale}
            onChange={(e) => setProduct({...product, isOnSale: e.target.checked})}
          />
          Producto en oferta
        </label>
      </div>
      
      {product.isOnSale && (
        <>
          <div className="form-group">
            <label>Descuento (%):</label>
            <input
              type="number"
              min="0"
              max="100"
              value={product.discount}
              onChange={(e) => setProduct({...product, discount: parseInt(e.target.value)})}
              required
            />
          </div>
          
          <div className="form-group">
            <label>Fecha de inicio:</label>
            <input
              type="date"
              value={product.offerStartDate}
              onChange={(e) => setProduct({...product, offerStartDate: e.target.value})}
            />
          </div>
          
          <div className="form-group">
            <label>Fecha de fin:</label>
            <input
              type="date"
              value={product.offerEndDate}
              onChange={(e) => setProduct({...product, offerEndDate: e.target.value})}
            />
          </div>
        </>
      )}
      
      <div className="form-actions">
        <button type="button" onClick={onCancel} disabled={loading}>
          Cancelar
        </button>
        <button type="submit" disabled={loading}>
          {loading ? 'Guardando...' : 'Guardar Cambios'}
        </button>
      </div>
    </form>
  );
}

export default AdminProductEdit;
```

### Respuestas del Backend

**✅ Éxito (200):**
```json
{
  "ok": true,
  "statusCode": 200,
  "message": "Producto actualizado",
  "data": {
    "id": 1,
    "name": "GeForce RTX 4070",
    "price": 799.99,
    "stock": 15,
    "isOnSale": true,
    "discount": 20,
    "offerStartDate": "2024-01-01",
    "offerEndDate": "2024-12-31"
  },
  "count": 1
}
```

**❌ Error de Validación (400):**
```json
{
  "ok": false,
  "statusCode": 400,
  "message": "El precio no puede ser negativo",
  "data": null,
  "count": 0
}
```

**❌ No Autenticado (401):**
```json
{
  "ok": false,
  "statusCode": 401,
  "message": "Token inválido o expirado",
  "data": null,
  "count": 0
}
```

**❌ Producto No Encontrado (404):**
```json
{
  "ok": false,
  "statusCode": 404,
  "message": "Producto no encontrado con ID: 999",
  "data": null,
  "count": 0
}
```

### Notas Importantes

1. **Campos Opcionales**: Todos los campos son opcionales en el PUT. Solo se actualizan los campos que se envían.
2. **Ofertas**: Para poner un producto en oferta, debes enviar `isOnSale: true` y un `discount` mayor a 0.
3. **Formato de Fechas**: Las fechas deben estar en formato `YYYY-MM-DD`.
4. **Objeto Offer**: También puedes usar el formato `offer: {discount, startDate, endDate}` que se mapea automáticamente.

---

## 🚀 ¡Listo para Implementar!

El backend está completamente funcional. Solo necesitas implementar estas mejoras en el frontend para una mejor experiencia de usuario. 🎉
