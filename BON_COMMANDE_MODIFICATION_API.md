# API de Modification de Bon de Commande - Documentation

## Endpoint de Modification

```
PUT /api/bons-commande/{id}
```

## Nouveau Format de Requête

Le JSON d'entrée pour la modification d'un bon de commande a été simplifié pour ne contenir que les champs modifiables :

```json
{
  "fournisseur": "Fournisseur ABC",
  "description": "Fournitures de bureau",
  "montantTotal": 1500.0,
  "serviceBeneficiaire": "Service Finance",
  "modePaiement": "Virement bancaire",
  "delaiPaiement": "2025-12-31"
}
```

## Champs Modifiables

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| fournisseur | string | Fournisseur du bon de commande | Non |
| description | string | Description du bon de commande | Non |
| montantTotal | number | Montant total du bon de commande | Non |
| serviceBeneficiaire | string | Service bénéficiaire | Non |
| modePaiement | string | Mode de paiement | Non |
| delaiPaiement | string (date) | Délai de paiement (format: YYYY-MM-DD) | Non |

## Champs Non Modifiables

Les champs suivants ne peuvent pas être modifiés et sont gérés automatiquement par le système :

- `id` - Identifiant unique généré par la base de données
- `code` - Code unique généré automatiquement
- `dateCreation` - Date de création (générée à la création)
- `dateExecution` - Date d'exécution (générée automatiquement)
- `statut` - Statut du bon de commande (géré par le workflow)
- `urlPdf` - URL du PDF (généré lors de l'approbation)
- `createurNom` - Nom du créateur (généré à la création)
- `createurEmail` - Email du créateur (généré à la création)
- `entrepriseNom` - Nom de l'entreprise (généré à la création)
- `demandeAchatId` - ID de la demande d'achat associée (généré à la création)
- `commentaires` - Commentaires associés (gérés via l'API de commentaires)

## Effet de la Modification

Lorsqu'un bon de commande est modifié :

1. **Réinitialisation du statut** : Si le bon était VALIDÉ ou APPROUVÉ, il repasse automatiquement en EN_COURS
2. **Historique** : Une entrée est ajoutée dans l'historique des actions
3. **Notifications** : Le responsable est notifié de la modification (sauf s'il est l'auteur)

## Exemple de Réponse

```json
{
  "message": "Bon de commande personnalisé avec succès",
  "bon": {
    "id": 1,
    "code": "BC-0001-10-2025",
    "fournisseur": "Fournisseur ABC",
    "description": "Fournitures de bureau",
    "montantTotal": 1500.0,
    "serviceBeneficiaire": "Service Finance",
    "modePaiement": "Virement bancaire",
    "dateCreation": "2025-10-23",
    "delaiPaiement": "2025-12-31",
    "dateExecution": "2025-10-30",
    "statut": "EN_COURS",
    "urlPdf": null,
    "createurNom": "Jean Dupont",
    "createurEmail": "jean.dupont@entreprise.com",
    "entrepriseNom": "Ma Entreprise",
    "demandeAchatId": 2,
    "commentaires": []
  }
}
```

## Restrictions d'Accès

Seuls les utilisateurs suivants peuvent modifier un bon de commande :
- **Comptable**
- **Responsable**
- **Directeur**

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que les champs modifiables
2. **Sécurité** : Les champs sensibles sont protégés contre les modifications non autorisées
3. **Clarté** : Interface beaucoup plus intuitive
4. **Maintenabilité** : Moins de champs à valider côté client
5. **Cohérence** : Respect du workflow de validation