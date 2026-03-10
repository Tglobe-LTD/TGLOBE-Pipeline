package com.tglobe.loadlinks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Data
public class Booking {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String loadType; // Auto-categorized in Service Layer

    @Positive(message = "Weight must be greater than zero")
    private Double weight;

    @NotBlank(message = "Pickup location is required")
    private String pickup;

    @NotBlank(message = "Dropoff location is required")
    private String dropoff;

    private String status = "PENDING";
}
