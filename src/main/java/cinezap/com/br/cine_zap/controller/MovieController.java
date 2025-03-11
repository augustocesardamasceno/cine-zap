package cinezap.com.br.cine_zap.controller;

import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;
import cinezap.com.br.cine_zap.dto.MovieStats;
import cinezap.com.br.cine_zap.service.MovieService;
import cinezap.com.br.cine_zap.service.MovieStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieController {

    private final MovieService movieService;
    private final MovieStatsService movieStatsService;

    public MovieController(MovieService movieService, MovieStatsService movieStatsService) {
        this.movieService = movieService;
        this.movieStatsService = movieStatsService;
    }

    @GetMapping("/movies/search")
    public List<MovieSearchResult> searchMovies(@RequestParam("searchTerm") String searchTerm) {
        return movieService.searchMovies(searchTerm);
    }

    @GetMapping("/movies/detail")
    public MovieDetail getMovieDetail(@RequestParam("tconst") String tconst) {
        return movieService.getMovieDetail(tconst);
    }

    @GetMapping("/movies/stats")
    public MovieStats getMovieStats(@RequestParam("id") String id) {
        return movieStatsService.getMovieStats(id);
    }
}

