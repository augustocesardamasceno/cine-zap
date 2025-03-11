package cinezap.com.br.cine_zap.service;

import cinezap.com.br.cine_zap.dto.RapidTranslateResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TranslateService {

    @Value("${rapid.api.translate.key}")
    private String rapidApiKey;

    @Value("${rapid.api.translate.host}")
    private String rapidApiHost;

    @Value("${rapid.api.translate.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final int MAX_TEXT_LENGTH = 5000;

    public String translateReviews(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String textToTranslate = text.length() > MAX_TEXT_LENGTH
                ? text.substring(0, MAX_TEXT_LENGTH)
                : text;

        String url = baseUrl + "/translate";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("target_lang", "pt");
        requestBody.put("text", textToTranslate); // Usa o texto truncado

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("x-rapidapi-key", rapidApiKey);
        headers.set("x-rapidapi-host", rapidApiHost);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return textToTranslate;
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            String responseBody = response.getBody();
            System.out.println("Raw response: " + responseBody);

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                try {
                    RapidTranslateResponseDto dto = objectMapper.readValue(responseBody, RapidTranslateResponseDto.class);
                    return dto.getTranslatedText();
                } catch (JsonProcessingException e) {
                    System.err.println("Erro ao analisar resposta da API: " + e.getMessage());
                    System.err.println("Resposta recebida: " + responseBody);
                    return textToTranslate;
                }
            } else {
                System.err.println("Resposta não bem-sucedida: " + response.getStatusCode());
                return textToTranslate;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return textToTranslate;
        }
    }
}
