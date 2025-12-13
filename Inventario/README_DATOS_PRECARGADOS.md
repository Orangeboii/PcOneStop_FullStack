# 📦 Datos Precargados de Productos

Este directorio contiene scripts para precargar productos en la base de datos con stock máximo de 10 unidades.

## 🚀 Carga Automática (Recomendado)

**¡Los productos se cargan automáticamente al iniciar la aplicación!**

El componente `DataInitializer` carga automáticamente 40 productos cuando:
- La aplicación se inicia por primera vez
- La base de datos está vacía (no hay productos)
- La propiedad `app.data.initializer.enabled=true` está activa (por defecto)

**No necesitas hacer nada manualmente** - solo inicia el servicio de Inventario y los productos se cargarán automáticamente.

### Deshabilitar la carga automática

Si no deseas que se carguen productos automáticamente, edita `application.properties`:

```properties
app.data.initializer.enabled=false
```

## 📋 Archivos Disponibles

1. **`DataInitializer.java`** - Componente que carga productos automáticamente (implementado en código)
2. **`data_precargados_productos.sql`** - Script SQL para insertar productos manualmente
3. **`data_precargados_productos.json`** - Archivo JSON con los productos para usar con la API REST

## 📊 Productos Incluidos

- **Total**: 40 productos
- **Stock máximo**: 10 unidades por producto
- **Categorías**:
  - 5 GPUs (Tarjetas gráficas)
  - 5 CPUs (Procesadores)
  - 5 RAM (Memoria)
  - 5 SSD (Almacenamiento)
  - 5 Motherboards (Placas base)
  - 5 PSU (Fuentes de poder)
  - 5 Cases (Gabinetes)
  - 5 Coolers (Refrigeración)
  - 5 Peripherals (Periféricos)

## 🗄️ Uso del Script SQL (Opcional)

### Opción 1: Ejecutar directamente en MySQL

```bash
# Conectar a MySQL
mysql -u root -p

# Seleccionar la base de datos
USE db_inventario;

# Ejecutar el script
SOURCE ruta/al/archivo/data_precargados_productos.sql;
```

### Opción 2: Desde la línea de comandos

```bash
mysql -u root -p db_inventario < data_precargados_productos.sql
```

### Opción 3: Usar un cliente MySQL (Workbench, DBeaver, etc.)

1. Abre el archivo `data_precargados_productos.sql`
2. Ejecuta el contenido completo

## 🔌 Uso del Archivo JSON con la API

### Requisitos previos

1. El servicio de Inventario debe estar corriendo en `http://localhost:8082`
2. Debes tener un token JWT válido con rol ADMIN

### Opción 1: Usando cURL

```bash
# Obtener token de autenticación primero
TOKEN="tu_token_jwt_aqui"

# Insertar productos uno por uno
curl -X POST http://localhost:8082/api/v1/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d @data_precargados_productos.json
```

### Opción 2: Usando un script Python

```python
import requests
import json

# Configuración
API_URL = "http://localhost:8082/api/v1/products"
TOKEN = "tu_token_jwt_aqui"
HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {TOKEN}"
}

# Cargar productos
with open('data_precargados_productos.json', 'r', encoding='utf-8') as f:
    products = json.load(f)

# Insertar cada producto
for product in products:
    response = requests.post(API_URL, json=product, headers=HEADERS)
    if response.status_code == 201:
        print(f"✅ Producto insertado: {product['name']}")
    else:
        print(f"❌ Error al insertar {product['name']}: {response.text}")
```

### Opción 3: Usando Postman o Insomnia

1. Importa el archivo JSON
2. Configura el endpoint: `POST http://localhost:8082/api/v1/products`
3. Agrega el header: `Authorization: Bearer <tu_token>`
4. Envía cada producto individualmente

## 📊 Estadísticas de los Datos Precargados

- **Total de productos**: 40
- **Stock máximo por producto**: 10 unidades
- **Rango de stock**: 0-10 unidades
- **Categorías incluidas**:
  - GPU: 5 productos
  - CPU: 5 productos
  - RAM: 5 productos
  - SSD: 5 productos
  - Motherboard: 5 productos
  - PSU: 5 productos
  - Case: 5 productos
  - Cooler: 5 productos
  - Peripheral: 5 productos

## ✅ Verificación

Después de insertar los datos, puedes verificar con estas consultas SQL:

```sql
-- Contar total de productos
SELECT COUNT(*) as total_productos FROM products;

-- Productos por categoría
SELECT category, COUNT(*) as cantidad, SUM(stock) as stock_total 
FROM products 
GROUP BY category 
ORDER BY category;

-- Productos con stock bajo (≤ 5)
SELECT name, category, stock 
FROM products 
WHERE stock <= 5 
ORDER BY stock ASC, category;

-- Productos con stock máximo (10)
SELECT name, category, stock 
FROM products 
WHERE stock = 10 
ORDER BY category;
```

## 🔄 Limpiar Datos (Opcional)

Si deseas eliminar todos los productos precargados:

```sql
DELETE FROM products;
-- O si quieres eliminar solo los precargados:
-- DELETE FROM products WHERE stock <= 10;
```

## 📝 Notas

- Todos los productos tienen `is_on_sale = false` por defecto
- Los precios están en pesos chilenos (CLP)
- Las descripciones están en español
- El stock varía entre 0 y 10 unidades para cada producto
- Los productos incluyen descripciones detalladas y especificaciones

## 🐛 Solución de Problemas

### Error: "Duplicate entry"
Si recibes un error de entrada duplicada, significa que algunos productos ya existen. Puedes:
1. Eliminar productos existentes primero
2. Modificar los nombres/modelos en el script
3. Usar `INSERT IGNORE` en lugar de `INSERT` (en MySQL)

### Error: "Table doesn't exist"
Asegúrate de que:
1. La base de datos `db_inventario` existe
2. El servicio de Inventario se haya ejecutado al menos una vez para crear las tablas
3. Estás conectado a la base de datos correcta

### Error 401 al usar la API
- Verifica que tengas un token JWT válido
- Asegúrate de que el token tenga rol ADMIN
- Verifica que el header `Authorization` esté correctamente formateado: `Bearer <token>`
