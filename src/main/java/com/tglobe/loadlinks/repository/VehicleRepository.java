package com.tglobe.loadlinks.repository;

import com.tglobe.loadlinks.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    List<Vehicle> findByStatus(String status);
    
    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.make) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Vehicle> searchVehicles(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance <= :date")
    List<Vehicle> findVehiclesDueForMaintenance(@Param("date") LocalDate date);
    
    @Query("SELECT v FROM Vehicle v WHERE v.mileage >= :threshold")
    List<Vehicle> findHighMileageVehicles(@Param("threshold") Double threshold);
    
    long countByStatus(String status);
    
    @Query("SELECT AVG(v.mileage) FROM Vehicle v")
    Double getAverageMileage();
}