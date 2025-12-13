package com.Pedidos.Pagos.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequestDTO {
    private Long userId;
    
    @JsonAlias({"total"})
    private Double totalAmount;
    
    // Formato antiguo: "1,2,3" (para compatibilidad)
    private String productIds;
    
    // Formato nuevo: Lista de items con cantidades
    private List<OrderItemDTO> items;
    
    // Método helper para obtener items
    public List<OrderItemDTO> getItems() {
        return items;
    }
    
    // Método helper para verificar si usa el formato nuevo
    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}
