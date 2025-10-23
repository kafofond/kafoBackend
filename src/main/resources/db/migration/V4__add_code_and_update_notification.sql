-- =====================================================
-- Script de migration V4 : Ajout de codes uniques et modification de Notification
-- Date: 2025-01-16
-- Description: 
--   1. Ajoute l'attribut 'code' aux entités de la chaîne de documents
--   2. Modifie la table notifications (ajout 'transmission', renommer 'lu' en 'etat')
-- =====================================================

-- =====================================================
-- PARTIE 1: Ajout de l'attribut 'code' aux tables
-- =====================================================

-- Table: budgets
-- Format code: BUD-NNNN-MM-YYYY
ALTER TABLE budgets 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format BUD-NNNN-MM-YYYY';

CREATE INDEX idx_budgets_code ON budgets(code);

-- Table: lignes_credit
-- Format code: LC-NNNN-MM-YYYY
ALTER TABLE lignes_credit 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format LC-NNNN-MM-YYYY';

CREATE INDEX idx_lignes_credit_code ON lignes_credit(code);

-- Table: fiches_de_besoin
-- Format code: FB-NNNN-MM-YYYY
ALTER TABLE fiches_de_besoin 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format FB-NNNN-MM-YYYY';

CREATE INDEX idx_fiches_de_besoin_code ON fiches_de_besoin(code);

-- Table: demandes_achat
-- Format code: DA-NNNN-MM-YYYY
ALTER TABLE demandes_achat 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format DA-NNNN-MM-YYYY';

CREATE INDEX idx_demandes_achat_code ON demandes_achat(code);

-- Table: bons_de_commande
-- Format code: BC-NNNN-MM-YYYY
ALTER TABLE bons_de_commande 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format BC-NNNN-MM-YYYY';

CREATE INDEX idx_bons_de_commande_code ON bons_de_commande(code);

-- Table: attestations_service_fait
-- Format code: ASF-NNNN-MM-YYYY
ALTER TABLE attestations_service_fait 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format ASF-NNNN-MM-YYYY';

CREATE INDEX idx_attestations_service_fait_code ON attestations_service_fait(code);

-- Table: decisions_prelevement
-- Format code: DP-NNNN-MM-YYYY
ALTER TABLE decisions_prelevement 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format DP-NNNN-MM-YYYY';

CREATE INDEX idx_decisions_prelevement_code ON decisions_prelevement(code);

-- Table: ordres_paiement
-- Format code: OP-NNNN-MM-YYYY
ALTER TABLE ordres_paiement 
ADD COLUMN code VARCHAR(20) UNIQUE COMMENT 'Code unique au format OP-NNNN-MM-YYYY';

CREATE INDEX idx_ordres_paiement_code ON ordres_paiement(code);

-- =====================================================
-- PARTIE 2: Modification de la table notifications
-- =====================================================

-- Renommer la colonne 'lu' en 'etat'
-- Note: Si votre base de données ne supporte pas RENAME COLUMN, 
-- utilisez la méthode ADD + UPDATE + DROP
ALTER TABLE notifications 
CHANGE COLUMN lu etat BOOLEAN DEFAULT FALSE COMMENT 'État de la notification interne (true=lu, false=non lu)';

-- Ajouter la colonne 'transmission'
ALTER TABLE notifications 
ADD COLUMN transmission BOOLEAN DEFAULT NULL COMMENT 'État de transmission par email (true=envoyé, false=échec, null=pas envoyé)';

-- =====================================================
-- PARTIE 3: Génération des codes pour les données existantes
-- =====================================================

-- NOTE IMPORTANTE:
-- Les codes seront générés automatiquement par le @PrePersist/@PreUpdate
-- lors des prochaines modifications d'enregistrements.
-- Pour générer immédiatement les codes pour les données existantes,
-- utilisez les requêtes UPDATE ci-dessous (à adapter selon vos besoins)

-- Exemple pour budgets (à personnaliser selon vos besoins)
-- UPDATE budgets 
-- SET code = CONCAT('BUD-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour lignes_credit
-- UPDATE lignes_credit 
-- SET code = CONCAT('LC-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour fiches_de_besoin
-- UPDATE fiches_de_besoin 
-- SET code = CONCAT('FB-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour demandes_achat
-- UPDATE demandes_achat 
-- SET code = CONCAT('DA-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour bons_de_commande
-- UPDATE bons_de_commande 
-- SET code = CONCAT('BC-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour attestations_service_fait
-- UPDATE attestations_service_fait 
-- SET code = CONCAT('ASF-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour decisions_prelevement
-- UPDATE decisions_prelevement 
-- SET code = CONCAT('DP-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- Exemple pour ordres_paiement
-- UPDATE ordres_paiement 
-- SET code = CONCAT('OP-', LPAD(id, 4, '0'), '-', 
--                   LPAD(MONTH(date_creation), 2, '0'), '-', 
--                   YEAR(date_creation))
-- WHERE code IS NULL AND date_creation IS NOT NULL;

-- =====================================================
-- PARTIE 4: Mise à jour des notifications existantes
-- =====================================================

-- Initialiser 'etat' à FALSE pour toutes les notifications existantes
-- (si elles n'ont pas déjà une valeur)
UPDATE notifications 
SET etat = COALESCE(etat, FALSE)
WHERE etat IS NULL;

-- Initialiser 'transmission' à NULL pour toutes les notifications existantes
-- (NULL = pas d'email envoyé)
UPDATE notifications 
SET transmission = NULL
WHERE transmission IS NULL;

-- =====================================================
-- FIN DE LA MIGRATION V4
-- =====================================================
