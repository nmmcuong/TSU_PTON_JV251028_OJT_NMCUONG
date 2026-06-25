package com.example.demo.service;

import com.example.demo.model.Showtime;
import java.util.List;

public interface ShowtimeService {
    List<Showtime> getAllShowtimes();
    void saveShowtime(Showtime showtime) throws Exception;
    void deleteShowtime(Long id);
}