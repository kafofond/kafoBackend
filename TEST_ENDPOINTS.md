# Tests des Endpoints KafoFond

## 🚀 Démarrage de l'Application

```bash
# Démarrer l'application
mvn spring-boot:run

# L'application sera disponible sur http://localhost:8080
```

## 🔐 Tests d'Authentification

### 1. Connexion Super Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "mamadou@kafofond.com",
    "motDePasse": "password123"
  }'
```

### 2. Connexion Directeur
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "directeur@tresor.ml",
    "motDePasse": "password123"
  }'
```

### 3. Connexion Trésorerie
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tresorerie@tresor.ml",
    "motDePasse": "password123"
  }'
```

## 💰 Tests Budgets

### 1. Créer un Budget (avec token Directeur)
```bash
# Remplacer {TOKEN} par le token JWT obtenu lors de la connexion
curl -X POST http://localhost:8080/api/budgets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "intituleBudget": "Budget Informatique 2024",
    "description": "Budget pour équipement informatique",
    "montantBudget": 5000000,
    "commentaire": "Budget prévisionnel",
    "periode": "Annuelle"
  }'
```

### 2. Lister les Budgets
```bash
curl -X GET http://localhost:8080/api/budgets \
  -H "Authorization: Bearer {TOKEN}"
```

### 3. Valider un Budget
```bash
curl -X POST http://localhost:8080/api/budgets/1/valider \
  -H "Authorization: Bearer {TOKEN}"
```

## 📋 Tests Fiches de Besoin

### 1. Créer une Fiche de Besoin (avec token Trésorerie)
```bash
curl -X POST http://localhost:8080/api/fiches-besoin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "serviceBeneficiaire": "Informatique",
    "objet": "Ordinateurs portables",
    "description": "Achat de 5 ordinateurs portables pour le service informatique",
    "quantite": 5,
    "montantEstime": 2500000,
    "dateAttendu": "2024-03-15"
  }'
```

### 2. Valider une Fiche de Besoin (avec token Gestionnaire)
```bash
curl -X POST http://localhost:8080/api/fiches-besoin/1/valider \
  -H "Authorization: Bearer {TOKEN}"
```

### 3. Approuver une Fiche de Besoin (avec token Comptable)
```bash
curl -X POST http://localhost:8080/api/fiches-besoin/1/approuver \
  -H "Authorization: Bearer {TOKEN}"
```

## 🛒 Tests Demandes d'Achat

### 1. Créer une Demande d'Achat
```bash
curl -X POST http://localhost:8080/api/demandes-achat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "referenceBesoin": "FB-2024-001",
    "description": "Achat ordinateurs portables",
    "fournisseur": "Tech Solutions",
    "quantite": 5,
    "prixUnitaire": 500000,
    "montantTotal": 2500000,
    "serviceBeneficiaire": "Informatique",
    "dateAttendu": "2024-03-15"
  }'
```

### 2. Approuver une Demande d'Achat (génère automatiquement un Bon de Commande)
```bash
curl -X POST http://localhost:8080/api/demandes-achat/1/approuver \
  -H "Authorization: Bearer {TOKEN}"
```

## 📄 Tests Bons de Commande

### 1. Lister les Bons de Commande
```bash
curl -X GET http://localhost:8080/api/bons-commande \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Personnaliser un Bon de Commande
```bash
curl -X PUT http://localhost:8080/api/bons-commande/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "modePaiement": "Virement bancaire",
    "delaiPaiement": "2024-04-15",
    "dateExecution": "2024-03-20"
  }'
```

### 3. Valider un Bon de Commande (génère automatiquement le PDF)
```bash
curl -X POST http://localhost:8080/api/bons-commande/1/valider \
  -H "Authorization: Bearer {TOKEN}"
```

## 💳 Tests Ordres de Paiement

### 1. Créer un Ordre de Paiement (avec token Comptable)
```bash
curl -X POST http://localhost:8080/api/ordres-paiement \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "referenceDecisionPrelevement": "DP-2024-001",
    "montant": 2500000,
    "commentaire": "Paiement fournisseur Tech Solutions",
    "description": "Paiement ordinateurs portables",
    "compteOrigine": "Compte Principal",
    "compteDestinataire": "Tech Solutions",
    "dateExecution": "2024-03-25"
  }'
```

