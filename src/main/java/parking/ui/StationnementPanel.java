package parking.ui;

import parking.dao.PlaceDAO;
import parking.dao.StationnementDAO;
import parking.dao.VehiculeDAO;
import parking.model.Place;
import parking.model.Stationnement;
import parking.model.Vehicule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Onglet Gestion des Stationnements.
 * - Enregistrer une entrée (attribuer place libre)
 * - Enregistrer une sortie (calcul automatique du montant)
 * - Filtrage par type de place, période, statut
 * - Historique par véhicule
 */
public class StationnementPanel extends JPanel {

    private final StationnementDAO dao          = new StationnementDAO();
    private final PlaceDAO         placeDAO     = new PlaceDAO();
    private final VehiculeDAO      vehiculeDAO  = new VehiculeDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DefaultTableModel tableModel;
    private JTable            table;

    // Filtres
    private JComboBox<String> cbFilterType, cbFilterStatut;
    private JTextField        tfFilterDateDebut, tfFilterDateFin, tfFilterVehicule;

    public StationnementPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 248, 255));

        add(buildTopPanel(),   BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(),BorderLayout.SOUTH);

        loadTable();
    }

    // ---- Filtres ----
    private JPanel buildTopPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(new Color(245, 248, 255));
        p.setBorder(BorderFactory.createTitledBorder("Filtres"));

        cbFilterType   = new JComboBox<>(new String[]{"Tous types", "Auto", "Moto", "PMR"});
        cbFilterStatut = new JComboBox<>(new String[]{"Tous", "En cours", "Terminé"});
        tfFilterDateDebut = new JTextField("", 10);
        tfFilterDateFin   = new JTextField("", 10);
        tfFilterVehicule  = new JTextField("", 12);

        JButton btnFilter  = new JButton("Filtrer");
        JButton btnReset   = new JButton("Réinitialiser");

        btnFilter.addActionListener(e  -> applyFilter());
        btnReset.addActionListener(e   -> { cbFilterType.setSelectedIndex(0); cbFilterStatut.setSelectedIndex(0);
                                             tfFilterDateDebut.setText(""); tfFilterDateFin.setText("");
                                             tfFilterVehicule.setText(""); loadTable(); });

        p.add(new JLabel("Type place :")); p.add(cbFilterType);
        p.add(new JLabel("Statut :"));    p.add(cbFilterStatut);
        p.add(new JLabel("Du (yyyy-mm-dd) :")); p.add(tfFilterDateDebut);
        p.add(new JLabel("Au :"));        p.add(tfFilterDateFin);
        p.add(new JLabel("Matricule :")); p.add(tfFilterVehicule);
        p.add(btnFilter); p.add(btnReset);
        return p;
    }

    // ---- Table ----
    private JPanel buildTablePanel() {
        String[] cols = {"ID", "Place", "Type Place", "Véhicule", "Marque", "Entrée", "Sortie", "Montant (€)", "Statut"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 248, 255));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ---- Boutons d'action ----
    private JPanel buildActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(new Color(245, 248, 255));

        JButton btnEntree   = new JButton("🚗 Nouvelle Entrée");
        JButton btnSortie   = new JButton("🏁 Enregistrer Sortie");
        JButton btnHistorique= new JButton("📋 Historique Véhicule");
        JButton btnSupprimer = new JButton("🗑 Supprimer");
        JButton btnRefresh  = new JButton("⟳ Actualiser");

        btnEntree.setBackground(new Color(60, 179, 113)); btnEntree.setForeground(Color.WHITE);
        btnSortie.setBackground(new Color(220, 90, 60));  btnSortie.setForeground(Color.WHITE);

        btnEntree.addActionListener(e    -> dialogEntree());
        btnSortie.addActionListener(e    -> enregistrerSortie());
        btnHistorique.addActionListener(e-> dialogHistorique());
        btnSupprimer.addActionListener(e -> supprimerStationnement());
        btnRefresh.addActionListener(e   -> loadTable());

        for (JButton b : new JButton[]{btnEntree, btnSortie, btnHistorique, btnSupprimer, btnRefresh}) p.add(b);
        return p;
    }

    // ---- Dialog Nouvelle Entrée ----
    private void dialogEntree() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nouvelle Entrée", true);
        dlg.setSize(400, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new GridLayout(0, 2, 8, 8));
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<String> cbPlaces    = new JComboBox<>();
        JComboBox<String> cbVehicules = new JComboBox<>();

        // Remplir listes
        try {
            List<Place> libres = placeDAO.findFiltered(null, "Libre");
            for (Place pl : libres) cbPlaces.addItem(pl.getId() + " – " + pl.getNumero() + " (" + pl.getType() + ") " + pl.getTarifHoraire() + "€/h");
            List<Vehicule> vehicules = vehiculeDAO.findAll();
            for (Vehicule v : vehicules) cbVehicules.addItem(v.getId() + " – " + v.getMatricule() + " " + v.getMarque());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dlg.add(new JLabel("Place libre :")); dlg.add(cbPlaces);
        dlg.add(new JLabel("Véhicule :"));   dlg.add(cbVehicules);

        JButton btnOk = new JButton("Enregistrer l'entrée");
        JButton btnAnnuler = new JButton("Annuler");
        dlg.add(btnAnnuler); dlg.add(btnOk);

        btnOk.addActionListener(e -> {
            if (cbPlaces.getItemCount() == 0 || cbVehicules.getItemCount() == 0) {
                JOptionPane.showMessageDialog(dlg, "Aucune place libre ou véhicule disponible.");
                return;
            }
            try {
                String placeStr = (String) cbPlaces.getSelectedItem();
                String vehicStr = (String) cbVehicules.getSelectedItem();
                int placeId    = Integer.parseInt(placeStr.split(" ")[0]);
                int vehiculeId = Integer.parseInt(vehicStr.split(" ")[0]);

                Place   place   = placeDAO.findById(placeId);
                Vehicule vehicule = vehiculeDAO.findById(vehiculeId);

                Stationnement s = new Stationnement();
                s.setPlace(place); s.setVehicule(vehicule);
                s.setDateEntree(LocalDateTime.now());
                dao.create(s);

                JOptionPane.showMessageDialog(dlg, "Entrée enregistrée ! Place " + place.getNumero() + " attribuée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                loadTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnAnnuler.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    // ---- Enregistrer Sortie ----
    private void enregistrerSortie() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un stationnement en cours."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String statut = (String) tableModel.getValueAt(row, 8);
        if (!"En cours".equals(statut)) {
            JOptionPane.showMessageDialog(this, "Ce stationnement est déjà terminé."); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Enregistrer la sortie maintenant ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.enregistrerSortie(id, LocalDateTime.now());
            // Retrieve and show montant
            Stationnement s = dao.findById(id);
            JOptionPane.showMessageDialog(this,
                    String.format("Sortie enregistrée.\nMontant calculé : %.2f €", s.getMontant()),
                    "Sortie", JOptionPane.INFORMATION_MESSAGE);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Historique Véhicule ----
    private void dialogHistorique() {
        String mat = JOptionPane.showInputDialog(this, "Matricule du véhicule :");
        if (mat == null || mat.trim().isEmpty()) return;
        try {
            Vehicule v = vehiculeDAO.findByMatricule(mat.trim().toUpperCase());
            if (v == null) { JOptionPane.showMessageDialog(this, "Véhicule non trouvé."); return; }
            List<Stationnement> hist = dao.findByVehicule(v.getId());

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Historique – " + v.getMatricule() + " " + v.getMarque(), true);
            dlg.setSize(700, 350); dlg.setLocationRelativeTo(this);

            String[] cols = {"Place", "Entrée", "Sortie", "Montant (€)", "Statut"};
            DefaultTableModel m = new DefaultTableModel(cols, 0);
            for (Stationnement s : hist) {
                m.addRow(new Object[]{
                        s.getPlace().getNumero(),
                        s.getDateEntree().format(FMT),
                        s.getDateSortie() != null ? s.getDateSortie().format(FMT) : "—",
                        s.getMontant() != null ? String.format("%.2f", s.getMontant()) : "—",
                        s.isEnCours() ? "En cours" : "Terminé"
                });
            }
            JTable t = new JTable(m); t.setRowHeight(24);
            dlg.add(new JScrollPane(t));
            dlg.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Supprimer ----
    private void supprimerStationnement() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un stationnement."); return; }
        if (JOptionPane.showConfirmDialog(this, "Supprimer ce stationnement ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            dao.delete(id);
            loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String type   = cbFilterType.getSelectedIndex()   == 0 ? null : (String) cbFilterType.getSelectedItem();
        String statut = cbFilterStatut.getSelectedIndex() == 0 ? null : (String) cbFilterStatut.getSelectedItem();
        String debut  = tfFilterDateDebut.getText().trim().isEmpty() ? null : tfFilterDateDebut.getText().trim();
        String fin    = tfFilterDateFin.getText().trim().isEmpty()   ? null : tfFilterDateFin.getText().trim();
        try {
            List<Stationnement> list = dao.findFiltered(type, statut, debut, fin);
            // Apply vehicle filter client-side
            String matFilter = tfFilterVehicule.getText().trim().toUpperCase();
            populateTable(list, matFilter);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTable() {
        try {
            List<Stationnement> list = dao.findAll();
            populateTable(list, "");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Stationnement> list, String matFilter) {
        tableModel.setRowCount(0);
        for (Stationnement s : list) {
            if (!matFilter.isEmpty() && !s.getVehicule().getMatricule().contains(matFilter)) continue;
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getPlace().getNumero(),
                    s.getPlace().getType(),
                    s.getVehicule().getMatricule(),
                    s.getVehicule().getMarque(),
                    s.getDateEntree().format(FMT),
                    s.getDateSortie() != null ? s.getDateSortie().format(FMT) : "—",
                    s.getMontant() != null ? String.format("%.2f", s.getMontant()) : "—",
                    s.isEnCours() ? "En cours" : "Terminé"
            });
        }
    }
}
