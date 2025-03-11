package cinezap.com.br.cine_zap.service;

import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;
import cinezap.com.br.cine_zap.dto.Review;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {

    @Value("${rapid.api.key}")
    private String rapidApiKey;

    @Value("${rapid.api.host}")
    private String rapidApiHost;

    @Value("${rapid.api.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MovieService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public List<MovieSearchResult> searchMovies(String query) {
        try {
            String url = baseUrl + "/search?searchTerm=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", rapidApiKey);
            headers.set("x-rapidapi-host", rapidApiHost);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JsonNode.class);
            JsonNode root = response.getBody();

            JsonNode dataNode = root.path("data");
            JsonNode mainSearchNode = dataNode.path("mainSearch");
            JsonNode edges = mainSearchNode.path("edges");
            if (!edges.isArray()) {
                throw new RuntimeException("Estrutura de dados inesperada");
            }

            List<MovieSearchResult> results = new ArrayList<>();
            for (JsonNode edge : edges) {
                JsonNode entity = edge.path("node").path("entity");
                MovieSearchResult result = new MovieSearchResult();
                result.setId(entity.path("id").asText(null));
                result.setTitle(entity.path("titleText").path("text").asText(""));
                result.setImage(entity.path("primaryImage").path("url").asText(null));
                StringBuilder castBuilder = new StringBuilder();
                JsonNode principalCredits = entity.path("principalCredits");

                if (principalCredits.isArray()) {
                    // Itera sobre cada grupo de créditos
                    for (JsonNode creditGroup : principalCredits) {
                        JsonNode creditsArray = creditGroup.path("credits");
                        if (creditsArray.isArray()) {
                            for (JsonNode credit : creditsArray) {
                                String name = credit.path("name").path("nameText").path("text").asText("");
                                if (!name.isEmpty()) {
                                    // Se já existe um nome, adiciona vírgula para separar
                                    if (castBuilder.length() > 0) {
                                        castBuilder.append(", ");
                                    }
                                    castBuilder.append(name);
                                }
                            }
                        }
                    }
                }

                result.setCast(castBuilder.toString());
                results.add(result);
            }
            return results;
        } catch (Exception e) {
            // Em uma aplicação real, você poderia criar uma exceção customizada
            throw new RuntimeException("Erro ao buscar filmes", e);
        }
    }

    public MovieDetail getMovieDetail(String tconst) {
        try {
            String url = baseUrl + "/title/get-user-reviews-summary?tconst=" + URLEncoder.encode(tconst, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", rapidApiKey);
            headers.set("x-rapidapi-host", rapidApiHost);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, JsonNode.class);
            JsonNode root = response.getBody();

            JsonNode dataNode = root.path("data");
            JsonNode titleNode = dataNode.path("title");
            if (titleNode.isMissingNode() || titleNode.isNull()) {
                throw new RuntimeException("Estrutura de dados inesperada: 'title' não encontrada.");
            }

            MovieDetail detail = new MovieDetail();
            detail.setId(titleNode.path("id").asText());
            detail.setRatingsSummary(titleNode.path("ratingsSummary").path("aggregateRating").asDouble());

            List<Review> reviews = new ArrayList<>();
            JsonNode featuredReviewsEdges = titleNode.path("featuredReviews").path("edges");
            if (featuredReviewsEdges.isArray()) {
                for (JsonNode edge : featuredReviewsEdges) {
                    JsonNode reviewNode = edge.path("node");
                    Review review = new Review();
                    review.setNickName(reviewNode.path("author").path("nickName").asText());
                    review.setPlainText(reviewNode.path("text").path("originalText").path("plainText").asText());
                    reviews.add(review);
                }
            }
            detail.setReviews(reviews);
            return detail;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao obter detalhes do filme", e);
        }
    }
}

