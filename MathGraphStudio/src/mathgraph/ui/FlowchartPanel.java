package mathgraph.ui;

import mathgraph.core.parser.CalculationPipeline;
import mathgraph.core.parser.FlowchartNode;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FlowchartPanel extends JPanel implements ThemeManager.ThemeChangeListener {
    private CalculationPipeline pipeline;
    private FlowchartNode selectedNode = null;

    // Viewport pan & zoom
    private double zoom = 1.0;
    private double panX = 30.0;
    private double panY = 30.0;
    private Point lastMousePoint = null;

    private JTextArea inspectorArea;
    private JLabel nodeTitleLabel;

    public FlowchartPanel() {
        setLayout(new BorderLayout());
        ThemeManager.getInstance().addListener(this);

        initUI();
    }

    private void initUI() {
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                renderFlowchart(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        canvasPanel.setOpaque(true);
        canvasPanel.setBackground(ThemeManager.getInstance().getCurrentTheme().bgCanvas);

        // Mouse interactions for canvas
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePoint = e.getPoint();
                // Check if clicked a node
                FlowchartNode clicked = findNodeAt(e.getX(), e.getY());
                if (clicked != null) {
                    selectedNode = clicked;
                    updateInspector(clicked);
                    canvasPanel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePoint != null) {
                    panX += (e.getX() - lastMousePoint.x);
                    panY += (e.getY() - lastMousePoint.y);
                    lastMousePoint = e.getPoint();
                    canvasPanel.repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
                zoom *= factor;
                zoom = Math.max(0.4, Math.min(2.5, zoom));
                canvasPanel.repaint();
            }
        };

        canvasPanel.addMouseListener(mouseHandler);
        canvasPanel.addMouseMotionListener(mouseHandler);
        canvasPanel.addMouseWheelListener(mouseHandler);

        add(canvasPanel, BorderLayout.CENTER);

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(true);
        topBar.setBackground(ThemeManager.getInstance().getCurrentTheme().bgSidebar);
        topBar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("Diagrama de Flujo: Pipeline de Calculo y Renderizado");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(ThemeManager.getInstance().getCurrentTheme().textPrimary);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setOpaque(false);

        JButton simulateBtn = new JButton("Simular Ejecucion en Vivo");
        simulateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        simulateBtn.setToolTipText("Animar el flujo de calculo paso a paso iluminando los bloques");
        simulateBtn.addActionListener(e -> startStepByStepAnimation());

        JButton resetZoomBtn = new JButton("Restablecer Vista");
        resetZoomBtn.addActionListener(e -> {
            zoom = 1.0;
            panX = 30.0;
            panY = 30.0;
            canvasPanel.repaint();
        });

        JButton exportBtn = new JButton("Exportar PNG");
        exportBtn.addActionListener(e -> exportFlowchartToPNG(canvasPanel));

        actionBtns.add(simulateBtn);
        actionBtns.add(resetZoomBtn);
        actionBtns.add(exportBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionBtns, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // East: Inspector Panel for clicked node
        JPanel eastInspector = new JPanel(new BorderLayout(0, 8));
        eastInspector.setPreferredSize(new Dimension(280, 0));
        eastInspector.setBorder(new EmptyBorder(10, 10, 10, 10));
        eastInspector.setBackground(ThemeManager.getInstance().getCurrentTheme().bgSidebar);

        nodeTitleLabel = new JLabel("Inspector de Nodo");
        nodeTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nodeTitleLabel.setForeground(ThemeManager.getInstance().getCurrentTheme().accentPrimary);

        inspectorArea = new JTextArea("Haz clic en cualquier bloque del diagrama de flujo para ver sus variables, tokens, AST y detalles de ejecucion en vivo.");
        inspectorArea.setEditable(false);
        inspectorArea.setLineWrap(true);
        inspectorArea.setWrapStyleWord(true);
        inspectorArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inspectorArea.setBackground(ThemeManager.getInstance().getCurrentTheme().bgCard);
        inspectorArea.setForeground(ThemeManager.getInstance().getCurrentTheme().textPrimary);
        inspectorArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        eastInspector.add(nodeTitleLabel, BorderLayout.NORTH);
        eastInspector.add(new JScrollPane(inspectorArea), BorderLayout.CENTER);
        add(eastInspector, BorderLayout.EAST);
    }

    public void setPipeline(CalculationPipeline pipeline) {
        this.pipeline = pipeline;
        if (pipeline != null && !pipeline.getNodes().isEmpty()) {
            selectedNode = pipeline.getNodes().get(0);
            updateInspector(selectedNode);
        }
        repaint();
    }

    public void startStepByStepAnimation() {
        if (pipeline == null || pipeline.getNodes().isEmpty()) return;
        List<FlowchartNode> nodes = pipeline.getNodes();

        // Clear all highlights
        for (FlowchartNode n : nodes) {
            n.setHighlighted(false);
        }

        final int[] step = {0};
        Timer simTimer = new Timer(600, null);
        simTimer.addActionListener(e -> {
            if (step[0] < nodes.size()) {
                // Highlight current node
                for (int i = 0; i < nodes.size(); i++) {
                    nodes.get(i).setHighlighted(i == step[0]);
                }
                selectedNode = nodes.get(step[0]);
                updateInspector(selectedNode);
                step[0]++;
                repaint();
            } else {
                // Done
                simTimer.stop();
            }
        });
        simTimer.setRepeats(true);
        simTimer.start();
    }

    private void updateInspector(FlowchartNode node) {
        if (node == null) return;
        nodeTitleLabel.setText("📌 " + node.getTitle());
        StringBuilder sb = new StringBuilder();
        sb.append("Categoría: ").append(node.getCategory()).append("\n");
        sb.append("Descripción: ").append(node.getDescription()).append("\n\n");
        sb.append("--- DETALLES DEL PROCESO ---\n");
        for (String d : node.getDetails()) {
            sb.append("• ").append(d).append("\n");
        }
        sb.append("\n--- CONEXIONES DE SALIDA ---\n");
        if (node.getOutputs().isEmpty()) {
            sb.append("(Nodo Terminal / Fin del flujo)\n");
        } else {
            for (String out : node.getOutputs()) {
                FlowchartNode target = (pipeline != null) ? pipeline.getNode(out) : null;
                sb.append("➜ ").append(target != null ? target.getTitle() : out).append("\n");
            }
        }
        inspectorArea.setText(sb.toString());
    }

    private FlowchartNode findNodeAt(int screenX, int screenY) {
        if (pipeline == null) return null;
        double worldX = (screenX - panX) / zoom;
        double worldY = (screenY - panY) / zoom;

        for (FlowchartNode node : pipeline.getNodes()) {
            if (worldX >= node.getX() && worldX <= node.getX() + node.getWidth() &&
                worldY >= node.getY() && worldY <= node.getY() + node.getHeight()) {
                return node;
            }
        }
        return null;
    }

    private void renderFlowchart(Graphics2D g2, int width, int height) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(theme.bgCanvas);
        g2.fillRect(0, 0, width, height);

        // Draw dot grid background
        g2.setColor(theme.gridMinor);
        for (int x = 0; x < width; x += 25) {
            for (int y = 0; y < height; y += 25) {
                g2.fillRect(x, y, 2, 2);
            }
        }

        if (pipeline == null) return;

        AffineTransform oldTx = g2.getTransform();
        g2.translate(panX, panY);
        g2.scale(zoom, zoom);

        // 1. Draw Connecting Arrows
        for (FlowchartNode node : pipeline.getNodes()) {
            for (String targetId : node.getOutputs()) {
                FlowchartNode target = pipeline.getNode(targetId);
                if (target != null) {
                    drawConnection(g2, node, target, theme);
                }
            }
        }

        // 2. Draw Nodes
        for (FlowchartNode node : pipeline.getNodes()) {
            drawNode(g2, node, node == selectedNode, theme);
        }

        g2.setTransform(oldTx);
    }

    private void drawConnection(Graphics2D g2, FlowchartNode from, FlowchartNode to, Theme theme) {
        double startX = from.getX() + from.getWidth() / 2.0;
        double startY = from.getY() + from.getHeight();
        double endX = to.getX() + to.getWidth() / 2.0;
        double endY = to.getY();

        if (from.getY() == to.getY() || Math.abs(from.getY() - to.getY()) < 30) {
            startX = from.getX() + from.getWidth();
            startY = from.getY() + from.getHeight() / 2.0;
            endX = to.getX();
            endY = to.getY() + to.getHeight() / 2.0;
        }

        g2.setColor(theme.flowchartLine);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Smooth Bezier Curve
        Path2D path = new Path2D.Double();
        path.moveTo(startX, startY);
        double ctrlY1 = startY + (endY - startY) * 0.5;
        double ctrlY2 = startY + (endY - startY) * 0.5;
        path.curveTo(startX, ctrlY1, endX, ctrlY2, endX, endY);
        g2.draw(path);

        // Arrow head at end
        drawArrowHead(g2, endX, endY);
    }

    private void drawArrowHead(Graphics2D g2, double x, double y) {
        int[] xPoints = {(int) x - 5, (int) x + 5, (int) x};
        int[] yPoints = {(int) y - 8, (int) y - 8, (int) y};
        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawNode(Graphics2D g2, FlowchartNode node, boolean isSelected, Theme theme) {
        int nx = node.getX();
        int ny = node.getY();
        int nw = node.getWidth();
        int nh = node.getHeight();

        boolean active = isSelected || node.isHighlighted();

        // Glow halo for active/highlighted simulation
        if (node.isHighlighted()) {
            g2.setColor(new Color(theme.accentSecondary.getRed(), theme.accentSecondary.getGreen(), theme.accentSecondary.getBlue(), 90));
            g2.fillRoundRect(nx - 5, ny - 5, nw + 10, nh + 10, 16, 16);
        }

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(nx + 3, ny + 3, nw, nh, 12, 12);

        // Card background
        g2.setColor(theme.flowchartNodeBg);
        g2.fillRoundRect(nx, ny, nw, nh, 12, 12);

        // Border
        Color borderColor = node.isHighlighted() ? theme.accentSecondary : (isSelected ? theme.accentPrimary : theme.border);
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(active ? 2.8f : 1.2f));
        g2.drawRoundRect(nx, ny, nw, nh, 12, 12);

        // Node Header Pill
        Color headerPillBg = node.isHighlighted() ?
                new Color(theme.accentSecondary.getRed(), theme.accentSecondary.getGreen(), theme.accentSecondary.getBlue(), 60) :
                new Color(theme.accentPrimary.getRed(), theme.accentPrimary.getGreen(), theme.accentPrimary.getBlue(), 30);
        g2.setColor(headerPillBg);
        g2.fillRoundRect(nx + 6, ny + 6, nw - 12, 24, 8, 8);

        // Node Title
        g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        g2.setColor(node.isHighlighted() ? theme.accentSecondary : theme.textAccent);
        g2.drawString(node.getTitle(), nx + 12, ny + 22);

        // Details lines
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(theme.textPrimary);
        int lineY = ny + 46;
        for (int i = 0; i < Math.min(3, node.getDetails().size()); i++) {
            String d = node.getDetails().get(i);
            if (d.length() > 32) d = d.substring(0, 30) + "...";
            g2.drawString("• " + d, nx + 12, lineY);
            lineY += 16;
        }
    }

    private void exportFlowchartToPNG(JPanel canvas) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("diagrama_flujo_calculo.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                BufferedImage img = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                renderFlowchart(g2, 1200, 800);
                g2.dispose();
                ImageIO.write(img, "PNG", f);
                JOptionPane.showMessageDialog(this, "Diagrama exportado con éxito a:\n" + f.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        setBackground(newTheme.bgCanvas);
        inspectorArea.setBackground(newTheme.bgCard);
        inspectorArea.setForeground(newTheme.textPrimary);
        nodeTitleLabel.setForeground(newTheme.accentPrimary);
        repaint();
    }
}
