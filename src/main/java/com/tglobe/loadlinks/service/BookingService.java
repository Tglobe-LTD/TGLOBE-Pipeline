package com.tglobe.loadlinks.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page; // Needed for Pagination
import org.springframework.data.domain.Pageable; // Needed for Pagination
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.tglobe.loadlinks.model.Booking;
import com.tglobe.loadlinks.repository.BookingRepository;

@Service
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    // CREATE with Business Logic for Port Harcourt Loads
    public Booking createBooking(@NonNull Booking booking) {
        if (booking.getWeight() != null && booking.getWeight() > 5000) {
            booking.setLoadType("HEAVY_DUTY");
        } else {
            booking.setLoadType("LIGHT_GOODS");
        }
        return repository.save(booking);
    }

    // THE MISSING LINK: Pagination support
    public Page<Booking> getAllPaginated(@NonNull Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Booking> getAll() {
        return repository.findAll();
    }

    public Optional<Booking> getById(@NonNull Long id) {
        return repository.findById(id);
    }

    public Booking updateBooking(@NonNull Long id, @NonNull Booking details) {
        Booking booking = repository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setPickup(details.getPickup());
        booking.setDropoff(details.getDropoff());
        booking.setStatus(details.getStatus());
        booking.setWeight(details.getWeight());
        return repository.save(booking);
    }

    public void deleteBooking(@NonNull Long id) {
        repository.deleteById(id);
    }
}
