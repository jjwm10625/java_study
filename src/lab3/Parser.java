package lab3;// Parser.java
// Parser for language S

public class Parser {
    Token token;          // current token
    Lexer lexer;
    String funId = "";

    public Parser(Lexer scan) {
        lexer = scan;
        token = lexer.getToken(); // get the first token
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
        // <command> ->  <decl> | <function> | <stmt>
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
        // <decl>  -> <type> id [=<expr>];
        Type t = type();
        String id = match(Token.ID);
        Decl d = null;
        if (token == Token.ASSIGN) {
            match(Token.ASSIGN);
            Expr e = expr();
            d = new Decl(id, t, e);
        } else
            d = new Decl(id, t);

        match(Token.SEMICOLON);
        return d;
    }

    private Decls decls() {
        // <decls> -> {<decl>}
        Decls ds = new Decls();
        while (isType()) {
            Decl d = decl();
            ds.add(d);
        }
        return ds;
    }

    private Function function() {
        // <function> -> fun <type> id(<params>) <stmt>
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
        // parse declarations of parameters
        Decls params = new Decls();
        while (isType()) {
            Decl d = decl();
            params.add(d);
        }
        return params;
    }

    private Type type() {
        // <type>  ->  int | bool | void | string
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
        // <stmt> -> <block> | <assignment> | <ifStmt> | <whileStmt> | ...
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
            case IF:    // if statement
                s = ifStmt();
                return s;
            case WHILE:      // while statement
                s = whileStmt();
                return s;
            case ID:    // assignment
                s = assignment();
                return s;
            case LET:    // let statement
                s = letStmt();
                return s;
            case READ:    // read statement
                s = readStmt();
                return s;
            case PRINT:    // print statement
                s = printStmt();
                return s;
            case RETURN: // return statement
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
        // for (<type> id = <expr>; <expr>; id = <expr>) <stmt>
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
        // <block> -> {<stmt>}
        Stmts ss = new Stmts();
        while ((token != Token.RBRACE) && (token != Token.END))
            ss.stmts.add(stmt());
        return ss;
    }

    private Let letStmt() {
        // <letStmt> -> let <decls> in <block> end
        match(Token.LET);
        Decls ds = decls();
        match(Token.IN);
        Stmts ss = stmts();
        match(Token.END);
        match(Token.SEMICOLON);
        return new Let(ds, ss);
    }


    private Read readStmt() {
        // <readStmt> -> read id;
        match(Token.READ);
        Identifier id = new Identifier(match(Token.ID));
        match(Token.SEMICOLON);
        return new Read(id);
    }


    private Print printStmt() {
        // <printStmt> -> print <expr>;
        match(Token.PRINT);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Print(e);
    }


    private Return returnStmt() {
        // <returnStmt> -> return <expr>;
        match(Token.RETURN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Return(funId, e);
    }

    private Stmt assignment() {
        // <assignment> -> id = <expr>;
        Identifier id = new Identifier(match(Token.ID));
        match(Token.ASSIGN);
        Expr e = expr();
        match(Token.SEMICOLON);
        return new Assignment(id, e);
    }

    private If ifStmt() {
        // <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
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
        // <whileStmt> -> while (<expr>) <stmt>
        match(Token.WHILE);
        match(Token.LPAREN);
        Expr condition = expr();
        match(Token.RPAREN);
        Stmt stmt = stmt();
        return new While(condition, stmt);
    }


    private Expr expr() {
        // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false
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

        // parse logical operations
        while (token == Token.AND || token == Token.OR) {
            Operator op = new Operator(match(token));
            Expr right = bexp();
            e = new Binary(op, e, right);
        }
        return e;
    }

    private Expr bexp() {
        // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ]
        Expr e = aexp();

        // parse relational operations
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
        // <aexp> -> <term> { + <term> | - <term> }
        Expr e = term();
        while (token == Token.PLUS || token == Token.MINUS) {
            Operator op = new Operator(match(token));
            Expr t = term();
            e = new Binary(op, e, t);
        }
        return e;
    }

    private Expr term() {
        // <term> -> <factor> { * <factor> | / <factor>}
        Expr t = factor();
        while (token == Token.MULTIPLY || token == Token.DIVIDE) {
            Operator op = new Operator(match(token));
            Expr f = factor();
            t = new Binary(op, t, f);
        }
        return t;
    }

    private Expr factor() {
        // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
        Operator op = null;
        if (token == Token.MINUS)
            op = new Operator(match(Token.MINUS));

        Expr e = null;
        switch (token) {
            case ID:
                Identifier v = new Identifier(match(Token.ID));
                e = v;
                if (token == Token.LPAREN) {  // function call
                    match(Token.LPAREN);
                    Call c = new Call(v, arguments());
                    match(Token.RPAREN);
                    e = c;
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
        else return e;
    }

    private Exprs arguments() {
        // arguments -> [ <expr> {, <expr> } ]
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
                return true;
            default:
                return false;
        }
    }

    public static void main(String args[]) {
        Parser parser;
        Command command;
        if (args.length == 0) {
            System.out.print(">> ");
            Lexer.interactive = true;
            parser = new Parser(new Lexer());
            Sint sint = new Sint();
            State state = new State();


            while (true) {
                if (parser.token == Token.EOF)
                    parser.token = parser.lexer.getToken();

                try {
                    command = parser.command();
                    if (command != null) {
                        command.display(0);

                        command.type = TypeChecker.Check(command);
                        if (command.type != Type.ERROR) {
                            System.out.println("\nInterpreting...");
                            state = sint.Eval(command, state);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }

                System.out.print("\n>> ");
            }
        } else {
            System.out.println("Begin parsing... " + args[0]);
            parser = new Parser(new Lexer(args[0]));
            Sint sint = new Sint();
            State state = new State();

            while (parser.token != Token.EOF) {
                try {
                    command = parser.command();
                    if (command != null) {
                        command.display(0);

                        command.type = TypeChecker.Check(command);
                        if (command.type != Type.ERROR) {
                            System.out.println("\nInterpreting..." + args[0]);
                            state = sint.Eval(command, state);
                        }
                    }
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        }
    }
}