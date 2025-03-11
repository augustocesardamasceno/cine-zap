package cinezap.com.br.cine_zap.service;

import cinezap.com.br.cine_zap.config.AppConfig;
import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;
import cinezap.com.br.cine_zap.dto.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@TestPropertySource(properties = {
        "rapid.api.key=fakeKeyForTests",
        "rapid.api.host=fakeHost",
        "rapid.api.base.url=http://fake-api"
})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MovieService.class, AppConfig.class })
class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void testSearchMovies() throws Exception {
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        String jsonResponse = """
        {
          "data": {
            "mainSearch": {
              "edges": [
                {
                  "node": {
                    "entity": {
                      "id": "tt1234567",
                      "titleText": { "text": "Mock Movie" },
                      "primaryImage": { "url": "http://example.com/image.jpg" },
                      "principalCredits": []
                    }
                  }
                }
              ]
            }
          }
        }
        """;

        // Define o endpoint exato (dependendo de baseUrl e query)
        server.expect(requestTo("http://fake-api/search?searchTerm=Harry+Potter"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<MovieSearchResult> results = movieService.searchMovies("Harry Potter");

        // Verifica se a lista voltou com 1 item
        assertEquals(1, results.size());
        assertEquals("tt1234567", results.get(0).getId());
        assertEquals("Mock Movie", results.get(0).getTitle());
    }

    @Test
    void testMovieDetail() {
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
         String jsonResponse = """
        {
            "data": {
            "title": {
                "id": "tt0133093",
                        "ratingsSummary": {
                    "aggregateRating": 8.7
                },
                "featuredReviews": {
                    "edges": [
                    {
                        "node": {
                        "author": {
                            "nickName": "Kirpianuscus"
                        },
                        "text": {
                            "originalText": {
                                "plainText": "and this is all. because each explanation..."
                            }
                        }
                    }
                    }
            ]
                }
            }
        }
        }
        """;


            server.expect(requestTo("http://fake-api/title/get-user-reviews-summary?tconst=tt0133093"))
                    .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

            MovieDetail movieDetail =  movieService.getMovieDetail("tt0133093");
            assertEquals("tt0133093", movieDetail.getId());
            assertEquals(8.7, movieDetail.getRatingsSummary());

            List<Review> review = movieDetail.getReviews();
            assertEquals("Kirpianuscus", review.get(0).getNickName());
            assertEquals("and this is all. because each explanation...", review.get(0).getPlainText());
        }
    }


