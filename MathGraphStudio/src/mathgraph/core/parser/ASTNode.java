package mathgraph.core.parser;

import java.util.List;

public abstract class ASTNode {
    public abstract double evaluate(double x, double t);
    public abstract String toTreeString(int indent);
    public abstract String toInfix();

    protected String indentStr(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    // Concrete Nodes
    public static class NumberNode extends ASTNode {
        private final double value;
        private final String rawText;

        public NumberNode(double value, String rawText) {
            this.value = value;
            this.rawText = rawText;
        }

        public double getValue() { return value; }

        @Override
        public double evaluate(double x, double t) {
            return value;
        }

        @Override
        public String toTreeString(int indent) {
            return indentStr(indent) + "Num(" + (rawText != null ? rawText : value) + ")\n";
        }

        @Override
        public String toInfix() {
            if (rawText != null && !rawText.isEmpty()) return rawText;
            if (value == (long) value) return String.format("%d", (long) value);
            return String.valueOf(value);
        }
    }

    public static class VariableNode extends ASTNode {
        private final String name; // "x" or "t"

        public VariableNode(String name) {
            this.name = name;
        }

        public String getName() { return name; }

        @Override
        public double evaluate(double x, double t) {
            if ("t".equalsIgnoreCase(name)) return t;
            return x;
        }

        @Override
        public String toTreeString(int indent) {
            return indentStr(indent) + "Var(" + name + ")\n";
        }

        @Override
        public String toInfix() {
            return name;
        }
    }

    public static class UnaryOpNode extends ASTNode {
        private final TokenType op;
        private final ASTNode operand;

        public UnaryOpNode(TokenType op, ASTNode operand) {
            this.op = op;
            this.operand = operand;
        }

        public TokenType getOp() { return op; }
        public ASTNode getOperand() { return operand; }

        @Override
        public double evaluate(double x, double t) {
            double val = operand.evaluate(x, t);
            if (op == TokenType.OPERATOR_MINUS) return -val;
            return val;
        }

        @Override
        public String toTreeString(int indent) {
            return indentStr(indent) + "Unary(" + (op == TokenType.OPERATOR_MINUS ? "-" : "+") + ")\n" + operand.toTreeString(indent + 1);
        }

        @Override
        public String toInfix() {
            return (op == TokenType.OPERATOR_MINUS ? "-" : "+") + operand.toInfix();
        }
    }

    public static class BinaryOpNode extends ASTNode {
        private final TokenType op;
        private final ASTNode left;
        private final ASTNode right;

        public BinaryOpNode(TokenType op, ASTNode left, ASTNode right) {
            this.op = op;
            this.left = left;
            this.right = right;
        }

        public TokenType getOp() { return op; }
        public ASTNode getLeft() { return left; }
        public ASTNode getRight() { return right; }

        @Override
        public double evaluate(double x, double t) {
            double a = left.evaluate(x, t);
            double b = right.evaluate(x, t);
            switch (op) {
                case OPERATOR_PLUS: return a + b;
                case OPERATOR_MINUS: return a - b;
                case OPERATOR_MULTIPLY: return a * b;
                case OPERATOR_DIVIDE:
                    if (Math.abs(b) < 1e-15) return (a >= 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
                    return a / b;
                case OPERATOR_POWER: return Math.pow(a, b);
                case OPERATOR_MODULO: return a % b;
                case REL_GT: return a > b ? 1.0 : 0.0;
                case REL_LT: return a < b ? 1.0 : 0.0;
                case REL_GTE: return a >= b ? 1.0 : 0.0;
                case REL_LTE: return a <= b ? 1.0 : 0.0;
                case REL_EQ: return Math.abs(a - b) < 1e-12 ? 1.0 : 0.0;
                case REL_NEQ: return Math.abs(a - b) >= 1e-12 ? 1.0 : 0.0;
                default: return 0.0;
            }
        }

        @Override
        public String toTreeString(int indent) {
            String opSymbol = switch (op) {
                case OPERATOR_PLUS -> "+";
                case OPERATOR_MINUS -> "-";
                case OPERATOR_MULTIPLY -> "*";
                case OPERATOR_DIVIDE -> "/";
                case OPERATOR_POWER -> "^";
                case OPERATOR_MODULO -> "%";
                case REL_GT -> ">";
                case REL_LT -> "<";
                case REL_GTE -> ">=";
                case REL_LTE -> "<=";
                case REL_EQ -> "==";
                case REL_NEQ -> "!=";
                default -> "?";
            };
            return indentStr(indent) + "BinaryOp(" + opSymbol + ")\n" +
                   left.toTreeString(indent + 1) +
                   right.toTreeString(indent + 1);
        }

        @Override
        public String toInfix() {
            String opSymbol = switch (op) {
                case OPERATOR_PLUS -> "+";
                case OPERATOR_MINUS -> "-";
                case OPERATOR_MULTIPLY -> "*";
                case OPERATOR_DIVIDE -> "/";
                case OPERATOR_POWER -> "^";
                case OPERATOR_MODULO -> "%";
                case REL_GT -> ">";
                case REL_LT -> "<";
                case REL_GTE -> ">=";
                case REL_LTE -> "<=";
                case REL_EQ -> "==";
                case REL_NEQ -> "!=";
                default -> "?";
            };
            return "(" + left.toInfix() + " " + opSymbol + " " + right.toInfix() + ")";
        }
    }

    public static class FunctionNode extends ASTNode {
        private final String funcName;
        private final List<ASTNode> arguments;

        public FunctionNode(String funcName, List<ASTNode> arguments) {
            this.funcName = funcName.toLowerCase();
            this.arguments = arguments;
        }

        public String getFuncName() { return funcName; }
        public List<ASTNode> getArguments() { return arguments; }

        @Override
        public double evaluate(double x, double t) {
            if (arguments.isEmpty()) return 0.0;
            double arg0 = arguments.get(0).evaluate(x, t);
            
            switch (funcName) {
                case "sen":
                case "sin": return Math.sin(arg0);
                case "cos": return Math.cos(arg0);
                case "tan":
                case "tg": return Math.tan(arg0);
                case "asen":
                case "asin":
                case "arcsen":
                case "arcsin": return Math.asin(arg0);
                case "acos":
                case "arccos": return Math.acos(arg0);
                case "atan":
                case "atg":
                case "arctan":
                case "arctg": return Math.atan(arg0);
                case "atan2":
                    if (arguments.size() >= 2) {
                        return Math.atan2(arg0, arguments.get(1).evaluate(x, t));
                    }
                    return Math.atan(arg0);
                case "senh":
                case "sinh": return Math.sinh(arg0);
                case "cosh": return Math.cosh(arg0);
                case "tanh":
                case "tgh": return Math.tanh(arg0);
                case "sqrt":
                case "raiz": return arg0 >= 0 ? Math.sqrt(arg0) : Double.NaN;
                case "cbrt": return Math.cbrt(arg0);
                case "exp": return Math.exp(arg0);
                case "ln": return arg0 > 0 ? Math.log(arg0) : Double.NaN;
                case "log":
                case "log10": return arg0 > 0 ? Math.log10(arg0) : Double.NaN;
                case "log2": return arg0 > 0 ? Math.log(arg0) / Math.log(2) : Double.NaN;
                case "abs": return Math.abs(arg0);
                case "floor": return Math.floor(arg0);
                case "ceil": return Math.ceil(arg0);
                case "round": return Math.round(arg0);
                case "signum":
                case "sgn": return Math.signum(arg0);
                case "sinc": return (Math.abs(arg0) < 1e-12) ? 1.0 : (Math.sin(arg0) / arg0);
                case "step": return arg0 >= 0 ? 1.0 : 0.0;
                case "pulse": return (arg0 >= 0 && arg0 <= 1.0) ? 1.0 : 0.0;
                case "pow":
                    if (arguments.size() >= 2) {
                        return Math.pow(arg0, arguments.get(1).evaluate(x, t));
                    }
                    return arg0;
                case "min":
                    if (arguments.size() >= 2) {
                        return Math.min(arg0, arguments.get(1).evaluate(x, t));
                    }
                    return arg0;
                case "max":
                    if (arguments.size() >= 2) {
                        return Math.max(arg0, arguments.get(1).evaluate(x, t));
                    }
                    return arg0;
                default:
                    return Double.NaN;
            }
        }

        @Override
        public String toTreeString(int indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(indentStr(indent)).append("Func(").append(funcName).append(")\n");
            for (ASTNode arg : arguments) {
                sb.append(arg.toTreeString(indent + 1));
            }
            return sb.toString();
        }

        @Override
        public String toInfix() {
            StringBuilder sb = new StringBuilder();
            sb.append(funcName).append("(");
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arguments.get(i).toInfix());
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static class TernaryNode extends ASTNode {
        private final ASTNode condition;
        private final ASTNode trueBranch;
        private final ASTNode falseBranch;

        public TernaryNode(ASTNode condition, ASTNode trueBranch, ASTNode falseBranch) {
            this.condition = condition;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
        }

        public ASTNode getCondition() { return condition; }
        public ASTNode getTrueBranch() { return trueBranch; }
        public ASTNode getFalseBranch() { return falseBranch; }

        @Override
        public double evaluate(double x, double t) {
            double condVal = condition.evaluate(x, t);
            if (Math.abs(condVal) > 1e-12) {
                return trueBranch.evaluate(x, t);
            } else {
                return falseBranch.evaluate(x, t);
            }
        }

        @Override
        public String toTreeString(int indent) {
            return indentStr(indent) + "Ternary(? :)\n" +
                   condition.toTreeString(indent + 1) +
                   trueBranch.toTreeString(indent + 1) +
                   falseBranch.toTreeString(indent + 1);
        }

        @Override
        public String toInfix() {
            return "(" + condition.toInfix() + " ? " + trueBranch.toInfix() + " : " + falseBranch.toInfix() + ")";
        }
    }
}
