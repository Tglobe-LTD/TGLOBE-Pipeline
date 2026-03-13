package com.tglobe.loadlinks.controller;

import com.tglobe.loadlinks.model.Vehicle;
import com.tglobe.loadlinks.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        return new ResponseEntity<>(vehicleService.createVehicle(vehicle), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<Vehicle> getAllVehicles(
            @PageableDefault(size = 10, sort = "licensePlate", direction = Sort.Direction.ASC) Pageable pageable) {
        return vehicleService.getAllVehicles(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/plate/{licensePlate}")
    public ResponseEntity<Vehicle> getVehicleByPlate(@PathVariable String licensePlate) {
        return ResponseEntity.ok(vehicleService.getVehicleByPlate(licensePlate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Vehicle> updateVehicleStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(vehicleService.updateVehicleStatus(id, status));
    }

    @PostMapping("/{id}/assign-driver")
    public ResponseEntity<Vehicle> assignDriver(
            @PathVariable Long id,
            @RequestParam String driverName) {
        return ResponseEntity.ok(vehicleService.assignDriver(id, driverName));
    }

    @PatchMapping("/{id}/mileage")
    public ResponseEntity<Vehicle> updateMileage(
            @PathVariable Long id,
            @RequestParam Double mileage) {
        return ResponseEntity.ok(vehicleService.updateMileage(id, mileage));
    }

    @PostMapping("/{id}/schedule-maintenance")
    public ResponseEntity<Vehicle> scheduleMaintenance(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextMaintenance) {
        return ResponseEntity.ok(vehicleService.scheduleMaintenance(id, nextMaintenance));
    }

    @PostMapping("/{id}/complete-maintenance")
    public ResponseEntity<Vehicle> completeMaintenance(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.completeMaintenance(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        return ResponseEntity.ok(vehicleService.getAvailableVehicles());
    }

    @GetMapping("/maintenance-due")
    public ResponseEntity<List<Vehicle>> getVehiclesDueForMaintenance() {
        return ResponseEntity.ok(vehicleService.getVehiclesDueForMaintenance());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getVehicleStatistics() {
        return ResponseEntity.ok(vehicleService.getVehicleStatistics());
    }

    @GetMapping("/search")
    public Page<Vehicle> searchVehicles(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return vehicleService.searchVehicles(q, pageable);
    }
}