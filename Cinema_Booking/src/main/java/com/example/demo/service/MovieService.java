package com.example.demo.service;

import com.example.demo.model.Movie;
import com.example.demo.model.Genre;
import java.util.List;

public interface MovieService {
    List<Movie> getAllMovies();
    List<Genre> getAllGenres();
    Movie getMovieById(Long id);
    void saveOrUpdateMovie(Movie movie, List<Long> genreIds);
    void softDeleteMovie(Long id);
}