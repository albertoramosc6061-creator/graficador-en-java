package mathgraph.ui;

import mathgraph.core.parser.CompiledExpression;
import mathgraph.model.CalculatedDataset;
import mathgraph.model.FunctionDataPoint;
import mathgraph.model.FunctionSeries;
import mathgraph.model.PointType;
import mathgraph.theme.Theme;
import mathgraph.theme.ThemeManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GraphCanvas extends JPanel implements ThemeManager.ThemeChangeListener {
    private final List<FunctionSeries> seriesList = new ArrayList<>();

    // Viewport Bounds in Mathematical Coordinates
    private double xMin = -10.0;
    private double xMax = 10.0;
    private double yMin = -6.0;
    private double yMax = 6.0;

    // Default reset bounds
    private static final double DEFAULT_X_MIN = -10.0;
    private static final double DEFAULT_X_MAX = 10.0;
    private static final double DEFAULT_Y_MIN = -6.0;
    private static final double DEFAULT_Y_MAX = 6.0;

    // Parameter t for dynamic animations
    private double parameterT = 0.0;

    // Interaction state
    private Point mousePos = null;
    private Point lastDragPoint = null;
    private boolean isPanning = false;

    // Visual options
    private boolean showGrid = true;
    private boolean showAxes = true;
    private boolean showNumbers = true;
    private boolean showCrosshair = true;
    private boolean showLegend = true;
    private boolean showTangent = true;
    private boolean showRiemannBoxes = true;
    private int riemannBoxesCount = 24;
    private CompiledExpression.RiemannType riemannType = CompiledExpression.RiemannType.MIDPOINT;

    public GraphCanvas() {
        setOpaque(true);
        setFocusable(true);
        setBackground(ThemeManager.getInstance().getCurrentTheme().bgCanvas);
        ThemeManager.getInstance().addListener(this);

        initMouseListeners();
    }

    private void initMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                    lastDragPoint = e.getPoint();
                    isPanning = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPanning = false;
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resetView();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mousePos = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isPanning && lastDragPoint != null) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;

                    double mathDx = dx * (xMax - xMin) / getWidth();
                    double mathDy = dy * (yMax - yMin) / getHeight();

                    xMin -= mathDx;
                    xMax -= mathDx;
                    yMin += mathDy;
                    yMax += mathDy;

                    lastDragPoint = e.getPoint();
                    mousePos = e.getPoint();
                    repaint();
                }
            }
        });

        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            double zoomFactor = notches < 0 ? 0.85 : 1.18; // Zoom in / out

            // Zoom centered at mouse cursor
            Point p = e.getPoint();
            double mathX = screenToMathX(p.x);
            double mathY = screenToMathY(p.y);

            double newHalfW = (xMax - xMin) * zoomFactor / 2.0;
            double newHalfH = (yMax - yMin) * zoomFactor / 2.0;

            // Prevent extreme zooms
            if (newHalfW > 1e-5 && newHalfW < 1e5 && newHalfH > 1e-5 && newHalfH < 1e5) {
                xMin = mathX - (mathX - xMin) * zoomFactor;
                xMax = mathX + (xMax - mathX) * zoomFactor;
                yMin = mathY - (mathY - yMin) * zoomFactor;
                yMax = mathY + (yMax - mathY) * zoomFactor;
                repaint();
            }
        });
    }

    public void resetView() {
        xMin = DEFAULT_X_MIN;
        xMax = DEFAULT_X_MAX;
        yMin = DEFAULT_Y_MIN;
        yMax = DEFAULT_Y_MAX;
        repaint();
    }

    public void setViewBounds(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        repaint();
    }

    public void setSeries(List<FunctionSeries> list) {
        this.seriesList.clear();
        if (list != null) {
            this.seriesList.addAll(list);
        }
        repaint();
    }

    public void addSeries(FunctionSeries series) {
        this.seriesList.add(series);
        repaint();
    }

    public void clearSeries() {
        this.seriesList.clear();
        repaint();
    }

    public double getParameterT() { return parameterT; }
    public void setParameterT(double t) {
        this.parameterT = t;
        repaint();
    }

    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean showGrid) { this.showGrid = showGrid; repaint(); }

    public boolean isShowAxes() { return showAxes; }
    public void setShowAxes(boolean showAxes) { this.showAxes = showAxes; repaint(); }

    public boolean isShowNumbers() { return showNumbers; }
    public void setShowNumbers(boolean showNumbers) { this.showNumbers = showNumbers; repaint(); }

    public boolean isShowCrosshair() { return showCrosshair; }
    public void setShowCrosshair(boolean showCrosshair) { this.showCrosshair = showCrosshair; repaint(); }

    public boolean isShowRiemannBoxes() { return showRiemannBoxes; }
    public void setShowRiemannBoxes(boolean showRiemannBoxes) { this.showRiemannBoxes = showRiemannBoxes; repaint(); }

    public int getRiemannBoxesCount() { return riemannBoxesCount; }
    public void setRiemannBoxesCount(int count) { this.riemannBoxesCount = count; repaint(); }

    public CompiledExpression.RiemannType getRiemannType() { return riemannType; }
    public void setRiemannType(CompiledExpression.RiemannType type) { this.riemannType = type; repaint(); }

    public boolean isShowTangent() { return showTangent; }
    public void setShowTangent(boolean showTangent) { this.showTangent = showTangent; repaint(); }

    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }

    // Math to Screen Transformations
    public double mathToScreenX(double x) {
        return (x - xMin) / (xMax - xMin) * getWidth();
    }

    public double mathToScreenY(double y) {
        return (yMax - y) / (yMax - yMin) * getHeight();
    }

    public double screenToMathX(double sx) {
        return xMin + (sx / getWidth()) * (xMax - xMin);
    }

    public double screenToMathY(double sy) {
        return yMax - (sy / getHeight()) * (yMax - yMin);
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        setBackground(newTheme.bgCanvas);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        renderScene(g2, getWidth(), getHeight(), 1.0);
        g2.dispose();
    }

    public void renderScene(Graphics2D g2, int width, int height, double scale) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();

        // Enable high-grade antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fill background
        g2.setColor(theme.bgCanvas);
        g2.fillRect(0, 0, width, height);

        // 1. Grid & Axes
        if (showGrid) {
            drawGrid(g2, width, height, theme);
        }
        if (showAxes) {
            drawAxes(g2, width, height, theme);
        }

        // 2. Riemann Sum Rectangles / "Cajas" (if enabled)
        if (showRiemannBoxes && !seriesList.isEmpty()) {
            drawRiemannBoxes(g2, width, height, theme);
        }

        // 3. Curves
        for (FunctionSeries series : seriesList) {
            if (series.isVisible() && series.getCompiled() != null) {
                drawSeriesCurve(g2, width, height, series, theme);
                if (series.isShowCriticalPoints()) {
                    drawCriticalPoints(g2, width, height, series, theme);
                }
            }
        }

        // 4. Cursor Tracking & Tangent
        if (showCrosshair && mousePos != null && mousePos.x >= 0 && mousePos.x <= width && mousePos.y >= 0 && mousePos.y <= height) {
            drawCursorInspector(g2, width, height, theme);
        }

        // 5. Overlays (Legend, Watermark)
        if (showLegend) {
            drawLegendOverlay(g2, width, height, theme);
        }
    }

    private void drawGrid(Graphics2D g2, int width, int height, Theme theme) {
        double step = calculateOptimalStep(xMax - xMin);
        double subStep = step / 5.0;

        // Minor grid
        g2.setColor(theme.gridMinor);
        g2.setStroke(new BasicStroke(1.0f));

        double firstSubX = Math.floor(xMin / subStep) * subStep;
        for (double x = firstSubX; x <= xMax; x += subStep) {
            int sx = (int) mathToScreenX(x);
            g2.drawLine(sx, 0, sx, height);
        }

        double firstSubY = Math.floor(yMin / subStep) * subStep;
        for (double y = firstSubY; y <= yMax; y += subStep) {
            int sy = (int) mathToScreenY(y);
            g2.drawLine(0, sy, width, sy);
        }

        // Major grid
        g2.setColor(theme.gridMajor);
        g2.setStroke(new BasicStroke(1.2f));

        double firstX = Math.floor(xMin / step) * step;
        for (double x = firstX; x <= xMax; x += step) {
            int sx = (int) mathToScreenX(x);
            g2.drawLine(sx, 0, sx, height);
        }

        double firstY = Math.floor(yMin / step) * step;
        for (double y = firstY; y <= yMax; y += step) {
            int sy = (int) mathToScreenY(y);
            g2.drawLine(0, sy, width, sy);
        }
    }

    private void drawAxes(Graphics2D g2, int width, int height, Theme theme) {
        int originX = (int) mathToScreenX(0.0);
        int originY = (int) mathToScreenY(0.0);

        g2.setColor(theme.axis);
        g2.setStroke(new BasicStroke(2.0f));

        // X Axis
        if (originY >= 0 && originY <= height) {
            g2.drawLine(0, originY, width, originY);
            // Arrows
            drawArrow(g2, width - 15, originY, width - 2, originY);
        } else {
            // Draw axis at border if origin is out of screen
            int yBorder = originY < 0 ? 15 : height - 15;
            g2.drawLine(0, yBorder, width, yBorder);
        }

        // Y Axis
        if (originX >= 0 && originX <= width) {
            g2.drawLine(originX, 0, originX, height);
            // Arrows
            drawArrow(g2, originX, 15, originX, 2);
        } else {
            int xBorder = originX < 0 ? 15 : width - 15;
            g2.drawLine(xBorder, 0, xBorder, height);
        }

        // Numbers / Labels
        if (showNumbers) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(theme.axisLabels);
            FontMetrics fm = g2.getFontMetrics();

            double step = calculateOptimalStep(xMax - xMin);

            // X Numbers
            double firstX = Math.floor(xMin / step) * step;
            int textY = (originY >= 20 && originY <= height - 20) ? originY + 16 : (originY < 20 ? 20 : height - 8);

            for (double x = firstX; x <= xMax; x += step) {
                if (Math.abs(x) < 1e-9) continue; // skip 0
                int sx = (int) mathToScreenX(x);
                String label = formatNumber(x);
                int tw = fm.stringWidth(label);
                g2.drawString(label, sx - tw / 2, textY);
                // Tick
                int tickY = (originY >= 0 && originY <= height) ? originY : (originY < 0 ? 0 : height);
                g2.drawLine(sx, tickY - 3, sx, tickY + 3);
            }

            // Y Numbers
            double firstY = Math.floor(yMin / step) * step;
            int textX = (originX >= 40 && originX <= width - 40) ? originX + 8 : (originX < 40 ? 10 : width - 40);

            for (double y = firstY; y <= yMax; y += step) {
                if (Math.abs(y) < 1e-9) continue;
                int sy = (int) mathToScreenY(y);
                String label = formatNumber(y);
                g2.drawString(label, textX, sy + 4);
                // Tick
                int tickX = (originX >= 0 && originX <= width) ? originX : (originX < 0 ? 0 : width);
                g2.drawLine(tickX - 3, sy, tickX + 3, sy);
            }

            // Origin Label
            if (originX >= 15 && originX <= width - 15 && originY >= 15 && originY <= height - 15) {
                g2.drawString("0", originX + 6, originY + 14);
            }
        }
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double phi = Math.toRadians(30);
        int barb = 8;
        double dy = y2 - y1;
        double dx = x2 - x1;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + Math.PI - phi;
        for (int j = 0; j < 2; j++) {
            x = x2 + barb * Math.cos(rho);
            y = y2 + barb * Math.sin(rho);
            g2.draw(new Line2D.Double(x2, y2, x, y));
            rho = theta + Math.PI + phi;
        }
    }

    private void drawRiemannBoxes(Graphics2D g2, int width, int height, Theme theme) {
        FunctionSeries primary = seriesList.get(0);
        if (!primary.isVisible() || primary.getCompiled() == null) return;

        CompiledExpression compiled = primary.getCompiled();
        int n = riemannBoxesCount;
        double dx = (xMax - xMin) / n;
        double axisY = mathToScreenY(0.0);

        Color fillColor = new Color(primary.getColor().getRed(), primary.getColor().getGreen(), primary.getColor().getBlue(), 45);
        Color borderColor = new Color(primary.getColor().getRed(), primary.getColor().getGreen(), primary.getColor().getBlue(), 160);

        for (int i = 0; i < n; i++) {
            double xLeft = xMin + i * dx;
            double xRight = xMin + (i + 1) * dx;
            double evalX;

            switch (riemannType) {
                case LEFT: evalX = xLeft; break;
                case RIGHT: evalX = xRight; break;
                case MIDPOINT: default: evalX = (xLeft + xRight) / 2.0; break;
                case TRAPEZOID: evalX = xLeft; break;
            }

            double yVal = compiled.eval(evalX, parameterT);
            if (Double.isNaN(yVal) || Double.isInfinite(yVal)) continue;

            double sx1 = mathToScreenX(xLeft);
            double sx2 = mathToScreenX(xRight);
            double syVal = mathToScreenY(yVal);

            double boxX = Math.min(sx1, sx2);
            double boxW = Math.abs(sx2 - sx1);
            double boxY = Math.min(axisY, syVal);
            double boxH = Math.abs(axisY - syVal);

            if (riemannType == CompiledExpression.RiemannType.TRAPEZOID) {
                double yRightVal = compiled.eval(xRight, parameterT);
                if (!Double.isNaN(yRightVal) && !Double.isInfinite(yRightVal)) {
                    double syRight = mathToScreenY(yRightVal);
                    Path2D trap = new Path2D.Double();
                    trap.moveTo(sx1, axisY);
                    trap.lineTo(sx1, syVal);
                    trap.lineTo(sx2, syRight);
                    trap.lineTo(sx2, axisY);
                    trap.closePath();

                    g2.setColor(fillColor);
                    g2.fill(trap);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(trap);
                    continue;
                }
            }

            Rectangle2D rect = new Rectangle2D.Double(boxX, boxY, boxW, boxH);
            g2.setColor(fillColor);
            g2.fill(rect);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(rect);
        }
    }

    private void drawSeriesCurve(Graphics2D g2, int width, int height, FunctionSeries series, Theme theme) {
        CompiledExpression compiled = series.getCompiled();
        int pixels = width * 2; // high resolution sampling
        double step = (xMax - xMin) / pixels;

        Path2D curvePath = new Path2D.Double();
        Path2D fillPath = new Path2D.Double();

        boolean pathStarted = false;
        double zeroYScreen = mathToScreenY(0.0);
        double firstScreenX = 0;

        for (int i = 0; i <= pixels; i++) {
            double x = xMin + i * step;
            double y = compiled.eval(x, parameterT);

            if (Double.isNaN(y) || Double.isInfinite(y) || Math.abs(y) > 1e6) {
                if (pathStarted) {
                    // Close and fill chunk
                    fillPath.lineTo(mathToScreenX(x - step), zeroYScreen);
                    fillPath.closePath();
                    if (series.isShowGradientFill()) {
                        drawGradientArea(g2, fillPath, series.getColor(), zeroYScreen);
                    }
                    pathStarted = false;
                    fillPath = new Path2D.Double();
                }
                continue;
            }

            double sx = mathToScreenX(x);
            double sy = mathToScreenY(y);

            // Asymptote jump check (tan(x), 1/x, etc.)
            if (!pathStarted) {
                curvePath.moveTo(sx, sy);
                fillPath.moveTo(sx, zeroYScreen);
                fillPath.lineTo(sx, sy);
                firstScreenX = sx;
                pathStarted = true;
            } else {
                double prevY = screenToMathY(curvePath.getCurrentPoint().getY());
                if (Math.abs(y - prevY) > (yMax - yMin) * 0.75 || (prevY * y < 0 && Math.abs(y - prevY) > (yMax - yMin) * 0.4)) {
                    // Discontinuity asymptote jump: break curve without connecting vertical line
                    fillPath.lineTo(curvePath.getCurrentPoint().getX(), zeroYScreen);
                    fillPath.closePath();
                    if (series.isShowGradientFill()) {
                        drawGradientArea(g2, fillPath, series.getColor(), zeroYScreen);
                    }
                    fillPath = new Path2D.Double();
                    curvePath.moveTo(sx, sy);
                    fillPath.moveTo(sx, zeroYScreen);
                    fillPath.lineTo(sx, sy);
                } else {
                    curvePath.lineTo(sx, sy);
                    fillPath.lineTo(sx, sy);
                }
            }
        }

        if (pathStarted) {
            fillPath.lineTo(mathToScreenX(xMax), zeroYScreen);
            fillPath.closePath();
            if (series.isShowGradientFill()) {
                drawGradientArea(g2, fillPath, series.getColor(), zeroYScreen);
            }
        }

        // Draw curve outline with sleek neon glow or smooth stroke
        Color strokeCol = series.getColor();
        // Glow pass
        g2.setColor(new Color(strokeCol.getRed(), strokeCol.getGreen(), strokeCol.getBlue(), 60));
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curvePath);

        // Core pass
        g2.setColor(strokeCol);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curvePath);
    }

    private void drawGradientArea(Graphics2D g2, Path2D path, Color baseColor, double zeroY) {
        Paint oldPaint = g2.getPaint();
        Color topColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 80);
        Color botColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 5);

        GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, getHeight(), botColor);
        g2.setPaint(gradient);
        g2.fill(path);
        g2.setPaint(oldPaint);
    }

    private void drawCriticalPoints(Graphics2D g2, int width, int height, FunctionSeries series, Theme theme) {
        CalculatedDataset ds = series.getDataset();
        if (ds == null) return;

        for (FunctionDataPoint p : ds.getAllPoints()) {
            if (!p.isSpecialPoint()) continue;

            double sx = mathToScreenX(p.getX());
            double sy = mathToScreenY(p.getY());

            if (sx < -20 || sx > width + 20 || sy < -20 || sy > height + 20) continue;

            PointType type = p.getPointType();
            Color badgeColor;
            String symbol;

            switch (type) {
                case ROOT:
                    badgeColor = theme.rootBadge;
                    symbol = "Raiz";
                    break;
                case LOCAL_MAX:
                    badgeColor = theme.maxBadge;
                    symbol = "MAX";
                    break;
                case LOCAL_MIN:
                    badgeColor = theme.minBadge;
                    symbol = "MIN";
                    break;
                default:
                    badgeColor = theme.accentPrimary;
                    symbol = "Pto";
                    break;
            }

            // Glow circle
            g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 80));
            g2.fillOval((int) sx - 9, (int) sy - 9, 18, 18);

            // Core dot
            g2.setColor(badgeColor);
            g2.fillOval((int) sx - 5, (int) sy - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((int) sx - 5, (int) sy - 5, 10, 10);

            // Label pill
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String text = String.format(Locale.US, "%s (%.2f, %.2f)", symbol, p.getX(), p.getY());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(text);
            int pillX = (int) sx + 8;
            int pillY = (int) sy - 12;

            g2.setColor(new Color(theme.bgCard.getRed(), theme.bgCard.getGreen(), theme.bgCard.getBlue(), 220));
            g2.fillRoundRect(pillX - 4, pillY - 11, tw + 8, 16, 6, 6);
            g2.setColor(badgeColor);
            g2.drawRoundRect(pillX - 4, pillY - 11, tw + 8, 16, 6, 6);
            g2.setColor(theme.textPrimary);
            g2.drawString(text, pillX, pillY + 1);
        }
    }

    private void drawCursorInspector(Graphics2D g2, int width, int height, Theme theme) {
        int mx = mousePos.x;
        int my = mousePos.y;
        double mathX = screenToMathX(mx);
        double mathY = screenToMathY(my);

        // Crosshair lines
        g2.setColor(theme.crosshair);
        float[] dash = {4f, 4f};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2.drawLine(mx, 0, mx, height);
        g2.drawLine(0, my, width, my);

        // Find closest point on primary curve & Tangent Line
        if (!seriesList.isEmpty()) {
            FunctionSeries primary = seriesList.get(0);
            if (primary.isVisible() && primary.getCompiled() != null) {
                CompiledExpression comp = primary.getCompiled();
                double curveY = comp.eval(mathX, parameterT);

                if (!Double.isNaN(curveY) && !Double.isInfinite(curveY)) {
                    double curveScreenY = mathToScreenY(curveY);
                    double slope = comp.derivative(mathX, parameterT);

                    // Tangent Line
                    if (showTangent && !Double.isNaN(slope)) {
                        g2.setColor(new Color(theme.tangentLine.getRed(), theme.tangentLine.getGreen(), theme.tangentLine.getBlue(), 200));
                        g2.setStroke(new BasicStroke(1.8f));

                        double span = (xMax - xMin) * 0.35;
                        double xA = mathX - span;
                        double yA = curveY - slope * span;
                        double xB = mathX + span;
                        double yB = curveY + slope * span;

                        g2.drawLine((int) mathToScreenX(xA), (int) mathToScreenY(yA),
                                (int) mathToScreenX(xB), (int) mathToScreenY(yB));
                    }

                    // Snapped Point Circle
                    g2.setColor(theme.accentSecondary);
                    g2.fillOval(mx - 6, (int) curveScreenY - 6, 12, 12);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.drawOval(mx - 6, (int) curveScreenY - 6, 12, 12);

                    // HUD floating tooltip badge
                    drawCursorHudPill(g2, width, height, mathX, curveY, slope, theme);
                }
            }
        }
    }

    private void drawCursorHudPill(Graphics2D g2, int width, int height, double x, double y, double slope, Theme theme) {
        g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        String line1 = String.format(Locale.US, "X: %+.4f   Y: %+.4f", x, y);
        String line2 = String.format(Locale.US, "Pendiente f'(x): %+.4f (%+.1f°)", slope, Math.toDegrees(Math.atan(slope)));

        int pad = 10;
        int cardW = Math.max(fm.stringWidth(line1), fm.stringWidth(line2)) + pad * 2;
        int cardH = 48;

        int cardX = mousePos.x + 15;
        int cardY = mousePos.y - cardH - 10;

        if (cardX + cardW > width - 10) cardX = mousePos.x - cardW - 15;
        if (cardY < 10) cardY = mousePos.y + 15;

        // Card shadow & background
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(cardX + 2, cardY + 2, cardW, cardH, 10, 10);

        g2.setColor(new Color(theme.bgCard.getRed(), theme.bgCard.getGreen(), theme.bgCard.getBlue(), 245));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 10, 10);

        g2.setColor(theme.border);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 10, 10);

        // Texts
        g2.setColor(theme.textPrimary);
        g2.drawString(line1, cardX + pad, cardY + 20);

        g2.setColor(theme.textAccent);
        g2.drawString(line2, cardX + pad, cardY + 38);
    }

    private void drawLegendOverlay(Graphics2D g2, int width, int height, Theme theme) {
        if (seriesList.isEmpty()) return;

        int startX = 20;
        int startY = 20;
        int cardW = 240;
        int rowH = 22;
        int cardH = seriesList.size() * rowH + 20;

        // Glassmorphism Card
        g2.setColor(new Color(theme.bgCard.getRed(), theme.bgCard.getGreen(), theme.bgCard.getBlue(), 210));
        g2.fillRoundRect(startX, startY, cardW, cardH, 12, 12);
        g2.setColor(theme.border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(startX, startY, cardW, cardH, 12, 12);

        g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < seriesList.size(); i++) {
            FunctionSeries s = seriesList.get(i);
            int curY = startY + 18 + i * rowH;

            // Color indicator dot
            g2.setColor(s.getColor());
            g2.fillOval(startX + 12, curY - 8, 10, 10);

            // Series formula text
            g2.setColor(s.isVisible() ? theme.textPrimary : theme.textSecondary);
            String title = s.getName() + " = " + s.getExpressionText();
            if (fm.stringWidth(title) > cardW - 35) {
                title = title.substring(0, Math.min(title.length(), 24)) + "...";
            }
            g2.drawString(title, startX + 30, curY);
        }
    }

    private double calculateOptimalStep(double range) {
        double rawStep = range / 10.0;
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double norm = rawStep / magnitude;

        if (norm < 1.5) return magnitude;
        if (norm < 3.5) return 2 * magnitude;
        if (norm < 7.5) return 5 * magnitude;
        return 10 * magnitude;
    }

    private String formatNumber(double val) {
        if (Math.abs(val - Math.round(val)) < 1e-6) {
            return String.valueOf((long) Math.round(val));
        }
        return String.format(Locale.US, "%.2f", val);
    }

    // High Resolution PNG Export
    public BufferedImage exportToImage(int width, int height, double scale) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        renderScene(g2, width, height, scale);
        g2.dispose();
        return image;
    }

    public boolean exportToPNGFile(File file, int width, int height) {
        try {
            BufferedImage img = exportToImage(width, height, 2.0);
            ImageIO.write(img, "PNG", file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void copyImageToClipboard() {
        BufferedImage img = exportToImage(getWidth() * 2, getHeight() * 2, 2.0);
        TransferableImage trans = new TransferableImage(img);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
    }

    // Helper for clipboard transfer
    private static class TransferableImage implements Transferable {
        private final Image image;

        public TransferableImage(Image image) {
            this.image = image;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            return image;
        }
    }
}
