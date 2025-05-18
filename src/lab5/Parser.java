package lab5;// Parser.java

public class Parser {
    Token token;
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) {
        lexer = scan;
        token = lexer.getToken();
    }

    private String match(Token t) {
        String value = token.value();
        if (token == t)
            token = lexer.getToken();
        else
            error(t);
        return value;
    }

    private void error(Token tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token = lexer.getToken();
    }

    private void error(String tok) {
        System.err.println("Syntax error: " + tok + " --> " + token);
        token = lexer.getToken();
    }

    public Command command() {
        if (token == Token.FUN) {
            Function f = function();
            return f;
        }

        if (isType()) {
            Decl d = decl();
            return d;
        }

        if (token != Token.EOF) {
            Stmt s = stmt();
            return s;
        }

        return null;
    }

    private Decl decl() {
        Type t = type();
        String id = match(Token.ID);
        Decl d = null;

        if (token == Token.LBRACKET) {
            match(Token.LBRACKET);
            String sizeStr = match(Token.NUMBER);
            int size = Integer.parseInt(sizeStr);
            match(Token.RBRACKET);
            d = new Decl(id, t, size);
        } else if (token == Token.ASSIGN) {
            match(Token.ASSIGN);
            Expr e = expr();
            d = new Decl(id, t, e);
        } else {
            d = new Decl(id, t);
        }

        match(Token.SEMICOLON);
        return d;
    }

    private Decl paramDecl() {
        Type t = type();
        String id = match(Token.ID);
        return new Decl(id, t);
    }

    private Decls decls() {
        Decls ds = new Decls();
        while (isType()) {
            Decl d = decl();
            ds.add(d);
        }
        return ds;
    }

    private Function function() {
        match(Token.FUN);
        Type t = type();
        String str = match(Token.ID);
        funId = str;
        Function f = new Function(str, t);
        match(Token.LPAREN);
        if (token != Token.RPAREN) {
            f.params = params();
        }
        match(Token.RPAREN);
        Stmt s = stmt();
        f.stmt = s;
        return f;
    }

    private Decls params() {
        Decls params = new Decls();
        params.add(paramDecl());
        while (token == Token.COMMA) {
            match(Token.COMMA);
            params.add(paramDecl());
        }
        return params;
    }

    private Type type() {
        Type t = null;
        switch (token) {
            case INT:
                t = Type.INT;
                break;
            case BOOL:
                t = Type.BOOL;
                break;
            case VOID:
                t = Type.VOID;
                break;
            case STRING:
                t = Type.STRING;
                break;
            default:
                error("int | bool | void | string");
        }
        match(token);
        return t;
    }

    private Stmt stmt() {
        Stmt s = new Empty();
        switch (token) {
            case SEMICOLON:
                match(token.SEMICOLON);
                return s;
            case LBRACE:
                match(Token.LBRACE);
                s = stmts();
                match(Token.RBRACE);
                return s;
            case IF:
                s = ifStmt();
                return s;
            case WHILE:
                s = whileStmt();
                return s;
            case ID:
                s = assignment();
                return s;
            case LET:
                s = letStmt();
                return s;
            case READ:
                s = readStmt();
                return s;
            case PRINT:
                s = printStmt();
                return s;
            case RETURN:
                s = returnStmt();
                return s;
            case DO:
                s = doWhileStmt();
                return s;
            case FOR:
                s = forStmt();
                return s;
            default:
                error("Illegal stmt");
                return null;
        }
    }

    private Stmt doWhileStmt() {
        match(Token.DO);
        Stmt body = stmt();
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr condition = expr();
        match(Token.RPAREN);
        match(Token.SEMICOLON);

        Stmts stmts = new Stmts();
        stmts.stmts.add(body);
        stmts.stmts.add(new While(condition, body));
        return stmts;
    }

    private Stmt forStmt() {
        match(Token.FOR);
        match(Token.LPAREN);

        Type type = type();
        String id = match(Token.ID);
        match(Token.ASSIGN);
        Expr initExpr = expr();
        Decl decl = new Decl(id, type, initExpr);

        match(Token.SEMICOLON);
        Expr condition = expr();
        match(Token.SEMICOLON);

        Identifier updateId = new Identifier(match(Token.ID));
        match(Token.ASSIGN);
        Expr updateExpr = expr();
        Assignment update = new Assignment(updateId, updateExpr);

        match(Token.RPAREN);
        Stmt body = stmt();

        Stmts whileBody = new Stmts();
        whileBody.stmts.add(body);
        whileBody.stmts.add(update);

        While whileStmt = new While(condition, whileBody);
        Stmts stmts = new Stmts();
        stmts.stmts.add(whileStmt);

        return new Let(new Decls(decl), stmts);
    }

    private Stmts stmts() {
        Stmts ss = new Stmts();
        while ((token != Token.RBRACE) && (token != Token.END))
            ss.stmts.add(stmt());
        return ss;
    }

    private Let letStmt() {
        match(Token.LET);
        Decls ds = decls();
        match(Token.IN);
        Stmts ss = stmts();
        match(Token.END);
        match(Token.SEMICOLON);
        return new Let(ds, ss);
    }

    private Read readStmt() {
        match(Token.READ);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Read(id);
    }

    private Print printStmt() {
        match(Token.PRINT);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Print(e);
    }

    private Return returnStmt() {
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }

    private Stmt assignment() {
        Identifier id = new Identifier(match(Token.ID));
        if (token == Token.LBRACKET) {
            match(Token.LBRACKET);
            Expr index = expr();
            match(Token.RBRACKET);
            match(Token.ASSIGN);
            Expr value = expr();
            match(Token.SEMICOLON);
            return new ArrayAssignment(id, index, value);
        } else {
            match(Token.ASSIGN);
            Expr value = expr();
            match(Token.SEMICOLON);
            return new Assignment(id, value);
        }
    }

    private If ifStmt() {
        match(Token.IF);
        match(Token.LPAREN);
        Expr e = expr();
        match(Token.RPAREN);
        match(Token.THEN);
        Stmt s1 = stmt();
        Stmt s2 = new Empty();
        if (token == Token.ELSE) {
            match(Token.ELSE);
            s2 = stmt();
        }
        return new If(e, s1, s2);
    }

    private While whileStmt() {
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr condition = expr();
        match(Token.RPAREN);
        Stmt stmt = stmt();
        return new While(condition, stmt);
    }

    private Expr expr() {
        switch (token) {
            case NOT:
                Operator op = new Operator(match(token));
                Expr e = expr();
                return new Unary(op, e);
            case TRUE:
                match(Token.TRUE);
                return new Value(true);
            case FALSE:
                match(Token.FALSE);
                return new Value(false);
        }

        Expr e = bexp();
        while (token == Token.AND || token == Token.OR) {
            Operator op = new Operator(match(token));
            Expr right = bexp();
            e = new Binary(op, e, right);
        }
        return e;
    }

    private Expr bexp() {
        Expr e = aexp();
        if (token == Token.EQUAL || token == Token.NOTEQ ||
                token == Token.LT || token == Token.LTEQ ||
                token == Token.GT || token == Token.GTEQ) {
            Operator op = new Operator(match(token));
            Expr right = aexp();
            e = new Binary(op, e, right);
        }
        return e;
    }

    private Expr aexp() {
        Expr e = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Operator op = new Operator(match(token));
            Expr t = term();
            e = new Binary(op, e, t);
        }
        return e;
    }

    private Expr term() {
        Expr t = factor();
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));
            Expr f = factor();
            t = new Binary(op, t, f);
        }
        return t;
    }

    private Expr factor() {
        Operator op = null;
        if (token == Token.MINUS)
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch (token) {
            case ID:
                Identifier id = new Identifier(match(Token.ID));
                if (token == Token.LBRACKET) {
                    match(Token.LBRACKET);
                    Expr index = expr();
                    match(Token.RBRACKET);
                    e = new Array(id, index);
                } else if (token == Token.LPAREN) {
                    match(Token.LPAREN);
                    Call c = new Call(id, arguments());
                    match(Token.RPAREN);
                    e = c;
                } else {
                    e = id;
                }
                break;
            case NUMBER:
            case STRLITERAL:
                e = literal();
                break;
            case LPAREN:
                match(Token.LPAREN);
                e = aexp();
                match(Token.RPAREN);
                break;
            default:
                error("Identifier | Literal");
        }

        if (op != null)
            return new Unary(op, e);
        else
            return e;
    }

    private Exprs arguments() {
        Exprs es = new Exprs();
        while (token != Token.RPAREN) {
            es.add(expr());
            if (token == Token.COMMA)
                match(Token.COMMA);
            else if (token != Token.RPAREN)
                error("Exprs");
        }
        return es;
    }

    private Value literal() {
        String s = null;
        switch (token) {
            case NUMBER:
                s = match(Token.NUMBER);
                return new Value(Integer.parseInt(s));
            case STRLITERAL:
                s = match(Token.STRLITERAL);
                return new Value(s);
        }
        throw new IllegalArgumentException("no literal");
    }

    private boolean isType() {
        switch (token) {
            case INT:
            case BOOL:
            case STRING:
            case VOID:
                return true;
            default:
                return false;
        }
    }
}
