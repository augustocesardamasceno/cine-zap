package cinezap.com.br.cine_zap.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {
    private String messaging_product;
    private Metadata metadata;
    private List<Contact> contacts;
    private List<Message> messages;
}