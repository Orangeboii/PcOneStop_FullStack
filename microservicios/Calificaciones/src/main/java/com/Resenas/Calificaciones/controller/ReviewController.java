package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.dto.ApiResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Nota: Cambiamos la base a /api para tener libertad en las sub-rutas
@RequestMapping("/api") 
@Tag(name = "Calificaciones", description = "Gestión de reseñas vinculadas a productos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 1. GET /api/products/:productId/reviews - Obtener reseñas
    @Operation(summary = "Obtener reseñas de un producto")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviews(@PathVariable String productId) {
        List<Review> reviews = reviewService.findByProductId(productId);
        
        if (reviews.isEmpty()) {
             return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, "Este producto aún no tiene reseñas", List.of(), 0L));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Reseñas obtenidas", reviews, (long) reviews.size()));
    }

    // 2. POST /api/products/:productId/reviews - Crear reseña
    @Operation(summary = "Crear una nueva reseña para un producto")
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Review>> createReview(
            @PathVariable String productId, 
            @RequestBody Review review) {
        try {
            Review savedReview = reviewService.create(productId, review);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Reseña creada exitosamente", savedReview, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    // 3. PUT /api/products/:productId/reviews/:reviewId - Actualizar reseña
    @Operation(summary = "Actualizar una reseña existente")
    @PutMapping("/products/{productId}/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable String productId,
            @PathVariable String reviewId,
            @RequestBody Review review) {
        try {
            // Nota: productId no se usa para actualizar la reseña en sí, pero la ruta lo pide por estándar REST
            Review updatedReview = reviewService.update(reviewId, review);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reseña actualizada", updatedReview, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }
    
    // Extra: Eliminar reseña (Mantenemos el endpoint directo por ID si lo necesitas)
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reseña eliminada", null, 0L));
    }
}