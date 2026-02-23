package parking.dao;

import parking.model.Place;
import parking.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'entité Place.
 */
public class PlaceDAO implements IDao<Place> {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    @Override
    public void create(Place p) throws SQLException {
        String sql = "INSERT INTO Place (numero, type, statut, tarifHoraire) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNumero());
            ps.setString(2, p.getType().name());
            ps.setString(3, p.getStatut().name());
            ps.setDouble(4, p.getTarifHoraire());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) p.setId(rs.getInt(1));
        }
    }

    @Override
    public void update(Place p) throws SQLException {
        String sql = "UPDATE Place SET numero=?, type=?, statut=?, tarifHoraire=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getNumero());
            ps.setString(2, p.getType().name());
            ps.setString(3, p.getStatut().name());
            ps.setDouble(4, p.getTarifHoraire());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM Place WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Place findById(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM Place WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Place> findAll() throws SQLException {
        List<Place> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Place ORDER BY numero")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Retourne uniquement les places libres d'un type donné. */
    public List<Place> findLibresByType(Place.Type type) throws SQLException {
        List<Place> list = new ArrayList<>();
        String sql = "SELECT * FROM Place WHERE statut='Libre' AND type=? ORDER BY numero";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Filtre par type et/ou statut (null = pas de filtre). */
    public List<Place> findFiltered(String type, String statut) throws SQLException {
        List<Place> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM Place WHERE 1=1");
        if (type   != null && !type.isEmpty())   sb.append(" AND type=?");
        if (statut != null && !statut.isEmpty()) sb.append(" AND statut=?");
        sb.append(" ORDER BY numero");
        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            int idx = 1;
            if (type   != null && !type.isEmpty())   ps.setString(idx++, type);
            if (statut != null && !statut.isEmpty()) ps.setString(idx,   statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Place mapRow(ResultSet rs) throws SQLException {
        return new Place(
                rs.getInt("id"),
                rs.getString("numero"),
                Place.Type.valueOf(rs.getString("type")),
                Place.Statut.valueOf(rs.getString("statut")),
                rs.getDouble("tarifHoraire")
        );
    }
}
