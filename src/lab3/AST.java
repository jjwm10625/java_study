package lab3;// AST.java
// AST for S

import java.util.ArrayList;

class Indent {
    public static void display(int level, String s) {
        StringBuilder tab = new StringBuilder();

        for (int i = 0; i < level * 8; i++) {
            tab.append(" ");
        }

        System.out.println(tab.toString() + s);
    }
}

abstract class Command {
    // Command = Decl | Function | Stmt
    Type type =Type.UNDEF;
    public void display(int l) {}
}

class Decls extends ArrayList<Decl> {
    // Decls = Decl*

    Decls() { super(); };
    Decls(Decl d) {
        this.add(d);
    }
}

class Decl extends Command {
    // Decl = Type type; Identifier id; Value expr;
    Identifier id;
    Expr expr = null;
    int arraysize = 0;

    Decl(String s, Type t) {
        id = new Identifier(s); type = t;
    } // declaration

    Decl(String s, Type t, int n) {
        id = new Identifier(s); type = t;
        arraysize = n;
    } // array declaration

    Decl(String s, Type t, Expr e) {
        id = new Identifier(s); type = t; expr = e;
    } // declaration

    @Override
    public void display(int level) {
        Indent.display(level, "Decl");
        Indent.display(level + 1, "Type: " + type.toString());
        id.display(level + 1);
        if (expr != null) {
            expr.display(level + 1);
        }
    }
}


class Functions extends ArrayList<Function> {
    // Functions = Function*
}

class Function extends Command  {
    // Function = Type type; Identifier id; Decls params; Stmt stmt
    Identifier id;
    Decls params;
    Stmt stmt;

    Function(String s, Type t) {
        id = new Identifier(s); type = t; params = null; stmt = null;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Function");
        id.display(level + 1); // 함수 이름 출력
        if (params != null) {
            Indent.display(level + 1, "Params:");
            for (Decl param : params) {
                param.display(level + 2); // 각 파라미터 출력
            }
        }
        stmt.display(level + 1); // 함수 본문 출력
    }

    @Override
    public String toString() {
        return id.toString() + params.toString();
    }
}

class Type {
    // Type = int | bool | string | fun | array | except | void
    final static Type INT = new Type("int");
    final static Type BOOL = new Type("bool");
    final static Type STRING = new Type("string");
    final static Type VOID = new Type("void");
    final static Type FUN = new Type("fun");
    final static Type ARRAY = new Type("array");
    final static Type EXC = new Type("exc");
    final static Type RAISEDEXC = new Type("raisedexc");
    final static Type UNDEF = new Type("undef");
    final static Type ERROR = new Type("error");

    protected String id;
    protected Type(String s) { id = s; }
    public String toString ( ) { return id; }
}

class ProtoType extends Type {
    // defines the type of a function and its parameters
    Type result;
    Decls params;
    ProtoType (Type t, Decls ds) {
        super(t.id);
        result = t;
        params = ds;
    }
}

abstract class Stmt extends Command {
    // Stmt = Empty | Stmts | Assignment | If  | While | Let | Read | Print

}

class Empty extends Stmt {

}

class Stmts extends Stmt {
    // Stmts = Stmt*
    public ArrayList<Stmt> stmts = new ArrayList<Stmt>();

    Stmts() {
        super();
    }

    Stmts(Stmt s) {
        stmts.add(s);
    }

    @Override
    public void display(int level) {
        for (Stmt stmt : stmts) {
            stmt.display(level);
        }
    }
}

class Assignment extends Stmt {
    // Assignment = Identifier id; Expr expr
    Identifier id;
    Expr expr;

    Assignment (Identifier t, Expr e) {
        id = t;
        expr = e;
    }

    public void display(int level) {
        Indent.display(level, "Assignment");
        id.display(level+1);
        expr.display(level+1);
    }
}

class If extends Stmt {
    // If = Expr expr; Stmt stmt1, stmt2;
    Expr expr;
    Stmt stmt1, stmt2;

    If (Expr t, Stmt tp) {
        expr = t; stmt1 = tp; stmt2 = new Empty();
    }

    If (Expr t, Stmt tp, Stmt ep) {
        expr = t; stmt1 = tp; stmt2 = ep;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "If");
        expr.display(level + 1);
        stmt1.display(level + 1);
        stmt2.display(level + 1);
    }
}

class While extends Stmt {
    Expr expr;
    Stmt stmt;

    While (Expr t, Stmt b) {
        expr = t; stmt = b;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "While");
        expr.display(level + 1);
        stmt.display(level + 1);
    }
}

class Let extends Stmt {
    Decls decls;
    Functions funs;
    Stmts stmts;

    Let(Decls ds, Stmts ss) {
        decls = ds;
        funs = null;
        stmts = ss;
    }

    Let(Decls ds, Functions fs, Stmts ss) {
        decls = ds;
        funs = fs;
        stmts = ss;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Let");
        Indent.display(level + 1, "Decls");
        for (Decl d : decls) {
            d.display(level + 2);
        }
        Indent.display(level + 1, "Stmts");
        stmts.display(level + 2);
    }
}

class Read extends Stmt {
    Identifier id;