### 2. Valider un Ordre de Paiement (si montant < seuil)
```bash
curl -X POST http://localhost:8080/api/ordres-paiement/1/valider \
  -H "Authorization: Bearer {TOKEN}"
```

### 3. Approuver un Ordre de Paiement (si montant >= seuil)
```bash
curl -X POST http://localhost:8080/api/ordres-paiement/1/approuver \
  -H "Authorization: Bearer {TOKEN}"
```

## ⚙️ Tests Seuils de Validation

### 1. Configurer un Seuil (avec token Directeur)
```bash
curl -X POST http://localhost:8080/api/seuils \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "typeDocument": "ORDRE_PAIEMENT",
    "typeSeuil": "MONTANT_APPROBATION_DIRECTEUR",
    "valeurSeuil": 1000000
  }'
```

### 2. Obtenir le Seuil Actif
```bash
curl -X GET http://localhost:8080/api/seuils/actif \
  -H "Authorization: Bearer {TOKEN}"
```

## 🔔 Tests Notifications

### 1. Lister les Notifications
```bash
curl -X GET http://localhost:8080/api/notifications \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Compter les Notifications Non Lues
```bash
curl -X GET http://localhost:8080/api/notifications/non-lues \
  -H "Authorization: Bearer {TOKEN}"
```

### 3. Marquer une Notification comme Lue
```bash
curl -X POST http://localhost:8080/api/notifications/1/marquer-lu \
  -H "Authorization: Bearer {TOKEN}"
```

## 📊 Tests Historique

### 1. Historique d'un Document
```bash
curl -X GET http://localhost:8080/api/historique/document/BUDGET/1 \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Historique de l'Entreprise
```bash
curl -X GET http://localhost:8080/api/historique/entreprise \
  -H "Authorization: Bearer {TOKEN}"
```

## 📈 Tests Rapports

### 1. Rapport PDF Budget
```bash
curl -X GET http://localhost:8080/api/rapports/budget/1/pdf \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Rapport Excel Budget
```bash
curl -X GET http://localhost:8080/api/rapports/budget/1/excel \
  -H "Authorization: Bearer {TOKEN}"
```

### 3. Rapport Excel Demande d'Achat
```bash
curl -X GET http://localhost:8080/api/rapports/demande-achat/1/excel \
  -H "Authorization: Bearer {TOKEN}"
```

## 👥 Tests Utilisateurs

### 1. Lister les Utilisateurs (avec token Admin)
```bash
curl -X GET http://localhost:8080/api/utilisateurs \
  -H "Authorization: Bearer {TOKEN}"
```

### 2. Créer un Utilisateur
```bash
curl -X POST http://localhost:8080/api/utilisateurs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "email": "jean.dupont@tresor.ml",
    "departement": "Finance",
    "role": "GESTIONNAIRE",
    "actif": true
  }'
```

## 🧪 Scénario de Test Complet

### Workflow Complet : Fiche de Besoin → Demande d'Achat → Bon de Commande → Ordre de Paiement

1. **Connexion Trésorerie**
2. **Créer Fiche de Besoin**
3. **Connexion Gestionnaire**
4. **Valider Fiche de Besoin**
5. **Connexion Comptable**
6. **Approuver Fiche de Besoin**
7. **Créer Demande d'Achat**
8. **Valider Demande d'Achat**
9. **Approuver Demande d'Achat** (génère Bon de Commande)
10. **Personnaliser Bon de Commande**
11. **Valider Bon de Commande** (génère PDF)
12. **Créer Ordre de Paiement**
13. **Valider/Approuver Ordre de Paiement** (selon seuil)

## 📝 Notes de Test

- **Remplacer {TOKEN}** par le token JWT obtenu lors de la connexion
- **Vérifier les rôles** : chaque endpoint nécessite un rôle spécifique
- **Tester les rejets** : ajouter des commentaires obligatoires
- **Vérifier les notifications** : chaque action doit générer des notifications
- **Consulter l'historique** : toutes les actions doivent être tracées
- **Tester les seuils** : créer des ordres de paiement avec différents montants

## 🔍 Validation des Réponses

### Réponse de Succès Typique
```json
{
  "message": "Opération réussie",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Réponse d'Erreur Typique
```json
{
  "message": "Message d'erreur en français",
  "code": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/endpoint"
}
```
