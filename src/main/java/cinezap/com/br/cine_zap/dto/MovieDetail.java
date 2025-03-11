package cinezap.com.br.cine_zap.dto;

import lombok.Data;

import java.util.List;

@Data
public class MovieDetail {
    private String id;
    private Double ratingsSummary;
    private List<Review> reviews;

}

