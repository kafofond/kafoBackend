package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la création des attestations de service fait
 * Structure d'entrée simplifiée pour l'API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttestationServiceFaitCreateDTO {
    
    private String fournisseur;
    private String titre;
    private String constat;
    private LocalDate dateLivraison;
    private String urlFichierJoint;
    
    // Référence au bon de commande
    private Long bonDeCommandeId;
}