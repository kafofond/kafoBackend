package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "builder")
public class SeuilValidationDTO {
    private Long id;
    private double montantSeuil;
    private LocalDate dateCreation;
    private boolean actif;
    private String entrepriseNom;
}
