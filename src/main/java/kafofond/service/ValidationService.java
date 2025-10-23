package kafofond.service;

import kafofond.entity.Entreprise;
import kafofond.entity.Statut;
import kafofond.entity.SeuilValidation;
import kafofond.repository.SeuilValidationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import kafofond.entity.Utilisateur;


import java.util.Optional;

/**
 * Service de validation et vérification des seuils
 * Détermine les validateurs selon les règles métier
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final SeuilValidationRepo seuilValidationRepo;

    /**
     * Vérifie si un montant dépasse le seuil de validation configuré
     */
    public boolean verifierSeuilValidation(double montant, Entreprise entreprise) {
        Optional<SeuilValidation> seuilOpt = seuilValidationRepo.findByEntrepriseAndActif(entreprise, true);
        
        if (seuilOpt.isEmpty()) {
            log.warn("Aucun seuil configuré pour l'entreprise {}", entreprise.getNom());
            return false; // Pas de seuil configuré, validation par défaut
        }
        
        SeuilValidation seuil = seuilOpt.get();
        boolean depasseSeuil = montant >= seuil.getMontantSeuil();
        
        log.info("Vérification seuil pour {} FCFA : seuil={} FCFA, dépasse={}", 
                montant, seuil.getMontantSeuil(), depasseSeuil);
        
        return depasseSeuil;
    }

    /**
     * Détermine le validateur suivant selon le type de document et le statut
     */
    public String determinerValidateurSuivant(String typeDocument, Statut statutActuel, 
                                            double montant, Entreprise entreprise) {
        log.info("Détermination du validateur pour {} en statut {} avec montant {}", 
                typeDocument, statutActuel, montant);
        
        switch (typeDocument.toUpperCase()) {
            case "BUDGET":
            case "LIGNE_CREDIT":
                if (statutActuel == Statut.EN_COURS) {
                    return "DIRECTEUR"; // Le Directeur valide les budgets
                }
                break;
                
            case "FICHE_BESOIN":
            case "DEMANDE_ACHAT":
                if (statutActuel == Statut.EN_COURS) {
                    return "GESTIONNAIRE"; // Le Gestionnaire valide d'abord
                } else if (statutActuel == Statut.VALIDE) {
                    return "COMPTABLE"; // Puis le Comptable approuve
                }
                break;
                
            case "BON_COMMANDE":
                if (statutActuel == Statut.EN_COURS) {
                    return "RESPONSABLE"; // Le Responsable valide les bons
                }
                break;
                
            case "ATTESTATION_SERVICE_FAIT":
            case "PIECE_JUSTIFICATIVE":
                if (statutActuel == Statut.EN_COURS) {
                    return "GESTIONNAIRE"; // Le Gestionnaire valide d'abord
                } else if (statutActuel == Statut.VALIDE) {
                    return "COMPTABLE"; // Puis le Comptable approuve
                }
                break;
                
            case "DECISION_PRELEVEMENT":
                if (statutActuel == Statut.EN_COURS) {
                    return "RESPONSABLE"; // Le Responsable valide les décisions
                }
                break;
                
            case "ORDRE_PAIEMENT":
                if (statutActuel == Statut.EN_COURS) {
                    // Vérifier le seuil pour déterminer le validateur
                    if (verifierSeuilValidation(montant, entreprise)) {
                        return "DIRECTEUR"; // Directeur si montant >= seuil
                    } else {
                        return "RESPONSABLE"; // Responsable si montant < seuil
                    }
                } else if (statutActuel == Statut.VALIDE && verifierSeuilValidation(montant, entreprise)) {
                    return "DIRECTEUR"; // Directeur pour approbation finale si seuil atteint
                }
                break;
        }
        
        return null; // Aucun validateur suivant
    }

    /**
     * Valide un document (change le statut)
     */
    public void validerDocument(Object document, Utilisateur validateur, Statut nouveauStatut) {
        log.info("Validation du document {} par {}", document.getClass().getSimpleName(), validateur.getEmail());
        // Cette méthode sera implémentée dans les services spécifiques
    }

    /**
     * Rejette un document avec commentaire obligatoire
     */
    public void rejeterDocument(Object document, Utilisateur validateur, String commentaire) {
        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new IllegalArgumentException("Un commentaire est obligatoire lors du rejet d'un document");
        }
        
        log.info("Rejet du document {} par {} avec commentaire : {}", 
                document.getClass().getSimpleName(), validateur.getEmail(), commentaire);
        // Cette méthode sera implémentée dans les services spécifiques
    }
}
