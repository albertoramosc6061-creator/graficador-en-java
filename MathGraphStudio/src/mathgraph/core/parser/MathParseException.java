package mathgraph.core.parser;

public class MathParseException extends Exception {
    private final int position;

    public MathParseException(String message) {
        super(message);
        this.position = -1;
    }

    public MathParseException(String message, int position) {
        super(message + (position >= 0 ? " (en columna " + (position + 1) + ")" : ""));
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
