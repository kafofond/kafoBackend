# Structure Complète du Projet KafoFond

## 📁 Architecture du Projet

```
kafofond/
├── config/                          # Configuration Spring
│   ├── jwt/                         # Configuration JWT
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── JwtUtils.java
│   ├── MailConfig.java              # Configuration email
│   └── SecurityConfig.java          # Configuration sécurité
├── controller/                      # Controllers REST
│   ├── AuthController.java          # Authentification
│   ├── BudgetController.java        # Gestion budgets
│   ├── BonDeCommandeController.java # Gestion bons de commande
│   ├── DemandeDAchatController.java # Gestion demandes d'achat
│   ├── FicheBesoinController.java   # Gestion fiches de besoin
│   ├── HistoriqueController.java    # Consultation historique
│   ├── NotificationController.java  # Gestion notifications
│   ├── OrdreDePaiementController.java # Gestion ordres de paiement
│   ├── ReportController.java        # Génération rapports
│   ├── SeuilValidationController.java # Gestion seuils
│   └── UtilisateurController.java   # Gestion utilisateurs
├── dto/                            # Data Transfer Objects
│   ├── BonDeCommandeDTO.java
│   ├── BudgetDTO.java
│   ├── DemandeDAchatDTO.java
│   ├── EntrepriseDTO.java
│   ├── ErrorResponse.java
│   ├── FicheBesoinDTO.java
│   ├── HistoriqueDTO.java
│   ├── NotificationDTO.java
│   ├── OrdreDePaiementDTO.java
│   ├── SeuilValidationDTO.java
│   └── UtilisateurDTO.java
├── entity/                         # Entités JPA
│   ├── AttestationDeServiceFait.java
│   ├── BonDeCommande.java
│   ├── Budget.java
│   ├── Commentaire.java
│   ├── DecisonDePrelevement.java
│   ├── DemandeDAchat.java
│   ├── Entreprise.java
│   ├── FicheDeBesoin.java
│   ├── HistoriqueAction.java
│   ├── LigneCredit.java
│   ├── Notification.java
│   ├── OrdreDePaiement.java
│   ├── RapportAchat.java
│   ├── Role.java
│   ├── SeuilValidation.java
│   ├── Statut.java
│   └── Utilisateur.java
├── exception/                      # Gestion des exceptions
│   ├── CommentaireObligatoireException.java
│   ├── DocumentNotFoundException.java
│   ├── GlobalExceptionHandler.java
│   └── SeuilNonConfigureException.java
├── mapper/                         # Mappers Entity ↔ DTO
│   ├── BonDeCommandeMapper.java
│   ├── BudgetMapper.java
│   ├── DemandeDAchatMapper.java
│   ├── FicheBesoinMapper.java
│   ├── OrdreDePaiementMapper.java
│   ├── RapportAchatMapper.java
│   ├── SeuilValidationMapper.java
│   └── UtilisateurMapper.java
├── repository/                     # Repositories JPA
│   ├── AttestationDeServiceFaitRepo.java
│   ├── BonDeCommandeRepo.java
│   ├── BudgetRepo.java
│   ├── DecisionDePrelevementRepo.java
│   ├── DemandeDAchatRepo.java
│   ├── EntrepriseRepo.java
│   ├── FicheBesoinRepo.java
│   ├── HistoriqueActionRepo.java
│   ├── LigneCreditRepo.java
│   ├── NotificationRepo.java
│   ├── OrdreDePaiementRepo.java
│   ├── RapportAchatRepo.java
│   ├── SeuilValidationRepo.java
│   └── UtilisateurRepo.java
├── security/                       # Sécurité
│   └── payload/                    # DTOs d'authentification
│       ├── JwtResponse.java
│       ├── LoginRequest.java
│       └── SignupRequest.java
├── service/                        # Services métier
│   ├── BonDeCommandeService.java
│   ├── BudgetService.java
│   ├── CustomUserDetailsService.java
│   ├── DemandeDAchatService.java
│   ├── ExcelService.java
│   ├── FicheBesoinService.java
│   ├── HistoriqueService.java
│   ├── NotificationService.java
│   ├── OrdreDePaiementService.java
│   ├── PdfService.java
│   ├── RapportAchatService.java
│   ├── SeuilValidationService.java
│   ├── UtilisateurService.java
│   └── ValidationService.java
├── util/                          # Utilitaires
│   └── PasswordEncoderConfig.java
├── KafobackendApplication.java    # Classe principale
└── resources/                     # Ressources
    ├── application.properties     # Configuration
    ├── data.sql                  # Données de test
    ├── static/                   # Fichiers statiques
    └── templates/                # Templates
```

## 🔧 Technologies Utilisées

### Backend
- **Spring Boot 3.x** : Framework principal
- **Spring Security** : Authentification et autorisation
- **Spring Data JPA** : Persistance des données
- **Spring Mail** : Envoi d'emails
- **JWT** : Tokens d'authentification
- **MySQL** : Base de données
- **Lombok** : Réduction du code boilerplate

### Génération de Documents
- **iText 7** : Génération de PDF
- **Apache POI** : Génération d'Excel

### Outils de Développement
- **Maven** : Gestion des dépendances
- **JAXB** : Compatibilité JWT avec Java 11+

## 🏗️ Architecture en Couches

### 1. Couche Présentation (Controllers)
- **REST Controllers** : Exposent les endpoints API
- **Validation** : Validation des données d'entrée
- **Sécurité** : Vérification des rôles et permissions
- **Mapping** : Conversion DTO ↔ Entity

