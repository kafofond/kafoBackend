package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour la création des fiches de besoin
 * Structure d'entrée simplifiée pour l'API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheBesoinCreateDTO {
    
    private String serviceBeneficiaire;
    private String objet;
    private String description;
    private double montantEstime;
    private LocalDate dateAttendu;
    private String urlFichierJoint;
    
    // Désignations (optionnelles)
    private List<DesignationCreateDTO> designations;
}