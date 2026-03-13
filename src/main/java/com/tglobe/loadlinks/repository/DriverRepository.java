package com.tglobe.loadlinks.repository;

import com.tglobe.loadlinks.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    
    List<Driver> findByStatus(String status);
    
    @Query("SELECT d FROM Driver d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Driver> searchDrivers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT d FROM Driver d WHERE d.rating >= :minRating")
    List<Driver> findTopRated(@Param("minRating") Double minRating);
    
    long countByStatus(String status);
    
    // Haversine formula for distance calculation (in km)
    @Query(value = "SELECT d.*, (6371 * acos(cos(radians(:lat)) * cos(radians(d.current_latitude)) * " +
           "cos(radians(d.current_longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(d.current_latitude)))) AS distance " +
           "FROM drivers d " +
           "WHERE d.status = 'AVAILABLE' " +
           "AND d.current_latitude IS NOT NULL " +
           "AND d.current_longitude IS NOT NULL " +
           "HAVING distance < :maxDistance " +
           "ORDER BY distance " +
           "LIMIT :limit", nativeQuery = true)
    List<Driver> findNearestAvailableDrivers(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("maxDistance") Double maxDistanceKm,
            @Param("limit") int limit);
    
    @Query(value = "SELECT d.*, (6371 * acos(cos(radians(:lat)) * cos(radians(d.current_latitude)) * " +
           "cos(radians(d.current_longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(d.current_latitude)))) AS distance " +
           "FROM drivers d " +
           "WHERE d.status = 'AVAILABLE' " +
           "AND d.current_latitude IS NOT NULL " +
           "AND d.current_longitude IS NOT NULL " +
           "ORDER BY distance " +
           "LIMIT 1", nativeQuery = true)
    Optional<Driver> findClosestAvailableDriver(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude);
}