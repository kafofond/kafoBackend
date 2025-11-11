package kafofond.controller;

import kafofond.dto.BonDeCommandeDTO;
import kafofond.dto.CommentaireDTO;
import kafofond.entity.BonDeCommande;
import kafofond.entity.TypeDocument;
import kafofond.entity.Utilisateur;
import kafofond.mapper.BonDeCommandeMapper;
import kafofond.mapper.CommentaireMapper;
import kafofond.service.BonDeCommandeService;
import kafofond.service.CommentaireService;
import kafofond.service.DocumentService;
import kafofond.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion des bons de commande
 * Workflow : EN_COURS → VALIDÉ par Comptable → APPROUVÉ par Responsable → génère PDF
 */
@RestController
@RequestMapping("/api/bons-commande")
@RequiredArgsConstructor
@Slf4j
public class BonDeCommandeController {

    private final BonDeCommandeService bonDeCommandeService;
    private final UtilisateurService utilisateurService;
    private final BonDeCommandeMapper bonDeCommandeMapper;
    private final CommentaireService commentaireService;
    private final CommentaireMapper commentaireMapper;
    private final DocumentService documentService; // Ajout du service de document

    /**
     * Liste tous les bons de commande d'une entreprise spécifique
     */
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<?> listerBonsDeCommandeParEntreprise(@PathVariable Long entrepriseId, Authentication authentication) {
        try {
            log.info("Liste des bons de commande de l'entreprise {} demandée par {}", 
                    entrepriseId, authentication.getName());
            
            // Vérifier que l'utilisateur a le droit d'accéder à cette entreprise
            Utilisateur utilisateur = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Vérifier que l'utilisateur appartient à l'entreprise ou est admin
            if (!utilisateur.getEntreprise().getId().equals(entrepriseId) && 
                utilisateur.getRole() != kafofond.entity.Role.SUPER_ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Accès non autorisé à cette entreprise");
                return ResponseEntity.status(403).body(error);
            }
            
            // Utiliser la méthode du service pour récupérer les bons de commande
            List<BonDeCommande> bons = bonDeCommandeService.listerParEntrepriseId(entrepriseId);
            
            // Convertir en DTOs
            List<BonDeCommandeDTO> bonsDTO = bons.stream()
                    .map(bonDeCommandeMapper::toDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("bons", bonsDTO);
            response.put("total", bonsDTO.size());
            response.put("entrepriseId", entrepriseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des bons de commande par entreprise : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Liste tous les bons de commande de l'entreprise
     */
    @GetMapping
    public ResponseEntity<?> listerBonsDeCommande(Authentication authentication) {
        try {
            log.info("Liste des bons de commande demandée par {}", authentication.getName());
            
            // Utiliser la méthode DTO du service pour éviter les problèmes de lazy loading
            List<BonDeCommandeDTO> bonsDTO = bonDeCommandeService.listerParEntrepriseDTO(authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("bons", bonsDTO);
            response.put("total", bonsDTO.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des bons de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Récupère les détails d'un bon de commande
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirBonDeCommande(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Détails du bon de commande {} demandés par {}", id, authentication.getName());
            
            // Récupérer l'utilisateur avec son entreprise
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Utiliser la méthode DTO du service pour éviter les problèmes de lazy loading
            BonDeCommandeDTO bonDTO = bonDeCommandeService.obtenirBonDeCommandeDTO(id);
            
            // Vérifier que le bon appartient à l'entreprise de l'utilisateur
            if (bonDTO == null || 
                (bonDTO.getEntrepriseNom() != null && 
                 !bonDTO.getEntrepriseNom().equals(user.getEntreprise().getNom()))) {
                return ResponseEntity.notFound().build();
            }
            
            // Inclure les commentaires
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE);
            bonDTO.setCommentaires(commentaires.stream().map(commentaireMapper::toDTO).toList());
            
            return ResponseEntity.ok(bonDTO);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du bon de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Personnalise un bon de commande (Comptable, Responsable, Directeur)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> personnaliserBonDeCommande(@PathVariable Long id, @RequestBody BonDeCommandeDTO bonDTO, 
                                                      Authentication authentication) {
        try {
            log.info("Personnalisation du bon de commande {} par {}", id, authentication.getName());
            
            // Récupérer l'utilisateur avec son entreprise
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
            
            // Récupérer le bon de commande existant pour vérifier l'entreprise
            BonDeCommandeDTO bonExistant = bonDeCommandeService.obtenirBonDeCommandeDTO(id);
            
            // Vérifier que le bon appartient à l'entreprise de l'utilisateur
            if (bonExistant == null || 
                (bonExistant.getEntrepriseNom() != null && 
                 !bonExistant.getEntrepriseNom().equals(user.getEntreprise().getNom()))) {
                return ResponseEntity.notFound().build();
            }
            
            // Utiliser la méthode DTO du service pour éviter les problèmes de lazy loading
            BonDeCommandeDTO bonPersonnaliseDTO = bonDeCommandeService.personnaliserDTO(id, bonDTO, authentication.getName());
            
            // Inclure les commentaires
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE);
            bonPersonnaliseDTO.setCommentaires(commentaires.stream().map(commentaireMapper::toDTO).toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bon de commande personnalisé avec succès");
            response.put("bon", bonPersonnaliseDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la personnalisation du bon de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Valide un bon de commande (Comptable - première étape)
     */
    @PostMapping("/{id}/valider")
    public ResponseEntity<?> validerBonDeCommande(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Validation du bon de commande {} par {}", id, authentication.getName());
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            BonDeCommande bonValide = bonDeCommandeService.validerDTO(id, authentication.getName());
            BonDeCommandeDTO bonValideDTO = bonDeCommandeMapper.toDTO(bonValide);
            
            // Inclure les commentaires
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE);
            bonValideDTO.setCommentaires(commentaires.stream().map(commentaireMapper::toDTO).toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bon de commande validé avec succès. En attente d'approbation.");
            response.put("bon", bonValideDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la validation du bon de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Approuve un bon de commande (Responsable - deuxième étape)
     */
    @PostMapping("/{id}/approuver")
    public ResponseEntity<?> approuverBonDeCommande(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Approbation du bon de commande {} par {}", id, authentication.getName());
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            BonDeCommande bonApprouve = bonDeCommandeService.approuverDTO(id, authentication.getName());
            BonDeCommandeDTO bonApprouveDTO = bonDeCommandeMapper.toDTO(bonApprouve);
            
            // Inclure les commentaires
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE);
            bonApprouveDTO.setCommentaires(commentaires.stream().map(commentaireMapper::toDTO).toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bon de commande approuvé avec succès. PDF généré automatiquement.");
            response.put("bon", bonApprouveDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation du bon de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Rejette un bon de commande avec commentaire obligatoire
     */
    @PostMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterBonDeCommande(@PathVariable Long id, @RequestBody Map<String, String> request, 
                                                Authentication authentication) {
        try {
            log.info("Rejet du bon de commande {} par {}", id, authentication.getName());
            
            String commentaire = request.get("commentaire");
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet");
            }
            
            // Utiliser la méthode DTO pour éviter les problèmes de lazy loading
            BonDeCommande bonRejete = bonDeCommandeService.rejeterDTO(id, authentication.getName(), commentaire);
            BonDeCommandeDTO bonRejeteDTO = bonDeCommandeMapper.toDTO(bonRejete);
            
            // Inclure les commentaires
            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE);
            bonRejeteDTO.setCommentaires(commentaires.stream().map(commentaireMapper::toDTO).toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bon de commande rejeté avec succès");
            response.put("bon", bonRejeteDTO);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors du rejet du bon de commande : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Génère le PDF d'un bon de commande
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> genererPdfBonDeCommande(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Génération du PDF pour le bon de commande {} par {}", id, authentication.getName());
            
            String urlPdf = bonDeCommandeService.genererPdf(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "PDF généré avec succès");
            response.put("urlPdf", urlPdf);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF : {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Télécharge le PDF d'un bon de commande
     * Endpoint pour compatibilité avec les anciens clients
     */
    @GetMapping("/{id}/pdf/download")
    public ResponseEntity<?> telechargerPdfBonDeCommande(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Téléchargement du PDF pour le bon de commande {} par {}", id, authentication.getName());
            
            // Récupérer l'utilisateur avec son entreprise
            Utilisateur user = utilisateurService.trouverParEmailAvecEntreprise(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Récupérer le bon de commande
            BonDeCommande bon = bonDeCommandeService.getBonDeCommandeById(id);
            
            // Vérifier que l'utilisateur appartient à la même entreprise
            if (!bon.getEntreprise().getId().equals(user.getEntreprise().getId())) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Accès interdit : ce bon appartient à une autre entreprise"));
            }

            // Générer le PDF en utilisant le nouveau service
            String urlPdf = documentService.genererBonCommandePdf(bon);
            
            // Extraire le nom du fichier
            String fileName = urlPdf.substring(urlPdf.lastIndexOf("/") + 1);
            String filePathStr = "reports/" + fileName;
            java.nio.file.Path filePath = java.nio.file.Paths.get(filePathStr);
            
            // Vérifier si le fichier existe
            if (!java.nio.file.Files.exists(filePath)) {
                throw new RuntimeException("Fichier PDF non trouvé: " + filePathStr);
            }
            
            // Lire le fichier PDF
            byte[] pdfBytes = java.nio.file.Files.readAllBytes(filePath);

            // Préparer les headers de réponse
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "bon_commande_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
            
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement du PDF : {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Liste les commentaires d'un bon de commande
     */
    @GetMapping("/{id}/commentaires")
    public ResponseEntity<?> listerCommentaires(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Liste des commentaires du bon de commande {} demandée par {}", id, authentication.getName());

            var commentaires = commentaireService.getCommentairesByDocument(id, TypeDocument.BON_COMMANDE)
                    .stream().map(commentaireMapper::toDTO).toList();

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