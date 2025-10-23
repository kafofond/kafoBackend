package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les ordres de paiement
 * Représente un ordre de paiement dans les réponses API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreDePaiementDTO {
    
    private Long id;
    private String code;
    private String referenceDecisionPrelevement;
    private double montant;
    private String description;
    private String compteOrigine;
    private String compteDestinataire;
    private LocalDate dateExecution;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;
    private Statut statut;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;
    private Long decisionId;
}