# 🗂️ SCHÉMA DE LA BASE DE DONNÉES - KAFOFOND (REFONTE 2025)

## 📊 VUE D'ENSEMBLE DES TABLES

### TABLES PRINCIPALES DU FLUX DE DÉPENSE

```
┌─────────────────────────────────────────────────────────────────┐
│                    CHAÎNE DE TRAÇABILITÉ                        │
└─────────────────────────────────────────────────────────────────┘

   ┌──────────────────┐
   │ FicheDeBesoin    │
   ├──────────────────┤
   │ id (PK)          │ ◄─── Point de départ du flux
   │ serviceBenef     │
   │ objet            │
   │ description      │
   │ quantite         │
   │ montantEstime    │
   │ statut           │
   │ entreprise_id(FK)│
   └────────┬─────────┘
            │ OneToOne
            │
   ┌────────▼─────────┐
   │ DemandeDAchat    │
   ├──────────────────┤
   │ id (PK)          │
   │ referenceBesoin  │
   │ fournisseur      │
   │ montantTotal     │
   │ statut           │
   │ fiche_besoin_id  │◄─ UNIQUE (OneToOne)
   │ entreprise_id(FK)│
   └────────┬─────────┘
            │ OneToOne
            │
   ┌────────▼─────────┐
   │ BonDeCommande    │
   ├──────────────────┤
   │ id (PK)          │
   │ fournisseur      │
   │ montantTotal     │
   │ modePaiement     │
   │ statut           │
   │ demande_achat_id │◄─ UNIQUE (OneToOne)
   │ entreprise_id(FK)│
   └────────┬─────────┘
            │ OneToOne
            │
   ┌────────▼─────────────────┐
   │ AttestationServiceFait   │
   ├──────────────────────────┤
   │ id (PK)                  │
   │ referenceBonCommande     │
   │ fournisseur              │
   │ constat                  │
   │ dateLivraison            │
   │ bon_commande_id          │◄─ UNIQUE (OneToOne)
   │ entreprise_id (FK)       │
   │ ⚠️  PLUS DE STATUT       │
   └────────┬─────────────────┘
            │ OneToOne
            │
   ┌────────▼──────────────────────┐
   │ DecisionDePrelevement         │
   ├───────────────────────────────┤
   │ id (PK)                       │
   │ montant                       │
   │ compteOrigine                 │
   │ compteDestinataire            │
   │ statut                        │
   │ attestation_service_fait_id   │◄─ UNIQUE (OneToOne)
   │ ligne_credit_id (FK)          │◄─ ManyToOne
   │ entreprise_id (FK)            │
   └────────┬──────────────────────┘
            │ OneToOne
            │
   ┌────────▼─────────────────┐
   │ OrdreDePaiement          │
   ├──────────────────────────┤
   │ id (PK)                  │
   │ montant                  │
   │ compteOrigine            │
   │ compteDestinataire       │
   │ statut                   │
   │ decision_id              │◄─ UNIQUE (OneToOne)
   │ ligne_credit_id (FK)     │◄─ ManyToOne
   │ entreprise_id (FK)       │
   └──────────────────────────┘
            ▲
            │ Point final du flux
```

---

## 🆕 NOUVELLES TABLES

### 1. DESIGNATION (Nouveau)

```
┌──────────────────────────┐
│ Designation              │
├──────────────────────────┤
│ id (PK)                  │
│ produit                  │
│ quantite                 │
│ prixUnitaire             │
│ montantTotal             │
│ date                     │
│ fiche_besoin_id (FK)     │◄─ ManyToOne
└──────────────────────────┘
         │
         │ ManyToOne
         ▼
┌──────────────────────────┐
│ FicheDeBesoin            │
│ OneToMany ───────────────┤
│ List<Designation>        │
└──────────────────────────┘
```

**Usage :** Une fiche de besoin peut contenir plusieurs désignations (produits/services)

---

### 2. TABLE_VALIDATION (Remplace Commentaire)

