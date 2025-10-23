package kafofond.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * DTO pour RapportAchat (anciennement PieceJustificativeDTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Rapport d'achat avec tous les documents d'une dépense")
public class RapportAchatDTO {

    private Long id;

    private String nom;

    private String ficheBesoin;
    private String demandeAchat;
    private String bonCommande;
    private String attestationServiceFait;
    private String decisionPrelevement;
    private String ordrePaiement;

    private LocalDate dateAjout;

    private String entrepriseNom;
}
