# API de Création d'Ordre de Paiement - Documentation

## Endpoint de Création

```
POST /api/ordres-paiement
```

## Nouveau Format de Requête

Le JSON d'entrée pour la création d'un ordre de paiement a été simplifié pour ne contenir que les champs essentiels :

```json
{
  "montant": 1500.0,
  "description": "Paiement fournitures",
  "compteOrigine": "COMPTE-ORIGINE-001",
  "compteDestinataire": "COMPTE-DEST-001",
  "decisionId": 1
}
```

## Champs Requis

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| montant | number | Montant de l'ordre de paiement | Conditionnel |
| description | string | Description de l'ordre de paiement | Oui |
| compteOrigine | string | Compte d'origine | Conditionnel |
| compteDestinataire | string | Compte destinataire | Conditionnel |
| decisionId | integer | ID de la décision de prélèvement | Conditionnel |

## Logique de Remplissage Automatique

Si le champ [decisionId](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\OrdreDePaiementDTO.java#L34-L34) est fourni, les champs suivants sont automatiquement remplis à partir de la décision :

- [montant](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L32-L32) - Montant de la décision
- [compteOrigine](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L34-L34) - Compte d'origine de la décision
- [compteDestinataire](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L35-L35) - Compte destinataire de la décision

Si [decisionId](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\OrdreDePaiementDTO.java#L34-L34) n'est pas fourni, les champs [montant](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L32-L32), [compteOrigine](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L34-L34) et [compteDestinataire](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\entity\OrdreDePaiement.java#L35-L35) doivent être fournis explicitement.

## Champs Générés Automatiquement

Les champs suivants sont générés automatiquement par le système et ne doivent pas être inclus dans la requête :

- `id` - Identifiant unique généré par la base de données
- `code` - Code unique généré automatiquement (format: OP-0012-11-2025)
- `referenceDecisionPrelevement` - Référence de la décision (rempli automatiquement si [decisionId](file://c:\Users\Kalandew20\Desktop\kafofond\src\main\java\kafofond\dto\OrdreDePaiementDTO.java#L34-L34) est fourni)
- `dateCreation` - Date de création (date courante)
- `dateModification` - Date de dernière modification
- `dateExecution` - Date d'exécution (7 jours après la date de création)
- `statut` - Statut initial (EN_COURS)
- `createurNom` - Nom du créateur (récupéré à partir du token d'authentification)
- `createurEmail` - Email du créateur (récupéré à partir du token d'authentification)
- `entrepriseNom` - Nom de l'entreprise (récupéré à partir de l'utilisateur authentifié)

## Exemple de Réponse

```json
{
  "message": "Ordre de paiement créé avec succès",
  "ordre": {
    "id": 1,
    "code": "OP-0001-10-2025",
    "referenceDecisionPrelevement": "DP-0001-10-2025",
    "montant": 1500.0,
    "description": "Paiement fournitures",
    "compteOrigine": "COMPTE-ORIGINE-001",
    "compteDestinataire": "COMPTE-DEST-001",
    "dateExecution": "2025-10-30",
    "dateCreation": "2025-10-23",
    "dateModification": "2025-10-23T17:31:03.409Z",
    "statut": "EN_COURS",
    "createurNom": "Jean Dupont",
    "createurEmail": "jean.dupont@entreprise.com",
    "entrepriseNom": "Ma Entreprise",
    "decisionId": 1
  }
}
```

## Workflow de Validation

Après la création, l'ordre de paiement suit un workflow de validation basé sur le montant :

1. **Montant < Seuil** : Validation par le Responsable
2. **Montant >= Seuil** : Approbation par le Directeur

## Avantages de cette Approche

1. **Simplicité** : L'utilisateur n'a besoin de fournir que les informations essentielles
2. **Automatisation** : Remplissage automatique des champs à partir de la décision
3. **Sécurité** : Les champs sensibles sont gérés automatiquement par le système
4. **Clarté** : Interface beaucoup plus intuitive
5. **Maintenabilité** : Moins de champs à valider côté client