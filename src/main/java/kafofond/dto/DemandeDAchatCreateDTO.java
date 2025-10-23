package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la création des demandes d'achat
 * Structure d'entrée simplifiée pour l'API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeDAchatCreateDTO {
    
    private String description;
    private String fournisseur;
    private double montantTotal;
    private String serviceBeneficiaire;
    private LocalDate dateAttendu;
    private String urlFichierJoint;
    
    // Référence à la fiche de besoin (optionnel)
    private Long ficheBesoinId;
}