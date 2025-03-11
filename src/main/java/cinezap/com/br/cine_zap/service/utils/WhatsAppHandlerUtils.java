package cinezap.com.br.cine_zap.service.utils;

import cinezap.com.br.cine_zap.dto.MovieDetail;
import cinezap.com.br.cine_zap.dto.MovieSearchResult;
import cinezap.com.br.cine_zap.dto.MovieStats;
import cinezap.com.br.cine_zap.dto.Review;
import cinezap.com.br.cine_zap.service.MovieStatsService;
import cinezap.com.br.cine_zap.service.TranslateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class WhatsAppHandlerUtils {

    @Autowired
    private TranslateService translateService;

    private WhatsAppHandlerUtils() {
    }

    public static String fixBrazilianNumber(String numero) {
        if (numero != null
                && numero.startsWith("55")
                && numero.length() == 12)
        {
            String pais = numero.substring(0, 2);
            String ddd  = numero.substring(2, 4);
            String restante = numero.substring(4);
            return pais + ddd + "9" + restante;
        }
        return numero;
    }

    public static String formatSearchResults(List<MovieSearchResult> results, MovieStatsService movieStats) {
        StringBuilder sb = new StringBuilder("*Filmes encontrados:*\n\n");
        int count = Math.min(5, results.size());
        for (int i = 0; i < count; i++) {
            sb.append(i + 1).append(" - ").append("*").append(results.get(i).getTitle()).append("*").append("\n\n");
            sb.append("Elenco: \n");
            sb.append("- ").append(results.get(i).getCast()).append("\n");
            sb.append("Avaliações: \n");

                MovieStats stats = movieStats.getMovieStats(results.get(i).getId());

                if(stats.getImdbScore().equals(0.0)){
                    sb.append("- Ainda sem reviews\n\n");
                } else {
                    sb.append("- IMDB: ")
                            .append(stats.getImdbScore())
                            .append("\n");

                    sb.append("- Metacritic: ")
                            .append(stats.getMetaScore())
                            .append(" (Críticos), ")
                            .append(stats.getUserScore())
                            .append(" (Usuários)\n");

                    sb.append("- Rotten Tomatoes: ")
                            .append(stats.getTomatometer())
                            .append(" (Críticos), ")
                            .append(stats.getAudienceScore())
                            .append(" (Usuários)\n");

                    sb.append("- Letterboxd: ")
                            .append(stats.getScore()).append(" (0-5)")
                            .append("\n\n");
                }

        }

        sb.append("\nPara ler algumas avaliações, envie o número ao lado esquerdo do título do filme.");
        return sb.toString();
    }

    private static final int MAX_LENGTH = 4000;

    public String formatMovieDetail(MovieDetail detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("*DETALHES DO FILME:* \n");
        sb.append("*Avaliação:* ").append(detail.getRatingsSummary()).append("\n");
        sb.append("*Reviews:*\n\n");

        if (detail.getReviews() != null && !detail.getReviews().isEmpty()) {
            for (int i = 0; i < 1; i++) {
                Review review = detail.getReviews().get(i);
                sb.append("*").append("@").append(review.getNickName()).append("*").append(":\n");

                String plainText = review.getPlainText();
                if (plainText == null || plainText.trim().isEmpty()) {
                    sb.append(" ").append("Sem texto para traduzir.").append("\n");
                } else {
                    String translatedText = String.valueOf(translateService.translateReviews(plainText));
                    String[] lines = translatedText.split("\\r?\\n");
                    for (String line : lines) {
                        if (line.length() > 500) {
                            line = line.substring(0, 500) + "...";
                            sb.append(line).append("\n");
                            sb.append("\n").append("*---- DIVISÃO DE REVIEWS ----*").append("\n");;
                            break;
                        }
                        sb.append(line).append("\n");
                    }
                    sb.append("\n");
                    sb.append("Todos os reviews completos:\n");
                    sb.append("https://www.imdb.com/title/").append(detail.getId()).append("/reviews/?ref_=tt_ururv_sm");
                }
            }
        } else {
            sb.append("Nenhuma review disponível.\n");
        }

        if (sb.length() > MAX_LENGTH) {
            sb.setLength(MAX_LENGTH);
            sb.append("...");
        }
        return sb.toString();
    }

}

