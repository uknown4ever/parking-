package parking.dao;

import parking.model.Vehicule;
import parking.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'entité Véhicule.
 */
public class VehiculeDAO implements IDao<Vehicule> {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    @Override
    public void create(Vehicule v) throws SQLException {
        String sql = "INSERT INTO Vehicule (matricule, marque, categorie) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getMatricule());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getCategorie().name());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) v.setId(rs.getInt(1));
        }
    }

    @Override
    public void update(Vehicule v) throws SQLException {
        String sql = "UPDATE Vehicule SET matricule=?, marque=?, categorie=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, v.getMatricule());
            ps.setString(2, v.getMarque());
            ps.setString(3, v.getCategorie().name());
            ps.setInt(4, v.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM Vehicule WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Vehicule findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM Vehicule WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Vehicule ORDER BY matricule")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Vehicule findByMatricule(String matricule) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM Vehicule WHERE matricule=?")) {
            ps.setString(1, matricule);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    private Vehicule mapRow(ResultSet rs) throws SQLException {
        return new Vehicule(
                rs.getInt("id"),
                rs.getString("matricule"),
                rs.getString("marque"),
                Vehicule.Categorie.valueOf(rs.getString("categorie"))
        );
    }
}
