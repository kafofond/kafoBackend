package kafofond.mapper;

import kafofond.dto.AttestationServiceFaitDTO;
import kafofond.entity.AttestationDeServiceFait;
import kafofond.entity.BonDeCommande;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Mapper pour l'entité AttestationDeServiceFait
 * Convertit entre entité et DTO
 */
@Component
@Slf4j
public class AttestationServiceFaitMapper {

    /**
     * Convertit une entité AttestationDeServiceFait en DTO
     */
    public AttestationServiceFaitDTO toDTO(AttestationDeServiceFait attestation) {
        if (attestation == null) {
            return null;
        }
        
        // Éviter les accès directs aux propriétés d'entreprise en mode lazy
        String entrepriseNom = null;
        // Ne pas tenter d'accéder à l'entreprise pour éviter les problèmes de proxy
        
        return AttestationServiceFaitDTO.builder()
                .id(attestation.getId())
                .code(attestation.getCode())
                .referenceBonCommande(attestation.getReferenceBonCommande())
                .fournisseur(attestation.getFournisseur())
                .titre(attestation.getTitre())
                .constat(attestation.getConstat())
                .dateLivraison(attestation.getDateLivraison())
                .dateCreation(LocalDate.from(attestation.getDateCreation()))
                .urlFichierJoint(attestation.getUrlFichierJoint())
                .createurNom(attestation.getCreePar() != null ? 
                    attestation.getCreePar().getPrenom() + " " + attestation.getCreePar().getNom() : null)
                .createurEmail(attestation.getCreePar() != null ? attestation.getCreePar().getEmail() : null)
                .entrepriseNom(entrepriseNom) // Toujours null pour éviter les problèmes de proxy
                .bonDeCommandeId(attestation.getBonDeCommande() != null ? attestation.getBonDeCommande().getId() : null)
                .build();
    }

    /**
     * Convertit un DTO en entité AttestationDeServiceFait
     */
    public AttestationDeServiceFait toEntity(AttestationServiceFaitDTO attestationDTO) {
        if (attestationDTO == null) {
            return null;
        }
        
        AttestationDeServiceFait.AttestationDeServiceFaitBuilder builder = AttestationDeServiceFait.builder()
                .id(attestationDTO.getId())
                .code(attestationDTO.getCode())
                .referenceBonCommande(attestationDTO.getReferenceBonCommande())
                .fournisseur(attestationDTO.getFournisseur())
                .titre(attestationDTO.getTitre())
                .constat(attestationDTO.getConstat())
                .dateLivraison(attestationDTO.getDateLivraison())
                .dateCreation(attestationDTO.getDateCreation().atStartOfDay())
                .urlFichierJoint(attestationDTO.getUrlFichierJoint());
        
        // Si un bonDeCommandeId est fourni, le lier
        if (attestationDTO.getBonDeCommandeId() != null) {
            // Créer un bon de commande avec seulement l'ID pour éviter les problèmes de proxy
            BonDeCommande bonDeCommande = new BonDeCommande();
            bonDeCommande.setId(attestationDTO.getBonDeCommandeId());
            builder.bonDeCommande(bonDeCommande);
        }
        
        return builder.build();
    }
}