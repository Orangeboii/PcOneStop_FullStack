package com.Pedidos.Pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    private Long id;
    private String name;
    private String brand;
    private String model;
    private String category;
    private Double price;
    private Integer stock;
    private String description;
    private String image;
    private Boolean isOnSale;
    private Integer discount;
    private String offerStartDate;
    private String offerEndDate;
}
