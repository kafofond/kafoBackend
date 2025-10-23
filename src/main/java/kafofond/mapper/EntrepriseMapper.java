package kafofond.mapper;

import kafofond.dto.EntrepriseDTO;
import kafofond.entity.Entreprise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntrepriseMapper {

    public EntrepriseDTO toDTO(Entreprise entreprise) {
        if (entreprise == null) {
            return null;
        }

        return EntrepriseDTO.builder()
                .id(entreprise.getId())
                .nom(entreprise.getNom())
                .domaine(entreprise.getDomaine())
                .adresse(entreprise.getAdresse())
                .telephone(entreprise.getTelephone())
                .email(entreprise.getEmail())
                .dateCreation(entreprise.getDateCreation())
                .etat(entreprise.getEtat())
                .build();
    }

    public Entreprise toEntity(EntrepriseDTO dto) {
        if (dto == null) {
            return null;
        }

        return Entreprise.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .domaine(dto.getDomaine())
                .adresse(dto.getAdresse())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .dateCreation(dto.getDateCreation())
                .etat(dto.getEtat())
                .build();
    }

    public List<EntrepriseDTO> toDTOList(List<Entreprise> entreprises) {
        return entreprises.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Entreprise> toEntityList(List<EntrepriseDTO> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}