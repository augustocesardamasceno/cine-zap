package cinezap.com.br.cine_zap.controller;

import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;

import cinezap.com.br.cine_zap.dto.whatsapp.WhatsAppWebhookRoot;

import cinezap.com.br.cine_zap.service.handler.WhatsAppWebhookHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @Autowired
    private WhatsAppWebhookHandler webhookHandler;
    @GetMapping
    public ResponseEntity<String> verifyWebhook(@RequestParam(name = "hub.mode") String mode,
                                                @RequestParam(name = "hub.verify_token") String token,
                                                @RequestParam(name = "hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(403).body("Erro na verificação");
        }
    }

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody WhatsAppWebhookRoot root) {
        try {
            System.out.println("==== Webhook RECEBIDO ====");
            System.out.println(root);

            if (!"whatsapp_business_account".equals(root.getObject())) {
                System.out.println("Objeto inesperado: " + root.getObject());
                return ResponseEntity.badRequest().build();
            }

            webhookHandler.receiveWebhook(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }


}


