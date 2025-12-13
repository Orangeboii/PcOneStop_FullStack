# 👥 Usuarios Precargados

Este documento describe los usuarios que se cargan automáticamente al iniciar el servicio de Usuarios.

## 🚀 Carga Automática

Los usuarios se cargan automáticamente al iniciar la aplicación cuando:
- La aplicación se inicia por primera vez
- Los usuarios no existen en la base de datos
- La propiedad `app.data.initializer.enabled=true` está activa (por defecto)

**No necesitas hacer nada manualmente** - solo inicia el servicio de Usuarios y los usuarios se crearán automáticamente.

## 📋 Usuarios Precargados

### 1. Usuario ADMIN

- **Email**: `admin@pconestop.com`
- **Contraseña**: `admin123`
- **Rol**: `ADMIN`
- **Nombre**: Administrador
- **Apellido**: PcOneStop

**Permisos**: Acceso completo al sistema, puede gestionar productos, ver todos los pedidos, etc.

### 2. Usuario CLIENTE

- **Email**: `cliente@pconestop.com`
- **Contraseña**: `cliente123`
- **Rol**: `CLIENTE`
- **Nombre**: Juan
- **Apellido**: Pérez

**Permisos**: Puede ver productos, realizar compras, ver su historial de pedidos, etc.

## 🔐 Credenciales de Acceso

### Para Login (API o Swagger)

**Admin:**
```json
{
  "email": "admin@pconestop.com",
  "password": "admin123"
}
```

**Cliente:**
```json
{
  "email": "cliente@pconestop.com",
  "password": "cliente123"
}
```

## ⚙️ Configuración

### Deshabilitar la carga automática

Si no deseas que se carguen usuarios automáticamente, edita `application.properties`:

```properties
app.data.initializer.enabled=false
```

### Cambiar las credenciales

Si deseas cambiar las credenciales de los usuarios precargados, edita el archivo:
`src/main/java/com/Gestion/Usuarios/config/DataInitializer.java`

## 🔒 Seguridad

- Las contraseñas se encriptan automáticamente usando BCrypt antes de guardarse
- Las contraseñas en texto plano (`admin123`, `cliente123`) solo se usan durante la creación
- Una vez guardados, las contraseñas encriptadas no se pueden revertir a texto plano

## 📝 Notas Importantes

1. **No duplicados**: El sistema verifica si los usuarios ya existen antes de crearlos
2. **Encriptación automática**: Las contraseñas se encriptan usando `UserService.save()`
3. **Solo se crean si no existen**: Si los usuarios ya están en la base de datos, no se duplican

## 🧪 Probar los Usuarios

### Usando Swagger

1. Abre Swagger: `http://localhost:8081/swagger-ui.html`
2. Busca el endpoint `/api/v1/auth/login`
3. Prueba con las credenciales del admin o cliente
4. Copia el token JWT de la respuesta
5. Usa el botón "Authorize" en Swagger para autenticarte

### Usando cURL

```bash
# Login como Admin
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pconestop.com","password":"admin123"}'

# Login como Cliente
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@pconestop.com","password":"cliente123"}'
```

## 🔄 Reiniciar Usuarios

Si necesitas eliminar y recrear los usuarios precargados:

```sql
-- Conectar a MySQL
mysql -u root -p

-- Seleccionar base de datos
USE db_usuarios;

-- Eliminar usuarios precargados
DELETE FROM users WHERE email IN ('admin@pconestop.com', 'cliente@pconestop.com');
```

Luego reinicia el servicio y los usuarios se recrearán automáticamente.

## ✅ Verificación

Después de iniciar el servicio, verifica en los logs:

```
=== VERIFICANDO USUARIOS PRECARGADOS ===
=== CREANDO USUARIO ADMIN PRECARGADO ===
✅ Usuario ADMIN creado: admin@pconestop.com / admin123
=== CREANDO USUARIO CLIENTE PRECARGADO ===
✅ Usuario CLIENTE creado: cliente@pconestop.com / cliente123
=== 2 USUARIOS PRECARGADOS CREADOS EXITOSAMENTE ===
```
