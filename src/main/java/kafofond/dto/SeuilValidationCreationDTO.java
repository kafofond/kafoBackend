package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer un seuil de validation")
public class SeuilValidationCreationDTO {

    @Schema(description = "Montant du seuil de validation", example = "5000.0")
    private double montantSeuil;
}