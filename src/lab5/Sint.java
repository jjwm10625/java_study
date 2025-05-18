
package lab5;

import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) {
        if (c instanceof Decl) {
            Decls decls = new Decls();
            decls.add((Decl) c);
            state = allocate(decls, state);

            Decl d = (Decl) c;
            if (d.expr != null) {
                state = Eval(new Assignment(d.id, d.expr), state);
            }

            return state;
        }

        if (c instanceof Function) {
            Function f = (Function) c;
            state.push(f.id, new Value(f));
            return state;
        }

        if (c instanceof Stmt)
            return Eval((Stmt) c, state);

        throw new IllegalArgumentException("no command");
    }

    State Eval(Stmt s, State state) {
        if (s instanceof Empty) return Eval((Empty)s, state);
        if (s instanceof Assignment) return Eval((Assignment)s, state);
        if (s instanceof If) return Eval((If)s, state);
        if (s instanceof While) return Eval((While)s, state);
        if (s instanceof Stmts) return Eval((Stmts)s, state);
        if (s instanceof Let) return Eval((Let)s, state);
        if (s instanceof Read) return Eval((Read)s, state);
        if (s instanceof Print) return Eval((Print)s, state);
        if (s instanceof Call) return Eval((Call)s, state);
        if (s instanceof Return) return Eval((Return)s, state);
        if (s instanceof DoWhile) return Eval((DoWhile)s, state);
        if (s instanceof For) return Eval((For)s, state);
        if (s instanceof ArrayAssignment) return Eval((ArrayAssignment)s, state);
        if (s instanceof Expr) return state;

        throw new IllegalArgumentException("no statement");
    }

    State Eval(Empty s, State state) {
        return state;
    }

    State Eval(Assignment a, State state) {
        Value v = V(a.expr, state);
        return state.set(a.id, v);
    }

    State Eval(ArrayAssignment a, State state) {
        Value arrayValue = V(a.id, state);
        Value[] elements = arrayValue.arrValue();
        int idx = V(a.index, state).intValue();
        Value val = V(a.expr, state);
        elements[idx] = val;
        return state;
    }

    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) {
            int i = sc.nextInt();
            state.set(r.id, new Value(i));
        }
        if (r.id.type == Type.BOOL) {
            boolean b = sc.nextBoolean();
            state.set(r.id, new Value(b));
        }
        return state;
    }

    State Eval(Print p, State state) {
        System.out.println(V(p.expr, state));
        return state;
    }

    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
            if (s instanceof Return)
                return state;
        }
        return state;
    }

    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue())
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }

    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue())
            return Eval(l, Eval(l.stmt, state));
        else
            return state;
    }

    State Eval(DoWhile dw, State state) {
        do {
            state = Eval(dw.stmt, state);
        } while (V(dw.expr, state).boolValue());
        return state;
    }

    State Eval(For f, State state) {
        Decls decls = new Decls();
        decls.add(f.decl);
        state = allocate(decls, state);
        state = Eval(new Assignment(f.decl.id, f.decl.expr), state);

        while (V(f.condition, state).boolValue()) {
            state = Eval(f.stmt, state);
            state = Eval(f.update, state);
        }

        state = free(decls, state);
        return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);

        for (Decl d : l.decls) {
            if (d.expr != null) {
                s = Eval(new Assignment(d.id, d.expr), s);
            }
        }

        s = Eval(l.stmts, s);

        for (Decl d : l.decls) {
            int outerIdx = state.lookup(d.id);
            int innerIdx = s.lookup(d.id);
            if (outerIdx != -1 && innerIdx != -1) {
                Value v = s.get(innerIdx).val;
                state.set(d.id, v);
            }
        }

        return free(l.decls, s);
    }

    State allocate(Decls ds, State state) {
        if (ds != null) {
            for (Decl d : ds) {
                if (d.arraysize > 0) {
                    Value[] arr = new Value[d.arraysize];
                    for (int i = 0; i < d.arraysize; i++) {
                        arr[i] = new Value(d.type);
                    }
                    state.push(d.id, new Value(arr));
                } else {
                    state.push(d.id, new Value(d.type));
                }
            }
        }
        return state;
    }

    State free(Decls ds, State state) {
        if (ds != null) {
            for (int i = ds.size() - 1; i >= 0; i--) {
                Decl d = ds.get(i);
                int index = state.lookup(d.id);
                if (index != -1) {
                    state.remove(index);
                }
            }
        }
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef, "reference to undef value");
        switch (op.val) {
            case "+": return new Value(v1.intValue() + v2.intValue());
            case "-": return new Value(v1.intValue() - v2.intValue());
            case "*": return new Value(v1.intValue() * v2.intValue());
            case "/": return new Value(v1.intValue() / v2.intValue());
            case ">": return new Value(v1.intValue() > v2.intValue());
            case "<": return new Value(v1.intValue() < v2.intValue());
            case ">=": return new Value(v1.intValue() >= v2.intValue());
            case "<=": return new Value(v1.intValue() <= v2.intValue());
            case "==": return new Value(v1.intValue() == v2.intValue());
            case "!=": return new Value(v1.intValue() != v2.intValue());
            case "&": return new Value(v1.boolValue() && v2.boolValue());
            case "|": return new Value(v1.boolValue() || v2.boolValue());
            default: throw new IllegalArgumentException("no operation");
        }
    }

    Value unaryOperation(Operator op, Value v) {
        check(!v.undef, "reference to undef value");
        switch (op.val) {
            case "!": return new Value(!v.boolValue());
            case "-": return new Value(-v.intValue());
            default: throw new IllegalArgumentException("no operation: " + op.val);
        }
    }

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) return (Value) e;
        if (e instanceof Identifier) return (Value) state.get((Identifier) e);
        if (e instanceof Array) {
            Array arr = (Array) e;
            Value[] elements = V(arr.id, state).arrValue();
            int idx = V(arr.expr, state).intValue();
            return elements[idx];
        }
        if (e instanceof Binary) return binaryOperation(((Binary)e).op, V(((Binary)e).expr1, state), V(((Binary)e).expr2, state));
        if (e instanceof Unary) return unaryOperation(((Unary)e).op, V(((Unary)e).expr, state));
        if (e instanceof Call) return V((Call)e, state);
        throw new IllegalArgumentException("no operation");
    }

    State Eval(Expr e, State state) {
        V(e, state);
        return state;
    }

    Value V(Call c, State state) {
        Value v = state.get(c.fid);
        if (v == null || v.funValue() == null) {
            throw new IllegalArgumentException("undefined function: " + c.fid);
        }

        Function f = v.funValue();

        state = newFrame(state, c, f);
        state = Eval(f.stmt, state);
        Value returnValue = state.get(new Identifier("return"));
        state = deleteFrame(state, c, f);

        return returnValue;
    }

    State Eval(Return r, State state) {
        Value v = V(r.expr, state);
        return state.set(new Identifier("return"), v);
    }

    State newFrame(State state, Call c, Function f) {
        if (f.params.size() != c.args.size()) {
            throw new IllegalArgumentException("parameter count mismatch for function: " + f.id);
        }

        Value[] evaluatedArgs = new Value[c.args.size()];
        for (int i = 0; i < c.args.size(); i++) {
            evaluatedArgs[i] = V(c.args.get(i), state);
        }

        state.push(new Identifier("return"), null);

        for (int i = 0; i < f.params.size(); i++) {
            state.push(f.params.get(i).id, evaluatedArgs[i]);
        }

        return state;
    }

    State deleteFrame(State state, Call c, Function f) {
        for (int i = 0; i < f.params.size(); i++) {
            state.pop();
        }
        state.pop();
        return state;
    }


    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a .s source file to interpret.");
            return;
        }

        String sourceFile = args[0];
        System.out.println("Begin parsing... " + sourceFile);

        Parser parser = new Parser(new Lexer(sourceFile));
        Sint sint = new Sint();
        Command command;

        while (parser.token != Token.EOF) {
            try {
                command = parser.command();
                if (command == null) break;

                System.out.println("Interpreting..." + sourceFile);

                command.type = TypeChecker.Check(command);
                state = sint.Eval(command, state);

            } catch (Exception e) {
                System.err.println("Runtime error: " + e.getMessage());
            }
        }
    }
}
