# Gestion de Parking — Mini Projet Java 20

## Description

Application de bureau Java Swing pour la gestion complète d'un parking :
- CRUD complet sur 3 entités : **Place**, **Véhicule**, **Stationnement**
- Enregistrement d'entrées/sorties avec calcul automatique du montant
- Contrainte métier : interdiction d'occupation simultanée d'une même place
- Recherche & filtrage avancés (type, statut, période, matricule)
- Graphique en barres des revenus par mois
- Packaging Windows (Inno Setup)

---

## Schéma de la base de données

```
Place
├── id            INT PK AUTO_INCREMENT
├── numero        VARCHAR(10) UNIQUE
├── type          ENUM('Auto','Moto','PMR')
├── statut        ENUM('Libre','Occupée')
└── tarifHoraire  DECIMAL(6,2)

Vehicule
├── id            INT PK AUTO_INCREMENT
├── matricule     VARCHAR(20) UNIQUE
├── marque        VARCHAR(50)
└── categorie     ENUM('Auto','Moto','PMR')

Stationnement
├── id            INT PK AUTO_INCREMENT
├── place_id      INT FK → Place.id
├── vehicule_id   INT FK → Vehicule.id
├── dateEntree    DATETIME
├── dateSortie    DATETIME (NULL si en cours)
└── montant       DECIMAL(8,2) (NULL si en cours)
```

---

## Structure du projet

```
parking/
├── pom.xml                          ← Build Maven
├── sql/
│   └── init.sql                     ← Script création BDD + données test
├── setup/
│   └── GestionParking.iss           ← Script Inno Setup
├── assets/
│   └── launch.bat                   ← Lanceur Windows
└── src/main/java/parking/
    ├── App.java                     ← Point d'entrée
    ├── model/
    │   ├── Place.java
    │   ├── Vehicule.java
    │   └── Stationnement.java
    ├── dao/
    │   ├── IDao.java               ← Interface générique CRUD
    │   ├── PlaceDAO.java
    │   ├── VehiculeDAO.java
    │   ├── StationnementDAO.java
    │   └── DaoTest.java            ← Tests JUnit 5
    ├── ui/
    │   ├── MainFrame.java          ← Fenêtre principale (onglets)
    │   ├── DashboardPanel.java     ← Tableau de bord
    │   ├── PlacePanel.java         ← CRUD Places
    │   ├── VehiculePanel.java      ← CRUD Véhicules
    │   ├── StationnementPanel.java ← CRUD + Entrée/Sortie
    │   └── GraphiquePanel.java     ← Graphique revenus/mois
    └── util/
        └── DatabaseConnection.java  ← Connexion JDBC (Singleton)
```

---

## Prérequis

| Composant       | Version minimale  |
|-----------------|-------------------|
| Java JDK        | 11+               |
| Maven           | 3.8+              |
| MySQL/MariaDB   | 8.0+              |

---

## Installation & Exécution

### 1. Base de données
```sql
-- Dans MySQL Workbench ou CLI :
source sql/init.sql
```

### 2. Configuration JDBC
Modifier si nécessaire dans `src/main/java/parking/util/DatabaseConnection.java` :
```java
private static final String URL      = "jdbc:mysql://localhost:3306/parking_db...";
private static final String USER     = "root";
private static final String PASSWORD = "";
```

### 3. Compilation & lancement
```bash
mvn clean package
java -jar target/GestionParking.jar
```

### 4. Tests JUnit
```bash
mvn test
```

---

## Packaging Windows (Inno Setup)

1. Compiler le JAR : `mvn package`
2. Copier `target/GestionParking.jar` dans le dossier du projet
3. Ouvrir `setup/GestionParking.iss` avec Inno Setup Compiler
4. Cliquer **Build → Compile**
5. Le fichier `dist/GestionParking_Setup.exe` est généré

---

## Règles métiers implémentées

| Règle | Implémentation |
|-------|---------------|
| Une place ne peut être occupée que par un véhicule à la fois | `StationnementDAO.isPlaceOccupee()` → `SQLException` si violation |
| Le montant est calculé automatiquement à la sortie | `Stationnement.calculerMontant()` = durée(h) × tarifHoraire |
| Le statut de la place passe à "Occupée"/"Libre" automatiquement | `StationnementDAO.create()` et `enregistrerSortie()` |
| Validation des champs obligatoires | Méthodes `validate()` dans chaque Panel |

---

## Fonctionnalités par onglet

### 🏠 Tableau de bord
- Compteurs : total places / libres / occupées / en cours
- Liste en temps réel des stationnements en cours

### 🅿 Places
- CRUD complet
- Filtre par type (Auto/Moto/PMR) et statut (Libre/Occupée)
- Recherche textuelle sur le numéro

### 🚗 Véhicules
- CRUD complet
- Recherche par matricule ou marque

### 📋 Stationnements
- Nouvelle entrée : sélection parmi places libres + véhicules disponibles
- Enregistrement sortie : calcul et affichage du montant
- Historique par véhicule (saisie matricule)
- Filtrage multi-critères : type place, statut, période, matricule

### 📊 Graphique
- Barres des revenus mensuels (données réelles de la BDD)
- Rendu Graphics2D intégré (sans dépendance externe)

---

## Auteur
Mini-Projet 20 — Planning 15 jours — Java Swing + MySQL + JDBC + JUnit 5
