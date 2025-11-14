package kafofond.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GestionnaireChartDataDTO {
    
    private List<String> labels;
    private Map<String, List<Integer>> datasets;
}