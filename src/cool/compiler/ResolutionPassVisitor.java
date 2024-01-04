package cool.compiler;

import cool.structures.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResolutionPassVisitor implements  ASTVisitor<ClassSymbol>{

    protected ArrayList<String> getInheritanceChain(IdSymbol currentClass) {
        ArrayList<String> inheritanceChain = new ArrayList<>();
        var copy = currentClass.getType();
        while (copy != null) {
            inheritanceChain.add(copy.getName());
            copy = copy.getParentClassSymbol();
        }
        if (currentClass.isSelfType()) {
            inheritanceChain.add(0, "SELF_TYPE");
        }
        return inheritanceChain;
    }

    protected String getLeastUpperBound(List<List<String>> inheritanceChains) {
        String resultType = null;
        for (var type : inheritanceChains.get(0)) {
            boolean found = true;
            for (var branch : inheritanceChains) {
                if (!branch.contains(type)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                resultType = type;
                break;
            }
        }
        return resultType;
    }

    protected boolean checkValidType(IdSymbol type, String superType, Scope currentScope) {
        if (type.isSelfType() && Objects.equals(superType, "SELF_TYPE")) {
            return true;
        }

        if (Objects.equals(type.getName(), superType)) {
            return true;
        }
        var realType = (IdSymbol) SymbolTable.globals.lookup(type.getName());
        if (!getInheritanceChain(realType).contains(superType)) {
            return false;
        }
        return true;
    }

    protected IdSymbol getSelfType(Scope callerScope) {
        while (callerScope.getParent().getParent() != null) {
            callerScope = callerScope.getParent();
        }
        ClassSymbol callerClass = (ClassSymbol) callerScope;
        IdSymbol symbolToCopy = (IdSymbol) SymbolTable.globals.lookup(callerClass.getName());
        IdSymbol callerClassCopy = new IdSymbol(symbolToCopy);
        callerClassCopy.switchToSelfType();
        callerClassCopy.getType().switchToSelfType();
        return callerClassCopy;
    }
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
//        var symbolcopy = symbol.getType();
//        System.out.println("Id: " + id.token.getText());
//        while (symbolcopy != null) {
//            System.out.println("Symbol: " + symbolcopy.getName());
//            for (var token : symbolcopy.getFeatures().keySet()) {
//                System.out.println(token);
//            }
//            symbolcopy = symbolcopy.getParentClassSymbol();
//        }

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
        var type = attribute.type.token.getText();
        var symbol = (IdSymbol) id.getSymbol();
        var classSymbol = (ClassSymbol) id.getScope();
        var globalScope = SymbolTable.globals;
        IdSymbol realType = (IdSymbol) globalScope.lookup(type);
        if (symbol == null) {
            return null;
        }
        if (!Objects.equals(type, "SELF_TYPE")) {
            if (realType == null) {
                SymbolTable.error(attribute.ctx, attribute.type.token, "Class " + classSymbol.getName() + " has attribute " + id.token.getText() + " with undefined type " + attribute.type.token.getText());
                return null;
            }
            symbol.setType(realType.getType());
        } else {
            symbol.setType(getSelfType(id.getScope()).getType());
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

        if (attribute.value != null) {
            var valueType = attribute.value.accept(this);
            if (valueType == null) {
                return null;
            }
            if (!checkValidType(valueType, attribute.type.token.getText(), attribute.name.getScope())) {
                SymbolTable.error(attribute.ctx, attribute.value.token, "Type " + valueType.getName() + " of initialization expression of attribute " + id.token.getText() + " is incompatible with declared type " + attribute.type.token.getText());
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
        var realBody = (IdSymbol) globalScope.lookup(body.getName());
        if (realBody == null) {
            return null;
        }
        if (body.isSelfType()) {
            realBody.switchToSelfType();
        }
        if (!getInheritanceChain(realBody).contains(returnType.token.getText())) {
            SymbolTable.error(method.ctx, method.body.token, "Type " + body.getName() + " of the body of method " + id.token.getText() + " is incompatible with declared return type " + returnType.token.getText());
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
            var idSymbol = getSelfType(currentScope);
            idSymbol.getType().switchToSelfType();
            return idSymbol.getType();
        }
        if (currentScope == null) {
            return null;
        }
        if (currentScope instanceof MethodSymbol && currentScope.toString().equals("let-" + id.token.getText()) && ((MethodSymbol) currentScope).isLetScope()) {
            currentScope = currentScope.getParent();
        }
        var symbol = (IdSymbol) currentScope.lookup(id.token.getText());
        if (symbol == null) {
            SymbolTable.error(id.ctx, id.token, "Undefined identifier " + id.token.getText());
            return null;
        }
        if (symbol.getType().getName().equals("SELF_TYPE")) {
            var idSymbol = getSelfType(currentScope);
            idSymbol.getType().switchToSelfType();
            return idSymbol.getType();
        }
        var realSymbolType = (IdSymbol) SymbolTable.globals.lookup(symbol.getType().getName());
        if (realSymbolType == null) {
            return null;
        }
        return realSymbolType.getType();
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
            SymbolTable.error(plusMinus.ctx, plusMinus.left.token, "Operand of " + plusMinus.op + " has type " + leftType.getNameOrSelf() + " instead of Int");
            return intBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(plusMinus.ctx, plusMinus.right.token, "Operand of " + plusMinus.op + " has type " + rightType.getNameOrSelf() + " instead of Int");
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
            SymbolTable.error(multDiv.ctx, multDiv.left.token, "Operand of " + multDiv.op + " has type " + leftType.getNameOrSelf() + " instead of Int");
            return intBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(multDiv.ctx, multDiv.right.token, "Operand of " + multDiv.op + " has type " + rightType.getNameOrSelf() + " instead of Int");
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
                SymbolTable.error(rel.ctx, rel.op.token, "Cannot compare " + leftType.getNameOrSelf() + " with " + rightType.getNameOrSelf());
                return boolBasicClass.getType();
            }
            return boolBasicClass.getType();
        }
        if (leftType != null && !Objects.equals(leftType.getName(), "Int")) {
            SymbolTable.error(rel.ctx, rel.left.token, "Operand of " + rel.op.token.getText() + " has type " + leftType.getNameOrSelf() + " instead of Int");
            return boolBasicClass.getType();
        }
        if (rightType != null && !Objects.equals(rightType.getName(), "Int")) {
            SymbolTable.error(rel.ctx, rel.right.token, "Operand of " + rel.op.token.getText() + " has type " + rightType.getNameOrSelf() + " instead of Int");
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
        var id = assign.name.accept(this);
        var expr = assign.value.accept(this);
        if (id == null || expr == null) {
            return null;
        }
        var realId = (IdSymbol) SymbolTable.globals.lookup(id.getName());
        var realExpr = (IdSymbol) SymbolTable.globals.lookup(expr.getName());
        if (id.isSelfType())
            realId.switchToSelfType();

        System.out.println(assign.value.token.getLine() + " " + assign.value.token.getCharPositionInLine());
        System.out.println("real expr: " + realExpr.getName() + " " + realExpr.isSelfType());
        if (expr.isSelfType())
            realExpr.switchToSelfType();

//        SymbolTable.error(assign.ctx, assign.name.token, "Assignment to method " + assign.name.token.getText() + " " + realExpr.getType().isSelfType() + " " + realId.isSelfType());
        if (!checkValidType(realExpr.getType(), realId.getType().getNameOrSelf(), assign.name.getScope())) {
            SymbolTable.error(assign.ctx, assign.value.token, "Type " + expr.getName() + " of assigned expression is incompatible with declared type " + id.getName() + " of identifier " + assign.name.token.getText());
            return null;
        }
        return expr;
    }

    @Override
    public ClassSymbol visit(New neww) {
//        SymbolTable.error(neww.ctx, neww.name.token, neww.name.token.getText());
        var newwType = neww.name.token.getText();
        if (Objects.equals(newwType, "SELF_TYPE")) {
            var idSymbol = getSelfType(neww.scope);
            idSymbol.getType().switchToSelfType();
            return idSymbol.getType();
        }

        var globalScope = SymbolTable.globals;
        var expr = (IdSymbol) globalScope.lookup(newwType);

        if (expr == null) {
            SymbolTable.error(neww.ctx, neww.name.token, "new is used with undefined type " + newwType);
            return null;
        }
        return expr.getType();
    }

    @Override
    public ClassSymbol visit(Isvoid isvoid) {
        var expr = (BoolBasicClass) SymbolTable.globals.lookup("Bool");
        return expr.getType();
    }

    private class DispatchCast {
        public ASTNode dispatch;
        public ASTNode caller;
        public ASTNode method_name;
        public List<Expression> parameters;
        public ParserRuleContext ctx;

        public DispatchCast(ASTNode dispatch, ASTNode caller, ASTNode method_name, List<Expression> parameters, ParserRuleContext ctx) {
            this.dispatch = dispatch;
            this.caller = caller;
            this.method_name = method_name;
            this.parameters = parameters;
            this.ctx = ctx;
        }
    }
    private ClassSymbol dispatchErrorChecking(Symbol searchMethod, DispatchCast dispatchCast, IdSymbol realCaller) {
        if (searchMethod == null) {
            SymbolTable.error(dispatchCast.ctx, dispatchCast.method_name.token, "Undefined method " + dispatchCast.method_name.token.getText() + " in class " + realCaller.getName());
            return null;
        }

        var method = (MethodSymbol) searchMethod;
        if (method.getFormals().size() != dispatchCast.parameters.size()) {
            SymbolTable.error(dispatchCast.ctx, dispatchCast.method_name.token, "Method " + dispatchCast.method_name.token.getText() + " of class " + realCaller.getName() + " is applied to wrong number of arguments");
            return null;
        }
        int i = 0;
        for (var formal : method.getFormals().entrySet()) {
            ClassSymbol formalType = ((IdSymbol) formal.getValue()).getType();
            ClassSymbol param = dispatchCast.parameters.get(i).accept(this);

            if (!checkValidType(param, formalType.getName(), ((Id) dispatchCast.method_name).getScope())) {
                SymbolTable.error(dispatchCast.ctx, dispatchCast.parameters.get(i).token, "In call to method " + dispatchCast.method_name.token.getText() + " of class " + realCaller.getName() + ", actual type " + param.getName() + " of formal parameter " + formal.getKey() + " is incompatible with declared type " + formalType.getName());
            }
            i++;
        }
        return method.getReturnType();
    }

    @Override
    public ClassSymbol visit(ExplicitDispatch dispatch) {
        var caller = dispatch.object.accept(this);
        if (caller == null) {
            return null;
        }

        var realCaller = (IdSymbol) SymbolTable.globals.lookup(caller.getName());
        if (realCaller == null) {
            return null;
        }

        // get static
        if (dispatch.parent != null && !Objects.equals(dispatch.parent.token.getText(), "SELF_TYPE"))  {
            if (SymbolTable.globals.lookup(dispatch.parent.token.getText()) == null) {
                SymbolTable.error(dispatch.ctx, dispatch.parent.token, "Type " + dispatch.parent.token.getText() + " of static dispatch is undefined");
                return null;
            }
            var inheritanceChain = getInheritanceChain(realCaller);
            if (!inheritanceChain.contains(dispatch.parent.token.getText())) {
                SymbolTable.error(dispatch.ctx, dispatch.parent.token, "Type " + dispatch.parent.token.getText() + " of static dispatch is not a superclass of type " + realCaller.getName());
                return null;
            }
            realCaller = (IdSymbol) SymbolTable.globals.lookup(dispatch.parent.token.getText());
        }

        Symbol searchMethod = null;
        var parentCaller = realCaller.getType();
        while (parentCaller != null) {
            searchMethod = parentCaller.lookup(dispatch.method_name.token.getText());
            if (searchMethod != null) {
                break;
            }
            parentCaller = parentCaller.getParentClassSymbol();
        }


        var dispatchCast = new DispatchCast(dispatch, dispatch.object, dispatch.method_name, dispatch.parameters, dispatch.ctx);

        var temp = dispatchErrorChecking(searchMethod, dispatchCast, realCaller);
        var realMethod = (MethodSymbol) searchMethod;
        if (realMethod != null && realMethod.getReturnType().getName().equals("SELF_TYPE"))
            return realCaller.getType();
        return temp;
    }

    @Override
    public ClassSymbol visit(SelfDispatch dispatch) {
        var caller = dispatch.method_name.getScope();
        if (caller == null) {
            return null;
        }
        while (caller.getParent().getParent() != null) {
            caller = caller.getParent();
        }
        ClassSymbol callerClass = (ClassSymbol) caller;
        Symbol searchMethod = null;
        var parentCaller = callerClass;
        while (parentCaller != null) {
            searchMethod = parentCaller.lookup(dispatch.method_name.token.getText());
            if (searchMethod != null) {
                break;
            }
            parentCaller = parentCaller.getParentClassSymbol();
        }
        if (searchMethod == null) {
            SymbolTable.error(dispatch.ctx, dispatch.method_name.token, "Undefined method " + dispatch.method_name.token.getText() + " in class " + callerClass.getName());
            return null;
        }

        var dispatchCast = new DispatchCast(dispatch, dispatch, dispatch.method_name, dispatch.parameters, dispatch.ctx);

        var temp = dispatchErrorChecking(searchMethod, dispatchCast, callerClass);
        var realMethod = (MethodSymbol) searchMethod;
        if (realMethod != null && realMethod.getReturnType().getName().equals("SELF_TYPE"))
            return callerClass.getType();
        return temp;
    }

    @Override
    public ClassSymbol visit(If iff) {
        var objectBasicClass = (ObjectBasicClass) SymbolTable.globals.lookup("Object");
        var expr = iff.condition.accept(this);
        if (expr != null && !Objects.equals(expr.getName(), "Bool")) {
            SymbolTable.error(iff.ctx, iff.condition.token, "If condition has type " + expr.getName() + " instead of Bool");
            return objectBasicClass.getType();
        }
        var thenType = iff.then.accept(this);
        var elseType = iff.elsee.accept(this);

        if (thenType == null || elseType == null) {
            return null;
        }
        var globalScope = SymbolTable.globals;
        IdSymbol realThenType = (IdSymbol) globalScope.lookup(thenType.getName());
        IdSymbol realElseType = (IdSymbol) globalScope.lookup(elseType.getName());

        if (thenType.isSelfType())
            realThenType.switchToSelfType();
        if (elseType.isSelfType())
            realElseType.switchToSelfType();

        var thenInheritanceChain = getInheritanceChain(realThenType);
        var elseInheritanceChain = getInheritanceChain(realElseType);

        List<List<String>> branchTypes = new ArrayList<>();
        branchTypes.add(thenInheritanceChain);
        branchTypes.add(elseInheritanceChain);

//        System.out.println(branchTypes);
        String resultType = getLeastUpperBound(branchTypes);
        if (resultType == null) {
            return objectBasicClass.getType();
        }

        if (Objects.equals(resultType, "SELF_TYPE")) {
            return getSelfType(iff.scope).getType();
        }
        return ((IdSymbol) SymbolTable.globals.lookup(resultType)).getType();
    }

    @Override
    public ClassSymbol visit(While whilee) {
        var expr = whilee.condition.accept(this);
        if (!Objects.equals(expr.getName(), "Bool")) {
            SymbolTable.error(whilee.ctx, whilee.condition.token, "While condition has type " + expr.getName() + " instead of Bool");
        }
        return ((ObjectBasicClass) SymbolTable.globals.lookup("Object")).getType();
    }

    @Override
    public ClassSymbol visit(Let let) {
        for (var local : let.local_vars) {
            var localType = local.type.token.getText();

            if (!Objects.equals(localType, "SELF_TYPE") && SymbolTable.globals.lookup(localType) == null) {
                SymbolTable.error(let.ctx, local.type.token, "Let variable " + local.name.token.getText() + " has undefined type " + localType);
                continue;
            }

            if (local.value == null) {
                continue;
            }
            var localInit = local.value.accept(this);
            if (localInit == null) {
                continue;
            }
            if (!checkValidType(localInit, localType, local.name.getScope())) {
                SymbolTable.error(let.ctx, local.value.token, "Type " + localInit.getName() + " of initialization expression of identifier " + local.name.token.getText() + " is incompatible with declared type " + localType);
            }
        }
//        System.out.println("Let scope: " + let.local_vars.get(0).name.getScope().lookup("z"));
        return let.body.accept(this);
    }

    @Override
    public ClassSymbol visit(Case casee) {
        casee.checked_expression.accept(this);
        List<List<String>> branchTypes = new ArrayList<>();
        for (var branch : casee.branches) {
            var branchType = branch.accept(this);
            var realBranchType = (IdSymbol) SymbolTable.globals.lookup(branchType.getName());
            branchTypes.add(getInheritanceChain(realBranchType));
        }
        String resultType = getLeastUpperBound(branchTypes);

        if (resultType == null) {
            return ((ObjectBasicClass) SymbolTable.globals.lookup("Object")).getType();
        }

        return ((IdSymbol) SymbolTable.globals.lookup(resultType)).getType();
    }

    @Override
    public ClassSymbol visit(CaseBranch caseBranch) {
        var id = caseBranch.temp_name;
        var type = caseBranch.new_type;
        var currentScope = id.getScope();

        if (currentScope != null && currentScope.lookup(type.token.getText()) == null) {
            SymbolTable.error(caseBranch.ctx, type.token, "Case variable " + id.token.getText() + " has undefined type " + type.token.getText());
        }
        return caseBranch.body.accept(this);
    }

    @Override
    public ClassSymbol visit(Block block) {
        ClassSymbol last_expression = null;
        for (var expr : block.expressions) {
            last_expression = expr.accept(this);
        }
        return last_expression;
    }
}
