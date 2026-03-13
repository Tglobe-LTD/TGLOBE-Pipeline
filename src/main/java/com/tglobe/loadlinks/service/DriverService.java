package com.tglobe.loadlinks.service;

import com.tglobe.loadlinks.model.Driver;
import com.tglobe.loadlinks.repository.DriverRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public Driver createDriver(Driver driver) {
        // Set default values
        if (driver.getStatus() == null) {
            driver.setStatus("AVAILABLE");
        }
        if (driver.getRating() == null) {
            driver.setRating(5.0);
        }
        if (driver.getTotalTrips() == null) {
            driver.setTotalTrips(0);
        }
        return driverRepository.save(driver);
    }

    public Page<Driver> getAllDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable);
    }

    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + id));
    }

    @Transactional
    public Driver updateDriver(Long id, Driver driverDetails) {
        Driver driver = getDriverById(id);
        
        driver.setName(driverDetails.getName());
        driver.setLicenseNumber(driverDetails.getLicenseNumber());
        driver.setPhone(driverDetails.getPhone());
        driver.setEmail(driverDetails.getEmail());
        driver.setStatus(driverDetails.getStatus());
        driver.setRating(driverDetails.getRating());
        driver.setVehicleAssigned(driverDetails.getVehicleAssigned());
        driver.setCurrentLatitude(driverDetails.getCurrentLatitude());
        driver.setCurrentLongitude(driverDetails.getCurrentLongitude());
        driver.setCurrentLocation(driverDetails.getCurrentLocation());
        driver.setEmergencyContact(driverDetails.getEmergencyContact());
        driver.setNotes(driverDetails.getNotes());
        
        return driverRepository.save(driver);
    }

    @Transactional
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new EntityNotFoundException("Driver not found with id: " + id);
        }
        driverRepository.deleteById(id);
    }

    @Transactional
    public Driver updateDriverStatus(Long id, String status) {
        Driver driver = getDriverById(id);
        driver.setStatus(status);
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver updateDriverLocation(Long id, Double latitude, Double longitude, String locationName) {
        Driver driver = getDriverById(id);
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setCurrentLocation(locationName);
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver assignVehicle(Long driverId, String vehiclePlate) {
        Driver driver = getDriverById(driverId);
        driver.setVehicleAssigned(vehiclePlate);
        driver.setStatus("ON_TRIP");
        return driverRepository.save(driver);
    }

    @Transactional
    public Driver completeTrip(Long driverId) {
        Driver driver = getDriverById(driverId);
        driver.setTotalTrips(driver.getTotalTrips() + 1);
        driver.setStatus("AVAILABLE");
        return driverRepository.save(driver);
    }

    public List<Driver> getAvailableDrivers() {
        return driverRepository.findByStatus("AVAILABLE");
    }

    public Map<String, Long> getDriverStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", driverRepository.count());
        stats.put("available", driverRepository.countByStatus("AVAILABLE"));
        stats.put("onTrip", driverRepository.countByStatus("ON_TRIP"));
        stats.put("offDuty", driverRepository.countByStatus("OFF_DUTY"));
        stats.put("onLeave", driverRepository.countByStatus("ON_LEAVE"));
        return stats;
    }

    public Page<Driver> searchDrivers(String searchTerm, Pageable pageable) {
        return driverRepository.searchDrivers(searchTerm, pageable);
    }

    // NEW: Find nearest available drivers
    public List<Driver> findNearestAvailableDrivers(Double latitude, Double longitude, Double maxDistanceKm, int limit) {
        return driverRepository.findNearestAvailableDrivers(latitude, longitude, maxDistanceKm, limit);
    }

    // NEW: Find the single closest available driver
    public Optional<Driver> findClosestAvailableDriver(Double latitude, Double longitude) {
        return driverRepository.findClosestAvailableDriver(latitude, longitude);
    }

    // NEW: Match driver to booking based on pickup location
    public Optional<Driver> matchDriverToBooking(Double pickupLatitude, Double pickupLongitude) {
        // Find closest available driver within 50km
        return findClosestAvailableDriver(pickupLatitude, pickupLongitude);
    }

    // NEW: Get drivers with location data
    public List<Driver> getDriversWithLocation() {
        return driverRepository.findByStatus("AVAILABLE").stream()
                .filter(d -> d.getCurrentLatitude() != null && d.getCurrentLongitude() != null)
                .toList();
    }
}