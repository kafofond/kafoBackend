package kafofond.dto;

import kafofond.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String departement;
    private Role role;
    private boolean actif;
    private Long entrepriseId;
    private String entrepriseNom;
}