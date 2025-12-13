package com.Catalogo.Inventario.service;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository; // Importar esto
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReportRepository reportRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // Lógica para descontar stock
    // IMPORTANTE: Valida y reduce el stock de forma atómica dentro de una transacción
    public Product reduceStock(Long id, Integer quantity) {
        Product product = findById(id);
        
        int currentStock = product.getStock() != null ? product.getStock() : 0;

        // Validar que el stock sea suficiente
        if (currentStock <= 0) {
            throw new RuntimeException("Producto agotado: " + product.getName() + ". No hay unidades disponibles.");
        }
        
        if (currentStock < quantity) {
            throw new RuntimeException("Stock insuficiente para el producto: " + product.getName() + ". Solo hay " + currentStock + " unidad(es) disponible(s) (solicitaste " + quantity + ")");
        }

        // Reducir el stock
        product.setStock(currentStock - quantity);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        // 1. Primero borramos los reportes asociados (para evitar error de Foreign Key)
        reportRepository.deleteByProductId(id);
        
        // 2. Ahora sí, borramos el producto
        productRepository.deleteById(id);
    }

    public List<Product> findOnSaleProducts() {
        return productRepository.findByIsOnSaleTrue();
    }
    
    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param updatedProduct Producto con los datos actualizados
     * @return Producto actualizado
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = findById(id);
        
        // Actualizar campos (mantener el ID y solo actualizar si no son null)
        if (updatedProduct.getName() != null && !updatedProduct.getName().trim().isEmpty()) {
            existingProduct.setName(updatedProduct.getName().trim());
        }
        if (updatedProduct.getBrand() != null && !updatedProduct.getBrand().trim().isEmpty()) {
            existingProduct.setBrand(updatedProduct.getBrand().trim());
        }
        if (updatedProduct.getModel() != null && !updatedProduct.getModel().trim().isEmpty()) {
            existingProduct.setModel(updatedProduct.getModel().trim());
        }
        if (updatedProduct.getCategory() != null && !updatedProduct.getCategory().trim().isEmpty()) {
            existingProduct.setCategory(updatedProduct.getCategory().trim());
        }
        if (updatedProduct.getPrice() != null) {
            existingProduct.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getStock() != null) {
            existingProduct.setStock(updatedProduct.getStock());
        }
        // Descripción puede ser null o vacía
        existingProduct.setDescription(updatedProduct.getDescription());
        // Imagen puede ser null
        existingProduct.setImage(updatedProduct.getImage());
        
        // Campos de oferta - permitir actualizar incluso si son null (para desactivar ofertas)
        if (updatedProduct.getIsOnSale() != null) {
            existingProduct.setIsOnSale(updatedProduct.getIsOnSale());
        }
        if (updatedProduct.getDiscount() != null) {
            existingProduct.setDiscount(updatedProduct.getDiscount());
        } else if (updatedProduct.getIsOnSale() != null && !updatedProduct.getIsOnSale()) {
            // Si se desactiva la oferta, resetear descuento a 0
            existingProduct.setDiscount(0);
        }
        // Fechas de oferta pueden ser null (para limpiar ofertas)
        existingProduct.setOfferStartDate(updatedProduct.getOfferStartDate());
        existingProduct.setOfferEndDate(updatedProduct.getOfferEndDate());
        
        // Validar que si isOnSale es true, haya un descuento válido
        if (existingProduct.getIsOnSale() != null && existingProduct.getIsOnSale() && 
            (existingProduct.getDiscount() == null || existingProduct.getDiscount() <= 0)) {
            throw new RuntimeException("No se puede activar una oferta sin un descuento válido (mayor a 0)");
        }
        
        return productRepository.save(existingProduct);
    }
}