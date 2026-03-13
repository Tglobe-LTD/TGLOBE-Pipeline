package com.tglobe.loadlinks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String loadType;

    @Positive(message = "Weight must be greater than zero")
    private Double weight;

    @NotBlank(message = "Pickup location is required")
    private String pickup;

    @NotBlank(message = "Dropoff location is required")
    private String dropoff;

    private String status = "PENDING";

    // Constructors
    public Booking() {
    }

    public Booking(String customerName, Double weight, String pickup, String dropoff) {
        this.customerName = customerName;
        this.weight = weight;
        this.pickup = pickup;
        this.dropoff = dropoff;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getLoadType() {
        return loadType;
    }

    public Double getWeight() {
        return weight;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDropoff() {
        return dropoff;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setLoadType(String loadType) {
        this.loadType = loadType;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public void setDropoff(String dropoff) {
        this.dropoff = dropoff;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "PENDING";
        }
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", customerName='" + customerName + '\'' +
                ", loadType='" + loadType + '\'' +
                ", weight=" + weight +
                ", pickup='" + pickup + '\'' +
                ", dropoff='" + dropoff + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}