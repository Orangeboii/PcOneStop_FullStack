package com.Pedidos.Pagos.service;

import com.Pedidos.Pagos.dto.ProductDTO;
import com.Pedidos.Pagos.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ProductClientService {

    private static final Logger logger = LoggerFactory.getLogger(ProductClientService.class);

    @Autowired
    private WebClient productServiceWebClient;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Obtiene un producto por su ID desde el servicio de Productos
     * @param productId ID del producto
     * @return ProductDTO si existe, null si no existe
     */
    @SuppressWarnings("unchecked")
    public ProductDTO getProductById(Long productId) {
        try {
            logger.info("Consultando producto ID: {} en servicio de Productos", productId);
            
            // GET de producto no requiere autenticación (endpoint público)
            Object responseObj = productServiceWebClient
                    .get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            
            if (responseObj != null) {
                // Convertir la respuesta a un mapa para extraer los datos
                java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) responseObj;
                Boolean ok = responseMap.get("ok") != null ? (Boolean) responseMap.get("ok") : false;
                Object dataObj = responseMap.get("data");
                
                if (ok && dataObj != null) {
                    // Convertir el objeto genérico a ProductDTO
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) dataObj;
                    ProductDTO product = new ProductDTO();
                    product.setId(map.get("id") != null ? Long.valueOf(map.get("id").toString()) : null);
                    product.setName((String) map.get("name"));
                    product.setBrand((String) map.get("brand"));
                    product.setModel((String) map.get("model"));
                    product.setCategory((String) map.get("category"));
                    product.setPrice(map.get("price") != null ? Double.valueOf(map.get("price").toString()) : null);
                    product.setStock(map.get("stock") != null ? Integer.valueOf(map.get("stock").toString()) : 0);
                    product.setDescription((String) map.get("description"));
                    product.setImage((String) map.get("image"));
                    product.setIsOnSale(map.get("isOnSale") != null ? Boolean.valueOf(map.get("isOnSale").toString()) : false);
                    product.setDiscount(map.get("discount") != null ? Integer.valueOf(map.get("discount").toString()) : null);
                    product.setOfferStartDate((String) map.get("offerStartDate"));
                    product.setOfferEndDate((String) map.get("offerEndDate"));
                    
                    logger.info("Producto encontrado: {} (Stock: {})", product.getName(), product.getStock());
                    return product;
                }
            }
            
            logger.warn("Producto ID: {} no encontrado o respuesta inválida", productId);
            return null;
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Producto ID: {} no encontrado (404)", productId);
            return null;
        } catch (Exception e) {
            logger.error("Error al consultar producto ID: {} - {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error al consultar el servicio de productos: " + e.getMessage());
        }
    }

    /**
     * Reduce el stock de un producto
     * @param productId ID del producto
     * @param quantity Cantidad a descontar
     * @return true si se pudo reducir, false en caso contrario
     */
    @SuppressWarnings("unchecked")
    public boolean reduceProductStock(Long productId, Integer quantity) {
        try {
            logger.info("Reduciendo stock del producto ID: {} en cantidad: {}", productId, quantity);
            
            // Generar token temporal para autenticación entre servicios
            // Usamos un usuario "system" con rol ADMIN para operaciones internas
            String systemToken = jwtUtil.generateToken("system@pconestop.com", "ADMIN", 0L);
            
            Object responseObj = productServiceWebClient
                    .put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/products/{id}/stock")
                            .queryParam("quantity", quantity)
                            .build(productId))
                    .header("Authorization", "Bearer " + systemToken)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            
            if (responseObj != null) {
                java.util.Map<String, Object> responseMap = (java.util.Map<String, Object>) responseObj;
                Boolean ok = responseMap.get("ok") != null ? (Boolean) responseMap.get("ok") : false;
                
                if (ok) {
                    logger.info("Stock reducido exitosamente para producto ID: {}", productId);
                    return true;
                }
            }
            
            logger.warn("No se pudo reducir el stock del producto ID: {}", productId);
            return false;
        } catch (WebClientResponseException.BadRequest e) {
            logger.error("Error al reducir stock: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Stock insuficiente para el producto ID: " + productId);
        } catch (Exception e) {
            logger.error("Error al reducir stock del producto ID: {} - {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error al reducir stock: " + e.getMessage());
        }
    }
}
