package com.tglobe.loadlinks.service;

import com.tglobe.loadlinks.model.Vehicle;
import com.tglobe.loadlinks.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        // Set default values
        if (vehicle.getStatus() == null) {
            vehicle.setStatus("AVAILABLE");
        }
        if (vehicle.getMileage() == null) {
            vehicle.setMileage(0.0);
        }
        return vehicleRepository.save(vehicle);
    }

    public Page<Vehicle> getAllVehicles(Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
    }

    public Vehicle getVehicleByPlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
            .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with plate: " + licensePlate));
    }

    @Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);
        
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setMake(vehicleDetails.getMake());
        vehicle.setYear(vehicleDetails.getYear());
        vehicle.setCapacity(vehicleDetails.getCapacity());
        vehicle.setFuelType(vehicleDetails.getFuelType());
        vehicle.setStatus(vehicleDetails.getStatus());
        vehicle.setMileage(vehicleDetails.getMileage());
        vehicle.setLastMaintenance(vehicleDetails.getLastMaintenance());
        vehicle.setNextMaintenance(vehicleDetails.getNextMaintenance());
        vehicle.setAssignedDriver(vehicleDetails.getAssignedDriver());
        vehicle.setLocation(vehicleDetails.getLocation());
        vehicle.setNotes(vehicleDetails.getNotes());
        
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public Vehicle updateVehicleStatus(Long id, String status) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setStatus(status);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle assignDriver(Long vehicleId, String driverName) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setAssignedDriver(driverName);
        vehicle.setStatus("IN_USE");
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateMileage(Long id, Double newMileage) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setMileage(newMileage);
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle scheduleMaintenance(Long id, LocalDate nextMaintenanceDate) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setNextMaintenance(nextMaintenanceDate);
        vehicle.setStatus("MAINTENANCE");
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle completeMaintenance(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setLastMaintenance(LocalDate.now());
        vehicle.setStatus("AVAILABLE");
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus("AVAILABLE");
    }

    public List<Vehicle> getVehiclesDueForMaintenance() {
        return vehicleRepository.findVehiclesDueForMaintenance(LocalDate.now().plusWeeks(1));
    }

    public Map<String, Object> getVehicleStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", vehicleRepository.count());
        stats.put("available", vehicleRepository.countByStatus("AVAILABLE"));
        stats.put("inUse", vehicleRepository.countByStatus("IN_USE"));
        stats.put("maintenance", vehicleRepository.countByStatus("MAINTENANCE"));
        stats.put("outOfService", vehicleRepository.countByStatus("OUT_OF_SERVICE"));
        stats.put("averageMileage", vehicleRepository.getAverageMileage());
        return stats;
    }

    public Page<Vehicle> searchVehicles(String searchTerm, Pageable pageable) {
        return vehicleRepository.searchVehicles(searchTerm, pageable);
    }
}