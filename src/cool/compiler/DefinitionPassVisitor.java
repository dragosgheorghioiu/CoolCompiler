package cool.compiler;

import cool.structures.*;

import java.util.List;
import java.util.Objects;

public class DefinitionPassVisitor implements ASTVisitor<Void> {
    Scope currentScope = SymbolTable.globals;
    String currentClass = null;

    public Void visit (Program prog) {
        for (var classs : prog.classes) {
            classs.accept(this);
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
        }

        currentClass = id.token.getText();
        for (var feature : classDef.features) {
            feature.accept(this);
        }
        currentClass = null;

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
            SymbolTable.error(id.ctx, id.token, "Class " + currentClass + " redefines attribute " + id.token.getText());
            return null;
        }
        if (id.token.getText().equals("self")) {
            SymbolTable.error(id.ctx, id.token, "Class " + currentClass + " has attribute with illegal name self");
            return null;
        }

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
            SymbolTable.error(id.ctx, id.token, "Class " + currentClass + " redefines method " + id.token.getText());
            return null;
        }

        currentScope = symbol;
        for (var formal : method.formals) {
            var formalSymbol = new IdSymbol(formal.name.token.getText());
            formalSymbol.setType(new ClassSymbol(currentScope, formal.type.token.getText()));
            if (!symbol.add(formalSymbol)) {
                SymbolTable.error(formal.ctx, formal.name.token, "Method " + id.token.getText() + " of class " + currentClass + " redefines formal parameter " + formal.name.token.getText());
            }
            formal.accept(this);
            formal.name.setScope(currentScope);
            formal.name.setSymbol(formalSymbol);
        }
        currentScope = currentScope.getParent();
        id.setScope(currentScope);
        id.setSymbol(symbol);
        return null;
    }

    @Override
    public Void visit(Formal formal)
    {
        var id = formal.name;
        var symbol = new IdSymbol(id.token.getText());
        symbol.setType(new ClassSymbol(currentScope, formal.type.token.getText()));

        if (id.token.getText().equals("self")) {
            SymbolTable.error(id.ctx, id.token, "Method " + currentScope.toString() + " of class " + currentScope.getParent().toString() + " has formal parameter with illegal name self");
            return null;
        }

        id.setScope(currentScope);
        id.setSymbol(symbol);
        return null;
    }

    @Override
    public Void visit(Id id) {
        var symbol = (IdSymbol) currentScope.lookup(id.toString());

        id.setScope(currentScope);

        if (symbol == null) {
            SymbolTable.error(id.ctx, id.token, "Undeclared identifier " + id);
        }

        id.setSymbol(symbol);

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
        return null;
    }

    @Override
    public Void visit(UnaryMinus uMinus) {
        return null;
    }

    @Override
    public Void visit(PlusMinus plusMinus) {
        return null;
    }

    @Override
    public Void visit(MultDiv multDiv) {
        return null;
    }

    @Override
    public Void visit(RelationalComp rel) {
        return null;
    }

    @Override
    public Void visit(Not not) {
        return null;
    }

    @Override
    public Void visit(Assign assign) {
        return null;
    }

    @Override
    public Void visit(New neww) {
        return null;
    }

    @Override
    public Void visit(Isvoid isvoid) {
        return null;
    }

    @Override
    public Void visit(ExplicitDispatch dispatch) {
        return null;
    }

    @Override
    public Void visit(SelfDispatch dispatch) {
        return null;
    }

    @Override
    public Void visit(If iff) {
        return null;
    }

    @Override
    public Void visit(While whilee) {
        return null;
    }

    @Override
    public Void visit(Let let) {
        return null;
    }

    @Override
    public Void visit(Case casee) {
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        return null;
    }

    @Override
    public Void visit(Block block) {
        return null;
    }
}