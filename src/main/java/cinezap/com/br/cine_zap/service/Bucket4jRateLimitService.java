package cinezap.com.br.cine_zap.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Bucket4jRateLimitService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bandwidth limit = Bandwidth.classic(
            100, // capacidade
            Refill.intervally(100, Duration.ofHours(1)) // recarga de 100 tokens a cada 1 hora
    );

    // Cria (ou obtém) um Bucket para o "clientKey" (pode ser telefone, IP, etc.)
    private Bucket getBucket(String clientKey) {
        return buckets.computeIfAbsent(clientKey, k ->
                Bucket.builder().addLimit(limit).build()
        );
    }

    /**
     * Tenta consumir 'numTokens' do balde. Retorna 'true' se conseguiu,
     * ou 'false' se o balde não tinha tokens suficientes.
     */
    public boolean tryConsume(String clientKey, long numTokens) {
        Bucket bucket = getBucket(clientKey);
        return bucket.tryConsume(numTokens);
    }
}
