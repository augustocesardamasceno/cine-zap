package cinezap.com.br.cine_zap.service;

import cinezap.com.br.cine_zap.dto.MovieStats;
import cinezap.com.br.cine_zap.exceptions.TooManyRequestsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
public class MovieStatsService {
    @Value("${rapid.api.stats.key}")
    private String rapidApiKey;

    @Value("${rapid.api.stats.host}")
    private String rapidApiHost;

    @Value("${rapid.api.base.stats.url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MovieStatsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public MovieStats getMovieStats(String tconst) {
        try {
            String url = baseUrl + "/item/?id=" + URLEncoder.encode(tconst, StandardCharsets.UTF_8);
            System.out.println("Chamando a API com a URL: " + url);

            String responseBody = restTemplate.execute(url, HttpMethod.GET, request -> {
                HttpHeaders headers = request.getHeaders();
                headers.set("x-rapidapi-key", rapidApiKey);
                headers.set("x-rapidapi-host", rapidApiHost);
            }, clientHttpResponse -> {
                if (clientHttpResponse.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    throw new TooManyRequestsException("Limite de requisições da API externa atingido.");
                }
                String contentEncoding = clientHttpResponse.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
                InputStream responseStream = clientHttpResponse.getBody();
                if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                    responseStream = new GZIPInputStream(responseStream);
                }
                return StreamUtils.copyToString(responseStream, StandardCharsets.UTF_8);
            });

            System.out.println("Corpo bruto da resposta: " + responseBody);
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode resultNode = root.path("result");
            JsonNode ratingsNode = resultNode.path("ratings");

            if (ratingsNode.isMissingNode() || ratingsNode.isNull()){
                System.out.println("Estrutura de dados inesperada: 'ratings' não encontrado. Resposta completa: " + root.toString());
            }

            MovieStats stats = new MovieStats();
            stats.setImdbId(root.path("id").asText());
            if (ratingsNode.isMissingNode() || ratingsNode.isNull()){
                System.out.println("Estrutura de dados inesperada: 'ratings' não encontrado. Resposta completa: " + root.toString());
                stats.setImdbScore(0.0);
                stats.setMetaScore(0.0);
                stats.setUserScore(0.0);
                stats.setTomatometer(0.0);
                stats.setAudienceScore(0.0);
                stats.setScore(0.0);
            } else {
                stats.setImdbScore(ratingsNode.path("IMDb").path("audience").path("rating").asDouble());
                stats.setMetaScore(ratingsNode.path("Metacritic").path("audience").path("rating").asDouble());
                stats.setUserScore(ratingsNode.path("Metacritic").path("critics").path("rating").asDouble());
                stats.setTomatometer(ratingsNode.path("Rotten Tomatoes").path("audience").path("rating").asDouble());
                stats.setAudienceScore(ratingsNode.path("Rotten Tomatoes").path("critics").path("rating").asDouble());
                stats.setScore(ratingsNode.path("Letterboxd").path("audience").path("rating").asDouble());
            }
            return stats;
        } catch (TooManyRequestsException e) {
            System.out.println(e.getMessage());
            MovieStats stats = new MovieStats();
            stats.setImdbId(tconst);
            stats.setImdbScore(0.0);
            stats.setMetaScore(0.0);
            stats.setUserScore(0.0);
            stats.setTomatometer(0.0);
            stats.setAudienceScore(0.0);
            stats.setScore(0.0);
            return stats;
        } catch (Exception e) {
            System.out.println("Erro ao obter detalhes do filme: " + e);
            throw new RuntimeException("Erro ao obter detalhes do filme", e);
        }
    }
}
