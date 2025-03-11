package cinezap.com.br.cine_zap.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Text {
    private String body;
}
