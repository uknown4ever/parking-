package parking.ui;

import parking.dao.VehiculeDAO;
import parking.model.Vehicule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Onglet CRUD Gestion des Véhicules.
 */
public class VehiculePanel extends JPanel {

    private final VehiculeDAO dao = new VehiculeDAO();

    private DefaultTableModel tableModel;
    private JTable            table;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField  tfMatricule, tfMarque, tfSearch;
    private JComboBox<String> cbCategorie;

    public VehiculePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 248, 255));

        add(buildSearchBar(),  BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(),  BorderLayout.EAST);

        loadTable();
    }

    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(new Color(245, 248, 255));
        tfSearch = new JTextField(20);
        JButton btnSearch = new JButton("Rechercher");
        btnSearch.addActionListener(e -> {
            String s = tfSearch.getText().trim();
            if (s.isEmpty()) sorter.setRowFilter(null);
            else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + s));
        });
        p.add(new JLabel("Rechercher (matricule / marque) :")); p.add(tfSearch); p.add(btnSearch);
        return p;
    }

    private JPanel buildTablePanel() {
        String[] cols = {"ID", "Matricule", "Marque", "Catégorie"};
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

    private JPanel buildFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("  Détails"));
        p.setPreferredSize(new Dimension(220, 0));

        tfMatricule = new JTextField(12);
        tfMarque    = new JTextField(12);
        cbCategorie = new JComboBox<>(new String[]{"Auto", "Moto", "PMR"});

        addFormRow(p, "Matricule :",  tfMatricule);
        addFormRow(p, "Marque :",     tfMarque);
        addFormRow(p, "Catégorie :",  cbCategorie);
        p.add(Box.createVerticalStrut(10));

        JButton btnAjouter   = new JButton("➕ Ajouter");
        JButton btnModifier  = new JButton("✏ Modifier");
        JButton btnSupprimer = new JButton("🗑 Supprimer");
        JButton btnVider     = new JButton("Vider");

        btnAjouter.addActionListener(e  -> createVehicule());
        btnModifier.addActionListener(e -> updateVehicule());
        btnSupprimer.addActionListener(e-> deleteVehicule());
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
        p.add(lbl); p.add(field);
    }

    private void createVehicule() {
        try {
            validateForm();
            Vehicule v = new Vehicule(0, tfMatricule.getText().trim().toUpperCase(),
                    tfMarque.getText().trim(),
                    Vehicule.Categorie.valueOf((String) cbCategorie.getSelectedItem()));
            dao.create(v);
            clearForm(); loadTable();
            JOptionPane.showMessageDialog(this, "Véhicule ajouté !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVehicule() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un véhicule."); return; }
        try {
            validateForm();
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            Vehicule v = new Vehicule(id, tfMatricule.getText().trim().toUpperCase(),
                    tfMarque.getText().trim(),
                    Vehicule.Categorie.valueOf((String) cbCategorie.getSelectedItem()));
            dao.update(v);
            loadTable();
            JOptionPane.showMessageDialog(this, "Véhicule modifié.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteVehicule() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un véhicule."); return; }
        if (JOptionPane.showConfirmDialog(this, "Supprimer ce véhicule ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
            dao.delete(id);
            clearForm(); loadTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTable() {
        try {
            List<Vehicule> list = dao.findAll();
            tableModel.setRowCount(0);
            for (Vehicule v : list) {
                tableModel.addRow(new Object[]{v.getId(), v.getMatricule(), v.getMarque(), v.getCategorie()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int m = table.convertRowIndexToModel(row);
        tfMatricule.setText((String) tableModel.getValueAt(m, 1));
        tfMarque.setText((String) tableModel.getValueAt(m, 2));
        cbCategorie.setSelectedItem(tableModel.getValueAt(m, 3).toString());
    }

    private void clearForm() {
        tfMatricule.setText(""); tfMarque.setText(""); cbCategorie.setSelectedIndex(0);
        table.clearSelection();
    }

    private void validateForm() throws Exception {
        if (tfMatricule.getText().trim().isEmpty()) throw new Exception("La matricule est obligatoire.");
        if (tfMarque.getText().trim().isEmpty())    throw new Exception("La marque est obligatoire.");
    }
}
