package mathgraph.model;

public enum PointType {
    REGULAR("Punto"),
    ROOT("Raíz f(x)=0"),
    LOCAL_MAX("Máximo Local"),
    LOCAL_MIN("Mínimo Local"),
    INFLECTION("Punto Inflexión"),
    ASYMPTOTE("Discontinuidad");

    private final String displayName;

    PointType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
