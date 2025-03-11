package cinezap.com.br.cine_zap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayloadDto {
    private String userName;
    private String userPhoneNumber;
    private String textMessage;


}
