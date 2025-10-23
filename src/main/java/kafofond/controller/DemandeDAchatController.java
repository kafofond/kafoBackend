package kafofond.controller;

import kafofond.entity.DemandeDAchat;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import kafofond.dto.DemandeDAchatDTO;
import kafofond.dto.DemandeDAchatCreateDTO;
import kafofond.dto.CommentaireSimplifieDTO;
import kafofond.service.DemandeDAchatService;
import kafofond.service.CommentaireService;
import kafofond.mapper.DemandeDAchatMapper;
import kafofond.mapper.CommentaireMapper;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller pour la gestion des demandes d'achat
 * Workflow : EN_COURS → VALIDÉ par Gestionnaire → APPROUVÉ par Comptable → génère BonDeCommande
 */
@RestController
@RequestMapping("/api/demandes-achat")
@RequiredArgsConstructor
@Slf4j
public class DemandeDAchatController {

    private final DemandeDAchatService demandeDAchatService;
    private final UtilisateurService utilisateurService;
    private final CommentaireService commentaireService;
    private final DemandeDAchatMapper demandeDAchatMapper;
    private final CommentaireMapper commentaireMapper;

    /**
     * Crée une nouvelle demande d'achat (Trésorerie et Gestionnaire)
     */
    @PostMapping
    public ResponseEntity<?> creerDemandeAchat(@RequestBody DemandeDAchatCreateDTO demandeDTO, Authentication authentication) {
        try {
            log.info("Création d'une demande d'achat par {}", authentication.getName());
            
            // Convertir le DTO de création en entité
            DemandeDAchat demande = DemandeDAchat.builder()
                    .description(demandeDTO.getDescription())
                    .fournisseur(demandeDTO.getFournisseur())
                    .montantTotal(demandeDTO.getMontantTotal())
                    .serviceBeneficiaire(demandeDTO.getServiceBeneficiaire())
                    .dateAttendu(demandeDTO.getDateAttendu())
                    .urlFichierJoint(demandeDTO.getUrlFichierJoint())
                    .dateCreation(LocalDate.now())
                    .build();
            
            // Si une fiche de besoin est référencée, la lier
            if (demandeDTO.getFicheBesoinId() != null) {
                kafofond.entity.FicheDeBesoin fiche = new kafofond.entity.FicheDeBesoin();
                fiche.setId(demandeDTO.getFicheBesoinId());
                demande.setFicheDeBesoin(fiche);
            }
            
            DemandeDAchatDTO dto = demandeDAchatService.creerDTO(demande, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande d'achat créée avec succès");
            response.put("demande", dto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la création de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Modifie une demande d'achat existante (Trésorerie et Gestionnaire)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierDemandeAchat(@PathVariable Long id, @RequestBody DemandeDAchatCreateDTO demandeDTO, 
                                                Authentication authentication) {
        try {
            log.info("Modification de la demande d'achat {} par {}", id, authentication.getName());
            
            // Convertir le DTO de création en entité pour la modification
            DemandeDAchat demande = DemandeDAchat.builder()
                    .description(demandeDTO.getDescription())
                    .fournisseur(demandeDTO.getFournisseur())
                    .montantTotal(demandeDTO.getMontantTotal())
                    .serviceBeneficiaire(demandeDTO.getServiceBeneficiaire())
                    .dateAttendu(demandeDTO.getDateAttendu())
                    .urlFichierJoint(demandeDTO.getUrlFichierJoint())
                    .build();
            
            // Si une fiche de besoin est référencée, la lier
            if (demandeDTO.getFicheBesoinId() != null) {
                kafofond.entity.FicheDeBesoin fiche = new kafofond.entity.FicheDeBesoin();
                fiche.setId(demandeDTO.getFicheBesoinId());
                demande.setFicheDeBesoin(fiche);
            }
            
            DemandeDAchatDTO dto = demandeDAchatService.modifierDTO(id, demande, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande d'achat modifiée avec succès");
            response.put("demande", dto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valide une demande d'achat (Gestionnaire uniquement)
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerDemandeAchat(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Validation de la demande d'achat {} par {}", id, authentication.getName());
            
            DemandeDAchatDTO dto = demandeDAchatService.validerDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande d'achat validée avec succès");
            response.put("demande", dto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approuve une demande d'achat (Comptable uniquement) et génère automatiquement le Bon de Commande
     */
    @PostMapping("/{id}/approuver")
    public ResponseEntity<?> approuverDemandeAchat(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Approbation de la demande d'achat {} par {}", id, authentication.getName());
            
            DemandeDAchatDTO dto = demandeDAchatService.approuverDTO(id, authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande d'achat approuvée avec succès. Un bon de commande a été généré automatiquement.");
            response.put("demande", dto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rejette une demande d'achat avec commentaire obligatoire
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterDemandeAchat(@PathVariable Long id, @RequestBody Map<String, String> request, 
                                                Authentication authentication) {
        try {
            log.info("Rejet de la demande d'achat {} par {}", id, authentication.getName());
            
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }
            
            DemandeDAchatDTO dto = demandeDAchatService.rejeterDTO(id, authentication.getName(), commentaire);
            
            // Récupérer les commentaires simplifiés pour le DTO
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.DEMANDE_ACHAT)
                    .stream().map(commentaireMapper::toSimplifieDTO).toList();
            dto.setCommentaires(commentaires);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Demande d'achat rejetée avec succès");
            response.put("demande", dto);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste toutes les demandes d'achat de l'entreprise
     */
    @GetMapping
    public ResponseEntity<?> listerDemandesAchat(Authentication authentication) {
        try {
            log.info("Liste des demandes d'achat demandée par {}", authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            List<DemandeDAchat> demandes = demandeDAchatService.listerParEntreprise(utilisateur.getEntreprise());
            
            // Convertir en DTOs et récupérer les commentaires simplifiés pour chaque demande
            List<DemandeDAchatDTO> dtos = demandes.stream()
                    .map(demande -> {
                        DemandeDAchatDTO dto = demandeDAchatMapper.toDTO(demande);
                        var commentaires = commentaireService.getCommentairesByDocument(demande.getId(), TypeDocument.DEMANDE_ACHAT)
                                .stream().map(commentaireMapper::toSimplifieDTO).toList();
                        dto.setCommentaires(commentaires);
                        return dto;
                    })
                    .toList();
            
            Map<String, Object> response = new HashMap<>();
            response.put("demandes", dtos);
            response.put("total", dtos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des demandes d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'une demande d'achat
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirDemandeAchat(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails de la demande d'achat {} demandés par {}", id, authentication.getName());
            
            Utilisateur utilisateur = utilisateurService.trouverParEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            Optional<DemandeDAchat> demande = demandeDAchatService.trouverParId(id);
            
            // Vérifier que la demande appartient à l'entreprise de l'utilisateur
            if (demande.isEmpty() || 
                !demande.get().getEntreprise().getId().equals(utilisateur.getEntreprise().getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Demande d'achat introuvable");
                return ResponseEntity.notFound().build();
            }
            
            // Convertir en DTO
            DemandeDAchatDTO dto = demandeDAchatMapper.toDTO(demande.get());
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.DEMANDE_ACHAT)
                    .stream().map(commentaireMapper::toSimplifieDTO).toList();
            dto.setCommentaires(commentaires);
            return ResponseEntity.ok(dto);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la demande d'achat : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste les commentaires d'une demande d'achat
     */
    @GetMapping("/{id}/commentaires")
    public ResponseEntity<?> listerCommentaires(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Liste des commentaires de la demande d'achat {} demandée par {}", id, authentication.getName());

            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.DEMANDE_ACHAT)
                    .stream().map(commentaireMapper::toSimplifieDTO).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("commentaires", commentaires);
            response.put("total", commentaires.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des commentaires : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}