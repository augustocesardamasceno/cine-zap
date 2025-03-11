package cinezap.com.br.cine_zap.dto;

import lombok.Data;

@Data
public class MovieStats {
    private String imdbId;
    private Double imdbScore;
    private Double metaScore;
    private Double userScore;
    private Double tomatometer;
    private Double audienceScore;
    private Double score;
}