```
┌──────────────────────────────┐
│ TableValidation              │
├──────────────────────────────┤
│ id (PK)                      │
│ validateur_id (FK)           │◄─ Vers Utilisateur
│ commentaire (TEXT)           │
│ statut (VARCHAR)             │
│ id_document (BIGINT)         │◄─ ID générique
│ type_document (ENUM)         │◄─ Type du document
│ date_validation (DATETIME)   │
└──────────────────────────────┘
```

**Enum TypeDocument :**
- `BUDGET`
- `LIGNE_CREDIT`
- `FICHE_BESOIN`
- `DEMANDE_ACHAT`
- `BON_COMMANDE`
- `ATTESTATION_SERVICE_FAIT`
- `DECISION_PRELEVEMENT`
- `ORDRE_PAIEMENT`

**Usage :** Table générique pour toutes les validations du système

---

### 3. RAPPORTS_ACHAT (Remplace PiecesJustificatives)

```
┌──────────────────────────────┐
│ RapportAchat                 │
├──────────────────────────────┤
│ id (PK)                      │
│ nom (VARCHAR)                │
│ fiche_besoin (VARCHAR)       │
│ demande_achat (VARCHAR)      │
│ bon_commande (VARCHAR)       │
│ attestation_service_fait     │
│ decision_prelevement         │
│ ordre_paiement               │
│ date_ajout (DATE)            │
│ entreprise_id (FK)           │
└──────────────────────────────┘
```

**Usage :** Registre complet de tous les documents d'une dépense

---

## 🔗 TABLES DE SUPPORT

### ENTREPRISE (Modifiée)

```
┌──────────────────────────────────┐
│ Entreprise                       │
├──────────────────────────────────┤
│ id (PK)                          │
│ nom                              │
│ domaine                          │
│ adresse                          │
│ email                            │
│ etat                             │
├──────────────────────────────────┤
│ OneToMany Relations:             │
│ - utilisateurs                   │
│ - budgets                        │
│ - fichesDeBesoins                │
│ - demandesDAchat                 │
│ - bonDeCommandes                 │
│ - attestationsServiceFait        │
│ - decisionsDePrelevement   (NEW) │
│ - ordresDePaiement         (NEW) │
│ - rapportsAchat            (NEW) │
└──────────────────────────────────┘
```

---

### LIGNE_CREDIT

```
┌──────────────────────────────┐
│ LigneCredit                  │
├──────────────────────────────┤
│ id (PK)                      │
│ intituleLigne                │
│ montantAllouer               │
│ montantEngager               │
│ montantRestant               │
│ budget_id (FK)               │
├──────────────────────────────┤
│ OneToMany Relations:         │
│ - decisionDePrelevements     │
│ - ordreDePaiements           │
└──────────────────────────────┘
```

---

### UTILISATEUR

```
┌──────────────────────────────┐
│ Utilisateur                  │
├──────────────────────────────┤
│ id (PK)                      │
│ nom                          │
│ prenom                       │
│ email (UNIQUE)               │
│ motDePasse                   │
│ role (ENUM)                  │
│ departement                  │
│ etat                         │
│ entreprise_id (FK)           │
└──────────────────────────────┘
```

**Enum Role :**
- `SUPER_ADMIN`
- `ADMIN`
- `DIRECTEUR`
- `RESPONSABLE`
- `COMPTABLE`
- `GESTIONNAIRE`
- `TRESORERIE`

---

## 📋 CONTRAINTES ET INDEX

### Contraintes UNIQUE (Garantissent les relations OneToOne)

```sql
-- DemandeDAchat
ALTER TABLE demandes_achat 
    ADD CONSTRAINT uq_demande_achat_fiche_besoin 
    UNIQUE (fiche_besoin_id);

-- BonDeCommande
ALTER TABLE bons_de_commande 
    ADD CONSTRAINT uq_bon_commande_demande_achat 
    UNIQUE (demande_achat_id);

-- AttestationServiceFait
ALTER TABLE attestations_service_fait 
    ADD CONSTRAINT uq_asf_bon_commande 
    UNIQUE (bon_commande_id);

-- DecisionDePrelevement
ALTER TABLE decisions_prelevement 
    ADD CONSTRAINT uq_decision_asf 
    UNIQUE (attestation_service_fait_id);

-- OrdreDePaiement
ALTER TABLE ordres_paiement 
    ADD CONSTRAINT uq_ordre_paiement_decision 
    UNIQUE (decision_id);
```

