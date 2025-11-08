# API Endpoints KafoFond - Documentation Complète

## 🔐 Authentification

### POST `/api/auth/login`

- **Description** : Connexion utilisateur
- **Body** : `{"email": "string", "motDePasse": "string"}`
- **Response** : JWT token + informations utilisateur
- **Accès** : Public

### POST `/api/auth/signup`

- **Description** : Inscription d'un nouvel utilisateur
- **Body** : `{"nom": "string", "prenom": "string", "email": "string", "motDePasse": "string", "departement": "string", "role": "string"}`
- **Response** : Confirmation de création
- **Accès** : Admin, Super Admin uniquement

### GET `/api/auth/verify`

- **Description** : Vérification du token JWT
- **Response** : Statut d'authentification
- **Accès** : Authentifié

---

## 👥 Gestion des Utilisateurs

### GET `/api/utilisateurs`

- **Description** : Liste tous les utilisateurs de l'entreprise
- **Response** : Liste des utilisateurs avec pagination
- **Accès** : Admin, Directeur

### POST `/api/utilisateurs`

- **Description** : Crée un nouvel utilisateur
- **Body** : UtilisateurDTO
- **Response** : Utilisateur créé
- **Accès** : Admin, Directeur

### PUT `/api/utilisateurs/{id}`

- **Description** : Modifie un utilisateur existant
- **Body** : UtilisateurDTO
- **Response** : Utilisateur modifié
- **Accès** : Admin, Directeur

### POST `/api/utilisateurs/{id}/desactiver`

- **Description** : Désactive un utilisateur
- **Response** : Confirmation de désactivation
- **Accès** : Admin, Directeur

### POST `/api/utilisateurs/{id}/reactiver`

- **Description** : Réactive un utilisateur
- **Response** : Confirmation de réactivation
- **Accès** : Admin, Directeur

### GET `/api/utilisateurs/{id}`

- **Description** : Récupère les détails d'un utilisateur
- **Response** : Détails de l'utilisateur
- **Accès** : Admin, Directeur

---

## 💰 Gestion des Budgets

### POST `/api/budgets`

- **Description** : Crée un nouveau budget
- **Body** : BudgetCreateDTO (simplifié)
  ```json
  {
    "intituleBudget": "string",
    "description": "string",
    "montantBudget": 0,
    "dateDebut": "2025-11-07",
    "dateFin": "2025-11-07"
  }
  ```
- **Response** : Budget créé
- **Accès** : Responsable, Directeur

### PUT `/api/budgets/{id}`

- **Description** : Modifie un budget existant
- **Body** : BudgetDTO
- **Response** : Budget modifié
- **Accès** : Responsable, Directeur

### POST `/api/budgets/{id}/valider`

- **Description** : Valide un budget
- **Response** : Budget validé
- **Accès** : Directeur uniquement

### POST `/api/budgets/{id}/rejeter`

- **Description** : Rejette un budget avec commentaire
- **Body** : `{"commentaire": "string"}`
- **Response** : Budget rejeté
- **Accès** : Directeur uniquement

### POST `/api/budgets/{id}/activer`

- **Description** : Active un budget
- **Response** : Budget activé
- **Accès** : Directeur uniquement

### POST `/api/budgets/{id}/desactiver`

- **Description** : Désactive un budget
- **Response** : Budget désactivé
- **Accès** : Directeur, Responsable

### GET `/api/budgets`

- **Description** : Liste tous les budgets
- **Response** : Liste des budgets
- **Accès** : Tous rôles

### GET `/api/budgets/{id}`

- **Description** : Récupère les détails d'un budget
- **Response** : Détails du budget
- **Accès** : Tous rôles

---

## 🏦 Gestion des Lignes de Crédit

### POST `/api/lignes-credit`

- **Description** : Crée une nouvelle ligne de crédit
- **Body** : LigneCreditCreateDTO (simplifié)
  ```json
  {
    "intituleLigne": "string",
    "description": "string",
    "montantAllouer": 0,
    "budgetId": 0
  }
  ```
- **Response** : Ligne de crédit créée
- **Accès** : Responsable, Directeur

### PUT `/api/lignes-credit/{id}`

