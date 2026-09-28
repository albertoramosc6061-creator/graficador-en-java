package mathgraph.core.parser;

public enum TokenType {
    NUMBER,
    VARIABLE_X,
    VARIABLE_T,
    OPERATOR_PLUS,
    OPERATOR_MINUS,
    OPERATOR_MULTIPLY,
    OPERATOR_DIVIDE,
    OPERATOR_POWER,
    OPERATOR_MODULO,
    LPAREN,
    RPAREN,
    COMMA,
    FUNCTION,
    TERNARY_IF,
    TERNARY_COLON,
    REL_GT,
    REL_LT,
    REL_GTE,
    REL_LTE,
    REL_EQ,
    REL_NEQ,
    EOF
}
