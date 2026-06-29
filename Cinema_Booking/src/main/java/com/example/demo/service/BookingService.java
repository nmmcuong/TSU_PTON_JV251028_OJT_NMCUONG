package com.example.demo.service;

import com.example.demo.model.Booking;
import java.util.List;

public interface BookingService {
    Booking createBooking(Long userId, Long showtimeId, List<String> selectedSeats, String paymentMethod);
}