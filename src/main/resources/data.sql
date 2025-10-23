-- Données de test pour KafoFond
-- Entreprise exemple "Trésor"

-- Création de l'entreprise
INSERT INTO entreprises (nom, domaine, adresse, telephone, email, mot_de_passe, date_creation, statut) 
VALUES ('Trésor', 'Finance', 'Bamako, Mali', '+223 20 22 33 44', 'contact@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '2024-01-01', true);

-- Récupération de l'ID de l'entreprise
SET @entreprise_id = LAST_INSERT_ID();

-- Création des utilisateurs avec mots de passe "password123"
-- Super Admin Mamadou
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Traoré', 'Mamadou', 'mamadou@kafofond.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Administration', 'SUPER_ADMIN', true, @entreprise_id);

-- Admin Awa
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Diallo', 'Awa', 'awa@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Administration', 'ADMIN', true, @entreprise_id);

-- Directeur Idrissa
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Keita', 'Idrissa', 'directeur@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Direction', 'DIRECTEUR', true, @entreprise_id);

-- Responsable Amadou
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Sangaré', 'Amadou', 'responsable@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Gestion', 'RESPONSABLE', true, @entreprise_id);

-- Comptable Fatou
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Coulibaly', 'Fatou', 'comptable@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Comptabilité', 'COMPTABLE', true, @entreprise_id);

-- Gestionnaire Moussa
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Diarra', 'Moussa', 'gestionnaire@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Gestion', 'GESTIONNAIRE', true, @entreprise_id);

-- Trésorerie Aïssata
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, departement, role, actif, entreprise_id) 
VALUES ('Konaté', 'Aïssata', 'tresorerie@tresor.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Trésorerie', 'TRESORERIE', true, @entreprise_id);

-- Récupération des IDs des utilisateurs
SET @directeur_id = (SELECT id FROM utilisateurs WHERE email = 'directeur@tresor.ml');
SET @responsable_id = (SELECT id FROM utilisateurs WHERE email = 'responsable@tresor.ml');

-- Création d'un budget exemple
INSERT INTO budgets (intitule_budget, description, montant_budget, commentaire, date_creation, date_modification, periode, statut, etat, commentaire_rejet, cree_par_id, entreprise_id) 
VALUES ('Budget Informatique 2024', 'Budget pour l\'équipement informatique de l\'entreprise', 10000000.00, 'Budget prévisionnel pour l\'année 2024', '2024-01-15', '2024-01-15 10:30:00', 'Annuelle', 'EN_COURS', 'INACTIF', NULL, @responsable_id, @entreprise_id);

-- Création d'un seuil de validation exemple
INSERT INTO seuils_validation (montant_seuil, date_creation, actif, entreprise_id) 
VALUES (500000.00, '2024-01-01', true, @entreprise_id);

-- Note: Le mot de passe pour tous les utilisateurs est "password123"
-- Les utilisateurs peuvent se connecter avec leur email et ce mot de passe
