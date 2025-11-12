package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "DTO pour les statistiques des demandes d'achat par statut")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesDemandesDTO {
    
    @Schema(description = "Nombre total de demandes d'achat", example = "10")
    private long total;
    
    @Schema(description = "Nombre de demandes d'achat en cours", example = "3")
    private long enCours;
    
    @Schema(description = "Nombre de demandes d'achat validées", example = "4")
    private long validees;
    
    @Schema(description = "Nombre de demandes d'achat approuvées", example = "2")
    private long approuvees;
    
    @Schema(description = "Nombre de demandes d'achat rejetées", example = "1")
    private long rejetees;
}