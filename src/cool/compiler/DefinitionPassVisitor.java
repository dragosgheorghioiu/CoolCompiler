package cool.compiler;

import cool.structures.*;

import java.util.List;
import java.util.Objects;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = SymbolTable.globals;

    public Void visit (Program prog) {
        for (var classs : prog.classes) {
            classs.accept(this);
        }
        if (SymbolTable.globals.lookup("Main") == null) {
            SymbolTable.error(prog.classes.get(0).ctx, prog.classes.get(0).token, "No method main in class Main");
            return null;
        }
        if (((IdSymbol) SymbolTable.globals.lookup("Main")).getType().lookupMethod("main") == null) {
            SymbolTable.error(prog.classes.get(mainClass).ctx, prog.classes.get(mainClass).token, "No method main in class Main");
            return null;
        }
        return null;
    }

    @Override
    public Void visit(ClassDef classDef) {
        List<String> invalidParents = List.of("Int", "String", "Bool", "SELF_TYPE");
        var id = classDef.name;

        var symbol = new IdSymbol(id.token.getText());
        symbol.setType(new ClassSymbol(currentScope, id.token.getText()));
        currentScope = symbol.getType();

        if (!currentScope.getParent().add(symbol))
        {
            SymbolTable.error(id.ctx, id.token, "Class " + id.token.getText() + " is redefined");
            currentScope = currentScope.getParent();
            return null;
        }
        if (Objects.equals(id.token.getText(), "SELF_TYPE")) {
            SymbolTable.error(id.ctx, id.token, "Class has illegal name SELF_TYPE");
            currentScope = currentScope.getParent();
            return null;
        }
        if (classDef.parent != null) {
            for (var invalidParent : invalidParents) {
                if (Objects.equals(classDef.parent.token.getText(), invalidParent)) {
                    SymbolTable.error(classDef.ctx, classDef.parent.token, "Class " + id.token.getText() + " has illegal parent " + invalidParent);
                    currentScope = currentScope.getParent();
                    return null;
                }
            }
            symbol.getType().setParentClass(classDef.parent.token.getText());
        } else {
            symbol.getType().setParentClass("Object");
        }

        for (var feature : classDef.features) {
            feature.accept(this);
        }

        currentScope = currentScope.getParent();


        classDef.name.setScope(currentScope);
        classDef.name.setSymbol(symbol);

        return null;
    }

    @Override
    public Void visit(Attribute attribute) {
        var id = attribute.name;
        var symbol = new IdSymbol(id.token.getText());

        symbol.setType(new ClassSymbol(currentScope, attribute.type.token.getText()));
        if (!currentScope.add(symbol)) {
            SymbolTable.error(id.ctx, id.token, "Class " + currentScope + " redefines attribute " + id.token.getText());
            return null;
        }
        if (id.token.getText().equals("self")) {
            SymbolTable.error(id.ctx, id.token, "Class " + currentScope + " has attribute with illegal name self");
            return null;
        }

        if (attribute.value != null)
            attribute.value.accept(this);

        id.setScope(currentScope);
        id.setSymbol(symbol);
        return null;
    }

    @Override
    public Void visit(ClassMethod method) {
        var id = method.name;
        var symbol = new MethodSymbol(currentScope, id.token.getText());
        symbol.setReturnType(new ClassSymbol(currentScope, method.returnType.token.getText()));

        if (!currentScope.add(symbol)) {
            SymbolTable.error(id.ctx, id.token, "Class " + currentScope + " redefines method " + id.token.getText());
            return null;
        }

        currentScope = symbol;
        for (var formal : method.formals) {
           formal.accept(this);
        }
        method.body.accept(this);
        currentScope = currentScope.getParent();
        id.setScope(currentScope);
        id.setSymbol(symbol);
        return null;
    }

    @Override
    public Void visit(Formal formal)
    {
        var formalSymbol = new IdSymbol(formal.name.token.getText());
        formalSymbol.setType(new ClassSymbol(currentScope, formal.type.token.getText()));
        var id = formal.name;
        var symbol = (MethodSymbol) currentScope;

        if (!symbol.add(formalSymbol)) {
            SymbolTable.error(formal.ctx, formal.name.token, "Method " + symbol + " of class " + currentScope.getParent() + " redefines formal parameter " + formal.name.token.getText());
        }
        if (id.token.getText().equals("self")) {
            SymbolTable.error(id.ctx, id.token, "Method " + currentScope + " of class " + currentScope.getParent() + " has formal parameter with illegal name self");
        }
        formal.name.setScope(currentScope);
        formal.name.setSymbol(formalSymbol);
        return null;
    }

    @Override
    public Void visit(Id id) {
        id.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(Type type) {
        return null;
    }

    @Override
    public Void visit(IntType intt) {
        return null;
    }

    @Override
    public Void visit(StringType str) {
        return null;
    }

    @Override
    public Void visit(BoolType bool) {
        return null;
    }

    @Override
    public Void visit(Paren paren) {
        paren.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(UnaryMinus uMinus) {
        uMinus.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        plusMinus.left.accept(this);
        plusMinus.right.accept(this);
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        multDiv.left.accept(this);
        multDiv.right.accept(this);
        return null;
    }

    @Override
    public Void visit(RelationalComp rel) {
        rel.left.accept(this);
        rel.right.accept(this);
        return null;
    }

    @Override
    public Void visit(Not not) {
        not.expr.accept(this);
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        if (assign.name.token.getText().equals("self")) {
            SymbolTable.error(assign.ctx, assign.name.token, "Cannot assign to self");
        }
        assign.name.accept(this);
        assign.value.accept(this);
        return null;
    }

    @Override
    public Void visit(New neww) {
        neww.scope = currentScope;
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch dispatch) {
        dispatch.object.accept(this);
        if (dispatch.parent != null) {
            if (dispatch.parent.token.getText().equals("SELF_TYPE")) {
                SymbolTable.error(dispatch.ctx, dispatch.parent.token, "Type of static dispatch cannot be SELF_TYPE");
            } else {
                dispatch.parent.accept(this);
            }
        }
        dispatch.parameters.forEach(p -> p.accept(this));
        dispatch.method_name.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(SelfDispatch dispatch) {
        dispatch.parameters.forEach(p -> p.accept(this));
        dispatch.method_name.setScope(currentScope);
        return null;
    }

    @Override
    public Void visit(If iff) {
        currentScope = new MethodSymbol(currentScope, "if");
        iff.condition.accept(this);
        iff.then.accept(this);
        iff.elsee.accept(this);
        currentScope = currentScope.getParent();
        return null;

    }

    @Override
    public Void visit(While whilee) {
        currentScope = new MethodSymbol(currentScope, "while");
        whilee.condition.accept(this);
        whilee.body.accept(this);
        currentScope = currentScope.getParent();
        return null;
    }

    @Override
    public Void visit(Let let) {
        for (var localVar : let.local_vars) {
            var localVarSymbol = new IdSymbol(localVar.name.token.getText());
            localVarSymbol.setType(new ClassSymbol(currentScope, localVar.type.token.getText()));
            var letScope = new MethodSymbol(currentScope, "let-" + localVar.name.token.getText());
            letScope.setLetScope(true);
            currentScope = letScope;
            if (localVar.name.token.getText().equals("self")) {
                SymbolTable.error(localVar.name.ctx, localVar.name.token, "Let variable has illegal name self");
            }
            if (localVar.value != null)
                localVar.value.accept(this);
            currentScope.add(localVarSymbol);
            localVar.name.setScope(currentScope);
            localVar.name.setSymbol(localVarSymbol);
        }
        currentScope = new MethodSymbol(currentScope, "let-body");

        let.body.accept(this);

        currentScope = currentScope.getParent();

        for (var localVar : let.local_vars) {
            currentScope = currentScope.getParent();
        }

        return null;
    }

    @Override
    public Void visit(Case casee) {
        casee.checked_expression.accept(this);
        for (var branch : casee.branches) {
            branch.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        var symbol = new IdSymbol(caseBranch.temp_name.token.getText());
        symbol.setType(new ClassSymbol(currentScope, caseBranch.new_type.token.getText()));

        if (caseBranch.temp_name.token.getText().equals("self")) {
            SymbolTable.error(caseBranch.ctx, caseBranch.temp_name.token, "Case variable has illegal name self");
            return null;
        }

        if (caseBranch.new_type.token.getText().equals("SELF_TYPE")) {
            SymbolTable.error(caseBranch.ctx, caseBranch.new_type.token, "Case variable " + caseBranch.temp_name.token.getText() + " has illegal type SELF_TYPE");
            return null;
        }

        currentScope = symbol.getType();
        caseBranch.body.accept(this);
        currentScope = currentScope.getParent();

        caseBranch.temp_name.setScope(currentScope);
        caseBranch.temp_name.setSymbol(symbol);
        return null;
    }

    @Override
    public Void visit(Block block) {
        for (var expr : block.expressions) {
            expr.accept(this);
        }
        return null;
    }
}
