package cool.compiler;

public class PrintVisitor implements ASTVisitor<Void> {
    int indent;
    boolean let_local_vars;

    PrintVisitor() {
        indent = 0;
        let_local_vars = false;
    }
    @Override
    public Void visit(Program prog) {
        printIndent("program");
        indent++;
        for (var entry : prog.classes) {
            entry.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(ClassDef classDef) {
        printIndent("class");
        indent++;
        printIndent(classDef.name.token.getText());
        if (classDef.parent != null)
            printIndent(classDef.parent.token.getText());
        for (var feat : classDef.features) {
            feat.accept(this);
        }
        indent--;
        return null;
    }
    @Override
    public Void visit(ClassMethod method) {
        printIndent("method");
        indent++;
        printIndent(method.name.token.getText());
        for (var formal : method.formals) {
            formal.accept(this);
        }
        printIndent(method.returnType.token.getText());
        method.body.accept(this);
        indent--;
        return null;
    }
    @Override
    public Void visit(Attribute attribute) {
        if (!let_local_vars)
            printIndent("attribute");
        else
            printIndent("local");
        indent++;
        printIndent(attribute.name.token.getText());
        printIndent(attribute.type.token.getText());
        if (attribute.value != null)
            attribute.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        paren.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryMinus uMinus) {
        printIndent("~");
        indent++;
        uMinus.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Not not) {
        printIndent("not");
        indent++;
        not.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        printIndent(plusMinus.op);
        indent++;
        plusMinus.left.accept(this);
        plusMinus.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        printIndent(multDiv.op);
        indent++;
        multDiv.left.accept(this);
        multDiv.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(RelationalComp rel) {
        printIndent(rel.op.token.getText());
        indent++;
        rel.left.accept(this);
        rel.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        printIndent("<-");
        indent++;
        printIndent(assign.name.token.getText());
        assign.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(If iff) {
        printIndent("if");
        indent++;
        iff.condition.accept(this);
        iff.then.accept(this);
        iff.elsee.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(While whilee) {
        printIndent("while");
        indent++;
        whilee.condition.accept(this);
        whilee.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Let let) {
        printIndent("let");
        indent++;
        let_local_vars = true;
        for (var local_var : let.local_vars)
            local_var.accept(this);
        let_local_vars = false;
        let.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Id id) {
        printIndent(id.token.getText());
        return null;
    }

    @Override
    public Void visit(Type type) {
        printIndent(type.token.getText());
        return null;
    }

    @Override
    public Void visit(IntType intt) {
        printIndent(intt.token.getText());
        return null;
    }

    @Override
    public Void visit(StringType str) {
        printIndent(str.token.getText());
        return null;
    }

    @Override
    public Void visit(BoolType bool) {
        printIndent(bool.token.getText());
        return null;
    }

    @Override
    public Void visit(Formal formal) {
        printIndent("formal");
        indent++;
        printIndent(formal.name.token.getText());
        printIndent(formal.type.token.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(New neww) {
        printIndent("new");
        indent++;
        printIndent(neww.name.token.getText());
        indent--;
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        printIndent("isvoid");
        indent++;
        isvoid.expr.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(SelfDispatch dispatch) {
        printIndent("implicit dispatch");
        indent++;
        printIndent(dispatch.method_name.token.getText());
        for (var arg : dispatch.parameters) {
            arg.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch dispatch) {
        printIndent(".");
        indent++;
        dispatch.object.accept(this);
        if (dispatch.parent != null)
            dispatch.parent.accept(this);
        printIndent(dispatch.method_name.token.getText());
        for (var arg : dispatch.parameters) {
            arg.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(CaseBranch branch) {
        printIndent("case branch");
        indent++;
        branch.temp_name.accept(this);
        branch.new_type.accept(this);
        branch.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Case casee) {
        printIndent("case");
        indent++;
        casee.checked_expression.accept(this);
        for (var branch : casee.branches)
            branch.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Block block) {
        printIndent("block");
        indent++;
        for (var expr : block.expressions) {
            expr.accept(this);
        }
        indent--;
        return null;
    }

    void printIndent(String str) {
        for (int i = 0; i < indent; i++)
            System.out.print("  ");
        System.out.println(str);
    }
}