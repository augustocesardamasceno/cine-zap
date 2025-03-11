package cinezap.com.br.cine_zap.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private String from;
    private String id;
    private String timestamp;
    private Text text;
    private String type;
}
