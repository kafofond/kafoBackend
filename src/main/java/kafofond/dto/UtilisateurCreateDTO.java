package kafofond.dto;

import kafofond.entity.Role;
import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurCreateDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String prenom;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir entre 6 et 100 caractères")
    private String motDePasse;

    @Size(max = 100, message = "Le département ne peut pas dépasser 100 caractères")
    private String departement;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    // L'ID de l'entreprise est optionnel
    // - Pour un SUPER_ADMIN : il doit être spécifié
    // - Pour un ADMIN/DIRECTEUR : il est automatiquement associé à leur entreprise
    private Long entrepriseId;
}