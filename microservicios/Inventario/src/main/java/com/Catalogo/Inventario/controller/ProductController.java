package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Inventario", description = "Gestión de catálogo y stock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true, 200, "No hay productos registrados", List.of(), 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos obtenidos", products, (long) products.size()));
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable String id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto encontrado", product, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener productos por categoría")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Product>>> getByCategory(@PathVariable String category) {
        List<Product> products = productService.findByCategory(category);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos por categoría", products, (long) products.size()));
    }

    @Operation(summary = "Obtener productos en oferta")
    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<List<Product>>> getOnSale() {
        List<Product> products = productService.findOnSale();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos en oferta", products, (long) products.size()));
    }

    @Operation(summary = "Crear nuevo producto")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@Valid @RequestBody Product product) {
        try {
            Product newProduct = productService.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Producto creado", newProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error: " + e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar producto existente")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(@PathVariable String id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Descontar Stock")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> reduceStock(@PathVariable String id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.reduceStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Stock actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto eliminado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }
}
