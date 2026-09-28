package mathgraph.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MathLexer {
    private final String input;
    private int cursor;
    private final int length;

    public MathLexer(String input) {
        this.input = (input == null) ? "" : input;
        this.cursor = 0;
        this.length = this.input.length();
    }

    public List<Token> tokenize() throws MathParseException {
        List<Token> rawTokens = new ArrayList<>();
        
        while (cursor < length) {
            char c = input.charAt(cursor);

            if (Character.isWhitespace(c)) {
                cursor++;
                continue;
            }

            int startPos = cursor;

            if (Character.isDigit(c) || (c == '.' && cursor + 1 < length && Character.isDigit(input.charAt(cursor + 1)))) {
                rawTokens.add(readNumber());
                continue;
            }

            if (Character.isLetter(c)) {
                rawTokens.add(readIdentifierOrFunction());
                continue;
            }

            switch (c) {
                case '+':
                    rawTokens.add(new Token(TokenType.OPERATOR_PLUS, "+", cursor++));
                    break;
                case '-':
                    rawTokens.add(new Token(TokenType.OPERATOR_MINUS, "-", cursor++));
                    break;
                case '*':
                    rawTokens.add(new Token(TokenType.OPERATOR_MULTIPLY, "*", cursor++));
                    break;
                case '/':
                    rawTokens.add(new Token(TokenType.OPERATOR_DIVIDE, "/", cursor++));
                    break;
                case '^':
                    rawTokens.add(new Token(TokenType.OPERATOR_POWER, "^", cursor++));
                    break;
                case '%':
                    rawTokens.add(new Token(TokenType.OPERATOR_MODULO, "%", cursor++));
                    break;
                case '(':
                    rawTokens.add(new Token(TokenType.LPAREN, "(", cursor++));
                    break;
                case ')':
                    rawTokens.add(new Token(TokenType.RPAREN, ")", cursor++));
                    break;
                case ',':
                    rawTokens.add(new Token(TokenType.COMMA, ",", cursor++));
                    break;
                case '?':
                    rawTokens.add(new Token(TokenType.TERNARY_IF, "?", cursor++));
                    break;
                case ':':
                    rawTokens.add(new Token(TokenType.TERNARY_COLON, ":", cursor++));
                    break;
                case '>':
                    if (cursor + 1 < length && input.charAt(cursor + 1) == '=') {
                        rawTokens.add(new Token(TokenType.REL_GTE, ">=", cursor));
                        cursor += 2;
                    } else {
                        rawTokens.add(new Token(TokenType.REL_GT, ">", cursor++));
                    }
                    break;
                case '<':
                    if (cursor + 1 < length && input.charAt(cursor + 1) == '=') {
                        rawTokens.add(new Token(TokenType.REL_LTE, "<=", cursor));
                        cursor += 2;
                    } else {
                        rawTokens.add(new Token(TokenType.REL_LT, "<", cursor++));
                    }
                    break;
                case '=':
                    if (cursor + 1 < length && input.charAt(cursor + 1) == '=') {
                        rawTokens.add(new Token(TokenType.REL_EQ, "==", cursor));
                        cursor += 2;
                    } else {
                        rawTokens.add(new Token(TokenType.REL_EQ, "=", cursor++));
                    }
                    break;
                case '!':
                    if (cursor + 1 < length && input.charAt(cursor + 1) == '=') {
                        rawTokens.add(new Token(TokenType.REL_NEQ, "!=", cursor));
                        cursor += 2;
                    } else {
                        throw new MathParseException("Carácter inesperado '!' en posición " + cursor, cursor);
                    }
                    break;
                default:
                    throw new MathParseException("Símbolo no reconocido '" + c + "' en posición " + cursor, cursor);
            }
        }

        rawTokens.add(new Token(TokenType.EOF, "", cursor));

        return insertImplicitMultiplication(rawTokens);
    }

    private Token readNumber() {
        int start = cursor;
        boolean hasDot = false;
        boolean hasExp = false;

        while (cursor < length) {
            char c = input.charAt(cursor);
            if (Character.isDigit(c)) {
                cursor++;
            } else if (c == '.' && !hasDot && !hasExp) {
                hasDot = true;
                cursor++;
            } else if ((c == 'e' || c == 'E') && !hasExp) {
                if (cursor + 1 < length && (Character.isDigit(input.charAt(cursor + 1)) || input.charAt(cursor + 1) == '+' || input.charAt(cursor + 1) == '-')) {
                    hasExp = true;
                    cursor++;
                    if (input.charAt(cursor) == '+' || input.charAt(cursor) == '-') {
                        cursor++;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        String numStr = input.substring(start, cursor);
        double val = Double.parseDouble(numStr);
        return new Token(TokenType.NUMBER, numStr, val, start);
    }

    private Token readIdentifierOrFunction() {
        int start = cursor;
        while (cursor < length && (Character.isLetterOrDigit(input.charAt(cursor)) || input.charAt(cursor) == '_')) {
            cursor++;
        }

        String word = input.substring(start, cursor).toLowerCase(Locale.ROOT);

        if (word.equals("x")) {
            return new Token(TokenType.VARIABLE_X, "x", start);
        }
        if (word.equals("t")) {
            return new Token(TokenType.VARIABLE_T, "t", start);
        }
        if (word.equals("pi") || word.equals("π")) {
            return new Token(TokenType.NUMBER, "pi", Math.PI, start);
        }
        if (word.equals("e")) {
            return new Token(TokenType.NUMBER, "e", Math.E, start);
        }
        if (word.equals("phi")) {
            return new Token(TokenType.NUMBER, "phi", 1.618033988749895, start);
        }
        if (word.equals("tau")) {
            return new Token(TokenType.NUMBER, "tau", 2.0 * Math.PI, start);
        }

        return new Token(TokenType.FUNCTION, word, start);
    }

    private List<Token> insertImplicitMultiplication(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        if (tokens.isEmpty()) return result;

        for (int i = 0; i < tokens.size() - 1; i++) {
            Token cur = tokens.get(i);
            Token next = tokens.get(i + 1);
            result.add(cur);

            boolean curIsVal = (cur.getType() == TokenType.NUMBER || cur.getType() == TokenType.VARIABLE_X ||
                                cur.getType() == TokenType.VARIABLE_T || cur.getType() == TokenType.RPAREN);

            boolean nextIsVal = (next.getType() == TokenType.NUMBER || next.getType() == TokenType.VARIABLE_X ||
                                 next.getType() == TokenType.VARIABLE_T || next.getType() == TokenType.FUNCTION ||
                                 next.getType() == TokenType.LPAREN);

            if (curIsVal && nextIsVal) {
                // Insert implicit multiplication
                result.add(new Token(TokenType.OPERATOR_MULTIPLY, "*", next.getPosition()));
            }
        }
        result.add(tokens.get(tokens.size() - 1)); // EOF
        return result;
    }
}