    Read (Identifier v) {
        id = v;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Read");
        id.display(level + 1);
    }
}

class Print extends Stmt {
    Expr expr;

    Print(Expr e) {
        expr = e;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Print");
        expr.display(level + 1);
    }
}

class Return extends Stmt {
    Identifier fid;
    Expr expr;

    Return (String s, Expr e) {
        fid = new Identifier(s);
        expr = e;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Return");
        fid.display(level + 1);
        expr.display(level + 1);
    }
}

class Try extends Stmt {
    Identifier eid;
    Stmt stmt1;
    Stmt stmt2;

    Try(Identifier id, Stmt s1, Stmt s2) {
        eid = id;
        stmt1 = s1;
        stmt2 = s2;
    }
}

class Raise extends Stmt {
    Identifier eid;

    Raise(Identifier id) {
        eid = id;
    }
}

class Exprs extends ArrayList<Expr> {
}

abstract class Expr extends Stmt {
}

class Call extends Expr {
    Identifier fid;
    Exprs args;

    Call(Identifier id, Exprs a) {
        fid = id;
        args = a;
    }
}

class Identifier extends Expr {
    private String id;

    Identifier(String s) {
        id = s;
    }

    public String toString() {
        return id;
    }

    public boolean equals(Object obj) {
        String s = ((Identifier) obj).id;
        return id.equals(s);
    }

    @Override
    public void display(int level) {
        Indent.display(level, "Identifier: " + id);
    }
}

class Array extends Expr {
    Identifier id;
    Expr expr = null;

    Array(Identifier s, Expr e) {id = s; expr = e;}

    public String toString( ) { return id.toString(); }

    public boolean equals (Object obj) {
        String s = ((Array) obj).id.toString();
        return id.equals(s);
    }
}

class Value extends Expr {
    protected boolean undef = true;
    Object value = null;
    Type type;

    Value(Type t) {
        type = t;
        if (type == Type.INT) value = Integer.valueOf(0);
        if (type == Type.BOOL) value = Boolean.valueOf(false);
        if (type == Type.STRING) value = "";
        undef = false;
    }

    Value(Object v) {
        if (v instanceof Integer) type = Type.INT;
        if (v instanceof Boolean) type = Type.BOOL;
        if (v instanceof String) type = Type.STRING;
        if (v instanceof Function) type = Type.FUN;
        if (v instanceof Value[]) type = Type.ARRAY;
        value = v; undef = false;
    }

    Object value() { return value; }

    @Override
    public void display(int level) {
        Indent.display(level, "Value: " + value);
    }

    int intValue() {
        if (value instanceof Integer)
            return ((Integer) value).intValue();
        else return 0;
    }

    boolean boolValue() {
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue();
        else return false;
    }

    String stringValue() {
        if (value instanceof String)
            return (String) value;
        else return "";
    }

    Function funValue() {
        if (value instanceof Function)
            return (Function) value;
        else return null;
    }

    Value[] arrValue() {
        if (value instanceof Value[])
            return (Value[]) value;
        else return null;
    }

    Type type() { return type; }

    @Override
    public String toString() {
        if (type == Type.INT) return "" + intValue();
        if (type == Type.BOOL) return "" + boolValue();
        if (type == Type.STRING) return "" + stringValue();
        if (type == Type.FUN) return "" + funValue();
        if (type == Type.ARRAY) return "" + arrValue();
        return "undef";
    }
}

class Binary extends Expr {
    Operator op;
    Expr expr1, expr2;

    Binary (Operator o, Expr e1, Expr e2) {
        op = o; expr1 = e1; expr2 = e2;
    }

    public void display(int level) {
        Indent.display(level, "Binary");
        Indent.display(level + 1, "Operator: " + op.toString());
        expr1.display(level+1);
        expr2.display(level+1);
    }
}

class Unary extends Expr {
    Operator op;
    Expr expr;

    Unary (Operator o, Expr e) {
        op = o;
        expr = e;
    }
}

class Operator {
    String val;

    Operator (String s) {
        val = s;
    }

    public String toString( ) {
        return val;
    }

    public boolean equals(Object obj) {
        return val.equals(obj);
    }
}

class DoWhile extends Stmt {
    Stmt stmt;
    Expr expr;

    DoWhile(Stmt stmt, Expr expr) {
        this.stmt = stmt;
        this.expr = expr;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "DoWhile");
        stmt.display(level + 1);
        expr.display(level + 1);
    }
}

class For extends Stmt {
    Decl decl;
    Expr condition;
    Assignment update;
    Stmt stmt;

    For(Decl decl, Expr condition, Assignment update, Stmt stmt) {
        this.decl = decl;
        this.condition = condition;
        this.update = update;
        this.stmt = stmt;
    }

    @Override
    public void display(int level) {
        Indent.display(level, "For");
        Indent.display(level + 1, "Initialization");
        decl.display(level + 2);
        Indent.display(level + 1, "Condition");
        condition.display(level + 2);
        Indent.display(level + 1, "Update");
        update.display(level + 2);
        Indent.display(level + 1, "Body");
        stmt.display(level + 2);
    }
}