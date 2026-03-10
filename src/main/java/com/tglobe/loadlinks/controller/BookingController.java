package com.tglobe.loadlinks.controller;

import com.tglobe.loadlinks.model.Booking;
import com.tglobe.loadlinks.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) { 
        this.service = service; 
    }

    // CREATE with Validation - Protecs Tglobe from bad data
    @PostMapping
    public ResponseEntity<Booking> create(@Valid @RequestBody Booking booking) { 
        return ResponseEntity.ok(service.createBooking(booking)); 
    }

    // READ all with Pagination - Mastery Move for performance
    @GetMapping
    public Page<Booking> getAll(Pageable pageable) { 
        return service.getAllPaginated(pageable); 
    }

    // READ one by ID
    @GetMapping("/{id}")
    public Optional<Booking> getOne(@PathVariable Long id) { 
        return service.getById(id); 
    }

    // UPDATE - Update pickup/dropoff/status
    @PutMapping("/{id}")
    public Booking update(@PathVariable Long id, @RequestBody Booking booking) { 
        return service.updateBooking(id, booking); 
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { 
        service.deleteBooking(id); 
    }
}
