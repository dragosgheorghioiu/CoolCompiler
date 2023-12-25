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
        IdSymbol realType = (IdSymbol) globalScope.lookup(attribute.type.token.getText());
        if (symbol == null) {
            return null;
        }
        if (realType == null) {
            SymbolTable.error(attribute.ctx, attribute.type.token, "Class " + classSymbol.getName() + " has attribute " + id.token.getText() + " with undefined type " + attribute.type.token.getText());
            return null;
        }
        symbol.setType(realType.getType());

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

        if (attribute.value != null) {
            var valueType = attribute.value.accept(this);
            if (valueType == null) {
                return null;
            }
            if (!Objects.equals(((IdSymbol) id.getSymbol()).getType().getName(), valueType.getName())) {
                SymbolTable.error(attribute.ctx, attribute.type.token,
                        "Type "
                        + valueType.getName()
                        + " of initialization expression of attribute "
                        + id.token.getText()
                        + " is incompatible with declared type "
                        + ((IdSymbol) id.getSymbol()).getType().getName());
                return null;
            }
        }

        return symbol.getType();
    }

    @Override
    public ClassSymbol visit(ClassMethod method) {
        var id = method.name;
        var returnType = method.returnType;
        var symbol = (MethodSymbol) id.getSymbol();
        if (symbol == null) {
            return null;
        }

        var classSymbol = (ClassSymbol) id.getScope();
        var globalScope = id.getScope().getParent();

        if (globalScope.lookup(returnType.token.getText()) == null && !returnType.token.getText().equals("SELF_TYPE")) {
            SymbolTable.error(method.ctx, returnType.token, "Class " + classSymbol.getName() + " has method " + id.token.getText() + " with undefined return type " + returnType.token.getText());
            return null;
        }

        var parent = classSymbol.getParentClassSymbol();
        while (parent != null) {
            var parentMethod = (MethodSymbol) parent.lookup(id.token.getText());
            if (parentMethod == null) {
                parent = parent.getParentClassSymbol();
                continue;
            }
            if (!Objects.equals(parentMethod.getReturnType().getName(), returnType.token.getText())) {
                SymbolTable.error(method.ctx, returnType.token, "Class " + classSymbol.getName() + " overrides method " + id.token.getText() + " but changes return type from " + parentMethod.getReturnType().getName() + " to " + returnType.token.getText());
                return null;
            }
            if (parentMethod.getFormals().size() != method.formals.size()) {
                SymbolTable.error(method.ctx, id.token, "Class " + classSymbol.getName() + " overrides method " + id.token.getText() + " with different number of formal parameters");
                return null;
            }
            var formalSymbols = parentMethod.getFormals();
            int i = 0;
            for (var formal : formalSymbols.entrySet()) {
                var formalSymbol = (IdSymbol) formal.getValue();
                var formalType = formalSymbol.getType();
                var currentFormal = method.formals.get(i);
                var currentFormalType = currentFormal.type.token.getText();

                if (!Objects.equals(formalType.getName(), currentFormalType)) {
                    SymbolTable.error(method.ctx, currentFormal.type.token, "Class " + classSymbol.getName() + " overrides method " + id.token.getText() + " but changes type of formal parameter " + currentFormal.name.token.getText() + " from " + formalType.getName() + " to " + currentFormalType);
                    return null;
                }
                i++;
            }
            return null;
        }

        for (var formal : method.formals) {
            formal.accept(this);
        }

        ClassSymbol body = method.body.accept(this);
        if (body == null) {
            return null;
        }
        if (!Objects.equals(body.getName(), returnType.token.getText())) {
            SymbolTable.error(method.ctx, returnType.token, "Type " + body.getName() + " of method " + id.token.getText() + " is incompatible with declared return type " + returnType.token.getText());
            return null;
        }

        return symbol.getReturnType();
    }

    @Override
    public ClassSymbol visit(Formal formal) {
        var id = formal.name;
        var type = formal.type;
        var currentScope = id.getScope().getParent();

        if (type.token.getText().equals("SELF_TYPE")) {
            SymbolTable.error(formal.ctx, type.token, "Method " + id.getScope().toString() + " of class " + currentScope.toString() + " has formal parameter " + id.token.getText() + " with illegal type SELF_TYPE");
            return null;
        }
        if (currentScope.lookup(type.token.getText()) == null) {
            SymbolTable.error(formal.ctx, type.token, "Method " + id.getScope().toString() + " of class " + currentScope + " has formal parameter " + id.token.getText() + " with undefined type " + type.token.getText());
            return null;
        }
        return null;
    }

    @Override
    public ClassSymbol visit(Id id) {
        var currentScope = id.getScope();
        if (id.token.getText().equals("self")) {
            while (currentScope != null) {
                if (currentScope instanceof ClassSymbol) {
                    break;
                }
                currentScope = currentScope.getParent();
            }
            return ((ClassSymbol) currentScope);
        }

        if (currentScope.toString().equals("let-" + id.token.getText())) {
            currentScope = currentScope.getParent();
        }
        var symbol = (IdSymbol) currentScope.lookup(id.token.getText());
        if (symbol == null) {
            SymbolTable.error(id.ctx, id.token, "Undefined identifier " + id.token.getText());
            return null;
        }

        return symbol.getType();
    }

    @Override
    public ClassSymbol visit(Type type) {
        return null;
    }

    @Override
    public ClassSymbol visit(IntType intt) {
        var expr = (IntBasicClass) SymbolTable.globals.lookup("Int");
        return expr.getType();
    }

    @Override
    public ClassSymbol visit(StringType str) {
        var expr = (StringBasicClass) SymbolTable.globals.lookup("String");
        return expr.getType();
    }

    @Override
    public ClassSymbol visit(BoolType bool) {
        var expr = (BoolBasicClass) SymbolTable.globals.lookup("Bool");
        return expr.getType();
    }

    @Override
    public ClassSymbol visit(Paren paren) {
        return paren.expr.accept(this);
    }

    @Override
    public ClassSymbol visit(UnaryMinus uMinus) {
        var intBasicClass = (IntBasicClass) SymbolTable.globals.lookup("Int");
        var expr = uMinus.expr.accept(this);
        if (!Objects.equals(expr.getName(), "Int")) {
            SymbolTable.error(uMinus.ctx, uMinus.expr.token, "Operand of ~ has type " + expr.getName() + " instead of Int");
            return intBasicClass.getType();
        }
        return intBasicClass.getType();
    }

    @Override
    public ClassSymbol visit(PlusMinus plusMinus) {
        var intBasicClass = (IntBasicClass) SymbolTable.globals.lookup("Int");
        var leftType = plusMinus.left.accept(this);
        var rightType = plusMinus.right.accept(this);
        if (leftType != null && !Objects.equals(leftType.getName(), "Int")) {
            SymbolTable.error(plusMinus.ctx, plusMinus.left.token, "Operand of " + plusMinus.op + " has type " + leftType.getName() + " instead of Int");
            return intBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(plusMinus.ctx, plusMinus.right.token, "Operand of " + plusMinus.op + " has type " + rightType.getName() + " instead of Int");
            return intBasicClass.getType();
        }
        return intBasicClass.getType();
    }

    @Override
    public ClassSymbol visit(MultDiv multDiv) {
        var intBasicClass = (IntBasicClass) SymbolTable.globals.lookup("Int");
        var leftType = multDiv.left.accept(this);
        var rightType = multDiv.right.accept(this);
        if (leftType != null && !Objects.equals(leftType.getName(), "Int")) {
            SymbolTable.error(multDiv.ctx, multDiv.left.token, "Operand of " + multDiv.op + " has type " + leftType.getName() + " instead of Int");
            return intBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(multDiv.ctx, multDiv.right.token, "Operand of " + multDiv.op + " has type " + rightType.getName() + " instead of Int");
            return intBasicClass.getType();
        }
        return intBasicClass.getType();
    }

    @Override
    public ClassSymbol visit(RelationalComp rel) {
        var boolBasicClass = (BoolBasicClass) SymbolTable.globals.lookup("Bool");
        var leftType = rel.left.accept(this);
        var rightType = rel.right.accept(this);
        if (Objects.equals(rel.op.token.getText(), "=")) {
            var validLeftClass = List.of("Int", "String", "Bool");
            if (leftType != null && rightType != null && validLeftClass.contains(leftType.getName()) && !Objects.equals(leftType.getName(), rightType.getName())) {
                SymbolTable.error(rel.ctx, rel.op.token, "Cannot compare " + leftType.getName() + " with " + rightType.getName());
                return boolBasicClass.getType();
            }
            return boolBasicClass.getType();
        }
        if (leftType != null && !Objects.equals(leftType.getName(), "Int")) {
            SymbolTable.error(rel.ctx, rel.left.token, "Operand of " + rel.op.token.getText() + " has type " + leftType.getName() + " instead of Int");
            return boolBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(rel.ctx, rel.right.token, "Operand of " + rel.op.token.getText() + " has type " + rightType.getName() + " instead of Int");
            return boolBasicClass.getType();
        }
        return boolBasicClass.getType();
    }

    @Override
    public ClassSymbol visit(Not not) {
        var boolBasicClass = (BoolBasicClass) SymbolTable.globals.lookup("Bool");
        var expr = not.expr.accept(this);
        if (expr != null && !Objects.equals(expr.getName(), "Bool")) {
            SymbolTable.error(not.ctx, not.expr.token, "Operand of not has type " + expr.getName() + " instead of Bool");
        }
        return boolBasicClass.getType();
    }

    @Override
    public ClassSymbol visit(Assign assign) {
        var objectBasicClass = (ObjectBasicClass) SymbolTable.globals.lookup("Object");
        var declaredType = assign.name.accept(this);
        var valueType = assign.value.accept(this);
        var parentValueType = valueType.getParentClassSymbol();

        if (Objects.equals(declaredType.getName(), valueType.getName())) {
            return objectBasicClass.getType();
        }

        while (parentValueType != null) {
            if (Objects.equals(parentValueType.getName(), declaredType.getName())) {
                return objectBasicClass.getType();
            }
            parentValueType = parentValueType.getParentClassSymbol();
        }

        if (parentValueType == null) {
            SymbolTable.error(assign.ctx, assign.value.token, "Type " + valueType.getName() + " of assigned expression is incompatible with declared type " + declaredType.getName() + " of identifier " + assign.name.token.getText());
        }

        return objectBasicClass.getType();
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
        for (var local : let.local_vars) {
            var localType = local.type.token.getText();

            if (SymbolTable.globals.lookup(localType) == null) {
                SymbolTable.error(let.ctx, local.type.token, "Let variable " + local.name.token.getText() + " has undefined type " + localType);
                return null;
            }

            if (local.value == null) {
                continue;
            }
            var localInit = local.value.accept(this);
            if (localInit != null && !Objects.equals(localInit.getName(), localType)) {
                SymbolTable.error(let.ctx, local.type.token, "Type " + localInit.getName() + " of initialization expression of identifier " + local.name.token.getText() + " is incompatible with declared type " + localType);
                return null;
            }

        }

        return let.body.accept(this);
    }

    @Override
    public ClassSymbol visit(Case casee) {
        var caseType = casee.checked_expression.accept(this);
        for (var branch : casee.branches) {
            var branchType = branch.accept(this);
        }
        return null;
    }

    @Override
    public ClassSymbol visit(CaseBranch caseBranch) {
        var id = caseBranch.temp_name;
        var type = caseBranch.new_type;
        var currentScope = id.getScope();

        if (currentScope != null && currentScope.lookup(type.token.getText()) == null) {
            SymbolTable.error(caseBranch.ctx, type.token, "Case variable " + id.token.getText() + " has undefined type " + type.token.getText());
            return null;
        }
        return null;
    }

    @Override
    public ClassSymbol visit(Block block) {
        return null;
    }
}
