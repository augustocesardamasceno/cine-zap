package cinezap.com.br.cine_zap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendTextMessage(String recipientPhone, String messageText) {
        String url = "https://graph.facebook.com/v15.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", recipientPhone);
        payload.put("type", "text");

        Map<String, String> textObject = new HashMap<>();
        textObject.put("body", messageText);
        payload.put("text", textObject);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println("Resposta da API do WhatsApp: " + response.getBody());

        if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Erro ao enviar mensagem: " + response.getBody());
        }
    }



}