- **Description** : Modifie une ligne de crédit existante
- **Body** : LigneCreditDTO
- **Response** : Ligne de crédit modifiée
- **Accès** : Responsable, Directeur

### POST `/api/lignes-credit/{id}/valider`

- **Description** : Valide une ligne de crédit
- **Response** : Ligne de crédit validée
- **Accès** : Directeur uniquement

### POST `/api/lignes-credit/{id}/rejeter`

- **Description** : Rejette une ligne de crédit avec commentaire
- **Body** : `{"commentaire": "string"}`
- **Response** : Ligne de crédit rejetée
- **Accès** : Directeur uniquement

### POST `/api/lignes-credit/{id}/activer`

- **Description** : Active une ligne de crédit
- **Response** : Ligne de crédit activée
- **Accès** : Directeur uniquement

### POST `/api/lignes-credit/{id}/desactiver`

- **Description** : Désactive une ligne de crédit
- **Response** : Ligne de crédit désactivée
- **Accès** : Directeur, Responsable

### GET `/api/lignes-credit`

- **Description** : Liste toutes les lignes de crédit
- **Response** : Liste des lignes de crédit
- **Accès** : Tous rôles

### GET `/api/lignes-credit/{id}`

- **Description** : Récupère les détails d'une ligne de crédit
- **Response** : Détails de la ligne de crédit
- **Accès** : Tous rôles

---

## 📋 Fiches de Besoin

### POST `/api/fiches-besoin`

- **Description** : Crée une nouvelle fiche de besoin
- **Body** : FicheBesoinDTO
- **Response** : Fiche créée
- **Accès** : Trésorerie uniquement

### PUT `/api/fiches-besoin/{id}`

- **Description** : Modifie une fiche de besoin
- **Body** : FicheBesoinDTO
- **Response** : Fiche modifiée
- **Accès** : Trésorerie uniquement

### POST `/api/fiches-besoin/{id}/valider`

- **Description** : Valide une fiche de besoin
- **Response** : Fiche validée
- **Accès** : Gestionnaire uniquement

### POST `/api/fiches-besoin/{id}/approuver`

- **Description** : Approuve une fiche de besoin
- **Response** : Fiche approuvée
- **Accès** : Comptable uniquement

### POST `/api/fiches-besoin/{id}/rejeter`

- **Description** : Rejette une fiche de besoin
- **Body** : `{"commentaire": "string"}`
- **Response** : Fiche rejetée
- **Accès** : Gestionnaire, Comptable

### GET `/api/fiches-besoin`

- **Description** : Liste toutes les fiches de besoin
- **Response** : Liste des fiches
- **Accès** : Authentifié

### GET `/api/fiches-besoin/{id}`

- **Description** : Récupère les détails d'une fiche
- **Response** : Détails de la fiche
- **Accès** : Authentifié

---

## 🛒 Demandes d'Achat

### POST `/api/demandes-achat`

- **Description** : Crée une nouvelle demande d'achat
- **Body** : DemandeDAchatDTO
- **Response** : Demande créée
- **Accès** : Trésorerie uniquement

### PUT `/api/demandes-achat/{id}`

- **Description** : Modifie une demande d'achat
- **Body** : DemandeDAchatDTO
- **Response** : Demande modifiée
- **Accès** : Trésorerie uniquement

### POST `/api/demandes-achat/{id}/valider`

- **Description** : Valide une demande d'achat
- **Response** : Demande validée
- **Accès** : Gestionnaire uniquement

### POST `/api/demandes-achat/{id}/approuver`

- **Description** : Approuve une demande d'achat (génère automatiquement un bon de commande)
- **Response** : Demande approuvée + Bon de commande créé
- **Accès** : Comptable uniquement

### POST `/api/demandes-achat/{id}/rejeter`

- **Description** : Rejette une demande d'achat
- **Body** : `{"commentaire": "string"}`
- **Response** : Demande rejetée
- **Accès** : Gestionnaire, Comptable

### GET `/api/demandes-achat`

- **Description** : Liste toutes les demandes d'achat
- **Response** : Liste des demandes
- **Accès** : Authentifié

### GET `/api/demandes-achat/{id}`

