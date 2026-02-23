package parking.dao;

import parking.model.Place;
import parking.model.Stationnement;
import parking.model.Vehicule;

import java.time.LocalDateTime;
import java.sql.SQLException;

/**
 * Tests manuels CRUD (sans JUnit - exécutable directement).
 * Lance avec : java -cp target/GestionParking.jar parking.dao.DaoTest
 */
public class DaoTest {

    static PlaceDAO         placeDAO    = new PlaceDAO();
    static VehiculeDAO      vehiculeDAO = new VehiculeDAO();
    static StationnementDAO statDAO     = new StationnementDAO();

    static int testPlaceId;
    static int testVehiculeId;
    static int testStatId;

    public static void main(String[] args) {
        System.out.println("=== TESTS DAO - Gestion Parking ===\n");
        int ok = 0, fail = 0;

        // Test 1
        try {
            Place p = new Place(0, "TEST01", Place.Type.Auto, Place.Statut.Libre, 3.0);
            placeDAO.create(p);
            testPlaceId = p.getId();
            System.out.println("✅ Test 1 - Create Place OK (id=" + testPlaceId + ")");
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 1 - FAIL: " + e.getMessage()); fail++; }

        // Test 2
        try {
            Place p = placeDAO.findById(testPlaceId);
            System.out.println("✅ Test 2 - FindById OK: " + p.getNumero());
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 2 - FAIL: " + e.getMessage()); fail++; }

        // Test 3
        try {
            Place p = placeDAO.findById(testPlaceId);
            p.setTarifHoraire(5.0);
            placeDAO.update(p);
            System.out.println("✅ Test 3 - Update Place OK");
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 3 - FAIL: " + e.getMessage()); fail++; }

        // Test 4
        try {
            Vehicule v = new Vehicule(0, "ZZ-000-ZZ", "TestMarque", Vehicule.Categorie.Auto);
            vehiculeDAO.create(v);
            testVehiculeId = v.getId();
            System.out.println("✅ Test 4 - Create Vehicule OK (id=" + testVehiculeId + ")");
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 4 - FAIL: " + e.getMessage()); fail++; }

        // Test 5
        try {
            Place p    = placeDAO.findById(testPlaceId);
            Vehicule v = vehiculeDAO.findById(testVehiculeId);
            Stationnement s = new Stationnement();
            s.setPlace(p); s.setVehicule(v);
            s.setDateEntree(LocalDateTime.now().minusHours(2));
            statDAO.create(s);
            testStatId = s.getId();
            System.out.println("✅ Test 5 - Create Stationnement OK (id=" + testStatId + ")");
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 5 - FAIL: " + e.getMessage()); fail++; }

        // Test 6
        try {
            statDAO.enregistrerSortie(testStatId, LocalDateTime.now());
            Stationnement s = statDAO.findById(testStatId);
            System.out.println("✅ Test 6 - Sortie OK (montant=" + s.getMontant() + " €)");
            ok++;
        } catch (Exception e) { System.out.println("❌ Test 6 - FAIL: " + e.getMessage()); fail++; }

        // Test 7 - Contrainte anti-chevauchement
        try {
            Place p    = placeDAO.findById(testPlaceId);
            Vehicule v = vehiculeDAO.findById(testVehiculeId);
            Stationnement s1 = new Stationnement();
            s1.setPlace(p); s1.setVehicule(v);
            s1.setDateEntree(LocalDateTime.now());
            statDAO.create(s1);

            Stationnement s2 = new Stationnement();
            s2.setPlace(p); s2.setVehicule(v);
            s2.setDateEntree(LocalDateTime.now());
            try {
                statDAO.create(s2);
                System.out.println("❌ Test 7 - Contrainte NON respectée !");
                fail++;
            } catch (SQLException ex) {
                System.out.println("✅ Test 7 - Contrainte anti-chevauchement OK");
                ok++;
            }
            statDAO.enregistrerSortie(s1.getId(), LocalDateTime.now());
        } catch (Exception e) { System.out.println("❌ Test 7 - FAIL: " + e.getMessage()); fail++; }

        // Nettoyage
        try {
            statDAO.delete(testStatId);
            vehiculeDAO.delete(testVehiculeId);
            placeDAO.delete(testPlaceId);
            System.out.println("✅ Nettoyage OK");
        } catch (Exception e) { System.out.println("⚠ Nettoyage: " + e.getMessage()); }

        System.out.println("\n=== Résultat : " + ok + " OK / " + fail + " FAIL ===");
    }
}
