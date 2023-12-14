package cool.compiler;

import cool.structures.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResolutionPassVisitor implements  ASTVisitor<ClassSymbol>{

    @Override
    public ClassSymbol visit(Program program) {
        for (var classs : program.classes) {
            classs.accept(this);
        }
        return null;
    }

    @Override
    public ClassSymbol visit(ClassDef classDef) {
        var id = classDef.name;
        var symbol = (IdSymbol) id.getSymbol();

        if (symbol == null) {
            return null;
        }

        if (classDef.parent != null) {
            var parentSymbol = (IdSymbol) classDef.name.getScope().lookup(classDef.parent.token.getText());
            if (parentSymbol == null) {
                SymbolTable.error(classDef.ctx, classDef.parent.token, "Class " + classDef.name.token.getText() + " has undefined parent " + classDef.parent.token.getText());
                return null;
            }
        }

        List<String> inheritanceChain = new ArrayList<>();
        var copy = symbol;
        while (copy != null) {
            if (inheritanceChain.contains(copy.getName())) {
                SymbolTable.error(classDef.ctx, classDef.name.token, "Inheritance cycle for class " + classDef.name.token.getText());
                return null;
            }
            inheritanceChain.add(copy.getName());
            copy = (IdSymbol) copy.getType().getParent().lookup(copy.getType().getParentClass());
        }

        for (var feature : classDef.features) {
            feature.accept(this);
        }

        return symbol.getType();
    }

    @Override
    public ClassSymbol visit(Attribute attribute) {
        var id = attribute.name;
        var symbol = (IdSymbol) id.getSymbol();
        var classSymbol = (ClassSymbol) id.getScope();
        var globalScope = SymbolTable.globals;

        if (symbol == null) {
            return null;
        }

        var classSymbolParent = classSymbol;
        while (classSymbolParent.getParentClass() != null) {
            var parent = (IdSymbol) classSymbolParent.getParent().lookup(classSymbolParent.getParentClass());
            if (parent.getType().getFeatures().containsKey(id.token.getText())) {
                SymbolTable.error(attribute.ctx, id.token, "Class " + classSymbol.getName() + " redefines inherited attribute " + id.token.getText());
                return null;
            }
            classSymbolParent = parent.getType();
        }

        if (globalScope.lookup(attribute.type.token.getText()) == null) {
            SymbolTable.error(attribute.ctx, attribute.type.token, "Class " + classSymbol.getName() + " has attribute " + id.token.getText() + " with undefined type " + attribute.type.token.getText());
            return null;
        }

        return null;
    }

    @Override
    public ClassSymbol visit(ClassMethod method) {
        return null;
    }

    @Override
    public ClassSymbol visit(Formal formal) {
        return null;
    }

    @Override
    public ClassSymbol visit(Id id) {
        return null;
    }

    @Override
    public ClassSymbol visit(Type type) {
        return null;
    }

    @Override
    public ClassSymbol visit(IntType intt) {
        return null;
    }

    @Override
    public ClassSymbol visit(StringType str) {
        return null;
    }

    @Override
    public ClassSymbol visit(BoolType bool) {
        return null;
    }

    @Override
    public ClassSymbol visit(Paren paren) {
        return null;
    }

    @Override
    public ClassSymbol visit(UnaryMinus uMinus) {
        return null;
    }

    @Override
    public ClassSymbol visit(PlusMinus plusMinus) {
        return null;
    }

    @Override
    public ClassSymbol visit(MultDiv multDiv) {
        return null;
    }

    @Override
    public ClassSymbol visit(RelationalComp rel) {
        return null;
    }

    @Override
    public ClassSymbol visit(Not not) {
        return null;
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        return null;
    }

    @Override
    public ClassSymbol visit(New neww) {
        return null;
    }

    @Override
    public ClassSymbol visit(Isvoid isvoid) {
        return null;
    }

    @Override
    public ClassSymbol visit(ExplicitDispatch dispatch) {
        return null;
    }

    @Override
    public ClassSymbol visit(SelfDispatch dispatch) {
        return null;
    }

    @Override
    public ClassSymbol visit(If iff) {
        return null;
    }

    @Override
    public ClassSymbol visit(While whilee) {
        return null;
    }

    @Override
    public ClassSymbol visit(Let let) {
        return null;
    }

    @Override
    public ClassSymbol visit(Case casee) {
        return null;
    }

    @Override
    public ClassSymbol visit(CaseBranch caseBranch) {
        return null;
    }

    @Override
    public ClassSymbol visit(Block block) {
        return null;
    }
}
