package mathgraph.core.parser;

import java.util.ArrayList;
import java.util.List;

public class CompiledExpression {
    private final String originalText;
    private final ASTNode rootNode;
    private static final double DEFAULT_H = 1e-5;

    public CompiledExpression(String originalText, ASTNode rootNode) {
        this.originalText = originalText;
        this.rootNode = rootNode;
    }

    public static CompiledExpression compile(String expression) throws MathParseException {
        ASTNode node = MathParser.parse(expression);
        return new CompiledExpression(expression, node);
    }

    public String getOriginalText() {
        return originalText;
    }

    public ASTNode getRootNode() {
        return rootNode;
    }

    public double eval(double x) {
        return eval(x, 0.0);
    }

    public double eval(double x, double t) {
        if (rootNode == null) return 0.0;
        try {
            double val = rootNode.evaluate(x, t);
            if (Double.isNaN(val) || Double.isInfinite(val)) {
                return val;
            }
            return val;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    // Numerical Derivative f'(x)
    public double derivative(double x, double t) {
        double h = DEFAULT_H;
        double yPlus = eval(x + h, t);
        double yMinus = eval(x - h, t);
        if (Double.isNaN(yPlus) || Double.isNaN(yMinus)) return Double.NaN;
        return (yPlus - yMinus) / (2.0 * h);
    }

    // Numerical 2nd Derivative f''(x)
    public double secondDerivative(double x, double t) {
        double h = DEFAULT_H;
        double yPlus = eval(x + h, t);
        double yMid = eval(x, t);
        double yMinus = eval(x - h, t);
        if (Double.isNaN(yPlus) || Double.isNaN(yMid) || Double.isNaN(yMinus)) return Double.NaN;
        return (yPlus - 2.0 * yMid + yMinus) / (h * h);
    }

    // Simpson 1/3 rule for definite integral
    public double integrateSimpson(double a, double b, double t, int intervals) {
        if (intervals % 2 != 0) intervals++;
        double h = (b - a) / intervals;
        double sum = eval(a, t) + eval(b, t);

        for (int i = 1; i < intervals; i++) {
            double x = a + i * h;
            double y = eval(x, t);
            if (Double.isNaN(y)) continue;
            sum += (i % 2 == 0 ? 2 : 4) * y;
        }

        return (h / 3.0) * sum;
    }

    // Riemann Sums: LEFT, RIGHT, MIDPOINT, TRAPEZOID
    public enum RiemannType { LEFT, RIGHT, MIDPOINT, TRAPEZOID }

    public double integrateRiemann(double a, double b, double t, int n, RiemannType type) {
        double dx = (b - a) / n;
        double total = 0.0;

        for (int i = 0; i < n; i++) {
            double x1 = a + i * dx;
            double x2 = a + (i + 1) * dx;
            double hVal;

            switch (type) {
                case LEFT:
                    hVal = eval(x1, t);
                    break;
                case RIGHT:
                    hVal = eval(x2, t);
                    break;
                case MIDPOINT:
                    hVal = eval((x1 + x2) / 2.0, t);
                    break;
                case TRAPEZOID:
                    hVal = (eval(x1, t) + eval(x2, t)) / 2.0;
                    break;
                default:
                    hVal = eval(x1, t);
            }

            if (!Double.isNaN(hVal) && !Double.isInfinite(hVal)) {
                total += hVal * dx;
            }
        }
        return total;
    }

    // Arc length calculation
    public double calculateArcLength(double a, double b, double t, int steps) {
        double dx = (b - a) / steps;
        double length = 0.0;
        double prevX = a;
        double prevY = eval(a, t);

        for (int i = 1; i <= steps; i++) {
            double curX = a + i * dx;
            double curY = eval(curX, t);
            if (!Double.isNaN(prevY) && !Double.isNaN(curY)) {
                double seg = Math.hypot(curX - prevX, curY - prevY);
                length += seg;
            }
            prevX = curX;
            prevY = curY;
        }
        return length;
    }

    // Find roots in range [xMin, xMax]
    public List<Double> findRoots(double xMin, double xMax, double t, int samples) {
        List<Double> roots = new ArrayList<>();
        double step = (xMax - xMin) / samples;
        double prevX = xMin;
        double prevY = eval(prevX, t);

        for (int i = 1; i <= samples; i++) {
            double curX = xMin + i * step;
            double curY = eval(curX, t);

            if (Double.isNaN(prevY) || Double.isNaN(curY)) {
                prevX = curX;
                prevY = curY;
                continue;
            }

            // Exact zero
            if (Math.abs(curY) < 1e-9) {
                addRootIfNotPresent(roots, curX);
            } else if (prevY * curY < 0) {
                // Sign change -> Bisection / Newton refine
                double root = refineRootBisection(prevX, curX, t);
                if (!Double.isNaN(root)) {
                    addRootIfNotPresent(roots, root);
                }
            }

            prevX = curX;
            prevY = curY;
        }
        return roots;
    }

    private double refineRootBisection(double a, double b, double t) {
        double fa = eval(a, t);
        for (int iter = 0; iter < 40; iter++) {
            double mid = (a + b) / 2.0;
            double fmid = eval(mid, t);
            if (Double.isNaN(fmid)) return Double.NaN;
            if (Math.abs(fmid) < 1e-12 || (b - a) / 2.0 < 1e-12) {
                return mid;
            }
            if (fa * fmid < 0) {
                b = mid;
            } else {
                a = mid;
                fa = fmid;
            }
        }
        return (a + b) / 2.0;
    }

    private void addRootIfNotPresent(List<Double> roots, double candidate) {
        for (double r : roots) {
            if (Math.abs(r - candidate) < 1e-4) {
                return;
            }
        }
        roots.add(candidate);
    }
}
