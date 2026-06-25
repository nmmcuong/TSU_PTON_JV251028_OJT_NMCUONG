package com.example.demo.controller;

import com.example.demo.model.Showtime;
import com.example.demo.service.MovieService; // Tận dụng Service cũ lấy danh sách phim
import com.example.demo.repository.RoomRepository; // Lấy danh sách phòng
import com.example.demo.service.ShowtimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/showtimes")
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomRepository roomRepository;

    public AdminShowtimeController(ShowtimeService showtimeService, MovieService movieService, RoomRepository roomRepository) {
        this.showtimeService = showtimeService;
        this.movieService = movieService;
        this.roomRepository = roomRepository;
    }

    // Hiển thị danh sách lịch chiếu hiện tại
    @GetMapping
    public String listShowtimes(Model model) {
        model.addAttribute("showtimes", showtimeService.getAllShowtimes());
        return "admin/showtime-list";
    }

    // Hiển thị form tạo mới suất chiếu
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("showtime", new Showtime());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/showtime-form";
    }

    // Tiếp nhận form và xử lý trùng lịch
    @PostMapping("/save")
    public String saveShowtime(@ModelAttribute("showtime") Showtime showtime, Model model) {
        try {
            showtimeService.saveShowtime(showtime);
            return "redirect:/admin/showtimes?success=true";
        } catch (Exception e) {
            // Nếu dính lỗi trùng phòng/lịch -> Đưa ngược lại Form kèm thông báo cảnh báo lỗi
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("rooms", roomRepository.findAll());
            return "admin/showtime-form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable("id") Long id) {
        showtimeService.deleteShowtime(id);
        return "redirect:/admin/showtimes?deleted=true";
    }
}