- **Description** : Récupère les détails d'une demande
- **Response** : Détails de la demande
- **Accès** : Authentifié

---

## 📄 Bons de Commande

### GET `/api/bons-commande`

- **Description** : Liste tous les bons de commande
- **Response** : Liste des bons
- **Accès** : Authentifié

### GET `/api/bons-commande/{id}`

- **Description** : Récupère les détails d'un bon de commande
- **Response** : Détails du bon
- **Accès** : Authentifié

### PUT `/api/bons-commande/{id}`

- **Description** : Personnalise un bon de commande
- **Body** : BonDeCommandeDTO
- **Response** : Bon personnalisé
- **Accès** : Comptable, Responsable, Directeur

### POST `/api/bons-commande/{id}/valider`

- **Description** : Valide un bon de commande (génère automatiquement le PDF)
- **Response** : Bon validé + PDF généré
- **Accès** : Responsable, Directeur

### POST `/api/bons-commande/{id}/rejeter`

- **Description** : Rejette un bon de commande
- **Body** : `{"commentaire": "string"}`
- **Response** : Bon rejeté
- **Accès** : Responsable, Directeur

### GET `/api/bons-commande/{id}/pdf`

- **Description** : Génère le PDF d'un bon de commande
- **Response** : URL du PDF généré
- **Accès** : Authentifié

---

## 💳 Ordres de Paiement

### POST `/api/ordres-paiement`

- **Description** : Crée un nouvel ordre de paiement
- **Body** : OrdreDePaiementDTO
- **Response** : Ordre créé
- **Accès** : Comptable uniquement

### POST `/api/ordres-paiement/{id}/valider`

- **Description** : Valide un ordre de paiement (si montant < seuil)
- **Response** : Ordre validé
- **Accès** : Responsable uniquement

### POST `/api/ordres-paiement/{id}/approuver`

- **Description** : Approuve un ordre de paiement (si montant >= seuil)
- **Response** : Ordre approuvé
- **Accès** : Directeur uniquement

### POST `/api/ordres-paiement/{id}/rejeter`

- **Description** : Rejette un ordre de paiement
- **Body** : `{"commentaire": "string"}`
- **Response** : Ordre rejeté
- **Accès** : Responsable, Directeur

### GET `/api/ordres-paiement`

- **Description** : Liste tous les ordres de paiement
- **Response** : Liste des ordres
- **Accès** : Authentifié

### GET `/api/ordres-paiement/{id}`

- **Description** : Récupère les détails d'un ordre
- **Response** : Détails de l'ordre
- **Accès** : Authentifié

---

## 📑 Décisions de Prélèvement

### POST `/api/decisions-prelevement`

- **Description** : Crée une décision de prélèvement
- **Body** : DecisionPrelevementCreateDTO (simplifié)
  ```json
  {
    "attestationId": 1,
    "montant": 2500000,
    "compteOrigine": "Compte Principal",
    "compteDestinataire": "Tech Solutions",
    "motifPrelevement": "Paiement facture"
  }
  ```
- **Response** : Décision de prélèvement créée
- **Accès** : Comptable uniquement

### POST `/api/decisions-prelevement/{id}/valider`

- **Description** : Valide une décision de prélèvement
- **Response** : Décision validée
- **Accès** : Responsable uniquement

### POST `/api/decisions-prelevement/{id}/approuver`

- **Description** : Approuve une décision de prélèvement
- **Response** : Décision approuvée
- **Accès** : Directeur uniquement

### POST `/api/decisions-prelevement/{id}/rejeter`

- **Description** : Rejette une décision de prélèvement
- **Body** : `{"commentaire": "string"}`
- **Response** : Décision rejetée
- **Accès** : Responsable, Directeur

### GET `/api/decisions-prelevement`

- **Description** : Liste toutes les décisions de prélèvement
- **Response** : Liste des décisions
- **Accès** : Authentifié

### GET `/api/decisions-prelevement/{id}`

- **Description** : Récupère les détails d'une décision
- **Response** : Détails de la décision
- **Accès** : Authentifié

---

## 🔔 Notifications

### GET `/api/notifications`

- **Description** : Liste les notifications de l'utilisateur connecté
- **Response** : Liste des notifications
- **Accès** : Authentifié

