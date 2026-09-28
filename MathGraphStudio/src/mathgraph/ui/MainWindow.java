package mathgraph.ui;

import mathgraph.core.parser.CalculationPipeline;
import mathgraph.core.parser.CompiledExpression;
import mathgraph.model.CalculatedDataset;
import mathgraph.model.FunctionDataPoint;
import mathgraph.model.FunctionSeries;
import mathgraph.model.PointType;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainWindow extends JFrame implements ThemeManager.ThemeChangeListener {
    private final ControlPanel controlPanel;
    private final GraphCanvas graphCanvas;
    private final DatabaseViewPanel databaseViewPanel;
    private final FlowchartPanel flowchartPanel;
    private final AnalysisPanel analysisPanel;

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private JLabel mathInfoLabel;

    private CompiledExpression currentCompiled;
    private CalculatedDataset currentDataset;
    private String currentFormula = "sin(x) * exp(-0.15*x)";
    private double currentT = 0.0;

    public MainWindow() {
        setTitle("MathGraph Studio Pro | Graficador, Base de Datos & Diagrama de Flujo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Initialize core panels
        graphCanvas = new GraphCanvas();
        databaseViewPanel = new DatabaseViewPanel();
        flowchartPanel = new FlowchartPanel();
        analysisPanel = new AnalysisPanel();
        controlPanel = new ControlPanel(this);

        initUI();
        ThemeManager.getInstance().addListener(this);
        applyTheme(ThemeManager.getInstance().getCurrentTheme());

        // Initial plot
        updateFormula(currentFormula);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        // Top Header + Control Panel
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setOpaque(false);

        // Top Brand Banner
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(true);
        brandPanel.setBackground(ThemeManager.getInstance().getCurrentTheme().bgSidebar);
        brandPanel.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel brandTitle = new JLabel("⚡ MathGraph Studio Pro  —  Graficador Matemático, BD & Flujograma");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brandTitle.setForeground(ThemeManager.getInstance().getCurrentTheme().accentPrimary);

        mathInfoLabel = new JLabel("Listo para evaluar expresiones");
        mathInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mathInfoLabel.setForeground(ThemeManager.getInstance().getCurrentTheme().textSecondary);

        brandPanel.add(brandTitle, BorderLayout.WEST);
        brandPanel.add(mathInfoLabel, BorderLayout.EAST);

        northContainer.add(brandPanel, BorderLayout.NORTH);
        northContainer.add(controlPanel, BorderLayout.CENTER);
        root.add(northContainer, BorderLayout.NORTH);

        // Center Tabbed Pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tabbedPane.addTab("Lienzo Grafico & Cuadros", graphCanvas);
        tabbedPane.addTab("Base de Datos & SQL Studio", databaseViewPanel);
        tabbedPane.addTab("Diagrama de Flujo (Algoritmo)", flowchartPanel);
        tabbedPane.addTab("Centro de Analisis & Calculo", analysisPanel);

        root.add(tabbedPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(true);
        statusBar.setBackground(ThemeManager.getInstance().getCurrentTheme().bgSidebar);
        statusBar.setBorder(new EmptyBorder(6, 14, 6, 14));

        statusLabel = new JLabel("Sistema inicializado correctamente.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(ThemeManager.getInstance().getCurrentTheme().textSecondary);

        JLabel authorLabel = new JLabel("Rastreador de Cursor Activo | PNG Retina 4K | Java Swing");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        authorLabel.setForeground(ThemeManager.getInstance().getCurrentTheme().textSecondary);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(authorLabel, BorderLayout.EAST);
        root.add(statusBar, BorderLayout.SOUTH);
    }

    public void updateFormula(String formula) {
        this.currentFormula = formula;
        long startTime = System.nanoTime();

        try {
            currentCompiled = CompiledExpression.compile(formula);
            controlPanel.setSyntaxError(null);

            // Compute dataset points
            double xMin = graphCanvas.getXMin();
            double xMax = graphCanvas.getXMax();
            int sampleCount = 400;
            double dx = (xMax - xMin) / sampleCount;

            currentDataset = new CalculatedDataset(formula);

            // Numerical sampling & critical points
            double prevY = currentCompiled.eval(xMin, currentT);
            double prevDy = currentCompiled.derivative(xMin, currentT);

            for (int i = 0; i <= sampleCount; i++) {
                double x = xMin + i * dx;
                double y = currentCompiled.eval(x, currentT);
                double dy = currentCompiled.derivative(x, currentT);
                double d2y = currentCompiled.secondDerivative(x, currentT);
                double cumInt = currentCompiled.integrateSimpson(xMin, x, currentT, Math.max(4, i * 2));

                PointType type = PointType.REGULAR;
                String meta = "";

                if (i > 0) {
                    if (Math.abs(y) < 1e-4 || (prevY * y < 0 && Math.abs(y - prevY) < 10.0)) {
                        type = PointType.ROOT;
                        meta = "Cruce por cero f(x)=0";
                    } else if (prevDy > 0 && dy <= 0) {
                        type = PointType.LOCAL_MAX;
                        meta = "Extremo Superior (f'=0, f''<0)";
                    } else if (prevDy < 0 && dy >= 0) {
                        type = PointType.LOCAL_MIN;
                        meta = "Extremo Inferior (f'=0, f''>0)";
                    }
                }

                currentDataset.addPoint(new FunctionDataPoint(i + 1, x, y, dy, d2y, cumInt, type, meta));

                prevY = y;
                prevDy = dy;
            }

            currentDataset.finalizeCalculations();

            // Setup Series
            FunctionSeries series = new FunctionSeries("f(x, t)", formula, ThemeManager.getInstance().getCurrentTheme().accentPrimary);
            series.setCompiled(currentCompiled);
            series.setDataset(currentDataset);

            List<FunctionSeries> list = new ArrayList<>();
            list.add(series);
            graphCanvas.setSeries(list);

            // Update Database View
            databaseViewPanel.setDataset(currentDataset);

            // Generate Flowchart Pipeline
            CalculationPipeline pipeline = CalculationPipeline.generate(
                    formula, xMin, xMax, sampleCount, currentT, currentCompiled,
                    currentDataset.getRootCount(),
                    currentDataset.getMaxCount() + currentDataset.getMinCount(),
                    currentDataset.getTotalIntegral()
            );
            flowchartPanel.setPipeline(pipeline);

            // Update Analysis Center
            analysisPanel.updateAnalysis(formula, currentCompiled, currentDataset, xMin, xMax, currentT);

            long elapsedNs = System.nanoTime() - startTime;
            double elapsedMs = elapsedNs / 1_000_000.0;
            statusLabel.setText(String.format(Locale.US, "f(x) = %s evaluada con exito | %d puntos | Tiempo: %.2f ms", formula, currentDataset.size(), elapsedMs));
            mathInfoLabel.setText(String.format(Locale.US, "Raices: %d | Extremos: %d | Area: %.4f", currentDataset.getRootCount(), currentDataset.getMaxCount() + currentDataset.getMinCount(), currentDataset.getTotalIntegral()));

        } catch (Exception ex) {
            controlPanel.setSyntaxError(ex.getMessage());
            statusLabel.setText("Error de sintaxis: " + ex.getMessage());
        }
    }

    public void setParameterT(double t) {
        this.currentT = t;
        graphCanvas.setParameterT(t);
        if (currentCompiled != null) {
            graphCanvas.repaint();
        }
    }

    public void toggleAudioPlayback() {
        if (AudioSynthesizer.isPlaying()) {
            AudioSynthesizer.stopSound();
            controlPanel.setAudioButtonState(false);
            statusLabel.setText("Reproduccion acustica detenida.");
        } else if (currentCompiled != null) {
            controlPanel.setAudioButtonState(true);
            statusLabel.setText("Reproduciendo sintesis acustica de la curva...");
            AudioSynthesizer.playFunctionSound(currentCompiled, graphCanvas.getXMin(), graphCanvas.getXMax(), 2.5, () -> {
                controlPanel.setAudioButtonState(false);
                statusLabel.setText("Sintesis acustica finalizada.");
            });
        }
    }

    public void exportGraphPNG() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("grafico_matematico.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            boolean ok = graphCanvas.exportToPNGFile(f, 1920, 1080);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Gráfico exportado en Alta Definición (PNG 1080p) a:\n" + f.getAbsolutePath(), "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo PNG.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void showFlowchartTab() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(2); // Tab 2 is Flowchart
            flowchartPanel.startStepByStepAnimation();
        }
    }

    public void showGraphTab() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(0); // Tab 0 is Graph
        }
    }

    public void showDatabaseTab() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(1); // Tab 1 is Database
        }
    }

    public GraphCanvas getCanvas() {
        return graphCanvas;
    }

    public FlowchartPanel getFlowchartPanel() {
        return flowchartPanel;
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    private void applyTheme(Theme theme) {
        getContentPane().setBackground(theme.bgCanvas);
        tabbedPane.setBackground(theme.bgSidebar);
        tabbedPane.setForeground(theme.textPrimary);
        mathInfoLabel.setForeground(theme.textSecondary);
        statusLabel.setForeground(theme.textSecondary);

        // Repaint canvas with updated theme colors
        if (graphCanvas != null) {
            graphCanvas.repaint();
        }
        repaint();
    }
}
