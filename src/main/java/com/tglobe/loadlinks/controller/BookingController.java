package com.tglobe.loadlinks.controller;

import com.tglobe.loadlinks.model.Booking;
import com.tglobe.loadlinks.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    
    private final BookingRepository repository;

    public BookingController(BookingRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/request")
    public Booking createBooking(@RequestBody Booking booking) {
        // Business Logic: If weight > 5tons, set category to HEAVY
        return repository.save(booking);
    }

    @GetMapping("/all")
    public List<Booking> getAllBookings() {
        return repository.findAll();
    }
}
