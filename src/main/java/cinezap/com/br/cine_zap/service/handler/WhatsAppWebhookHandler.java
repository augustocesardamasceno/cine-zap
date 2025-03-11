package cinezap.com.br.cine_zap.service.handler;

import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;
import cinezap.com.br.cine_zap.dto.whatsapp.*;
import cinezap.com.br.cine_zap.service.Bucket4jRateLimitService;
import cinezap.com.br.cine_zap.service.MovieService;
import cinezap.com.br.cine_zap.service.MovieStatsService;
import cinezap.com.br.cine_zap.service.WhatsAppService;
import cinezap.com.br.cine_zap.service.utils.WhatsAppHandlerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cinezap.com.br.cine_zap.service.utils.WhatsAppHandlerUtils.*;


@Component
public class WhatsAppWebhookHandler {

    private Map<String, List<MovieSearchResult>> sessions = new ConcurrentHashMap<>();
    @Autowired
    private MovieService movieService;
    @Autowired
    private WhatsAppService whatsappService;
    @Autowired
    private WhatsAppHandlerUtils formatter;
    @Autowired
    private Bucket4jRateLimitService rateLimitService;
    @Autowired
    private MovieStatsService movieStats;

    public ResponseEntity<Void> receiveWebhook(@RequestBody WhatsAppWebhookRoot root) {
        List<Entry> entries = root.getEntry();
        if (entries == null || entries.isEmpty()){
            return ResponseEntity.ok().build();
        }

        Entry firstEntry = entries.get(0);
        List<Change> changes = firstEntry.getChanges();
        if (changes == null || changes.isEmpty()){
            return ResponseEntity.ok().build();
        }

        Change firstChange = changes.get(0);
        Value value = firstChange.getValue();

        List<Contact> contacts = value.getContacts();
        if (contacts == null || contacts.isEmpty()) {
            System.out.println("Evento sem contatos, ignorando.");
            return ResponseEntity.ok().build();
        }
        String sendNumber = contacts.get(0).getWa_id();
        String fixedNumber = fixBrazilianNumber(sendNumber);

        boolean allowed = rateLimitService.tryConsume(fixedNumber, 1);

        if (!allowed) {
            whatsappService.sendTextMessage(fixedNumber,
                    "Você excedeu o limite de consultas por hora. Tente novamente mais tarde.");
            return ResponseEntity.ok().build();
        }

        List<Message> messages = value.getMessages();
        if (messages == null || messages.isEmpty()) {
            System.out.println("Evento sem mensagens, ignorando.");
            return ResponseEntity.ok().build();
        }

        String messageText = messages.get(0).getText() != null ? messages.get(0).getText().getBody() : null;
        if (messageText == null || messageText.isEmpty()) {
            System.out.println("Mensagem sem texto, ignorando.");
            return ResponseEntity.ok().build();
        }

        if (messageText.matches("\\d+")) {
            int index = Integer.parseInt(messageText) - 1;
            List<MovieSearchResult> results = sessions.get(fixedNumber);
            if (results != null && index >= 0 && index < results.size()) {
                MovieSearchResult selected = results.get(index);
                MovieDetail detail = movieService.getMovieDetail(selected.getId());
                String reply = formatter.formatMovieDetail(detail);
                whatsappService.sendTextMessage(fixedNumber, reply);
                sessions.remove(fixedNumber);
            } else {
                whatsappService.sendTextMessage(fixedNumber, "Opção inválida. Tente novamente.");
            }
        } else {
            List<MovieSearchResult> results = movieService.searchMovies(messageText);
            System.out.println("Mensagem recebida: " + messageText);
            if (results.isEmpty()) {
                whatsappService.sendTextMessage(fixedNumber, "Nenhum filme encontrado.");
            } else {
                System.out.println("Pesquisando filmes...");
                sessions.put(fixedNumber, results);
                String reply = formatSearchResults(results, movieStats);
                whatsappService.sendTextMessage(fixedNumber, reply);
            }
        }
        return ResponseEntity.ok().build();
    }
}



