package kafofond.mapper;

import kafofond.dto.DemandeDAchatDTO;
import kafofond.entity.DemandeDAchat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper pour l'entité DemandeDAchat
 * Convertit entre entité et DTO
 */
@Component
public class DemandeDAchatMapper {

    /**
     * Convertit une entité DemandeDAchat en DTO
     */
    public DemandeDAchatDTO toDTO(DemandeDAchat demande) {
        if (demande == null) {
            return null;
        }
        
        return DemandeDAchatDTO.builder()
                .id(demande.getId())
                .code(demande.getCode())
                .description(demande.getDescription())
                .fournisseur(demande.getFournisseur())
                .montantTotal(demande.getMontantTotal())
                .serviceBeneficiaire(demande.getServiceBeneficiaire())
                .dateCreation(LocalDate.from(demande.getDateCreation()))
                .dateAttendu(demande.getDateAttendu())
                .statut(demande.getStatut())
                .urlFichierJoint(demande.getUrlFichierJoint())
                .createurNom(demande.getCreePar() != null ? 
                    demande.getCreePar().getPrenom() + " " + demande.getCreePar().getNom() : null)
                .createurEmail(demande.getCreePar() != null ? demande.getCreePar().getEmail() : null)
                .entrepriseNom(demande.getEntreprise() != null ? demande.getEntreprise().getNom() : null)
                .ficheBesoinId(demande.getFicheDeBesoin() != null ? demande.getFicheDeBesoin().getId() : null)
                .ficheBesoinCode(demande.getFicheDeBesoin() != null ? demande.getFicheDeBesoin().getCode() : null)
                .build();
    }

    /**
     * Convertit un DTO en entité DemandeDAchat
     */
    public DemandeDAchat toEntity(DemandeDAchatDTO demandeDTO) {
        if (demandeDTO == null) {
            return null;
        }
        
        DemandeDAchat.DemandeDAchatBuilder builder = DemandeDAchat.builder()
                .id(demandeDTO.getId())
                .code(demandeDTO.getCode())
                .description(demandeDTO.getDescription())
                .fournisseur(demandeDTO.getFournisseur())
                .montantTotal(demandeDTO.getMontantTotal())
                .serviceBeneficiaire(demandeDTO.getServiceBeneficiaire())
                .dateCreation(demandeDTO.getDateCreation().atStartOfDay())
                .dateAttendu(demandeDTO.getDateAttendu())
                .statut(demandeDTO.getStatut())
                .urlFichierJoint(demandeDTO.getUrlFichierJoint());
        
        // Si un ficheBesoinId est fourni, créer un objet FicheDeBesoin avec cet ID
        if (demandeDTO.getFicheBesoinId() != null) {
            kafofond.entity.FicheDeBesoin fiche = kafofond.entity.FicheDeBesoin.builder()
                    .id(demandeDTO.getFicheBesoinId())
                    .build();
            builder.ficheDeBesoin(fiche);
        }
        
        return builder.build();
    }
}
