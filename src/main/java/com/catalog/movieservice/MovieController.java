package com.catalog.movieservice;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/movies")
public class MovieController {
    @Autowired
    private MovieRepository movieRepository;

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setGenre(movie.getGenre());
        dto.setRating((long) movie.getRating());
        return dto;
    }

    @GetMapping
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Movie> addMovie(@Valid @RequestBody MovieDTO movieDTO) {
        Movie movie = new Movie();
        movie.setTitle(movieDTO.getTitle());
        movie.setGenre(movieDTO.getGenre());
        movie.setRating(movieDTO.getRating());

        Movie savedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(savedMovie);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        return movieRepository.findById(id)
                .map(movie -> ResponseEntity.ok(convertToDTO(movie)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO movieDTO) {
        Optional<Movie> movieOptional = movieRepository.findById(id);

        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();
            movie.setTitle(movieDTO.getTitle());
            movie.setGenre(movieDTO.getGenre());
            movie.setRating(movieDTO.getRating());

            movieRepository.save(movie);
            return ResponseEntity.ok(movie);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movieRepository.delete(movie);
                    return ResponseEntity.noContent().<Void>build(); // 👈 this fixes the type
                })
                .orElse(ResponseEntity.notFound().<Void>build()); // 👈 force build() to return ResponseEntity<Void>
    }
}
