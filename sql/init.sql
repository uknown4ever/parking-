-- ============================================================
-- Gestion de Parking - Script SQL d'initialisation
-- Projet 20 | MySQL
-- ============================================================

CREATE DATABASE IF NOT EXISTS parking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parking_db;

-- -------------------------------------------------------
-- Table : Place
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS Place (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    numero      VARCHAR(10)  NOT NULL UNIQUE,
    type        ENUM('Auto','Moto','PMR') NOT NULL,
    statut      ENUM('Libre','Occupée') NOT NULL DEFAULT 'Libre',
    tarifHoraire DECIMAL(6,2) NOT NULL CHECK (tarifHoraire >= 0)
);

-- -------------------------------------------------------
-- Table : Vehicule
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS Vehicule (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    matricule   VARCHAR(20)  NOT NULL UNIQUE,
    marque      VARCHAR(50)  NOT NULL,
    categorie   ENUM('Auto','Moto','PMR') NOT NULL
);

-- -------------------------------------------------------
-- Table : Stationnement
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS Stationnement (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    place_id    INT          NOT NULL,
    vehicule_id INT          NOT NULL,
    dateEntree  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dateSortie  DATETIME     NULL,
    montant     DECIMAL(8,2) NULL,
    CONSTRAINT fk_stat_place    FOREIGN KEY (place_id)    REFERENCES Place(id),
    CONSTRAINT fk_stat_vehicule FOREIGN KEY (vehicule_id) REFERENCES Vehicule(id),
    -- Empêcher l'occupation simultanée d'une même place
    CONSTRAINT chk_montant CHECK (montant IS NULL OR montant >= 0)
);

-- Index pour la contrainte métier (non-chevauchement par place)
CREATE INDEX idx_stat_place   ON Stationnement(place_id);
CREATE INDEX idx_stat_vehicle ON Stationnement(vehicule_id);

-- -------------------------------------------------------
-- Données de test
-- -------------------------------------------------------
INSERT INTO Place (numero, type, statut, tarifHoraire) VALUES
  ('A01', 'Auto', 'Libre',   2.50),
  ('A02', 'Auto', 'Libre',   2.50),
  ('A03', 'Auto', 'Libre',   2.50),
  ('M01', 'Moto', 'Libre',   1.00),
  ('M02', 'Moto', 'Libre',   1.00),
  ('P01', 'PMR',  'Libre',   0.00),
  ('P02', 'PMR',  'Libre',   0.00);

INSERT INTO Vehicule (matricule, marque, categorie) VALUES
  ('AB-123-CD', 'Renault',  'Auto'),
  ('EF-456-GH', 'Peugeot',  'Auto'),
  ('IJ-789-KL', 'Yamaha',   'Moto'),
  ('MN-012-OP', 'Toyota',   'Auto'),
  ('QR-345-ST', 'Honda',    'Moto');

-- Quelques stationnements passés (terminés)
INSERT INTO Stationnement (place_id, vehicule_id, dateEntree, dateSortie, montant) VALUES
  (1, 1, '2024-01-10 08:00:00', '2024-01-10 10:30:00',  6.25),
  (2, 2, '2024-01-10 09:00:00', '2024-01-10 11:00:00',  5.00),
  (4, 3, '2024-01-11 07:30:00', '2024-01-11 09:00:00',  1.50),
  (1, 4, '2024-02-15 14:00:00', '2024-02-15 17:00:00',  7.50),
  (3, 5, '2024-03-01 10:00:00', '2024-03-01 12:00:00',  5.00);
