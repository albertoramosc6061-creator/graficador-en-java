package mathgraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatedDataset {
    private final String formula;
    private final List<FunctionDataPoint> allPoints;
    private double minX = Double.MAX_VALUE;
    private double maxX = -Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxY = -Double.MAX_VALUE;
    private double totalIntegral = 0.0;
    private int rootCount = 0;
    private int maxCount = 0;
    private int minCount = 0;
    private double avgY = 0.0;

    public CalculatedDataset(String formula) {
        this.formula = formula;
        this.allPoints = new ArrayList<>();
    }

    public void addPoint(FunctionDataPoint point) {
        allPoints.add(point);
        double x = point.getX();
        double y = point.getY();

        if (x < minX) minX = x;
        if (x > maxX) maxX = x;

        if (!Double.isNaN(y) && !Double.isInfinite(y)) {
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        if (point.getPointType() == PointType.ROOT) rootCount++;
        else if (point.getPointType() == PointType.LOCAL_MAX) maxCount++;
        else if (point.getPointType() == PointType.LOCAL_MIN) minCount++;
    }

    public void finalizeCalculations() {
        if (allPoints.isEmpty()) {
            minX = maxX = minY = maxY = avgY = 0.0;
            return;
        }
        double sumY = 0.0;
        int validCount = 0;
        for (FunctionDataPoint p : allPoints) {
            double y = p.getY();
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                sumY += y;
                validCount++;
            }
        }
        avgY = validCount > 0 ? sumY / validCount : 0.0;
        if (!allPoints.isEmpty()) {
            totalIntegral = allPoints.get(allPoints.size() - 1).getCumulativeIntegral();
        }
    }

    public String getFormula() { return formula; }
    public List<FunctionDataPoint> getAllPoints() { return allPoints; }
    public int size() { return allPoints.size(); }
    public double getMinX() { return minX; }
    public double getMaxX() { return maxX; }
    public double getMinY() { return minY; }
    public double getMaxY() { return maxY; }
    public double getTotalIntegral() { return totalIntegral; }
    public int getRootCount() { return rootCount; }
    public int getMaxCount() { return maxCount; }
    public int getMinCount() { return minCount; }
    public double getAvgY() { return avgY; }

    public List<FunctionDataPoint> filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            return allPoints;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);
        List<FunctionDataPoint> filtered = new ArrayList<>();

        for (FunctionDataPoint p : allPoints) {
            if (matchesFilter(p, q)) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private boolean matchesFilter(FunctionDataPoint p, String q) {
        if (q.equals("root") || q.equals("raices") || q.equals("raíz") || q.equals("raiz")) {
            return p.getPointType() == PointType.ROOT;
        }
        if (q.equals("max") || q.equals("maximos") || q.equals("máximo")) {
            return p.getPointType() == PointType.LOCAL_MAX;
        }
        if (q.equals("min") || q.equals("minimos") || q.equals("mínimo")) {
            return p.getPointType() == PointType.LOCAL_MIN;
        }
        if (q.equals("critical") || q.equals("especial") || q.equals("criticos")) {
            return p.isSpecialPoint();
        }

        // Numerical condition queries like: "y > 0", "y < 0", "x >= 2", "f' > 0"
        if (q.startsWith("y >") || q.startsWith("y>")) {
            double threshold = parseNumAfter(q, '>');
            return p.getY() > threshold;
        }
        if (q.startsWith("y <") || q.startsWith("y<")) {
            double threshold = parseNumAfter(q, '<');
            return p.getY() < threshold;
        }
        if (q.startsWith("x >") || q.startsWith("x>")) {
            double threshold = parseNumAfter(q, '>');
            return p.getX() > threshold;
        }
        if (q.startsWith("x <") || q.startsWith("x<")) {
            double threshold = parseNumAfter(q, '<');
            return p.getX() < threshold;
        }

        // Generic text search in metadata, pointType, x, y
        String strRepr = String.format(Locale.US, "%d %.4f %.4f %.4f %s %s", 
                p.getId(), p.getX(), p.getY(), p.getDerivative(), p.getPointType().name(), p.getMetadata()).toLowerCase();

        return strRepr.contains(q);
    }

    private double parseNumAfter(String q, char op) {
        try {
            int idx = q.indexOf(op);
            if (idx >= 0 && idx + 1 < q.length()) {
                return Double.parseDouble(q.substring(idx + 1).trim());
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    public String exportToCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,x,y_fx,derivative_f1,second_deriv_f2,cumulative_integral,slope_deg,point_type,metadata\n");
        for (FunctionDataPoint p : allPoints) {
            sb.append(String.format(Locale.US, "%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.2f,\"%s\",\"%s\"\n",
                    p.getId(), p.getX(), p.getY(), p.getDerivative(), p.getSecondDerivative(),
                    p.getCumulativeIntegral(), p.getSlopeAngleDeg(), p.getPointType().name(), p.getMetadata()));
        }
        return sb.toString();
    }

    public String exportToJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"formula\": \"").append(formula.replace("\"", "\\\"")).append("\",\n");
        sb.append("  \"total_points\": ").append(allPoints.size()).append(",\n");
        sb.append("  \"statistics\": {\n");
        sb.append(String.format(Locale.US, "    \"min_x\": %.6f,\n", minX));
        sb.append(String.format(Locale.US, "    \"max_x\": %.6f,\n", maxX));
        sb.append(String.format(Locale.US, "    \"min_y\": %.6f,\n", minY));
        sb.append(String.format(Locale.US, "    \"max_y\": %.6f,\n", maxY));
        sb.append(String.format(Locale.US, "    \"total_integral\": %.6f,\n", totalIntegral));
        sb.append(String.format(Locale.US, "    \"avg_y\": %.6f,\n", avgY));
        sb.append("    \"roots_count\": ").append(rootCount).append(",\n");
        sb.append("    \"extrema_count\": ").append(maxCount + minCount).append("\n");
        sb.append("  },\n");
        sb.append("  \"points\": [\n");

        for (int i = 0; i < allPoints.size(); i++) {
            FunctionDataPoint p = allPoints.get(i);
            sb.append(String.format(Locale.US, "    {\"id\": %d, \"x\": %.6f, \"y\": %.6f, \"f_prime\": %.6f, \"f_double_prime\": %.6f, \"integral\": %.6f, \"type\": \"%s\"}",
                    p.getId(), p.getX(), p.getY(), p.getDerivative(), p.getSecondDerivative(), p.getCumulativeIntegral(), p.getPointType().name()));
            if (i < allPoints.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n}");
        return sb.toString();
    }

    public String exportToSQL() {
        StringBuilder sb = new StringBuilder();
        sb.append("-- MathGraph Studio In-Memory Database Export\n");
        sb.append("-- Formula: ").append(formula).append("\n\n");
        sb.append("CREATE TABLE IF NOT EXISTS function_dataset (\n");
        sb.append("    id BIGINT PRIMARY KEY,\n");
        sb.append("    formula VARCHAR(255),\n");
        sb.append("    x DOUBLE PRECISION NOT NULL,\n");
        sb.append("    y_fx DOUBLE PRECISION NOT NULL,\n");
        sb.append("    derivative_f1 DOUBLE PRECISION,\n");
        sb.append("    second_deriv_f2 DOUBLE PRECISION,\n");
        sb.append("    cumulative_integral DOUBLE PRECISION,\n");
        sb.append("    slope_degrees DOUBLE PRECISION,\n");
        sb.append("    point_type VARCHAR(50),\n");
        sb.append("    metadata VARCHAR(255)\n");
        sb.append(");\n\n");

        sb.append("BEGIN TRANSACTION;\n");
        for (FunctionDataPoint p : allPoints) {
            sb.append(String.format(Locale.US,
                    "INSERT INTO function_dataset (id, formula, x, y_fx, derivative_f1, second_deriv_f2, cumulative_integral, slope_degrees, point_type, metadata) " +
                    "VALUES (%d, '%s', %.6f, %.6f, %.6f, %.6f, %.6f, %.2f, '%s', '%s');\n",
                    p.getId(), formula.replace("'", "''"), p.getX(), p.getY(), p.getDerivative(),
                    p.getSecondDerivative(), p.getCumulativeIntegral(), p.getSlopeAngleDeg(),
                    p.getPointType().name(), p.getMetadata().replace("'", "''")));
        }
        sb.append("COMMIT;\n");
        return sb.toString();
    }
}
