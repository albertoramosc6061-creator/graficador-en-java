package mathgraph.core.parser;

import java.util.ArrayList;
import java.util.List;

public class MathParser {
    private final List<Token> tokens;
    private int cursor;

    public MathParser(List<Token> tokens) {
        this.tokens = tokens;
        this.cursor = 0;
    }

    public static ASTNode parse(String expression) throws MathParseException {
        MathLexer lexer = new MathLexer(expression);
        List<Token> tokens = lexer.tokenize();
        MathParser parser = new MathParser(tokens);
        return parser.parseExpression();
    }

    public ASTNode parseExpression() throws MathParseException {
        ASTNode node = parseTernary();
        if (!isAtEnd() && peek().getType() != TokenType.EOF) {
            throw new MathParseException("Símbolo inesperado al final de la expresión: '" + peek().getText() + "'", peek().getPosition());
        }
        return node;
    }

    private ASTNode parseTernary() throws MathParseException {
        ASTNode cond = parseRelational();

        if (match(TokenType.TERNARY_IF)) {
            ASTNode trueBranch = parseTernary();
            if (!match(TokenType.TERNARY_COLON)) {
                throw new MathParseException("Se esperaba ':' en el operador ternario", previous().getPosition());
            }
            ASTNode falseBranch = parseTernary();
            return new ASTNode.TernaryNode(cond, trueBranch, falseBranch);
        }

        return cond;
    }

    private ASTNode parseRelational() throws MathParseException {
        ASTNode left = parseAdditive();

        while (match(TokenType.REL_GT, TokenType.REL_LT, TokenType.REL_GTE, TokenType.REL_LTE, TokenType.REL_EQ, TokenType.REL_NEQ)) {
            Token op = previous();
            ASTNode right = parseAdditive();
            left = new ASTNode.BinaryOpNode(op.getType(), left, right);
        }

        return left;
    }

    private ASTNode parseAdditive() throws MathParseException {
        ASTNode left = parseMultiplicative();

        while (match(TokenType.OPERATOR_PLUS, TokenType.OPERATOR_MINUS)) {
            Token op = previous();
            ASTNode right = parseMultiplicative();
            left = new ASTNode.BinaryOpNode(op.getType(), left, right);
        }

        return left;
    }

    private ASTNode parseMultiplicative() throws MathParseException {
        ASTNode left = parseUnary();

        while (match(TokenType.OPERATOR_MULTIPLY, TokenType.OPERATOR_DIVIDE, TokenType.OPERATOR_MODULO)) {
            Token op = previous();
            ASTNode right = parseUnary();
            left = new ASTNode.BinaryOpNode(op.getType(), left, right);
        }

        return left;
    }

    private ASTNode parseUnary() throws MathParseException {
        if (match(TokenType.OPERATOR_PLUS, TokenType.OPERATOR_MINUS)) {
            Token op = previous();
            ASTNode operand = parseUnary();
            return new ASTNode.UnaryOpNode(op.getType(), operand);
        }

        return parsePower();
    }

    private ASTNode parsePower() throws MathParseException {
        ASTNode left = parsePrimary();

        if (match(TokenType.OPERATOR_POWER)) {
            Token op = previous();
            // Right-associative: 2^3^4 = 2^(3^4)
            ASTNode right = parseUnary(); // allows negative powers like x^-2
            return new ASTNode.BinaryOpNode(op.getType(), left, right);
        }

        return left;
    }

    private ASTNode parsePrimary() throws MathParseException {
        if (match(TokenType.NUMBER)) {
            Token t = previous();
            return new ASTNode.NumberNode(t.getValue(), t.getText());
        }

        if (match(TokenType.VARIABLE_X)) {
            return new ASTNode.VariableNode("x");
        }

        if (match(TokenType.VARIABLE_T)) {
            return new ASTNode.VariableNode("t");
        }

        if (match(TokenType.FUNCTION)) {
            Token funcTok = previous();
            String fnName = funcTok.getText().toLowerCase();
            if (!isKnownFunction(fnName)) {
                throw new MathParseException("Función desconocida: '" + funcTok.getText() + "'. Válidas: sen, sin, cos, tan, ln, log, exp, sqrt, abs...", funcTok.getPosition());
            }
            if (!match(TokenType.LPAREN)) {
                throw new MathParseException("Se esperaba '(' después de la función '" + funcTok.getText() + "'", funcTok.getPosition());
            }
            List<ASTNode> args = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                do {
                    args.add(parseTernary());
                } while (match(TokenType.COMMA));
            }
            if (!match(TokenType.RPAREN)) {
                throw new MathParseException("Se esperaba ')' para cerrar la función '" + funcTok.getText() + "'", cursor < tokens.size() ? tokens.get(cursor).getPosition() : -1);
            }
            return new ASTNode.FunctionNode(fnName, args);
        }

        if (match(TokenType.LPAREN)) {
            ASTNode expr = parseTernary();
            if (!match(TokenType.RPAREN)) {
                throw new MathParseException("Paréntesis sin cerrar ')'", cursor < tokens.size() ? tokens.get(cursor).getPosition() : -1);
            }
            return expr;
        }

        Token errToken = peek();
        throw new MathParseException("Expresión inválida cerca de '" + (errToken != null ? errToken.getText() : "EOF") + "'", errToken != null ? errToken.getPosition() : -1);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) cursor++;
        return previous();
    }

    private boolean isAtEnd() {
        return cursor >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        if (cursor < tokens.size()) return tokens.get(cursor);
        return new Token(TokenType.EOF, "", -1);
    }

    private Token previous() {
        return tokens.get(cursor - 1);
    }

    private static boolean isKnownFunction(String fn) {
        return switch (fn) {
            case "sen", "sin", "cos", "tan", "tg",
                 "asen", "asin", "arcsen", "arcsin",
                 "acos", "arccos", "atan", "atg", "arctan", "arctg", "atan2",
                 "senh", "sinh", "cosh", "tanh", "tgh",
                 "sqrt", "raiz", "cbrt", "exp", "ln", "log", "log10", "log2",
                 "abs", "floor", "ceil", "round", "signum", "sgn",
                 "sinc", "step", "pulse", "pow", "min", "max" -> true;
            default -> false;
        };
    }
}
