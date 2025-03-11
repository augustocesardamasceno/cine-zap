package cinezap.com.br.cine_zap.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    private String display_phone_number;
    private String phone_number_id;
}