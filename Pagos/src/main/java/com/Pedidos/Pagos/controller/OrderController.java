package com.Pedidos.Pagos.controller;

import com.Pedidos.Pagos.dto.ApiResponse;
import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.service.OrderService;
import com.Pedidos.Pagos.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos y Compras", description = "Gestión de órdenes de compra de componentes PC. Los clientes pueden realizar pedidos y los administradores pueden gestionar el estado de los pedidos.")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Listar todos los pedidos",
        description = "Obtiene el listado completo de todos los pedidos realizados en PcOneStop, incluyendo información de cliente, monto total, " +
                     "estado del pedido (PENDIENTE, EN_PROCESO, ENVIADO, COMPLETADO, CANCELADO), productos comprados y fecha de creación. " +
                     "Requiere autenticación JWT con rol ADMIN. Útil para que los administradores gestionen todos los pedidos del sistema, " +
                     "realizar seguimiento de ventas y generar reportes."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de pedidos obtenida", orders, (long) orders.size()));
    }

    @Operation(
        summary = "Realizar una compra de componentes PC",
        description = "Crea un nuevo pedido en PcOneStop cuando un cliente compra componentes de computadora. " +
                     "El pedido se crea automáticamente en estado PENDIENTE. El userId se extrae automáticamente del token JWT si no se proporciona. " +
                     "Debe incluir el monto total (puede enviarse como 'total' o 'totalAmount') y los productos comprados. " +
                     "FORMATOS SOPORTADOS: " +
                     "(1) Formato antiguo: productIds separados por comas (ej: '1,5,8') - cantidad 1 por producto. " +
                     "(2) Formato nuevo: items con productId y quantity (ej: [{\"productId\":1,\"quantity\":2},{\"productId\":5,\"quantity\":1}]) - permite especificar cantidades. " +
                     "El sistema valida automáticamente: (1) que todos los productos existan, (2) que haya stock suficiente para la cantidad solicitada de cada producto. " +
                     "Si alguna validación falla, el pedido no se crea y se retorna un error descriptivo. " +
                     "Si todas las validaciones son exitosas, el stock se reduce automáticamente de forma transaccional con la cantidad exacta comprada. " +
                     "Requiere autenticación JWT. El token JWT debe incluirse en el header 'Authorization: Bearer <token>'."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pedido creado exitosamente. El cliente recibirá confirmación y el pedido quedará en estado PENDIENTE.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Pedido creado exitosamente\", \"data\": {\"id\": 1, \"userId\": 10, \"totalAmount\": 15000.00, \"status\": \"PENDIENTE\", \"productIds\": \"1,5,8\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: monto total faltante o inválido (debe ser mayor a cero), userId no se pudo extraer del token, stock insuficiente para uno o más productos, o datos incompletos",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Monto faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El monto total (total o totalAmount) es obligatorio y debe ser mayor a cero.\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Usuario faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"No se pudo determinar el ID del usuario para crear el pedido.\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Stock insuficiente", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"Stock insuficiente para uno o más productos. Solo hay 3 unidades disponibles (solicitaste 1) (Producto: GeForce RTX 4070).\", \"data\": null, \"count\": 0}")
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Uno o más productos no fueron encontrados",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 404, \"message\": \"Uno o más productos no fueron encontrados\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno al crear el pedido: error de base de datos, error al reducir stock, o problema al procesar la orden",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 500, \"message\": \"Error al crear pedido: Error al reducir stock del producto ID 5\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del pedido. El userId se extrae automáticamente del token JWT si no se proporciona. " +
                     "El campo totalAmount puede enviarse como 'total' o 'totalAmount'. El status se asigna automáticamente como PENDIENTE. " +
                     "Puedes usar dos formatos: (1) Formato antiguo con productIds (cantidad 1 por producto) o (2) Formato nuevo con items (permite especificar cantidades).",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Order.class),
            examples = {
                @ExampleObject(
                    name = "Formato antiguo - productIds",
                    summary = "Pedido con productIds (cantidad 1 por producto)",
                    value = "{\"totalAmount\":15999.99,\"productIds\":\"1,5,8\"}"
                ),
                @ExampleObject(
                    name = "Formato nuevo - items con cantidades",
                    summary = "Pedido con items y cantidades específicas (RECOMENDADO)",
                    value = "{\"totalAmount\":15999.99,\"items\":[{\"productId\":1,\"quantity\":2,\"price\":899990.00},{\"productId\":5,\"quantity\":1,\"price\":699990.00}]}"
                ),
                @ExampleObject(
                    name = "Pedido con total (alias)",
                    summary = "Usando el campo total en lugar de totalAmount",
                    value = "{\"total\":15999.99,\"productIds\":\"1,5,8\"}"
                )
            }
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR PEDIDO ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE OTROS ENDPOINTS)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR PEDIDO) ===");
                logger.info("Body recibido: {}", jsonBody);
            } catch (IOException e) {
                logger.error("ERROR al leer el body del request: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al leer el cuerpo de la petición: " + e.getMessage(), null, 0L));
            }
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("ERROR CRÍTICO: El JSON body está vacío o es NULL!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El cuerpo de la petición está vacío", null, 0L));
            }
            
            // Deserializar manualmente - Intentar primero con OrderRequestDTO (formato nuevo con items)
            // Si no tiene items, usar formato antiguo (productIds)
            Order order;
            try {
                // Intentar deserializar como OrderRequestDTO primero
                com.Pedidos.Pagos.dto.OrderRequestDTO requestDTO = objectMapper.readValue(jsonBody, com.Pedidos.Pagos.dto.OrderRequestDTO.class);
                logger.info("=== DESERIALIZACIÓN PEDIDO (OrderRequestDTO) ===");
                logger.info("RequestDTO deserializado: {}", requestDTO);
                logger.info("userId: {}", requestDTO.getUserId());
                logger.info("totalAmount: {}", requestDTO.getTotalAmount());
                logger.info("productIds: '{}'", requestDTO.getProductIds());
                logger.info("hasItems: {}", requestDTO.hasItems());
                if (requestDTO.hasItems()) {
                    logger.info("Items: {}", requestDTO.getItems());
                }
                
                // Si tiene items, usar el método que procesa cantidades
                if (requestDTO.hasItems()) {
                    // Extraer userId del token si no se proporciona
                    if (requestDTO.getUserId() == null) {
                        String authHeader = request.getHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            Long userId = jwtUtil.extractUserId(token);
                            if (userId != null) {
                                requestDTO.setUserId(userId);
                                logger.info("userId extraído del token JWT: {}", userId);
                            }
                        }
                    }
                    
                    // Validar que userId esté presente
                    if (requestDTO.getUserId() == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, 400, "El ID del usuario es obligatorio.", null, 0L));
                    }
                    
                    // Validar que totalAmount esté presente
                    if (requestDTO.getTotalAmount() == null || requestDTO.getTotalAmount() <= 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, 400, "El monto total (total o totalAmount) es obligatorio y debe ser mayor a cero.", null, 0L));
                    }
                    
                    // Crear pedido con items y cantidades
                    logger.info("Creando pedido con items y cantidades...");
                    Order newOrder = orderService.createOrderFromRequest(requestDTO);
                    logger.info("Pedido guardado con ID: {}", newOrder.getId());
                    logger.info("=== PEDIDO CREADO EXITOSAMENTE - ID: {} ===", newOrder.getId());
                    
                    ApiResponse<Order> response = new ApiResponse<>(
                            true, HttpStatus.CREATED.value(), "Pedido creado exitosamente", newOrder, 1L);
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                } else {
                    // Formato antiguo: convertir a Order
                    order = new Order();
                    order.setUserId(requestDTO.getUserId());
                    order.setTotalAmount(requestDTO.getTotalAmount());
                    order.setProductIds(requestDTO.getProductIds());
                }
            } catch (Exception e) {
                // Si falla, intentar deserializar como Order (formato antiguo)
                try {
                    order = objectMapper.readValue(jsonBody, Order.class);
                    logger.info("=== DESERIALIZACIÓN PEDIDO (Order - formato antiguo) ===");
                    logger.info("Order deserializado: {}", order);
                    logger.info("userId: {}", order.getUserId());
                    logger.info("totalAmount: {}", order.getTotalAmount());
                    logger.info("productIds: '{}'", order.getProductIds());
                } catch (Exception e2) {
                    logger.error("ERROR al deserializar JSON: {}", e2.getMessage(), e2);
                    logger.error("JSON que falló: {}", jsonBody);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e2.getMessage(), null, 0L));
                }
            }
            
            // Asegurar que el id sea null (se genera automáticamente)
            order.setId(null);
            
            // PRIMERO: Extraer userId del token JWT si no se proporciona (antes de validar)
            if (order.getUserId() == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    Long userId = jwtUtil.extractUserId(token);
                    if (userId != null) {
                        order.setUserId(userId);
                        logger.info("userId extraído del token JWT: {}", userId);
                    } else {
                        logger.warn("No se pudo extraer el userId del token para asignar al pedido.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, 400, "No se pudo determinar el ID del usuario para crear el pedido.", null, 0L));
                    }
                } else {
                    logger.warn("No se encontró token de autorización para asignar el userId.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ApiResponse<>(false, 401, "Se requiere token de autorización para crear un pedido.", null, 0L));
                }
            }
            
            // SEGUNDO: Validar que userId esté presente después de extraerlo del token
            if (order.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El ID del usuario es obligatorio.", null, 0L));
            }
            
            // TERCERO: Validar que totalAmount esté presente
            if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
                logger.warn("Intento de crear pedido sin totalAmount o con valor inválido: {}", order.getTotalAmount());
                logger.warn("JSON recibido: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El monto total (total o totalAmount) es obligatorio y debe ser mayor a cero.", null, 0L));
            }
            
            logger.info("Guardando pedido en la base de datos...");
            Order newOrder = orderService.createOrder(order);
            logger.info("Pedido guardado con ID: {}", newOrder.getId());
            logger.info("=== PEDIDO CREADO EXITOSAMENTE - ID: {} ===", newOrder.getId());
            
            ApiResponse<Order> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Pedido creado exitosamente", newOrder, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage();
            logger.error("=== ERROR AL CREAR PEDIDO ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", errorMessage);
            
            // Verificar si es un error de validación de stock o productos
            if (errorMessage != null) {
                if (errorMessage.contains("no fueron encontrados")) {
                    // Error 404: Productos no encontrados
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse<>(false, 404, "Uno o más productos no fueron encontrados", null, 0L));
                } else if (errorMessage.contains("agotados") || errorMessage.contains("agotado")) {
                    // Error 400: Producto agotado (stock = 0)
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(false, 400, errorMessage, null, 0L));
                } else if (errorMessage.contains("Stock insuficiente")) {
                    // Error 400: Stock insuficiente (stock > 0 pero menor a lo solicitado)
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse<>(false, 400, errorMessage, null, 0L));
                }
            }
            
            // Error genérico 500
            ApiResponse<Order> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al crear pedido: " + errorMessage, null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("=== ERROR AL CREAR PEDIDO ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            ApiResponse<Order> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al crear pedido: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Ver historial de compras de un cliente",
        description = "Obtiene todos los pedidos realizados por un cliente específico en PcOneStop. " +
                     "Muestra el historial completo de compras de componentes PC, incluyendo estado actual del pedido, monto total, " +
                     "IDs de productos comprados y fecha de creación. Requiere autenticación JWT. " +
                     "Útil para mostrar el historial de compras en el perfil del usuario o para que los clientes rastreen sus pedidos."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Historial de pedidos obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Pedidos encontrados\", \"data\": [{\"id\": 1, \"userId\": 10, \"totalAmount\": 15000.00, \"status\": \"COMPLETADO\"}], \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontraron pedidos para este usuario",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 404, \"message\": \"No se encontraron pedidos para este usuario\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "userId",
        description = "ID del cliente cuyo historial de compras se desea consultar",
        required = true,
        example = "10"
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        
        if (orders.isEmpty()) {
            ApiResponse<List<Order>> response = new ApiResponse<>(
                    false, HttpStatus.NOT_FOUND.value(), "No se encontraron pedidos para este usuario", null, 0L);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<List<Order>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Pedidos encontrados", orders, (long) orders.size());
        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Actualizar estado de un pedido",
        description = "Permite a los administradores cambiar el estado de un pedido en PcOneStop para reflejar el progreso del envío. " +
                     "Los estados posibles son: PENDIENTE (recién creado, esperando procesamiento), EN_PROCESO (en preparación para envío), " +
                     "ENVIADO (enviado al cliente, en tránsito), COMPLETADO (entregado exitosamente al cliente) o CANCELADO (pedido cancelado). " +
                     "Requiere autenticación JWT con rol ADMIN. Útil para gestionar el flujo de pedidos y notificar a los clientes sobre el estado de sus compras."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado del pedido actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Estado actualizado\", \"data\": {\"id\": 1, \"status\": \"ENVIADO\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado inválido o pedido no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al actualizar el estado",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del pedido cuyo estado se desea actualizar",
        required = true,
        example = "1"
    )
    @Parameter(
        name = "status",
        description = "Nuevo estado del pedido. Valores válidos: PENDIENTE (recién creado), EN_PROCESO (en preparación), ENVIADO (en tránsito), COMPLETADO (entregado), CANCELADO (cancelado)",
        required = true,
        example = "ENVIADO"
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status
    ) {
        try {
            Order updatedOrder = orderService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Estado actualizado", updatedOrder, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}