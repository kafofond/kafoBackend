# API de Création de Budget - Documentation

## Endpoint de Création

```
POST /api/budgets
```

## Nouveau Format de Requête

Le JSON d'entrée pour la création d'un budget a été simplifié pour ne contenir que les champs essentiels :

```json
{
  "intituleBudget": "Budget 2025",
  "description": "Budget annuel pour les opérations de l'entreprise",
  "montantBudget": 50000.0,
  "dateDebut": "2025-01-01",
  "dateFin": "2025-12-31"
}
```

## Champs Requis

| Champ | Type | Description |
|-------|------|-------------|
| intituleBudget | string | Intitulé du budget |
| description | string | Description détaillée du budget |
| montantBudget | number | Montant total du budget |
| dateDebut | string (date) | Date de début du budget (format: YYYY-MM-DD) |
| dateFin | string (date) | Date de fin du budget (format: YYYY-MM-DD) |

## Champs Générés Automatiquement

Les champs suivants sont générés automatiquement par le système et ne doivent pas être inclus dans la requête :

- `id` - Identifiant unique généré par la base de données
- `code` - Code unique généré automatiquement (format: BUD-0001-11-2025)
- `dateCreation` - Date de création (date courante)
- `dateModification` - Date de dernière modification
- `statut` - Statut initial (EN_COURS)
- `etat` - État initial (false - inactif)
- `creePar` - Utilisateur créateur (récupéré à partir du token d'authentification)
- `entreprise` - Entreprise associée (récupérée à partir de l'utilisateur)

## Exemple de Réponse

```json
{
  "message": "Budget créé avec succès",
  "budget": {
    "id": 1,
    "code": "BUD-0001-10-2025",
    "intituleBudget": "Budget 2025",
    "description": "Budget annuel pour les opérations de l'entreprise",
    "montantBudget": 50000.0,
    "dateCreation": "2025-10-23",
    "dateModification": "2025-10-23T16:39:32.919Z",
    "dateDebut": "2025-01-01",
    "dateFin": "2025-12-31",
    "statut": "EN_COURS",
    "actif": false,
    "createurNom": "John Doe",
    "createurEmail": "john.doe@entreprise.com",
    "entrepriseNom": "Ma Entreprise"
  }
}
```

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que les informations essentielles
2. **Sécurité** : Les champs sensibles sont gérés automatiquement par le système
3. **Cohérence** : Tous les budgets suivent le même format de codification
4. **Maintenabilité** : Moins de champs à valider côté client