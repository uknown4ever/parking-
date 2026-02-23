package parking.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application Gestion de Parking.
 * Architecture MVC léger : chaque onglet est un panel autonome.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("🅿 Gestion de Parking");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // Icône non incluse dans le JAR - ignorée

        initComponents();
    }

    private void initComponents() {
        // ---- Barre de menu ----
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFichier = new JMenu("Fichier");
        JMenuItem itemQuitter = new JMenuItem("Quitter");
        itemQuitter.addActionListener(e -> System.exit(0));
        menuFichier.add(itemQuitter);
        menuBar.add(menuFichier);

        JMenu menuAide = new JMenu("Aide");
        JMenuItem itemAbout = new JMenuItem("À propos");
        itemAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Gestion de Parking v1.0\nProjet Java – Mini-projet 20\n\nJava Swing + MySQL",
                "À propos", JOptionPane.INFORMATION_MESSAGE));
        menuAide.add(itemAbout);
        menuBar.add(menuAide);
        setJMenuBar(menuBar);

        // ---- En-tête ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 60, 114));
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = new JLabel("  🅿  Gestion de Parking", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        // ---- Onglets ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("🏠  Tableau de bord", new DashboardPanel());
        tabs.addTab("🅿  Places",           new PlacePanel());
        tabs.addTab("🚗  Véhicules",        new VehiculePanel());
        tabs.addTab("📋  Stationnements",   new StationnementPanel());
        tabs.addTab("📊  Graphique",        new GraphiquePanel());

        // ---- Layout principal ----
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs,   BorderLayout.CENTER);

        // Barre de statut
        JLabel statusBar = new JLabel("  Connecté à la base de données  parking_db");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setBorder(BorderFactory.createLoweredSoftBevelBorder());
        add(statusBar, BorderLayout.SOUTH);
    }
}
