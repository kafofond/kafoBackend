package kafofond.controller;

import kafofond.dto.RapportAchatDTO;
import kafofond.dto.RapportAchatCreationDTO;
import kafofond.entity.RapportAchat;
import kafofond.entity.Utilisateur;
import kafofond.mapper.RapportAchatMapper;
import kafofond.service.RapportAchatService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller pour RapportAchat (anciennement PieceJustificativeController)
 */
@RestController
@RequestMapping("/api/rapports-achat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rapports d'Achat", description = "Gestion des rapports d'achat (pièces justificatives)")
@SecurityRequirement(name = "bearerAuth")
public class RapportAchatController {

    private final RapportAchatService rapportService;
    private final UtilisateurService utilisateurService;
    private final RapportAchatMapper mapper;

    @PostMapping
    @Operation(summary = "Créer un rapport d'achat")
    public ResponseEntity<?> creer(@RequestBody RapportAchatCreationDTO creationDTO, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Convertir le DTO de création en entité
            RapportAchat rapport = RapportAchat.builder()
                    .nom(creationDTO.getNom())
                    .ficheBesoin(creationDTO.getFicheBesoin())
                    .demandeAchat(creationDTO.getDemandeAchat())
                    .bonCommande(creationDTO.getBonCommande())
                    .attestationServiceFait(creationDTO.getAttestationServiceFait())
                    .decisionPrelevement(creationDTO.getDecisionPrelevement())
                    .ordrePaiement(creationDTO.getOrdrePaiement())
                    .build();
                    
            // Définir l'entreprise à partir de l'utilisateur authentifié
            rapport.setEntreprise(user.getEntreprise());
            
            RapportAchat rapportCree = rapportService.creer(rapport, user);
            
            // Créer manuellement le DTO pour éviter les problèmes de proxy
            RapportAchatDTO rapportCreeDTO = RapportAchatDTO.builder()
                    .id(rapportCree.getId())
                    .nom(rapportCree.getNom())
                    .ficheBesoin(rapportCree.getFicheBesoin())
                    .demandeAchat(rapportCree.getDemandeAchat())
                    .bonCommande(rapportCree.getBonCommande())
                    .attestationServiceFait(rapportCree.getAttestationServiceFait())
                    .decisionPrelevement(rapportCree.getDecisionPrelevement())
                    .ordrePaiement(rapportCree.getOrdrePaiement())
                    .dateAjout(rapportCree.getDateAjout())
                    .entrepriseNom(user.getEntreprise().getNom()) // Utiliser le nom de l'entreprise de l'utilisateur
                    .build();
                    
            return ResponseEntity.ok(rapportCreeDTO);
        } catch (Exception e) {
            log.error("Erreur lors de la création du rapport d'achat : {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping
    @Operation(summary = "Lister tous les rapports d'achat d'une entreprise")
    public ResponseEntity<?> lister(Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            List<RapportAchat> rapports = rapportService.listerParEntreprise(user.getEntreprise());
            
            // Créer manuellement les DTOs pour éviter les problèmes de proxy
            List<RapportAchatDTO> rapportsDTO = rapports.stream()
                    .map(rapport -> RapportAchatDTO.builder()
                            .id(rapport.getId())
                            .nom(rapport.getNom())
                            .ficheBesoin(rapport.getFicheBesoin())
                            .demandeAchat(rapport.getDemandeAchat())
                            .bonCommande(rapport.getBonCommande())
                            .attestationServiceFait(rapport.getAttestationServiceFait())
                            .decisionPrelevement(rapport.getDecisionPrelevement())
                            .ordrePaiement(rapport.getOrdrePaiement())
                            .dateAjout(rapport.getDateAjout())
                            .entrepriseNom(user.getEntreprise().getNom()) // Utiliser le nom de l'entreprise de l'utilisateur
                            .build())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", rapportsDTO.size());
            response.put("rapports", rapportsDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rapports d'achat : {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un rapport d'achat")
    public ResponseEntity<?> obtenir(@PathVariable Long id, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Optional<RapportAchat> rapport = rapportService.trouverParId(id);
            
            // Vérifier que le rapport appartient à l'entreprise de l'utilisateur
            if (rapport.isEmpty() || !rapport.get().getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.notFound().build();
            }
            
            // Créer manuellement le DTO pour éviter les problèmes de proxy
            RapportAchatDTO rapportDTO = RapportAchatDTO.builder()
                    .id(rapport.get().getId())
                    .nom(rapport.get().getNom())
                    .ficheBesoin(rapport.get().getFicheBesoin())
                    .demandeAchat(rapport.get().getDemandeAchat())
                    .bonCommande(rapport.get().getBonCommande())
                    .attestationServiceFait(rapport.get().getAttestationServiceFait())
                    .decisionPrelevement(rapport.get().getDecisionPrelevement())
                    .ordrePaiement(rapport.get().getOrdrePaiement())
                    .dateAjout(rapport.get().getDateAjout())
                    .entrepriseNom(user.getEntreprise().getNom()) // Utiliser le nom de l'entreprise de l'utilisateur
                    .build();
            
            return ResponseEntity.ok(rapportDTO);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du rapport d'achat : {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/document")
    @Operation(summary = "Lister les rapports par document")
    public ResponseEntity<?> listerParDocument(@RequestParam String document, Authentication auth) {
        try {
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Filtrer les rapports par entreprise de l'utilisateur
            List<RapportAchat> rapports = rapportService.listerParDocumentEtEntreprise(document, user.getEntreprise());
            
            // Créer manuellement les DTOs pour éviter les problèmes de proxy
            List<RapportAchatDTO> rapportsDTO = rapports.stream()
                    .map(rapport -> RapportAchatDTO.builder()
                            .id(rapport.getId())
                            .nom(rapport.getNom())
                            .ficheBesoin(rapport.getFicheBesoin())
                            .demandeAchat(rapport.getDemandeAchat())
                            .bonCommande(rapport.getBonCommande())
                            .attestationServiceFait(rapport.getAttestationServiceFait())
                            .decisionPrelevement(rapport.getDecisionPrelevement())
                            .ordrePaiement(rapport.getOrdrePaiement())
                            .dateAjout(rapport.getDateAjout())
                            .entrepriseNom(user.getEntreprise().getNom()) // Utiliser le nom de l'entreprise de l'utilisateur
                            .build())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", rapportsDTO.size());
            response.put("rapports", rapportsDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des rapports d'achat par document : {}", e.getMessage(), e);
            Map<String, String> err = new HashMap<>();
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}