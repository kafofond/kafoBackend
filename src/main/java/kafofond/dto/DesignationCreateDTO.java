package kafofond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la création des désignations
 * Structure d'entrée simplifiée pour l'API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationCreateDTO {
    
    private String produit;
    private int quantite;
    private double prixUnitaire;
    private double montantTotal;
    private LocalDate date;
}