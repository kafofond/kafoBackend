package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "DTO simplifié pour la création d'un seuil de validation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeuilValidationCreateDTO {

    @Schema(description = "Montant du seuil de validation", example = "5000000")
    private double montantSeuil;
}