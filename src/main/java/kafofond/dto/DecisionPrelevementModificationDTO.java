package kafofond.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionPrelevementModificationDTO {
    private Long id;
    private Double montant;
    private String compteOrigine;
    private String compteDestinataire;
    private String motifPrelevement;
}