### 2. Couche Métier (Services)
- **Services métier** : Logique business
- **Workflow** : Gestion des flux de validation
- **Notifications** : Envoi d'emails et notifications système
- **Historique** : Traçabilité des actions

### 3. Couche Données (Repositories)
- **JPA Repositories** : Accès aux données
- **Requêtes personnalisées** : Recherches spécifiques
- **Transactions** : Gestion des transactions

### 4. Couche Persistance (Entities)
- **Entités JPA** : Modèle de données
- **Relations** : Associations entre entités
- **Contraintes** : Validation des données

## 🔐 Sécurité

### Authentification
- **JWT Tokens** : Authentification stateless
- **BCrypt** : Hachage des mots de passe
- **Filtres** : Validation automatique des tokens

### Autorisation
- **Rôles hiérarchiques** : 7 niveaux de rôles
- **Endpoints sécurisés** : Accès basé sur les rôles
- **Multi-tenant** : Isolation par entreprise

### Rôles et Permissions
1. **SUPER_ADMIN** : Gestion globale
2. **ADMIN** : Gestion utilisateurs entreprise
3. **DIRECTEUR** : Validation/approbation budgets et ordres
4. **RESPONSABLE** : Supervision documents et budgets
5. **COMPTABLE** : Contrôle et approbation dépenses
6. **GESTIONNAIRE** : Analyse et validation besoins
7. **TRESORERIE** : Création besoins et documents

## 📊 Workflow des Documents

### Fiche de Besoin
```
Trésorerie → Gestionnaire → Comptable
EN_COURS → VALIDÉ → APPROUVÉ
```

### Demande d'Achat
```
Trésorerie → Gestionnaire → Comptable → (génère Bon de Commande)
EN_COURS → VALIDÉ → APPROUVÉ
```

### Bon de Commande
```
Comptable → Responsable → (génère PDF)
EN_COURS → VALIDÉ
```

### Ordre de Paiement
```
Comptable → Responsable (si < seuil) ou Directeur (si >= seuil)
EN_COURS → VALIDÉ ou APPROUVÉ
```

### Budget
```
Responsable → Directeur
EN_COURS → VALIDÉ/REJETÉ
```

## 🔔 Notifications

### Types de Notifications
- **Système** : Stockées en base de données
- **Email** : Envoyées automatiquement
- **Workflow** : Notifications lors des modifications

### Déclencheurs
- **Création** : Notification au supérieur hiérarchique
- **Modification** : Notification au validateur
- **Validation** : Notification au créateur
- **Rejet** : Notification avec commentaire

## 📈 Rapports

### Génération PDF
- **Budgets** : Rapports détaillés
- **Bons de Commande** : Documents officiels
- **Stockage** : URLs d'accès aux fichiers

### Génération Excel
- **Budgets** : Données tabulaires
- **Demandes d'Achat** : Rapports d'analyse
- **Export** : Format compatible Excel

## 🧪 Données de Test

### Entreprise
- **Nom** : Trésor
- **Domaine** : Finance
- **Localisation** : Bamako, Mali

### Utilisateurs
- **Super Admin** : mamadou@kafofond.com
- **Admin** : awa@tresor.ml
- **Directeur** : directeur@tresor.ml
- **Responsable** : responsable@tresor.ml
- **Comptable** : comptable@tresor.ml
- **Gestionnaire** : gestionnaire@tresor.ml
- **Trésorerie** : tresorerie@tresor.ml

### Mot de Passe
- **Par défaut** : "password123"

## 🚀 Déploiement

### Prérequis
- **Java 17+**
- **Maven 3.6+**
- **MySQL 8.0+**

### Configuration
- **Base de données** : Configuration dans `application.properties`
- **Email** : Configuration SMTP dans `application.properties`
- **JWT** : Clé secrète configurable

### Démarrage
```bash
# Compilation
mvn clean compile

# Tests
mvn test

# Démarrage
mvn spring-boot:run
```

## 📋 Endpoints Principaux

### Authentification
- `POST /api/auth/login` : Connexion
- `POST /api/auth/signup` : Inscription
- `GET /api/auth/verify` : Vérification token

### Gestion des Documents
- `POST /api/budgets` : Créer budget
- `POST /api/fiches-besoin` : Créer fiche de besoin
- `POST /api/demandes-achat` : Créer demande d'achat
- `POST /api/ordres-paiement` : Créer ordre de paiement

### Workflow
- `POST /api/{document}/{id}/valider` : Valider
- `POST /api/{document}/{id}/approuver` : Approuver
- `POST /api/{document}/{id}/rejeter` : Rejeter

### Rapports
- `GET /api/rapports/{document}/{id}/pdf` : PDF
- `GET /api/rapports/{document}/{id}/excel` : Excel

## ✅ Fonctionnalités Implémentées

- ✅ **Authentification JWT** complète
- ✅ **Autorisation par rôles** fine
- ✅ **Workflow réactif** avec notifications
- ✅ **Historique complet** des actions
- ✅ **Génération PDF/Excel** automatique
- ✅ **Notifications email** automatiques
- ✅ **Seuils configurables** par entreprise
- ✅ **Multi-tenant** par entreprise
- ✅ **Gestion d'erreurs** centralisée
- ✅ **Données de test** complètes
- ✅ **API REST** complète et documentée

## 🎯 Prêt pour la Production

Le backend KafoFond est maintenant **complet et prêt** pour :
- **Tests d'intégration** avec le frontend
- **Déploiement en production**
- **Maintenance et évolutions**
- **Formation des utilisateurs**

Tous les endpoints sont documentés et testables avec les données de test fournies.