### GET `/api/notifications/non-lues`

- **Description** : Compte les notifications non lues
- **Response** : Nombre de notifications non lues
- **Accès** : Authentifié

### POST `/api/notifications/{id}/marquer-lu`

- **Description** : Marque une notification comme lue
- **Response** : Confirmation
- **Accès** : Authentifié

---

## 📊 Historique

### GET `/api/historique/document/{type}/{id}`

- **Description** : Récupère l'historique d'un document
- **Response** : Liste des actions sur le document
- **Accès** : Authentifié

### GET `/api/historique/entreprise`

- **Description** : Récupère l'historique complet de l'entreprise
- **Response** : Liste de toutes les actions
- **Accès** : Admin, Directeur

---

## ⚙️ Seuils de Validation

### POST `/api/seuils`

- **Description** : Configure un seuil de validation
- **Body** : SeuilValidationCreateDTO (simplifié)
  ```json
  {
    "montantSeuil": 0
  }
  ```
- **Response** : Seuil configuré
- **Accès** : Directeur uniquement

### GET `/api/seuils/actif`

- **Description** : Obtient le seuil de validation actif
- **Response** : Seuil actuel
- **Accès** : Authentifié

---

## 📈 Rapports

### GET `/api/rapports/budget/{id}/pdf`

- **Description** : Génère un rapport PDF pour un budget
- **Response** : URL du PDF généré
- **Accès** : Responsable, Directeur

### GET `/api/rapports/budget/{id}/excel`

- **Description** : Génère un rapport Excel pour un budget
- **Response** : URL de l'Excel généré
- **Accès** : Responsable, Directeur

### GET `/api/rapports/demande-achat/{id}/excel`

- **Description** : Génère un rapport Excel pour une demande d'achat
- **Response** : URL de l'Excel généré
- **Accès** : Authentifié

### GET `/api/rapports/bon-commande/{id}/pdf`

- **Description** : Génère un rapport PDF pour un bon de commande
- **Response** : URL du PDF généré
- **Accès** : Authentifié

---

## 🔐 Rôles et Permissions

### Hiérarchie des Rôles

1. **SUPER_ADMIN** : Gestion globale de la plateforme
2. **ADMIN** : Gestion des utilisateurs de l'entreprise
3. **DIRECTEUR** : Validation/approbation des budgets et ordres de paiement importants
4. **RESPONSABLE** : Supervision des documents et budgets
5. **COMPTABLE** : Contrôle et approbation des dépenses
6. **GESTIONNAIRE** : Analyse et validation des besoins
7. **TRESORERIE** : Création des besoins et documents financiers

### Workflow des Documents

- **Fiche de Besoin** : Trésorerie → Gestionnaire → Comptable
- **Demande d'Achat** : Trésorerie → Gestionnaire → Comptable → (génère Bon de Commande)
- **Bon de Commande** : Comptable → Responsable → (génère PDF)
- **Ordre de Paiement** : Comptable → Responsable (si < seuil) ou Directeur (si >= seuil)
- **Budget** : Responsable → Directeur

---

## 📝 Notes Importantes

1. **Commentaires obligatoires** : Tous les rejets nécessitent un commentaire
2. **Notifications automatiques** : Chaque modification notifie le supérieur hiérarchique
3. **Historique complet** : Toutes les actions sont tracées
4. **Seuils configurables** : Le Directeur peut configurer les seuils de validation
5. **Génération automatique** : Les documents sont générés automatiquement selon le workflow
6. **Multi-tenant** : Chaque entreprise a ses propres données et seuils

---

## 🧪 Données de Test

### Utilisateurs de Test (mot de passe : "password123")

- **Super Admin** : mamadou@kafofond.com
- **Admin** : awa@tresor.ml
- **Directeur** : directeur@tresor.ml
- **Responsable** : responsable@tresor.ml
- **Comptable** : comptable@tresor.ml
- **Gestionnaire** : gestionnaire@tresor.ml
- **Trésorerie** : tresorerie@tresor.ml

### Entreprise de Test

- **Nom** : Trésor
- **Domaine** : Finance
- **Adresse** : Bamako, Mali
