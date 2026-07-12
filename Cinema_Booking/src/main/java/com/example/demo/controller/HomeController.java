package com.example.demo.controller;

import com.example.demo.model.Movie;
import com.example.demo.model.Showtime;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MovieService;
import com.example.demo.service.ShowtimeService;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    public HomeController(UserRepository userRepository, MovieService movieService, ShowtimeService showtimeService) {
        this.userRepository = userRepository;
        this.movieService = movieService;
        this.showtimeService = showtimeService;
    }

    @GetMapping("/")
    public String viewHomePage(Model model) {
        // 1. Lấy thông tin xác thực từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiểm tra xem người dùng đã thực sự đăng nhập chưa (Loại trừ khách vãng lai anonymous)
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String currentUsername = auth.getName();
            
            // Chỉ khi đã đăng nhập thành công mới truy vấn DB để lấy Object User đưa ra View
            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        } else {
            // Nếu chưa đăng nhập, đảm bảo thuộc tính này bằng null để Thymeleaf không bị nhận diện sai
            model.addAttribute("currentUser", null);
        }

        // 3. Lấy danh sách phim để hiển thị
        List<Movie> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
     
        return "index";
    }

    @GetMapping("/403")
    public String viewAccessDeniedPage() {
        return "403";
    }
    
    @GetMapping("/movie/{id}/showtimes")
    public String getMovieShowtimes(@PathVariable("id") Long movieId, Model model) {
        // 1. Lấy thông tin phim
        Movie movie = movieService.getMovieById(movieId);
        model.addAttribute("movie", movie);

        // 2. Gọi hàm Service đã được xử lý lọc giờ và check Sold Out ở trên
        List<Showtime> showtimes = showtimeService.getAvailableShowtimesForMovie(movieId);
        model.addAttribute("showtimes", showtimes);

        // 3. Lấy thông tin user hiện tại để hiển thị trên header nếu cần
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            userRepository.findByUsername(auth.getName()).ifPresent(user -> model.addAttribute("currentUser", user));
        }

        return "movie-detail";
    }
}