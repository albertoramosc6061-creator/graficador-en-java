package mathgraph.core.parser;

public class Token {
    private final TokenType type;
    private final String text;
    private final double value;
    private final int position;

    public Token(TokenType type, String text, int position) {
        this(type, text, 0.0, position);
    }

    public Token(TokenType type, String text, double value, int position) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.position = position;
    }

    public TokenType getType() { return type; }
    public String getText() { return text; }
    public double getValue() { return value; }
    public int getPosition() { return position; }

    @Override
    public String toString() {
        if (type == TokenType.NUMBER) {
            return String.format("Token(%s, %f)", type, value);
        }
        return String.format("Token(%s, '%s')", type, text);
    }
}
