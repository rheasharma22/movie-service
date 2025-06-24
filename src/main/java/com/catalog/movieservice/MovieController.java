package com.catalog.movieservice;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    private UserRepository userRepository;

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setGenre(movie.getGenre());
        dto.setRating((long) movie.getRating());
        dto.setUserId(movie.getUser().getId()); // NEW
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        movie.setTitle(dto.getTitle());
        movie.setGenre(dto.getGenre());
        movie.setRating(dto.getRating());

        // Lookup and set the user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        movie.setUser(user);

        return movie;
    }

    @GetMapping
    public List<MovieDTO> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();

        return movies.stream().map(movie -> {
            MovieDTO dto = new MovieDTO();
            dto.setId(movie.getId());
            dto.setTitle(movie.getTitle());
            dto.setGenre(movie.getGenre());
            dto.setRating((long) movie.getRating());

            if (movie.getUser() != null) {
                User user = movie.getUser();
                UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail());
                dto.setUser(userDTO);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<MovieDTO> addMovie(@RequestBody @Valid MovieDTO dto) {
        // 1. Convert DTO to Entity
        Movie movie = new Movie(dto.getTitle(), dto.getGenre(), dto.getRating());

        // 2. Set user if userId is present
        if (dto.getUserId() != null) {
            Optional<User> userOptional = userRepository.findById(dto.getUserId());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            movie.setUser(userOptional.get());
        }

        // 3. Save the movie entity and capture the result
        Movie savedMovie = movieRepository.save(movie);

        // 4. Convert saved entity back to DTO for the response
        MovieDTO responseDto = new MovieDTO();
        responseDto.setId(savedMovie.getId());
        responseDto.setTitle(savedMovie.getTitle());
        responseDto.setGenre(savedMovie.getGenre());
        responseDto.setRating((long) savedMovie.getRating());

        if (savedMovie.getUser() != null) {
            responseDto.setUserId(savedMovie.getUser().getId());
        }

        // 5. Return DTO with HTTP 201
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
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
