package mathgraph.core.parser;

import java.util.ArrayList;
import java.util.List;

public class FlowchartNode {
    private final String id;
    private final String title;
    private final String category; // "INPUT", "PARSER", "SAMPLER", "EVALUATOR", "CALCULUS", "RENDER", "DATABASE"
    private final String description;
    private final List<String> details;
    private final List<String> outputs;
    private int x;
    private int y;
    private int width = 220;
    private int height = 110;
    private boolean isHighlighted;

    public FlowchartNode(String id, String title, String category, String description) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.details = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.isHighlighted = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public List<String> getDetails() { return details; }
    public List<String> getOutputs() { return outputs; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public boolean isHighlighted() { return isHighlighted; }
    public void setHighlighted(boolean highlighted) { isHighlighted = highlighted; }

    public void addDetail(String detail) {
        this.details.add(detail);
    }

    public void addOutput(String targetNodeId) {
        this.outputs.add(targetNodeId);
    }
}
