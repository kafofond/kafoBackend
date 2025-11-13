package kafofond.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComptableChartDataDTO {
    private List<String> labels;
    private Map<String, List<Integer>> datasets;
}