### Index de performance

```sql
-- Pour les recherches fréquentes
CREATE INDEX idx_designation_fiche_besoin 
    ON designations(fiche_besoin_id);

CREATE INDEX idx_table_validation_document 
    ON table_validation(id_document, type_document);

CREATE INDEX idx_table_validation_validateur 
    ON table_validation(validateur_id);

CREATE INDEX idx_rapport_achat_entreprise 
    ON rapports_achat(entreprise_id);
```

---

## 🎯 RELATIONS CLÉS

### Relations OneToOne (Nouvelle Architecture)

| **Table Source** | **Table Destination** | **Champ FK** | **Contrainte** |
|------------------|-----------------------|--------------|----------------|
| DemandeDAchat | FicheDeBesoin | fiche_besoin_id | UNIQUE |
| BonDeCommande | DemandeDAchat | demande_achat_id | UNIQUE |
| AttestationServiceFait | BonDeCommande | bon_commande_id | UNIQUE |
| DecisionDePrelevement | AttestationServiceFait | attestation_service_fait_id | UNIQUE |
| OrdreDePaiement | DecisionDePrelevement | decision_id | UNIQUE |

### Relations ManyToOne (Conservées)

| **Table Source** | **Table Destination** | **Champ FK** |
|------------------|-----------------------|--------------|
| Designation | FicheDeBesoin | fiche_besoin_id |
| DecisionDePrelevement | LigneCredit | ligne_credit_id |
| OrdreDePaiement | LigneCredit | ligne_credit_id |
| Tous les documents | Entreprise | entreprise_id |
| Tous les documents | Utilisateur | cree_par_id |

### Relations OneToMany

| **Table Source** | **Table Destination** | **mappedBy** |
|------------------|-----------------------|--------------|
| FicheDeBesoin | Designation | ficheDeBesoin |
| Entreprise | [Tous documents] | entreprise |
| LigneCredit | DecisionDePrelevement | ligneCredit |
| LigneCredit | OrdreDePaiement | ligneCredit |

---

## ⚡ TRIGGERS ET AUTOMATISMES (Recommandés)

### 1. Calcul automatique du montantTotal dans Designation

```sql
DELIMITER //
CREATE TRIGGER before_insert_designation
BEFORE INSERT ON designations
FOR EACH ROW
BEGIN
    SET NEW.montant_total = NEW.quantite * NEW.prix_unitaire;
END//
DELIMITER ;
```

### 2. Mise à jour automatique de LigneCredit

```sql
DELIMITER //
CREATE TRIGGER after_insert_decision_prelevement
AFTER INSERT ON decisions_prelevement
FOR EACH ROW
BEGIN
    UPDATE lignes_credit 
    SET montant_engager = montant_engager + NEW.montant,
        montant_restant = montant_restant - NEW.montant
    WHERE id = NEW.ligne_credit_id;
END//
DELIMITER ;
```

---

## 📊 STATISTIQUES DE LA BASE

| **Catégorie** | **Nombre** |
|---------------|------------|
| Tables totales | 15+ |
| Tables de flux | 6 |
| Tables de support | 5 |
| Nouvelles tables | 3 |
| Relations OneToOne | 5 |
| Relations ManyToOne | 10+ |
| Relations OneToMany | 15+ |
| Contraintes UNIQUE | 5 |
| Index de performance | 4+ |

---

## ✅ VALIDATIONS INTÉGRITÉ

### Cascade Delete

- ✅ Suppression d'une `Entreprise` → Supprime tous ses documents
- ✅ Suppression d'une `FicheDeBesoin` → Supprime toutes ses `Designations`
- ✅ Suppression d'un `BonDeCommande` → Supprime son `AttestationServiceFait`
- ✅ Et ainsi de suite pour toute la chaîne

### Contraintes NOT NULL

Tous les champs critiques ont des contraintes `NOT NULL` pour garantir l'intégrité des données.

---

**📅 Dernière mise à jour :** 2025-10-16  
**✅ Schéma validé et prêt pour la production**
