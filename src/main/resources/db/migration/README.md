# 📋 Migrations SQL - KafoFond

## 📝 Liste des Migrations

### V4 : Système de Codes Uniques et Notifications Améliorées

**Fichier** : [`V4__add_code_and_update_notification.sql`](V4__add_code_and_update_notification.sql)  
**Date** : 16 janvier 2025  
**Statut** : ✅ Prêt pour exécution

#### Actions

1. **Ajout de codes uniques** (8 tables)
   - `budgets` → Code format `BUD-NNNN-MM-YYYY`
   - `lignes_credit` → Code format `LC-NNNN-MM-YYYY`
   - `fiches_de_besoin` → Code format `FB-NNNN-MM-YYYY`
   - `demandes_achat` → Code format `DA-NNNN-MM-YYYY`
   - `bons_de_commande` → Code format `BC-NNNN-MM-YYYY`
   - `attestations_service_fait` → Code format `ASF-NNNN-MM-YYYY`
   - `decisions_prelevement` → Code format `DP-NNNN-MM-YYYY`
   - `ordres_paiement` → Code format `OP-NNNN-MM-YYYY`

2. **Modification table notifications**
   - Renommage colonne `lu` → `etat`
   - Ajout colonne `transmission`

3. **Création d'index** sur les colonnes `code`

4. **Scripts UPDATE** (commentés) pour données existantes

#### Impact

- Ajout de 8 colonnes `code` (VARCHAR(20) UNIQUE)
- Ajout de 8 index
- Modification de 2 colonnes dans `notifications`
- **Aucune perte de données**
- **Compatible avec données existantes**

#### Post-Migration

Après l'exécution de la migration, il faut :

1. **Générer les codes pour données existantes** (optionnel mais recommandé)
   - Décommenter et exécuter les requêtes UPDATE du script
   - Ou laisser les codes se générer au fil des modifications

2. **Vérifier les contraintes UNIQUE**
   - S'assurer qu'aucun conflit de code n'existe

#### Exemple d'Exécution Manuelle

```sql
-- Vérifier la version actuelle
SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;

-- Exécuter la migration (si Flyway n'est pas utilisé)
SOURCE V4__add_code_and_update_notification.sql;

-- Vérifier l'ajout des colonnes
DESCRIBE fiches_de_besoin;
DESCRIBE notifications;

-- Générer les codes pour fiches existantes
UPDATE fiches_de_besoin 
SET code = CONCAT('FB-', LPAD(id, 4, '0'), '-', 
                  LPAD(MONTH(date_creation), 2, '0'), '-', 
                  YEAR(date_creation))
WHERE code IS NULL AND date_creation IS NOT NULL;
```

---

## 🔄 Gestion des Migrations avec Flyway

### Configuration

Flyway est configuré pour exécuter automatiquement les migrations au démarrage de l'application Spring Boot.

**Emplacement** : `src/main/resources/db/migration/`

**Convention de nommage** : `V{VERSION}__{DESCRIPTION}.sql`

### Vérification

```java
// Les logs Spring Boot affichent :
// Flyway: Migrating schema `kafofond` to version 4 - add code and update notification
```

### Rollback Manuel (si nécessaire)

```sql
-- Supprimer les colonnes code
ALTER TABLE budgets DROP COLUMN code;
ALTER TABLE lignes_credit DROP COLUMN code;
-- ... (répéter pour les 8 tables)

-- Restaurer notifications
ALTER TABLE notifications CHANGE COLUMN etat lu BOOLEAN DEFAULT FALSE;
ALTER TABLE notifications DROP COLUMN transmission;
```

---

## 📚 Documentation

- **Guide complet** : [`GUIDE_CODES_ET_NOTIFICATIONS.md`](../../GUIDE_CODES_ET_NOTIFICATIONS.md)
- **Quick Start** : [`QUICK_START_CODES.md`](../../QUICK_START_CODES.md)
- **Rapport technique** : [`RAPPORT_IMPLEMENTATION_CODES.md`](../../RAPPORT_IMPLEMENTATION_CODES.md)
- **Synthèse** : [`SYNTHESE_MODIFICATIONS.md`](../../SYNTHESE_MODIFICATIONS.md)

---

**Date** : 16 janvier 2025  
**Version** : 4.0
