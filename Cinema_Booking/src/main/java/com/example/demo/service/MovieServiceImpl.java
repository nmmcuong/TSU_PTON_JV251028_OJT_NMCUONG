package com.example.demo.service;

import com.example.demo.model.Movie;
import com.example.demo.enums.MovieStatus;
import com.example.demo.model.Genre;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    // Constructor injection tay thuần không dùng Lombok
    public MovieServiceImpl(MovieRepository movieRepository, GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @Override
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ phim có ID: " + id));
    }

    @Override
    @Transactional
    public void saveOrUpdateMovie(Movie movie, List<Long> genreIds) {
        List<Genre> genres = new ArrayList<>();
        if (genreIds != null) {
            for (Long genreId : genreIds) {
                genreRepository.findById(genreId).ifPresent(genres::add);
            }
        }

        movie.setGenres(genres);
        movieRepository.save(movie);
    }

    @Override
    @Transactional
    public void softDeleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movie.setStatus(MovieStatus.STOPPED);
        movieRepository.save(movie);
    }
}