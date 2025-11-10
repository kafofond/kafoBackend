package kafofond.dto;

import kafofond.entity.Statut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCreditDTO {
    private Long id;
    private String code;
    private String intituleLigne;
    private String description;
    private double montantAllouer;
    private double montantEngager;
    private double montantRestant;
    private LocalDate dateCreation;
    private LocalDateTime dateModification;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Statut statut;
    private boolean actif;
    private Long budgetId;
    private String createurNom;
    private String createurEmail;
    private String entrepriseNom;
    private List<CommentaireDTO> commentaires; // récupérés via le service
}