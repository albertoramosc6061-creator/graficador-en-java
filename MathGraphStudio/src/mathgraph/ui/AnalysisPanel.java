package mathgraph.ui;

import mathgraph.core.parser.CompiledExpression;
import mathgraph.model.CalculatedDataset;
import mathgraph.model.FunctionDataPoint;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class AnalysisPanel extends JPanel implements ThemeManager.ThemeChangeListener {
    private JTextArea summaryArea;
    private JList<String> rootsList;
    private DefaultListModel<String> rootsModel;
    private JList<String> extremaList;
    private DefaultListModel<String> extremaModel;

    public AnalysisPanel() {
        setLayout(new GridLayout(1, 3, 12, 12));
        setBorder(new EmptyBorder(14, 14, 14, 14));
        ThemeManager.getInstance().addListener(this);

        initUI();
    }

    private void initUI() {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();

        // Card 1: Resumen de Calculo Integral & Diferencial
        JPanel card1 = createCard("Metricas de Calculo & Analisis");
        summaryArea = new JTextArea("Esperando calculo de funcion...");
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        summaryArea.setBackground(theme.bgCard);
        summaryArea.setForeground(theme.textPrimary);
        summaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card1.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        add(card1);

        // Card 2: Raices encontradas f(x) = 0
        JPanel card2 = createCard("Raices / Ceros f(x) = 0 (Newton-Raphson)");
        rootsModel = new DefaultListModel<>();
        rootsList = new JList<>(rootsModel);
        rootsList.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        rootsList.setBackground(theme.bgCard);
        rootsList.setForeground(theme.rootBadge);
        card2.add(new JScrollPane(rootsList), BorderLayout.CENTER);
        add(card2);

        // Card 3: Puntos Extremos (Maximos y Minimos)
        JPanel card3 = createCard("Puntos Criticos (Maximos & Minimos)");
        extremaModel = new DefaultListModel<>();
        extremaList = new JList<>(extremaModel);
        extremaList.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        extremaList.setBackground(theme.bgCard);
        extremaList.setForeground(theme.textAccent);
        card3.add(new JScrollPane(extremaList), BorderLayout.CENTER);
        add(card3);
    }

    private JPanel createCard(String title) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(true);
        panel.setBackground(theme.bgSidebar);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(theme.accentPrimary);
        panel.add(lbl, BorderLayout.NORTH);
        return panel;
    }

    public void updateAnalysis(String expression, CompiledExpression compiled, CalculatedDataset dataset, double a, double b, double t) {
        if (compiled == null || dataset == null) return;

        // Integral Simpson
        double simpsonInt = compiled.integrateSimpson(a, b, t, 200);
        // Riemann
        double riemannMid = compiled.integrateRiemann(a, b, t, 100, CompiledExpression.RiemannType.MIDPOINT);
        // Arc length
        double arcLen = compiled.calculateArcLength(a, b, t, 200);

        StringBuilder sb = new StringBuilder();
        sb.append("Función Analizada:\n  f(x) = ").append(expression).append("\n\n");
        sb.append(String.format(Locale.US, "Intervalo de Integración [a, b]:\n  a = %.3f,  b = %.3f\n\n", a, b));
        sb.append(String.format(Locale.US, "Integral Definida ∫a^b f(x)dx:\n  • Regla de Simpson (1/3): %.6f\n  • Suma de Riemann (P. Medio): %.6f\n\n", simpsonInt, riemannMid));
        sb.append(String.format(Locale.US, "Longitud de Arco L:\n  L = ∫ √(1 + [f'(x)]²) dx ≈ %.6f\n\n", arcLen));
        sb.append(String.format(Locale.US, "Valor Promedio ȳ:\n  ȳ = (1 / (b - a)) ∫ f(x)dx = %.6f\n\n", (simpsonInt / (b - a))));
        sb.append(String.format(Locale.US, "Rango Evaluado:\n  Y_mín = %.4f | Y_máx = %.4f\n", dataset.getMinY(), dataset.getMaxY()));

        summaryArea.setText(sb.toString());

        // Update Roots
        rootsModel.clear();
        for (FunctionDataPoint p : dataset.getAllPoints()) {
            if (p.getPointType() == mathgraph.model.PointType.ROOT) {
                rootsModel.addElement(String.format(Locale.US, " Raíz x ≈ %+.6f  (y = %+.2e)", p.getX(), p.getY()));
            }
        }
        if (rootsModel.isEmpty()) {
            rootsModel.addElement(" No se detectaron cruces por cero en el rango actual.");
        }

        // Update Extrema
        extremaModel.clear();
        for (FunctionDataPoint p : dataset.getAllPoints()) {
            if (p.getPointType() == mathgraph.model.PointType.LOCAL_MAX) {
                extremaModel.addElement(String.format(Locale.US, " ▲ MÁXIMO en (x = %+.4f, y = %+.4f)", p.getX(), p.getY()));
            } else if (p.getPointType() == mathgraph.model.PointType.LOCAL_MIN) {
                extremaModel.addElement(String.format(Locale.US, " ▼ MÍNIMO en (x = %+.4f, y = %+.4f)", p.getX(), p.getY()));
            }
        }
        if (extremaModel.isEmpty()) {
            extremaModel.addElement(" Función estrictamente monótona o sin extremos locales detectados.");
        }
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        setBackground(newTheme.bgCanvas);
        summaryArea.setBackground(newTheme.bgCard);
        summaryArea.setForeground(newTheme.textPrimary);
        rootsList.setBackground(newTheme.bgCard);
        rootsList.setForeground(newTheme.rootBadge);
        extremaList.setBackground(newTheme.bgCard);
        extremaList.setForeground(newTheme.textAccent);
        repaint();
    }
}
