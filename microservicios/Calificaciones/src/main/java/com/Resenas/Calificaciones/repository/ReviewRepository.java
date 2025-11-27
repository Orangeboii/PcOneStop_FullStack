package com.Resenas.Calificaciones.repository;

import com.Resenas.Calificaciones.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    // Obtener reseñas de un producto específico
    List<Review> findByProductId(String productId);
    
    // Buscar reseña existente de un usuario para un producto
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    
    // Obtener reseñas de un usuario
    List<Review> findByUserId(String userId);
}
