package kafofond.mapper;

import kafofond.dto.UtilisateurDTO;
import kafofond.entity.Entreprise;
import kafofond.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;

        UtilisateurDTO dto = UtilisateurDTO.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .departement(utilisateur.getDepartement())
                .role(utilisateur.getRole())
                .actif(utilisateur.isEtat())
                .build();
        
        // Gérer l'entreprise de manière sécurisée
        if (utilisateur.getEntreprise() != null) {
            dto.setEntrepriseId(utilisateur.getEntreprise().getId());
            // Ne pas accéder au nom de l'entreprise ici pour éviter le problème de proxy
            // Le nom de l'entreprise sera récupéré de manière transactionnelle si nécessaire
        }

        return dto;
    }

    public Utilisateur toEntity(UtilisateurDTO dto) {
        if (dto == null) return null;

        Utilisateur.UtilisateurBuilder builder = Utilisateur.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(dto.getMotDePasse())
                .departement(dto.getDepartement())
                .role(dto.getRole())
                .etat(dto.isActif());
        
        // Si entrepriseId est fourni, créer un objet Entreprise avec cet ID
        if (dto.getEntrepriseId() != null) {
            builder.entreprise(Entreprise.builder().id(dto.getEntrepriseId()).build());
        }
        
        return builder.build();
    }
}