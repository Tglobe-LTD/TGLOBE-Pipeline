package com.tglobe.loadlinks.controller;

import com.tglobe.loadlinks.model.Driver;
import com.tglobe.loadlinks.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) {
        return new ResponseEntity<>(driverService.createDriver(driver), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<Driver> getAllDrivers(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return driverService.getAllDrivers(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Driver> updateDriver(@PathVariable Long id, @Valid @RequestBody Driver driver) {
        return ResponseEntity.ok(driverService.updateDriver(id, driver));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Driver> updateDriverStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(driverService.updateDriverStatus(id, status));
    }

    @PatchMapping("/{id}/location")
    public ResponseEntity<Driver> updateDriverLocation(
            @PathVariable Long id,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String locationName) {
        return ResponseEntity.ok(driverService.updateDriverLocation(id, latitude, longitude, locationName));
    }

    @PostMapping("/{id}/assign-vehicle")
    public ResponseEntity<Driver> assignVehicle(
            @PathVariable Long id,
            @RequestParam String vehiclePlate) {
        return ResponseEntity.ok(driverService.assignVehicle(id, vehiclePlate));
    }

    @PostMapping("/{id}/complete-trip")
    public ResponseEntity<Driver> completeTrip(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.completeTrip(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    @GetMapping("/with-location")
    public ResponseEntity<List<Driver>> getDriversWithLocation() {
        return ResponseEntity.ok(driverService.getDriversWithLocation());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getDriverStatistics() {
        return ResponseEntity.ok(driverService.getDriverStatistics());
    }

    @GetMapping("/search")
    public Page<Driver> searchDrivers(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        return driverService.searchDrivers(q, pageable);
    }

    // NEW: Find nearest drivers
    @GetMapping("/nearest")
    public ResponseEntity<List<Driver>> findNearestDrivers(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "50") Double maxDistance,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(driverService.findNearestAvailableDrivers(lat, lng, maxDistance, limit));
    }

    // NEW: Find closest driver
    @GetMapping("/closest")
    public ResponseEntity<Map<String, Object>> findClosestDriver(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        
        Optional<Driver> closestDriver = driverService.findClosestAvailableDriver(lat, lng);
        
        Map<String, Object> response = new HashMap<>();
        if (closestDriver.isPresent()) {
            response.put("found", true);
            response.put("driver", closestDriver.get());
            
            // Calculate approximate distance (this will be recalculated in the query)
            double distance = calculateDistance(lat, lng, 
                closestDriver.get().getCurrentLatitude(), 
                closestDriver.get().getCurrentLongitude());
            response.put("distance_km", Math.round(distance * 10) / 10.0);
        } else {
            response.put("found", false);
            response.put("message", "No available drivers found in proximity");
        }
        
        return ResponseEntity.ok(response);
    }

    // NEW: Match driver to booking
    @PostMapping("/match-to-booking")
    public ResponseEntity<Map<String, Object>> matchDriverToBooking(
            @RequestParam Double pickupLat,
            @RequestParam Double pickupLng,
            @RequestParam(required = false) Long bookingId) {
        
        Optional<Driver> matchedDriver = driverService.matchDriverToBooking(pickupLat, pickupLng);
        
        Map<String, Object> response = new HashMap<>();
        if (matchedDriver.isPresent()) {
            response.put("matched", true);
            response.put("driver", matchedDriver.get());
            
            double distance = calculateDistance(pickupLat, pickupLng, 
                matchedDriver.get().getCurrentLatitude(), 
                matchedDriver.get().getCurrentLongitude());
            response.put("distance_km", Math.round(distance * 10) / 10.0);
            
            if (bookingId != null) {
                // Here you would update the booking with the assigned driver
                response.put("bookingId", bookingId);
                response.put("assigned", true);
            }
        } else {
            response.put("matched", false);
            response.put("message", "No available drivers found near pickup location");
        }
        
        return ResponseEntity.ok(response);
    }

    // Utility method to calculate distance using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}