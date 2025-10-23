package kafofond.dto;

import kafofond.entity.TableValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la table de validation
 * Représente une validation dans la table de validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableValidationDTO {
    
    private Long id;
    private Long validateurId;
    private String commentaire;
    private String statut;
    private Long idDocument;
    private String typeDocument;
    private LocalDateTime dateValidation;
    private String validateurNomComplet;
    private String validateurEmail;
    
    /**
     * Convertit une entité TableValidation en DTO
     * Cette méthode doit être appelée dans un contexte transactionnel
     */
    public static TableValidationDTO fromEntity(TableValidation validation) {
        TableValidationDTO dto = TableValidationDTO.builder()
                .id(validation.getId())
                .validateurId(validation.getValidateurId())
                .commentaire(validation.getCommentaire())
                .statut(validation.getStatut())
                .idDocument(validation.getIdDocument())
                .typeDocument(validation.getTypeDocument() != null ? validation.getTypeDocument().name() : null)
                .dateValidation(validation.getDateValidation())
                .build();
        
        // Gérer le validateur de manière sécurisée
        if (validation.getValidateur() != null) {
            dto.setValidateurId(validation.getValidateur().getId());
            dto.setValidateurEmail(validation.getValidateur().getEmail());
            dto.setValidateurNomComplet(
                validation.getValidateur().getPrenom() + " " + validation.getValidateur().getNom()
            );
        }
        
        return dto;
    }
}