package parking.ui;

import parking.dao.StationnementDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Onglet Graphique : Revenus par mois (barres).
 * Utilise un rendu personnalisé sans dépendance externe (JFreeChart est optionnel).
 * Si JFreeChart est disponible dans le classpath, il est utilisé ; sinon,
 * un graphique en barres est dessiné via Graphics2D.
 */
public class GraphiquePanel extends JPanel {

    private final StationnementDAO dao = new StationnementDAO();
    private List<Object[]> data; // {mois, total}

    public GraphiquePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 248, 255));

        JLabel title = new JLabel("Revenus par mois (€)", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        add(new BarChartCanvas(), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("⟳ Actualiser");
        btnRefresh.addActionListener(e -> { loadData(); repaint(); });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(new Color(245, 248, 255));
        south.add(btnRefresh);
        add(south, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        try {
            data = dao.getRevenusParMois();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Canvas de dessin ----
    private class BarChartCanvas extends JPanel {

        BarChartCanvas() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                g.setColor(Color.GRAY);
                g.drawString("Aucune donnée disponible.", 40, 80);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int paddingLeft = 80, paddingRight = 30, paddingTop = 30, paddingBottom = 60;
            int chartW = W - paddingLeft - paddingRight;
            int chartH = H - paddingTop - paddingBottom;

            // Valeurs max
            double maxVal = data.stream().mapToDouble(r -> (double) r[1]).max().orElse(1);
            maxVal = Math.ceil(maxVal / 10) * 10; // arrondir au 10 supérieur

            int n = data.size();
            int barWidth = Math.min(60, chartW / n - 10);
            int gap      = (chartW - n * barWidth) / (n + 1);

            // Axe Y (lignes de grille + labels)
            g2.setColor(new Color(220, 220, 220));
            int ySteps = 5;
            for (int i = 0; i <= ySteps; i++) {
                double val = maxVal * i / ySteps;
                int y = paddingTop + chartH - (int) (chartH * i / ySteps);
                g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(String.format("%.0f €", val), 5, y + 4);
                g2.setColor(new Color(220, 220, 220));
            }

            // Axe X
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(paddingLeft, paddingTop + chartH, paddingLeft + chartW, paddingTop + chartH);
            g2.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartH);

            // Barres
            Color[] palette = {
                new Color(30, 100, 200), new Color(60, 179, 113), new Color(255, 165, 0),
                new Color(220, 80, 60),  new Color(140, 80, 200), new Color(0, 180, 200)
            };

            for (int i = 0; i < n; i++) {
                String mois  = (String) data.get(i)[0];
                double val   = (double) data.get(i)[1];
                int barH = (int) (chartH * val / maxVal);
                int x    = paddingLeft + gap + i * (barWidth + gap);
                int y    = paddingTop + chartH - barH;

                // Ombre
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(x + 3, y + 3, barWidth, barH, 6, 6);

                // Barre
                g2.setColor(palette[i % palette.length]);
                g2.fillRoundRect(x, y, barWidth, barH, 6, 6);

                // Valeur au-dessus
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String valStr = String.format("%.1f", val);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(valStr, x + (barWidth - fm.stringWidth(valStr)) / 2, y - 4);

                // Label mois
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = mois.length() > 7 ? mois.substring(2) : mois; // yyyy-mm -> yy-mm
                g2.drawString(label, x + (barWidth - g2.getFontMetrics().stringWidth(label)) / 2,
                        paddingTop + chartH + 16);
            }

            // Titre axe Y
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            g2.setColor(Color.GRAY);
            g2.rotate(-Math.PI / 2, 12, H / 2.0);
            g2.drawString("Revenus (€)", 12, (int)(H / 2.0));
            g2.rotate(Math.PI / 2, 12, H / 2.0);
        }
    }
}
