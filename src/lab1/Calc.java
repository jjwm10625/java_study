package lab1;

import java.io.*;

class Calc {
    int token;
    int value;
    int ch;
    private PushbackInputStream inputStream;
    boolean isBooleanExpression = false;

    final int NUMBER = 256, TRUE = 257, FALSE = 258, EQ = 259, NE = 260,
            LT = 261, LE = 262, GT = 263, GE = 264, AND = 265, OR = 266, NOT = 267;

    Calc(PushbackInputStream is) {
        inputStream = is;
    }

    int getToken() {
        while (true) {
            try {
                ch = inputStream.read();
                if (ch == ' ' || ch == '\t' || ch == '\r')
                    continue;
                if (Character.isDigit(ch)) {
                    value = number();
                    inputStream.unread(ch);
                    return NUMBER;
                }
                if (Character.isLetter(ch)) {
                    StringBuilder sb = new StringBuilder();
                    while (Character.isLetter(ch)) {
                        sb.append((char) ch);
                        ch = inputStream.read();
                    }
                    inputStream.unread(ch);
                    String word = sb.toString();
                    if (word.equals("true")) return TRUE;
                    if (word.equals("false")) return FALSE;
                    return -1;
                }
                if (ch == '=') {
                    ch = inputStream.read();
                    if (ch == '=') return EQ;
                    inputStream.unread(ch);
                } else if (ch == '!') {
                    ch = inputStream.read();
                    if (ch == '=') return NE;
                    inputStream.unread(ch);
                    return NOT;
                } else if (ch == '<') {
                    ch = inputStream.read();
                    if (ch == '=') return LE;
                    inputStream.unread(ch);
                    isBooleanExpression = true;
                    return LT;
                } else if (ch == '>') {
                    ch = inputStream.read();
                    if (ch == '=') return GE;
                    inputStream.unread(ch);
                    isBooleanExpression = true;
                    return GT;
                } else if (ch == '&') {
                    ch = inputStream.read();
                    if (ch == '&') return AND;
                    inputStream.unread(ch);
                    return AND;
                } else if (ch == '|') {
                    ch = inputStream.read();
                    if (ch == '|') return OR;
                    inputStream.unread(ch);
                }
                return ch;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number() {
        int result = ch - '0';
        try {
            ch = inputStream.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch - '0';
                ch = inputStream.read();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void match(int c) {
        if (token == c)
            token = getToken();
    }

    void command() {
        if (token == '\n') return;
        isBooleanExpression = false;
        Object result = expr();
        if (token == '\n') {
            System.out.println("The result is: " + (isBooleanExpression ? result : (int) result));
        }
    }

    Object expr() {
        Object result = andExpr();
        while (token == OR) {
            match(OR);
            result = (boolean) result | (boolean) andExpr();
        }
        return result;
    }

    Object andExpr() {
        Object result = notExpr();
        while (token == AND) {
            match(AND);
            result = (boolean) result & (boolean) notExpr();
        }
        return result;
    }

    Object notExpr() {
        if (token == NOT) {
            match(NOT);
            return !(boolean) notExpr();
        }
        return bexp();
    }

    Object bexp() {
        int left = aexp();
        boolean result = false;
        boolean hasRelOp = false;

        while (token == EQ || token == NE || token == LT || token == LE || token == GT || token == GE) {
            int op = token;
            match(token);
            int right = aexp();
            isBooleanExpression = true;
            hasRelOp = true;

            switch (op) {
                case EQ: result = left == right; break;
                case NE: result = left != right; break;
                case LT: result = left < right; break;
                case LE: result = left <= right; break;
                case GT: result = left > right; break;
                case GE: result = left >= right; break;
            }
            left = right;
        }
        return hasRelOp ? result : left;
    }

    int aexp() {
        int result = term();
        while (token == '+' || token == '-') {
            if (token == '+') {
                match('+');
                result += term();
            } else if (token == '-') {
                match('-');
                result -= term();
            }
        }
        return result;
    }

    int term() {
        int result = factor();
        while (token == '*' || token == '/') {
            if (token == '*') {
                match('*');
                result *= factor();
            } else if (token == '/') {
                match('/');
                result /= factor();
            }
        }
        return result;
    }

    int factor() {
        int result;
        boolean negative = false;

        if (token == '-') {
            match('-');
            negative = true;
        }

        if (token == NUMBER) {
            result = value;
            match(NUMBER);
        } else if (token == '(') {
            match('(');
            result = (int) expr();
            match(')');
        } else {
            result = 0;
        }

        return negative ? -result : result;
    }

    void parse() {
        token = getToken();
        command();
    }

    public static void main(String args[]) {
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while (true) {
            System.out.print(">> ");
            calc.parse();
        }
    }
}