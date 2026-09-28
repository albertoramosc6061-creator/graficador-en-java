package mathgraph.model;

import mathgraph.core.parser.CompiledExpression;
import java.awt.Color;

public class FunctionSeries {
    private String name;
    private String expressionText;
    private Color color;
    private boolean visible = true;
    private boolean showGradientFill = true;
    private boolean showRiemannBoxes = false;
    private int riemannBoxesCount = 20;
    private CompiledExpression.RiemannType riemannType = CompiledExpression.RiemannType.MIDPOINT;
    private boolean showTangentOnHover = true;
    private boolean showCriticalPoints = true;

    private CompiledExpression compiled;
    private CalculatedDataset dataset;

    public FunctionSeries(String name, String expressionText, Color color) {
        this.name = name;
        this.expressionText = expressionText;
        this.color = color;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExpressionText() { return expressionText; }
    public void setExpressionText(String expressionText) { this.expressionText = expressionText; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isShowGradientFill() { return showGradientFill; }
    public void setShowGradientFill(boolean showGradientFill) { this.showGradientFill = showGradientFill; }

    public boolean isShowRiemannBoxes() { return showRiemannBoxes; }
    public void setShowRiemannBoxes(boolean showRiemannBoxes) { this.showRiemannBoxes = showRiemannBoxes; }

    public int getRiemannBoxesCount() { return riemannBoxesCount; }
    public void setRiemannBoxesCount(int riemannBoxesCount) { this.riemannBoxesCount = riemannBoxesCount; }

    public CompiledExpression.RiemannType getRiemannType() { return riemannType; }
    public void setRiemannType(CompiledExpression.RiemannType riemannType) { this.riemannType = riemannType; }

    public boolean isShowTangentOnHover() { return showTangentOnHover; }
    public void setShowTangentOnHover(boolean showTangentOnHover) { this.showTangentOnHover = showTangentOnHover; }

    public boolean isShowCriticalPoints() { return showCriticalPoints; }
    public void setShowCriticalPoints(boolean showCriticalPoints) { this.showCriticalPoints = showCriticalPoints; }

    public CompiledExpression getCompiled() { return compiled; }
    public void setCompiled(CompiledExpression compiled) { this.compiled = compiled; }

    public CalculatedDataset getDataset() { return dataset; }
    public void setDataset(CalculatedDataset dataset) { this.dataset = dataset; }
}
