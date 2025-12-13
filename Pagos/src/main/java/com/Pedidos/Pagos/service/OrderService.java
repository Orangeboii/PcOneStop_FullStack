package com.Pedidos.Pagos.service;

import com.Pedidos.Pagos.dto.OrderItemDTO;
import com.Pedidos.Pagos.dto.OrderRequestDTO;
import com.Pedidos.Pagos.dto.ProductDTO;
import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClientService productClientService;

    public Order createOrder(Order order) {
        logger.info("=== INICIO CREAR PEDIDO EN SERVICE ===");
        
        // IMPORTANTE: Reducir stock ANTES de guardar el pedido
        // Esto previene condiciones de carrera donde dos usuarios compran el mismo producto
        // La reducción de stock valida automáticamente que haya stock suficiente
        
        // Convertir productIds a lista de items (formato antiguo: "1,2,3" → items con cantidad 1)
        List<OrderItemDTO> items = parseOrderItems(order.getProductIds());
        
        if (!items.isEmpty()) {
            // Validar que los productos existan y tengan stock suficiente
            validateProductsAndStock(items);
            
            // Reducir stock ANTES de guardar el pedido
            // reduceStockForItems valida el stock y lo reduce de forma atómica
            // Si falla aquí (stock insuficiente), lanza excepción y hace rollback automático
            reduceStockForItems(items);
        }
        
        // Solo si la reducción de stock fue exitosa, guardar el pedido
        // Lógica de negocio: Al crear, el estado inicial siempre es PENDIENTE
        order.setStatus("PENDIENTE");
        
        // Guardar el pedido
        Order savedOrder = orderRepository.save(order);
        logger.info("Pedido guardado con ID: {}", savedOrder.getId());
        
        return savedOrder;
    }
    
    /**
     * Crea un Order desde un OrderRequestDTO (puede tener items con cantidades)
     */
    public Order createOrderFromRequest(OrderRequestDTO requestDTO) {
        logger.info("=== INICIO CREAR PEDIDO DESDE REQUEST DTO ===");
        
        Order order = new Order();
        order.setUserId(requestDTO.getUserId());
        order.setTotalAmount(requestDTO.getTotalAmount());
        
        // Si tiene items (formato nuevo), procesar con cantidades
        if (requestDTO.hasItems()) {
            List<OrderItemDTO> items = requestDTO.getItems();
            
            // Validar que los productos existan y tengan stock suficiente
            validateProductsAndStock(items);
            
            // Reducir stock ANTES de guardar el pedido
            reduceStockForItems(items);
            
            // Convertir items a productIds para almacenar (formato: "1,5,8")
            String productIds = items.stream()
                    .map(item -> item.getProductId().toString())
                    .collect(Collectors.joining(","));
            order.setProductIds(productIds);
        } else {
            // Formato antiguo: usar productIds directamente (cantidad 1 por producto)
            order.setProductIds(requestDTO.getProductIds());
        }
        
        // Lógica de negocio: Al crear, el estado inicial siempre es PENDIENTE
        order.setStatus("PENDIENTE");
        
        // Guardar el pedido
        Order savedOrder = orderRepository.save(order);
        logger.info("Pedido guardado con ID: {}", savedOrder.getId());
        
        return savedOrder;
    }
    
    /**
     * Convierte productIds (string) a lista de items con cantidad 1 cada uno
     * @param productIdsString String con IDs separados por comas (ej: "1,2,3")
     * @return Lista de OrderItemDTO con cantidad 1
     */
    private List<OrderItemDTO> parseOrderItems(String productIdsString) {
        List<OrderItemDTO> items = new ArrayList<>();
        if (productIdsString != null && !productIdsString.trim().isEmpty()) {
            List<Long> productIds = parseProductIds(productIdsString);
            for (Long productId : productIds) {
                items.add(new OrderItemDTO(productId, 1, null)); // Cantidad 1 por defecto
            }
        }
        return items;
    }
    
    /**
     * Valida que todos los productos existan y tengan stock suficiente
     * @param items Lista de items con productId y quantity
     * @throws RuntimeException si algún producto no existe o no tiene stock suficiente
     */
    private void validateProductsAndStock(List<OrderItemDTO> items) {
        logger.info("Validando productos y stock para {} items", items.size());
        
        List<Long> missingProducts = new ArrayList<>();
        List<String> stockErrors = new ArrayList<>();
        
        for (OrderItemDTO item : items) {
            Long productId = item.getProductId();
            Integer requestedQuantity = item.getQuantity() != null ? item.getQuantity() : 1;
            
            ProductDTO product = productClientService.getProductById(productId);
            
            if (product == null) {
                missingProducts.add(productId);
                logger.warn("Producto ID: {} no encontrado", productId);
            } else {
                int availableStock = product.getStock() != null ? product.getStock() : 0;
                
                // Validar stock
                if (availableStock <= 0) {
                    stockErrors.add("Producto agotado: " + product.getName() + ". No hay unidades disponibles.");
                    logger.warn("Producto agotado - ID: {} - Nombre: {}", productId, product.getName());
                } else if (availableStock < requestedQuantity) {
                    stockErrors.add("Stock insuficiente para " + product.getName() + 
                            ". Solo hay " + availableStock + " unidad(es) disponible(s) (solicitaste " + requestedQuantity + ")");
                    logger.warn("Stock insuficiente para producto ID: {} - Disponible: {}, Solicitado: {}", 
                            productId, availableStock, requestedQuantity);
                }
            }
        }
        
        // Lanzar excepciones si hay errores
        if (!missingProducts.isEmpty()) {
            throw new RuntimeException("Uno o más productos no fueron encontrados: " + missingProducts);
        }
        
        if (!stockErrors.isEmpty()) {
            String errorMessage = stockErrors.size() == 1 
                    ? stockErrors.get(0)
                    : "Problemas con el stock: " + String.join(" ", stockErrors);
            throw new RuntimeException(errorMessage);
        }
        
        logger.info("Validación de productos y stock exitosa");
    }


    /**
     * Reduce el stock de los productos ANTES de crear el pedido
     * IMPORTANTE: Este método valida y reduce el stock de forma atómica
     * Si falla, lanza excepción y hace rollback de toda la transacción
     * @param items Lista de items con productId y quantity
     * @throws RuntimeException si no se puede reducir el stock (producto agotado, no existe, etc.)
     */
    private void reduceStockForItems(List<OrderItemDTO> items) {
        logger.info("Reduciendo stock para {} items", items.size());
        
        // Reducir stock para cada item con su cantidad específica
        for (OrderItemDTO item : items) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity() != null ? item.getQuantity() : 1;
            
            try {
                // IMPORTANTE: reduceProductStock valida el stock antes de reducirlo
                // Si el stock es insuficiente, lanza RuntimeException
                boolean success = productClientService.reduceProductStock(productId, quantity);
                
                if (!success) {
                    // Si no se pudo reducir (aunque no lanzó excepción), obtener info del producto
                    ProductDTO product = productClientService.getProductById(productId);
                    String productName = product != null ? product.getName() : "ID " + productId;
                    throw new RuntimeException("No se pudo reducir el stock del producto: " + productName + 
                            ". El producto puede estar agotado o no hay suficiente stock.");
                }
                
                logger.info("Stock reducido exitosamente para producto ID: {} (cantidad: {})", productId, quantity);
            } catch (RuntimeException e) {
                // Re-lanzar RuntimeException para que se propague y haga rollback
                logger.error("Error al reducir stock del producto ID: {} (cantidad: {}) - {}", 
                        productId, quantity, e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Error inesperado al reducir stock del producto ID: {} (cantidad: {}) - {}", 
                        productId, quantity, e.getMessage(), e);
                // Si falla la reducción de stock, lanzar excepción para hacer rollback de la transacción
                throw new RuntimeException("Error al reducir stock del producto ID: " + productId + 
                        " (cantidad: " + quantity + ") - " + e.getMessage());
            }
        }
        
        logger.info("Stock reducido exitosamente para todos los productos");
    }

    /**
     * Parsea un string de IDs separados por comas en una lista de Long
     * @param productIdsString String con IDs separados por comas (ej: "1,2,3")
     * @return Lista de IDs
     */
    private List<Long> parseProductIds(String productIdsString) {
        List<Long> productIds = new ArrayList<>();
        if (productIdsString != null && !productIdsString.trim().isEmpty()) {
            String[] ids = productIdsString.split(",");
            for (String id : ids) {
                try {
                    Long productId = Long.parseLong(id.trim());
                    productIds.add(productId);
                } catch (NumberFormatException e) {
                    logger.warn("ID de producto inválido: {}", id);
                }
            }
        }
        return productIds;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public Order updateStatus(Long id, String newStatus) {
        Order order = findById(id); // Busca el pedido
        order.setStatus(newStatus); // Cambia el estado
        return orderRepository.save(order); // Guarda en MySQL
    }
}