package cinezap.com.br.cine_zap.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entry {
    private String id;
    private List<Change> changes;
}
