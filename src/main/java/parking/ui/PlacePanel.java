package parking.ui;

import parking.dao.PlaceDAO;
import parking.model.Place;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Onglet CRUD Gestion des Places.
 */
public class PlacePanel extends JPanel {

    private final PlaceDAO dao = new PlaceDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private TableRowSorter<DefaultTableModel> sorter;

    // Formulaire
    private JTextField  tfNumero, tfTarif, tfSearch;
    private JComboBox<String> cbType, cbStatut, cbFilterType, cbFilterStatut;

    public PlacePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 248, 255));

        add(buildFilterBar(),  BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(),  BorderLayout.EAST);

        loadTable(null, null);
    }

    // ---- Filter bar ----
    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(new Color(245, 248, 255));

        tfSearch = new JTextField(12);
        cbFilterType   = new JComboBox<>(new String[]{"Tous types", "Auto", "Moto", "PMR"});
        cbFilterStatut = new JComboBox<>(new String[]{"Tous statuts", "Libre", "Occupée"});

        JButton btnFilter = new JButton("Filtrer");
        btnFilter.addActionListener(e -> applyFilter());

        p.add(new JLabel("Rechercher :"));  p.add(tfSearch);
        p.add(new JLabel("Type :"));        p.add(cbFilterType);
        p.add(new JLabel("Statut :"));      p.add(cbFilterStatut);
        p.add(btnFilter);
        return p;
    }

    // ---- Table ----
    private JPanel buildTablePanel() {
        String[] cols = {"ID", "Numéro", "Type", "Statut", "Tarif (€/h)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(245, 248, 255));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ---- Formulaire ----
    private JPanel buildFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("  Détails"));
        p.setPreferredSize(new Dimension(220, 0));

        tfNumero = new JTextField(12);
        tfTarif  = new JTextField(12);
        cbType   = new JComboBox<>(new String[]{"Auto", "Moto", "PMR"});
        cbStatut = new JComboBox<>(new String[]{"Libre", "Occupée"});

        addFormRow(p, "Numéro :",  tfNumero);
        addFormRow(p, "Type :",    cbType);
        addFormRow(p, "Statut :",  cbStatut);
        addFormRow(p, "Tarif €/h:", tfTarif);

        p.add(Box.createVerticalStrut(10));

        JButton btnAjouter    = new JButton("➕ Ajouter");
        JButton btnModifier   = new JButton("✏ Modifier");
        JButton btnSupprimer  = new JButton("🗑 Supprimer");
        JButton btnVider      = new JButton("Vider");

        btnAjouter.addActionListener(e  -> createPlace());
        btnModifier.addActionListener(e -> updatePlace());
        btnSupprimer.addActionListener(e-> deletePlace());
        btnVider.addActionListener(e    -> clearForm());

        for (JButton btn : new JButton[]{btnAjouter, btnModifier, btnSupprimer, btnVider}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(180, 32));
            p.add(Box.createVerticalStrut(6));
            p.add(btn);
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    private void addFormRow(JPanel p, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 0));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(200, 28));
        p.add(lbl);
        p.add(field);
    }

    // ---- Actions CRUD ----
    private void createPlace() {
        try {
            validateForm();
            Place p = new Place(0, tfNumero.getText().trim(),
                    Place.Type.valueOf((String) cbType.getSelectedItem()),
                    Place.Statut.valueOf((String) cbStatut.getSelectedItem()),
                    Double.parseDouble(tfTarif.getText().trim()));
            dao.create(p);
            clearForm();
            loadTable(null, null);
            JOptionPane.showMessageDialog(this, "Place créée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePlace() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une place."); return; }
        try {
            validateForm();
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            Place p = new Place(id, tfNumero.getText().trim(),
                    Place.Type.valueOf((String) cbType.getSelectedItem()),
                    Place.Statut.valueOf((String) cbStatut.getSelectedItem()),
                    Double.parseDouble(tfTarif.getText().trim()));
            dao.update(p);
            loadTable(null, null);
            JOptionPane.showMessageDialog(this, "Place modifiée.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlace() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une place."); return; }
        if (JOptionPane.showConfirmDialog(this, "Supprimer cette place ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            dao.delete(id);
            clearForm();
            loadTable(null, null);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        String type   = cbFilterType.getSelectedIndex()   == 0 ? null : (String) cbFilterType.getSelectedItem();
        String statut = cbFilterStatut.getSelectedIndex() == 0 ? null : (String) cbFilterStatut.getSelectedItem();

        // Also apply text search
        String search = tfSearch.getText().trim();
        if (!search.isEmpty()) {
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + search);
            sorter.setRowFilter(rf);
        } else {
            sorter.setRowFilter(null);
        }
        loadTable(type, statut);
    }

    private void loadTable(String type, String statut) {
        try {
            List<Place> list = dao.findFiltered(type, statut);
            tableModel.setRowCount(0);
            for (Place p : list) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getNumero(), p.getType(), p.getStatut(), p.getTarifHoraire()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int model = table.convertRowIndexToModel(row);
        tfNumero.setText((String) tableModel.getValueAt(model, 1));
        cbType.setSelectedItem(tableModel.getValueAt(model, 2).toString());
        cbStatut.setSelectedItem(tableModel.getValueAt(model, 3).toString());
        tfTarif.setText(tableModel.getValueAt(model, 4).toString());
    }

    private void clearForm() {
        tfNumero.setText("");
        tfTarif.setText("");
        cbType.setSelectedIndex(0);
        cbStatut.setSelectedIndex(0);
        table.clearSelection();
    }

    private void validateForm() throws Exception {
        if (tfNumero.getText().trim().isEmpty()) throw new Exception("Le numéro est obligatoire.");
        if (tfTarif.getText().trim().isEmpty())  throw new Exception("Le tarif est obligatoire.");
        try { Double.parseDouble(tfTarif.getText().trim()); }
        catch (NumberFormatException e) { throw new Exception("Tarif invalide (nombre attendu)."); }
    }
}
