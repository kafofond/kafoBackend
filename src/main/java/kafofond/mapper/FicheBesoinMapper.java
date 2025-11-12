package kafofond.mapper;

import kafofond.dto.DesignationDTO;
import kafofond.dto.FicheBesoinDTO;
import kafofond.entity.Designation;
import kafofond.entity.FicheDeBesoin;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour l'entité FicheDeBesoin
 * Convertit entre entité et DTO
 */
@Component
public class FicheBesoinMapper {

    /**
     * Convertit une entité FicheDeBesoin en DTO
     */
    public FicheBesoinDTO toDTO(FicheDeBesoin fiche) {
        if (fiche == null) {
            return null;
        }
        
        // Mapper les désignations
        List<DesignationDTO> designationsDTO = null;
        if (fiche.getDesignations() != null && !fiche.getDesignations().isEmpty()) {
            designationsDTO = fiche.getDesignations().stream()
                    .map(d -> DesignationDTO.builder()
                            .id(d.getId())
                            .produit(d.getProduit())
                            .quantite(d.getQuantite())
                            .prixUnitaire(d.getPrixUnitaire())
                            .montantTotal(d.getMontantTotal())
                            .date(d.getDate())
                            .ficheBesoinId(fiche.getId())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return FicheBesoinDTO.builder()
                .id(fiche.getId())
                .code(fiche.getCode())
                .serviceBeneficiaire(fiche.getServiceBeneficiaire())
                .objet(fiche.getObjet())
                .description(fiche.getDescription())
                .montantEstime(fiche.getMontantEstime())
                .dateAttendu(fiche.getDateAttendu())
                .dateCreation(LocalDate.from(fiche.getDateCreation()))
                .statut(fiche.getStatut())
                .createurNom(fiche.getCreePar() != null ? 
                    fiche.getCreePar().getPrenom() + " " + fiche.getCreePar().getNom() : null)
                .createurEmail(fiche.getCreePar() != null ? fiche.getCreePar().getEmail() : null)
                .entrepriseNom(fiche.getEntreprise() != null ? fiche.getEntreprise().getNom() : null)
                .designations(designationsDTO)
                .build();
    }

    /**
     * Convertit un DTO en entité FicheDeBesoin
     */
    public FicheDeBesoin toEntity(FicheBesoinDTO ficheDTO) {
        if (ficheDTO == null) {
            return null;
        }
        
        FicheDeBesoin fiche = FicheDeBesoin.builder()
                .id(ficheDTO.getId())
                .code(ficheDTO.getCode())
                .serviceBeneficiaire(ficheDTO.getServiceBeneficiaire())
                .objet(ficheDTO.getObjet())
                .description(ficheDTO.getDescription())
                .montantEstime(ficheDTO.getMontantEstime())
                .dateAttendu(ficheDTO.getDateAttendu())
                .dateCreation(ficheDTO.getDateCreation().atStartOfDay())
                .statut(ficheDTO.getStatut())
                .designations(new ArrayList<>())
                .build();
        
        // Mapper les désignations si elles existent
        if (ficheDTO.getDesignations() != null && !ficheDTO.getDesignations().isEmpty()) {
            List<Designation> designations = ficheDTO.getDesignations().stream()
                    .map(dto -> {
                        Designation d = Designation.builder()
                                .id(dto.getId())
                                .produit(dto.getProduit())
                                .quantite(dto.getQuantite())
                                .prixUnitaire(dto.getPrixUnitaire())
                                .montantTotal(dto.getMontantTotal())
                                .date(dto.getDate())
                                .ficheDeBesoin(fiche)
                                .build();
                        return d;
                    })
                    .collect(Collectors.toList());
            fiche.setDesignations(designations);
        }
        
        return fiche;
    }
}
