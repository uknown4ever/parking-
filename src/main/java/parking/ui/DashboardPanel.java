package parking.ui;

import parking.dao.PlaceDAO;
import parking.dao.StationnementDAO;
import parking.model.Stationnement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Onglet Tableau de bord : résumé des places et stationnements en cours.
 */
public class DashboardPanel extends JPanel {

    private final PlaceDAO         placeDAO         = new PlaceDAO();
    private final StationnementDAO statDAO          = new StationnementDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JLabel lblLibres, lblOccupees, lblTotal, lblEnCours;
    private DefaultTableModel tableModel;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 248, 255));

        add(buildSummaryPanel(), BorderLayout.NORTH);
        add(buildTablePanel(),   BorderLayout.CENTER);

        JButton btnRefresh = new JButton("⟳  Actualiser");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> refresh());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(new Color(245, 248, 255));
        south.add(btnRefresh);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(new Color(245, 248, 255));

        lblTotal    = createCard("Total places",    "0", new Color(70, 130, 180));
        lblLibres   = createCard("Places libres",   "0", new Color(60, 179, 113));
        lblOccupees = createCard("Places occupées", "0", new Color(220, 90,  60));
        lblEnCours  = createCard("En cours",        "0", new Color(255, 165,  0));

        panel.add(lblTotal);
        panel.add(lblLibres);
        panel.add(lblOccupees);
        panel.add(lblEnCours);
        return panel;
    }

    private JLabel createCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setPreferredSize(new Dimension(0, 90));

        JLabel lTitle = new JLabel(title, JLabel.CENTER);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lTitle.setForeground(Color.WHITE);

        JLabel lValue = new JLabel(value, JLabel.CENTER);
        lValue.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lValue.setForeground(Color.WHITE);

        card.add(lTitle, BorderLayout.NORTH);
        card.add(lValue, BorderLayout.CENTER);

        // Return value label to update later
        lValue.setName(title);
        return lValue;
    }

    private JPanel buildTablePanel() {
        String[] cols = {"ID", "Place", "Type place", "Véhicule", "Marque", "Entrée", "Durée (h)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 248, 255));
        panel.add(new JLabel("  Stationnements en cours :", JLabel.LEFT), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        try {
            List<parking.model.Place> allPlaces = placeDAO.findAll();
            long libres   = allPlaces.stream().filter(p -> p.getStatut() == parking.model.Place.Statut.Libre).count();
            long occupees = allPlaces.stream().filter(p -> p.getStatut() == parking.model.Place.Statut.Occupée).count();

            lblTotal.setText(String.valueOf(allPlaces.size()));
            lblLibres.setText(String.valueOf(libres));
            lblOccupees.setText(String.valueOf(occupees));

            List<Stationnement> enCours = statDAO.findEnCours();
            lblEnCours.setText(String.valueOf(enCours.size()));

            tableModel.setRowCount(0);
            for (Stationnement s : enCours) {
                long minutes = java.time.Duration.between(s.getDateEntree(), java.time.LocalDateTime.now()).toMinutes();
                double heures = Math.round(minutes / 60.0 * 10) / 10.0;
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getPlace().getNumero(),
                        s.getPlace().getType(),
                        s.getVehicule().getMatricule(),
                        s.getVehicule().getMarque(),
                        s.getDateEntree().format(FMT),
                        heures
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
