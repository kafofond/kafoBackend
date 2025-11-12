package kafofond.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kafofond.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données nécessaires pour créer un utilisateur")
public class UtilisateurCreationDTO {

    @Schema(description = "Nom de l'utilisateur", example = "Dupont")
    private String nom;

    @Schema(description = "Prénom de l'utilisateur", example = "Jean")
    private String prenom;

    @Schema(description = "Email de l'utilisateur", example = "jean.dupont@entreprise.com")
    private String email;

    @Schema(description = "Mot de passe de l'utilisateur", example = "motdepasse123")
    private String motDePasse;

    @Schema(description = "Confirmation du mot de passe", example = "motdepasse123")
    private String confirmationMotDePasse;

    @Schema(description = "Département de l'utilisateur", example = "Finance")
    private String departement;

    @Schema(description = "Rôle de l'utilisateur", example = "COMPTABLE")
    private Role role;

    @Schema(description = "ID de l'entreprise (uniquement pour le SUPER_ADMIN)", example = "1")
    private Long entrepriseId;
}