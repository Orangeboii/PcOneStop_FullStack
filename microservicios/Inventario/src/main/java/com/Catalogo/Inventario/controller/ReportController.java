package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Nota: Cambiamos la base a /api para coincidir con tu petición
@RequestMapping("/api")
@Tag(name = "Reportes", description = "Gestión de reportes de productos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    // 1. POST /api/products/:productId/reports - Crear reporte
    @Operation(summary = "Crear un reporte para un producto")
    @PostMapping("/products/{productId}/reports")
    public ResponseEntity<ApiResponse<ProductReport>> createReport(
            @PathVariable String productId,
            @RequestBody ProductReport report) {
        
        // Asignamos el ID de la URL al objeto
        report.setProductId(productId);
        
        ProductReport saved = reportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "Reporte enviado exitosamente", saved, 1L));
    }

    // Endpoints adicionales útiles para admin
    
    @Operation(summary = "Ver reportes de un producto")
    @GetMapping("/products/{productId}/reports")
    public ResponseEntity<ApiResponse<List<ProductReport>>> getReportsByProduct(@PathVariable String productId) {
        List<ProductReport> reports = reportRepository.findByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reportes del producto", reports, (long) reports.size()));
    }
    
    @Operation(summary = "Ver todos los reportes (Admin)")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ProductReport>>> getAllReports() {
        List<ProductReport> list = reportRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Lista total de reportes", list, (long) list.size()));
    }
}