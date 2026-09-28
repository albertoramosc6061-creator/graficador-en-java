package mathgraph.ui;

import mathgraph.core.parser.CompiledExpression;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ControlPanel extends JPanel implements ThemeManager.ThemeChangeListener {
    private final MainWindow mainWindow;

    private JTextField formulaField;
    private JLabel errorLabel;
    private JLabel fxLabel;
    private JLabel tLabel;
    private JLabel themeLabel;
    private JComboBox<String> presetCombo;
    private JCheckBox riemannCheckBox;
    private JSlider riemannSlider;
    private JComboBox<String> riemannTypeCombo;
    private JSlider tSlider;
    private JButton playAnimBtn;
    private JButton soundBtn;
    private Timer animTimer;
    private boolean isPlaying = false;
    private JComboBox<Theme> themeCombo;

    public ControlPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(10, 8));
        setBorder(new EmptyBorder(8, 14, 8, 14));
        ThemeManager.getInstance().addListener(this);

        initUI();
        applyTheme(ThemeManager.getInstance().getCurrentTheme());
    }

    private void initUI() {
        JPanel mainGrid = new JPanel(new GridLayout(2, 1, 6, 6));
        mainGrid.setOpaque(false);

        // Row 1: Function Input + Error Label + Presets + Plot Button + Sound
        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setOpaque(false);

        JPanel inputGroup = new JPanel(new BorderLayout(6, 2));
        inputGroup.setOpaque(false);

        JPanel topInputLine = new JPanel(new BorderLayout(6, 0));
        topInputLine.setOpaque(false);

        fxLabel = new JLabel("f(x, t) = ");
        fxLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        formulaField = new JTextField("sin(x) * exp(-0.15*x)");
        formulaField.setFont(new Font("Consolas", Font.BOLD, 14));
        formulaField.addActionListener(e -> triggerPlot());

        topInputLine.add(fxLabel, BorderLayout.WEST);
        topInputLine.add(formulaField, BorderLayout.CENTER);

        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        errorLabel.setForeground(new Color(239, 68, 68)); // Bright Red
        errorLabel.setVisible(false);

        inputGroup.add(topInputLine, BorderLayout.CENTER);
        inputGroup.add(errorLabel, BorderLayout.SOUTH);

        // Preset Combo with "Ejemplos..."
        String[] presets = {
                "Ejemplos...",
                "Seno: sen(x)",
                "Coseno: cos(x)",
                "Tangente: tan(x)",
                "Raices cuadraticas: x^2 - 4",
                "Onda amortiguada: exp(-0.15*x) * cos(2*x)",
                "Funcion Sinc: sin(x) / x",
                "Polinomio cubico: x^3 - 3*x + 1",
                "Campana de Gauss: exp(-x^2 / 4)",
                "Onda de Fourier: sin(x) + sin(3*x)/3 + sin(5*x)/5",
                "Asintota 1/x: 1 / x",
                "Bruja de Agnesi: 8 / (x^2 + 4)",
                "Soliton Hiperbolico: 1 / cosh(x)^2",
                "Onda animada en t: sin(x - t) + cos(2*x + t)",
                "Funcion a trozos: x > 0 ? sin(x) : cos(x)"
        };
        presetCombo = new JComboBox<>(presets);
        presetCombo.setPreferredSize(new Dimension(200, 28));
        presetCombo.addActionListener(e -> {
            int idx = presetCombo.getSelectedIndex();
            if (idx > 0) {
                String selected = (String) presetCombo.getSelectedItem();
                int colonIdx = selected.indexOf(':');
                if (colonIdx >= 0) {
                    formulaField.setText(selected.substring(colonIdx + 1).trim());
                } else {
                    formulaField.setText(selected.trim());
                }
                triggerPlot();
            }
        });

        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightGroup.setOpaque(false);

        JButton plotBtn = new JButton("Graficar");
        plotBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        plotBtn.setToolTipText("Evaluar expresion matematica y graficar en el lienzo");
        plotBtn.addActionListener(e -> {
            triggerPlot();
            mainWindow.showGraphTab();
        });

        JButton flowchartBtn = new JButton("Ver Flujograma");
        flowchartBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        flowchartBtn.setToolTipText("Generar y visualizar el diagrama de flujo de calculo");
        flowchartBtn.addActionListener(e -> {
            triggerPlot();
            mainWindow.showFlowchartTab();
        });

        soundBtn = new JButton("Sonificar");
        soundBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        soundBtn.setToolTipText("Escuchar la curva matematica como onda acustica");
        soundBtn.addActionListener(e -> mainWindow.toggleAudioPlayback());

        JButton exportPngBtn = new JButton("Exportar PNG");
        exportPngBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exportPngBtn.addActionListener(e -> mainWindow.exportGraphPNG());

        rightGroup.add(presetCombo);
        rightGroup.add(plotBtn);
        rightGroup.add(flowchartBtn);
        rightGroup.add(soundBtn);
        rightGroup.add(exportPngBtn);

        row1.add(inputGroup, BorderLayout.CENTER);
        row1.add(rightGroup, BorderLayout.EAST);
        mainGrid.add(row1);

        // Row 2: Riemann Boxes Controls + Dynamic Parameter t Animation + Theme Selector
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);

        // Riemann toggle & slider
        riemannCheckBox = new JCheckBox("Ver Cuadros (Riemann):", true);
        riemannCheckBox.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        riemannCheckBox.setOpaque(false);
        riemannCheckBox.addActionListener(e -> {
            mainWindow.getCanvas().setShowRiemannBoxes(riemannCheckBox.isSelected());
        });

        riemannSlider = new JSlider(4, 80, 24);
        riemannSlider.setPreferredSize(new Dimension(90, 24));
        riemannSlider.setOpaque(false);
        riemannSlider.setToolTipText("Numero de rectangulos / cajas de integracion");
        riemannSlider.addChangeListener(e -> {
            mainWindow.getCanvas().setRiemannBoxesCount(riemannSlider.getValue());
        });

        String[] riemannTypes = {"Punto Medio", "Izquierda", "Derecha", "Trapecio"};
        riemannTypeCombo = new JComboBox<>(riemannTypes);
        riemannTypeCombo.setPreferredSize(new Dimension(110, 24));
        riemannTypeCombo.addActionListener(e -> {
            int idx = riemannTypeCombo.getSelectedIndex();
            CompiledExpression.RiemannType t = switch (idx) {
                case 1 -> CompiledExpression.RiemannType.LEFT;
                case 2 -> CompiledExpression.RiemannType.RIGHT;
                case 3 -> CompiledExpression.RiemannType.TRAPEZOID;
                default -> CompiledExpression.RiemannType.MIDPOINT;
            };
            mainWindow.getCanvas().setRiemannType(t);
        });

        // Animation Slider parameter t
        tLabel = new JLabel("Parametro t:");
        tLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        tSlider = new JSlider(0, 628, 0); // 0 to 2*PI in radians * 100
        tSlider.setPreferredSize(new Dimension(100, 24));
        tSlider.setOpaque(false);
        tSlider.addChangeListener(e -> {
            double tVal = tSlider.getValue() / 100.0;
            mainWindow.setParameterT(tVal);
        });

        playAnimBtn = new JButton("Animar");
        playAnimBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        initAnimationTimer();
        playAnimBtn.addActionListener(e -> toggleAnimation());

        // Theme Selector
        themeLabel = new JLabel("Tema:");
        themeLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));

        themeCombo = new JComboBox<>(ThemeManager.getAllThemes());
        themeCombo.setPreferredSize(new Dimension(170, 24));
        themeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Theme) {
                    lbl.setText(((Theme) value).getName());
                }
                return lbl;
            }
        });
        themeCombo.addActionListener(e -> {
            Theme sel = (Theme) themeCombo.getSelectedItem();
            if (sel != null) {
                ThemeManager.getInstance().setTheme(sel);
            }
        });

        row2.add(riemannCheckBox);
        row2.add(riemannSlider);
        row2.add(riemannTypeCombo);
        row2.add(new JSeparator(SwingConstants.VERTICAL));
        row2.add(tLabel);
        row2.add(tSlider);
        row2.add(playAnimBtn);
        row2.add(new JSeparator(SwingConstants.VERTICAL));
        row2.add(themeLabel);
        row2.add(themeCombo);

        mainGrid.add(row2);
        add(mainGrid, BorderLayout.CENTER);
    }

    private void initAnimationTimer() {
        animTimer = new Timer(33, e -> {
            int cur = tSlider.getValue();
            cur = (cur + 3) % 628;
            tSlider.setValue(cur);
        });
    }

    private void toggleAnimation() {
        isPlaying = !isPlaying;
        if (isPlaying) {
            animTimer.start();
            playAnimBtn.setText("Pausar");
        } else {
            animTimer.stop();
            playAnimBtn.setText("Animar");
        }
    }

    public void triggerPlot() {
        String formula = formulaField.getText().trim();
        if (!formula.isEmpty()) {
            mainWindow.updateFormula(formula);
        }
    }

    public void setSyntaxError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
        } else {
            errorLabel.setText("Error de sintaxis: " + errorMessage);
            errorLabel.setVisible(true);
        }
        revalidate();
        repaint();
    }

    public void setAudioButtonState(boolean isAudioPlaying) {
        if (soundBtn != null) {
            soundBtn.setText(isAudioPlaying ? "Detener Sonido" : "Sonificar");
        }
    }

    public String getFormulaText() {
        return formulaField.getText();
    }

    public void setFormulaText(String text) {
        formulaField.setText(text);
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    private void applyTheme(Theme theme) {
        setBackground(theme.bgSidebar);
        fxLabel.setForeground(theme.accentPrimary);
        if (tLabel != null) tLabel.setForeground(theme.textPrimary);
        if (themeLabel != null) themeLabel.setForeground(theme.textPrimary);
        if (riemannCheckBox != null) riemannCheckBox.setForeground(theme.textPrimary);

        formulaField.setBackground(theme.bgInput);
        formulaField.setForeground(theme.textPrimary);
        formulaField.setCaretColor(theme.textPrimary);
        formulaField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        if (presetCombo != null) {
            presetCombo.setBackground(theme.bgInput);
            presetCombo.setForeground(theme.textPrimary);
        }
        if (themeCombo != null) {
            themeCombo.setBackground(theme.bgInput);
            themeCombo.setForeground(theme.textPrimary);
        }
        if (riemannTypeCombo != null) {
            riemannTypeCombo.setBackground(theme.bgInput);
            riemannTypeCombo.setForeground(theme.textPrimary);
        }
        repaint();
    }
}
