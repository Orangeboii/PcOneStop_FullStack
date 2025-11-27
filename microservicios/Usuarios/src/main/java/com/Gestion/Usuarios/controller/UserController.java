package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de cuenta")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody User user) {
        try {
            // Verificar si el email ya existe
            User existingUser = userService.findByEmail(user.getEmail());
            if (existingUser != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, 409, "El email ya está registrado", null, 0L));
            }
            
            User newUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Usuario registrado", newUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales por email y contraseña")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            
            User user = userService.findByEmail(email);
            
            // Email no encontrado
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "El email ingresado no existe.", null, 0L));
            }
            
            // Verificación de contraseña
            if (!userService.checkPassword(user, password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "Contraseña incorrecta.", null, 0L));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", user, 1L));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener todos los usuarios")
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Lista de usuarios", users, (long) users.size()));
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Usuario encontrado", user, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar usuario")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User userData) {
        try {
            User updatedUser = userService.update(id, userData);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Usuario actualizado", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar contraseña")
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<User>> updatePassword(
            @PathVariable String id,
            @RequestParam String newPassword
    ) {
        try {
            User updatedUser = userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Contraseña actualizada", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Eliminar usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Cuenta eliminada", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}
