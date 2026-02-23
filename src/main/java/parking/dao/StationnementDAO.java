package parking.dao;

import parking.model.Place;
import parking.model.Stationnement;
import parking.model.Vehicule;
import parking.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour l'entité Stationnement.
 * Applique les règles métiers :
 *  - Empêche l'occupation simultanée d'une même place (2 véhicules en même temps)
 *  - Calcule automatiquement le montant à la sortie
 */
public class StationnementDAO implements IDao<Stationnement> {

    private final PlaceDAO    placeDAO    = new PlaceDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    /** Enregistre une entrée (attribue une place à un véhicule). */
    @Override
    public void create(Stationnement s) throws SQLException {
        // Règle : la place ne doit pas être déjà occupée
        if (isPlaceOccupee(s.getPlace().getId())) {
            throw new SQLException("La place " + s.getPlace().getNumero() + " est déjà occupée.");
        }
        String sql = "INSERT INTO Stationnement (place_id, vehicule_id, dateEntree) VALUES (?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getPlace().getId());
            ps.setInt(2, s.getVehicule().getId());
            ps.setTimestamp(3, Timestamp.valueOf(s.getDateEntree()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) s.setId(rs.getInt(1));
        }
        // Mettre à jour le statut de la place
        s.getPlace().setStatut(Place.Statut.Occupée);
        placeDAO.update(s.getPlace());
    }

    /** Enregistre la sortie : calcule et sauvegarde le montant. */
    public void enregistrerSortie(int statId, LocalDateTime dateSortie) throws SQLException {
        Stationnement s = findById(statId);
        if (s == null) throw new SQLException("Stationnement introuvable (id=" + statId + ")");
        if (!s.isEnCours()) throw new SQLException("Ce stationnement est déjà terminé.");

        s.setDateSortie(dateSortie);
        double montant = s.calculerMontant();
        s.setMontant(montant);

        String sql = "UPDATE Stationnement SET dateSortie=?, montant=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dateSortie));
            ps.setDouble(2, montant);
            ps.setInt(3, statId);
            ps.executeUpdate();
        }
        // Libérer la place
        Place place = s.getPlace();
        place.setStatut(Place.Statut.Libre);
        placeDAO.update(place);
    }

    @Override
    public void update(Stationnement s) throws SQLException {
        String sql = "UPDATE Stationnement SET place_id=?, vehicule_id=?, dateEntree=?, dateSortie=?, montant=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, s.getPlace().getId());
            ps.setInt(2, s.getVehicule().getId());
            ps.setTimestamp(3, Timestamp.valueOf(s.getDateEntree()));
            ps.setTimestamp(4, s.getDateSortie() != null ? Timestamp.valueOf(s.getDateSortie()) : null);
            if (s.getMontant() != null) ps.setDouble(5, s.getMontant());
            else ps.setNull(5, Types.DECIMAL);
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM Stationnement WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Stationnement findById(int id) throws SQLException {
        String sql = buildSelectSql("WHERE s.id=?");
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Stationnement> findAll() throws SQLException {
        List<Stationnement> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(buildSelectSql("ORDER BY s.dateEntree DESC"))) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Historique des stationnements pour un véhicule donné. */
    public List<Stationnement> findByVehicule(int vehiculeId) throws SQLException {
        List<Stationnement> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                buildSelectSql("WHERE s.vehicule_id=? ORDER BY s.dateEntree DESC"))) {
            ps.setInt(1, vehiculeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Stationnements en cours (non terminés). */
    public List<Stationnement> findEnCours() throws SQLException {
        List<Stationnement> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(buildSelectSql("WHERE s.dateSortie IS NULL ORDER BY s.dateEntree"))) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Filtrage avancé : type de place, période, statut (en cours / terminé). */
    public List<Stationnement> findFiltered(String typePlaceFilter, String statutFilter,
                                             String dateDebutFilter, String dateFinFilter) throws SQLException {
        List<Stationnement> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(buildSelectSql("WHERE 1=1"));

        List<Object> params = new ArrayList<>();
        if (typePlaceFilter != null && !typePlaceFilter.isEmpty()) {
            sb.append(" AND p.type=?");
            params.add(typePlaceFilter);
        }
        if ("En cours".equals(statutFilter)) {
            sb.append(" AND s.dateSortie IS NULL");
        } else if ("Terminé".equals(statutFilter)) {
            sb.append(" AND s.dateSortie IS NOT NULL");
        }
        if (dateDebutFilter != null && !dateDebutFilter.isEmpty()) {
            sb.append(" AND DATE(s.dateEntree) >= ?");
            params.add(dateDebutFilter);
        }
        if (dateFinFilter != null && !dateFinFilter.isEmpty()) {
            sb.append(" AND DATE(s.dateEntree) <= ?");
            params.add(dateFinFilter);
        }
        sb.append(" ORDER BY s.dateEntree DESC");

        try (PreparedStatement ps = conn().prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Revenus groupés par mois (pour le graphique). */
    public List<Object[]> getRevenusParMois() throws SQLException {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(dateSortie,'%Y-%m') AS mois, SUM(montant) AS total " +
                     "FROM Stationnement WHERE dateSortie IS NOT NULL " +
                     "GROUP BY mois ORDER BY mois";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Object[]{rs.getString("mois"), rs.getDouble("total")});
            }
        }
        return result;
    }

    // ---- Utilitaires ----

    public boolean isPlaceOccupee(int placeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Stationnement WHERE place_id=? AND dateSortie IS NULL";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, placeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    private String buildSelectSql(String whereClause) {
        return "SELECT s.id, s.dateEntree, s.dateSortie, s.montant, " +
               "p.id AS pid, p.numero, p.type AS ptype, p.statut, p.tarifHoraire, " +
               "v.id AS vid, v.matricule, v.marque, v.categorie " +
               "FROM Stationnement s " +
               "JOIN Place p ON s.place_id = p.id " +
               "JOIN Vehicule v ON s.vehicule_id = v.id " +
               whereClause;
    }

    private Stationnement mapRow(ResultSet rs) throws SQLException {
        Place place = new Place(
                rs.getInt("pid"), rs.getString("numero"),
                Place.Type.valueOf(rs.getString("ptype")),
                Place.Statut.valueOf(rs.getString("statut")),
                rs.getDouble("tarifHoraire"));

        Vehicule vehicule = new Vehicule(
                rs.getInt("vid"), rs.getString("matricule"),
                rs.getString("marque"),
                Vehicule.Categorie.valueOf(rs.getString("categorie")));

        Timestamp entree  = rs.getTimestamp("dateEntree");
        Timestamp sortie  = rs.getTimestamp("dateSortie");
        double    montant = rs.getDouble("montant");

        return new Stationnement(
                rs.getInt("id"), place, vehicule,
                entree != null ? entree.toLocalDateTime() : null,
                sortie != null ? sortie.toLocalDateTime() : null,
                rs.wasNull() ? null : montant);
    }
}
