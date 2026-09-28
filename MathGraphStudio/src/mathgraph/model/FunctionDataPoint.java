package mathgraph.model;

public class FunctionDataPoint {
    private final long id;
    private final double x;
    private final double y;
    private final double derivative;      // f'(x)
    private final double secondDerivative;// f''(x)
    private final double cumulativeIntegral;
    private final double slopeAngleDeg;
    private final PointType pointType;
    private final String metadata;

    public FunctionDataPoint(long id, double x, double y, double derivative, double secondDerivative, double cumulativeIntegral, PointType pointType, String metadata) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.derivative = derivative;
        this.secondDerivative = secondDerivative;
        this.cumulativeIntegral = cumulativeIntegral;
        this.slopeAngleDeg = (Double.isNaN(derivative)) ? 0.0 : Math.toDegrees(Math.atan(derivative));
        this.pointType = pointType;
        this.metadata = (metadata == null) ? "" : metadata;
    }

    public long getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDerivative() { return derivative; }
    public double getSecondDerivative() { return secondDerivative; }
    public double getCumulativeIntegral() { return cumulativeIntegral; }
    public double getSlopeAngleDeg() { return slopeAngleDeg; }
    public PointType getPointType() { return pointType; }
    public String getMetadata() { return metadata; }

    public boolean isSpecialPoint() {
        return pointType != PointType.REGULAR;
    }

    @Override
    public String toString() {
        return String.format("Point[%d: (%.4f, %.4f) | f'=%.4f | Type=%s]", id, x, y, derivative, pointType);
    }
}
