package com.tglobe.loadlinks.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String loadType; // e.g., "Heavy Machinery", "Light Goods"
    private Double weight;   // In KG or Tons
    private String pickup;   // e.g., "Onne Port"
    private String dropoff;  // e.g., "Trans Amadi"
    private String status = "PENDING";
}
