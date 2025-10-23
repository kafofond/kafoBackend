package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour les fiches de besoin
 * Représente une fiche de besoin dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheBesoinDTO {
    
    private Long id;
    private String code;
    private String serviceBeneficiaire;
    private String objet;
    private String description;
    // quantite supprimé - désormais dans les désignations
    private double montantEstime;
    private LocalDate dateAttendu;
    private LocalDate dateCreation;
    private Statut statut;
    private String urlFichierJoint;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;
    
    // Liste des désignations (produits/services demandés)
    private List<DesignationDTO> designations;
    
    // Commentaires associés
    private List<CommentaireSimplifieDTO> commentaires;
}