# 🅿 Gestion de Parking — Guide de démarrage

Application Java Swing de gestion de parking (Mini-Projet 20).
CRUD complet : Places, Véhicules, Stationnements + graphique des revenus.

---

## ✅ Prérequis à installer

| Outil | Version | Lien de téléchargement |
|-------|---------|------------------------|
| Java JDK | 17+ | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org/download.cgi |
| XAMPP (MySQL) | Toute version récente | https://www.apachefriends.org |

---

## 📦 Étape 1 — Installer Java

1. Va sur **https://adoptium.net**
2. Télécharge **Temurin JDK 17**
3. Lance l'installateur
4. ⚠️ Coche bien **"Set JAVA_HOME variable"** pendant l'installation
5. Vérifie dans un terminal :
   ```
   java -version
   ```
   → Tu dois voir `openjdk 17.x.x`

---

## 📦 Étape 2 — Installer Maven

1. Va sur **https://maven.apache.org/download.cgi**
2. Télécharge le **Binary zip archive** (ex: `apache-maven-3.9.x-bin.zip`)
3. Extrais dans `C:\Program Files\Maven\`
4. Ajoute au PATH Windows :
   - Recherche **"Variables d'environnement"** dans le menu Démarrer
   - Variables système → `Path` → Modifier → Nouveau
   - Ajoute : `C:\Program Files\Maven\apache-maven-3.9.x\bin`
5. **Redémarre le PC** puis vérifie :
   ```
   mvn -version
   ```
   → Tu dois voir `Apache Maven 3.x.x`

---

## 🗄️ Étape 3 — Créer la base de données

1. Ouvre **XAMPP Control Panel**
2. Clique **Start** à côté de **MySQL** (le statut devient vert)
3. Ouvre ton navigateur → **http://localhost/phpmyadmin**
4. Dans la colonne gauche, clique **"Nouvelle base de données"**
5. Nom : `parking_db` → clic **Créer**
6. Clique sur l'onglet **SQL** en haut
7. Ouvre le fichier `sql/init.sql` (dans le dossier du projet) avec le Bloc-notes
8. Copie tout le contenu → colle dans phpMyAdmin → clique **Exécuter**
9. Tu dois voir apparaître les tables : `Place`, `Vehicule`, `Stationnement`

---

## ⚙️ Étape 4 — Configurer la connexion

Ouvre le fichier :
```
src/main/java/parking/util/DatabaseConnection.java
```

Vérifie que ces lignes correspondent à ta configuration XAMPP :
```java
private static final String URL      = "jdbc:mysql://localhost:3306/parking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
private static final String USER     = "root";
private static final String PASSWORD = "";   // vide par défaut avec XAMPP
```

> Si tu as défini un mot de passe MySQL, remplace `""` par ton mot de passe.

---

## 🔨 Étape 5 — Compiler le projet

Ouvre un terminal **cmd** (pas VS Code) dans le dossier du projet :

```bash
cd C:\chemin\vers\le\dossier\parking
mvn clean package
```

Tu dois voir à la fin :
```
BUILD SUCCESS
```

Le fichier JAR est généré dans : `target\GestionParking.jar`

---

## 🚀 Étape 6 — Lancer l'application

```bash
java -jar target\GestionParking.jar
```

La fenêtre principale s'ouvre avec 5 onglets :

| Onglet | Contenu |
|--------|---------|
| 🏠 Tableau de bord | Résumé en temps réel (places libres/occupées, stationnements en cours) |
| 🅿 Places | Ajouter, modifier, supprimer des places + filtres |
| 🚗 Véhicules | Ajouter, modifier, supprimer des véhicules + recherche |
| 📋 Stationnements | Enregistrer entrées/sorties, calcul automatique du montant |
| 📊 Graphique | Revenus par mois en graphique à barres |

---

## ❓ Problèmes fréquents

**`mvn` ou `java` non reconnu**
→ Redémarre le PC après l'installation, puis réouvre le terminal.

**Erreur de connexion à la base de données**
→ Vérifie que MySQL est démarré dans XAMPP (bouton vert).
→ Vérifie que la base `parking_db` existe dans phpMyAdmin.

**La fenêtre ne s'affiche pas**
→ Lance depuis `cmd` (Windows + R → `cmd`), pas depuis VS Code.
→ Regarde dans la barre des tâches en bas de l'écran (Alt + Tab).

**`BUILD FAILURE` à la compilation**
→ Vérifie que tu es bien dans le bon dossier (celui qui contient `pom.xml`).

---

## 📁 Structure du projet

```
parking/
├── pom.xml                          ← Fichier de build Maven
├── sql/
│   └── init.sql                     ← Script SQL (tables + données de test)
├── src/main/java/parking/
│   ├── App.java                     ← Point d'entrée (main)
│   ├── model/                       ← Place, Vehicule, Stationnement
│   ├── dao/                         ← Accès base de données (CRUD)
│   ├── ui/                          ← Interface graphique (Swing)
│   └── util/
│       └── DatabaseConnection.java  ← Configuration JDBC
└── target/
    └── GestionParking.jar           ← JAR exécutable (généré après mvn package)
```



