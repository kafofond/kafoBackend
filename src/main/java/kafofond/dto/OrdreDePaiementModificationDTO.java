package kafofond.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreDePaiementModificationDTO {
    private Long id;
    private Double montant;
    private String description;
    private String compteOrigine;
    private String compteDestinataire;
}