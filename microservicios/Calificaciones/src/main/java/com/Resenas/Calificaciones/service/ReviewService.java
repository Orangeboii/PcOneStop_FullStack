package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    // Método para CREAR (POST)
    public Review create(String productId, Review review) {
        // Asignar el producto de la URL al objeto
        review.setProductId(productId);

        // Validar rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5.");
        }

        // Generar ID si no existe
        if (review.getId() == null || review.getId().isEmpty()) {
            review.setId("review-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        // Fecha actual
        review.setDate(LocalDateTime.now().toString());
        
        // Autor anónimo si falta
        if (review.getAuthor() == null || review.getAuthor().isEmpty()) {
            review.setAuthor("Anónimo");
        }

        return reviewRepository.save(review);
    }

    // Método para ACTUALIZAR (PUT)
    public Review update(String reviewId, Review reviewData) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        // Actualizamos solo los campos permitidos
        existingReview.setRating(reviewData.getRating());
        existingReview.setComment(reviewData.getComment());
        existingReview.setDate(LocalDateTime.now().toString()); // Actualizamos fecha

        return reviewRepository.save(existingReview);
    }

    public List<Review> findByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> findByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}