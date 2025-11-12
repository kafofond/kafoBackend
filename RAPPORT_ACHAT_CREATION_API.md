# API de Création de Rapport d'Achat - Documentation

## Endpoint de Création

```
POST /api/rapports-achat
```

## Nouveau Format de Requête

Le JSON d'entrée pour la création d'un rapport d'achat a été simplifié pour utiliser des IDs numériques au lieu de strings et ne contenir que les champs nécessaires :

```json
{
  "nom": "Rapport achat fournitures",
  "ficheBesoinId": 1,
  "demandeAchatId": 2,
  "bonCommandeId": 3,
  "attestationServiceFaitId": 4,
  "decisionPrelevementId": 5,
  "ordrePaiementId": 6
}
```

## Champs Requis

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| nom | string | Nom du rapport d'achat | Oui |
| ficheBesoinId | integer | ID de la fiche de besoin | Non |
| demandeAchatId | integer | ID de la demande d'achat | Non |
| bonCommandeId | integer | ID du bon de commande | Non |
| attestationServiceFaitId | integer | ID de l'attestation de service fait | Non |
| decisionPrelevementId | integer | ID de la décision de prélèvement | Non |
| ordrePaiementId | integer | ID de l'ordre de paiement | Non |

## Champs Générés Automatiquement

Les champs suivants sont générés automatiquement par le système et ne doivent pas être inclus dans la requête :

- `id` - Identifiant unique généré par la base de données
- `dateAjout` - Date de création (date courante)
- `entrepriseNom` - Nom de l'entreprise (récupéré à partir de l'utilisateur authentifié)

## Conversion des IDs en Strings

Le système convertit automatiquement les IDs numériques en strings pour le stockage dans l'entité, conformément au format existant :

- `ficheBesoinId` → `ficheBesoin` (stocké comme string)
- `demandeAchatId` → `demandeAchat` (stocké comme string)
- `bonCommandeId` → `bonCommande` (stocké comme string)
- `attestationServiceFaitId` → `attestationServiceFait` (stocké comme string)
- `decisionPrelevementId` → `decisionPrelevement` (stocké comme string)
- `ordrePaiementId` → `ordrePaiement` (stocké comme string)

## Exemple de Réponse

```json
{
  "id": 1,
  "nom": "Rapport achat fournitures",
  "ficheBesoin": "1",
  "demandeAchat": "2",
  "bonCommande": "3",
  "attestationServiceFait": "4",
  "decisionPrelevement": "5",
  "ordrePaiement": "6",
  "dateAjout": "2025-10-23",
  "entrepriseNom": "Ma Entreprise"
}
```

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que les informations essentielles
2. **Clarté** : Utilisation d'IDs numériques plus intuitifs que des strings
3. **Sécurité** : Les champs sensibles sont gérés automatiquement par le système
4. **Compatibilité** : Le format de stockage existant est conservé
5. **Validation** : Moins de champs à valider côté client

## Exemple d'Erreur

```json
{
  "message": "Seul le Comptable peut créer des rapports d'achat"
}
```