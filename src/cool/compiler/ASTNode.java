package cool.compiler;
import java.util.List;

import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.Symbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public abstract class ASTNode {
    Token token;
    ParserRuleContext ctx;

    ASTNode(Token token) {
        this.token = token;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Program extends ASTNode {
    CoolParser.ProgramContext ctx;
    List<ClassDef> classes;
    Program(Token token, List<ClassDef> classes, CoolParser.ProgramContext ctx) {
        super(token);
        this.classes = classes;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClassDef extends ASTNode {
    Id name;
    Type parent;
    List<Feature> features;
    ClassDef(Token start, Id name, Type parent, List<Feature> features, CoolParser.ClassContext ctx) {
        super(start);
        this.name = name;
        this.parent = parent;
        this.features = features;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
abstract class Feature extends ASTNode {
    Feature(Token token) {super(token);}
}
abstract class Expression extends ASTNode {
    Expression(Token token) {
        super(token);
    }
}

class ClassMethod extends Feature {
    Id name;
    Type returnType;
    List<Formal> formals;
    Expression body;

    ClassMethod(Token token, Id name, Type returnType, List<Formal> formals, Expression body, CoolParser.MethodContext ctx) {
        super(token);
        this.name = name;
        this.returnType = returnType;
        this.formals = formals;
        this.body = body;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {
    Type type;
    Id name;
    Formal(Token token, Type type, Id name, CoolParser.FormalContext ctx) {
        super(token);
        this.type = type;
        this.name = name;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Attribute extends Feature {
    Type type;
    Id name;
    Expression value;
    Attribute(Token token, Id name, Type type, Expression value, CoolParser.AttributeContext ctx) {
        super(token);
        this.type = type;
        this.name = name;
        this.value = value;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Id extends Expression {
    private Symbol symbol;
    private Scope scope;

    Id(Token token, ParserRuleContext ctx) {
        super(token);
        this.ctx = ctx;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Type extends ASTNode {
    Type(Token token, ParserRuleContext ctx) {
        super(token);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Paren extends Expression {
    Expression expr;
    Paren(Token token, Expression expr, CoolParser.ParenContext ctx) {
        super(token);
        this.expr = expr;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class UnaryMinus extends Expression {
    Expression expr;
    UnaryMinus(Token token, Expression expr, CoolParser.UnaryMinusContext ctx) {
        super(token);
        this.expr = expr;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Not extends Expression {
    Expression expr;
    Not(Token token, Expression expr, CoolParser.NotContext ctx) {
        super(token);
        this.expr = expr;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class PlusMinus extends Expression {
    Expression left;
    Expression right;
    String op;
    PlusMinus(Token token, Expression left, Expression right, String op, CoolParser.AddSubContext ctx) {
        super(token);
        this.left = left;
        this.right = right;
        this.op = op;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class MultDiv extends Expression {
    Expression left;
    Expression right;
    String op;
    MultDiv(Token token, Expression left, Expression right, String op, CoolParser.MulDivContext ctx) {
        super(token);
        this.left = left;
        this.right = right;
        this.op = op;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class RelationalComp extends Expression {
    Expression left;
    Expression right;
    Id op;
    RelationalComp(Token token, Expression left, Expression right, Id op, CoolParser.RelationalContext ctx) {
        super(token);
        this.left = left;
        this.right = right;
        this.op = op;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assign extends Expression {
    Id name;
    Expression value;
    Assign(Token token, Id name, Expression value, CoolParser.AssignContext ctx) {
        super(token);
        this.name = name;
        this.value = value;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IntType extends Expression {
    IntType(Token token, CoolParser.IntContext ctx) {
        super(token);
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class StringType extends Expression {
    StringType(Token token, CoolParser.StringContext ctx) {
        super(token);
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolType extends Expression {
    BoolType(Token token, CoolParser.BoolContext ctx) {
        super(token);
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class New extends Expression {
    Type name;
    New(Token token, Type type, CoolParser.NewContext ctx) {
        super(token);
        this.name = type;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class SelfDispatch extends Expression {
    Id method_name;
    List<Expression> parameters;
    SelfDispatch(Token token, Id method_name, List<Expression> parameters, CoolParser.SelfDispatchContext ctx) {
        super(token);
        this.method_name = method_name;
        this.parameters = parameters;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ExplicitDispatch extends Expression {
    Expression object;
    Type parent;
    Id method_name;
    List<Expression> parameters;

    ExplicitDispatch(Token token, Expression object, Type parent, Id method_name, List<Expression> parameters, CoolParser.ExplicitDispatchContext ctx) {
        super(token);
        this.object = object;
        this.parent = parent;
        this.method_name = method_name;
        this.parameters = parameters;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Isvoid extends Expression {
    Expression expr;
    Isvoid(Token token, Expression expr, CoolParser.IsvoidContext ctx) {
        super(token);
        this.expr = expr;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class If extends Expression {
    Expression condition;
    Expression then;
    Expression elsee;
    Scope scope;
    If(Token token, Expression condition, Expression then, Expression elsee, CoolParser.IfContext ctx) {
        super(token);
        this.condition = condition;
        this.then = then;
        this.elsee = elsee;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class While extends Expression {
    Expression condition;
    Expression body;
    While(Token token, Expression condition, Expression body, CoolParser.WhileContext ctx) {
        super(token);
        this.condition = condition;
        this.body = body;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {
    List<Attribute> local_vars;
    Expression body;

    Let(Token token, List<Attribute>local_vars, Expression body, CoolParser.LetContext ctx) {
        super(token);
        this.local_vars = local_vars;
        this.body = body;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class CaseBranch extends Expression {
    Id temp_name;
    Type new_type;
    Expression body;

    CaseBranch(Token token, Id temp_name, Type new_type, Expression body, CoolParser.Case_branchContext ctx) {
        super(token);
        this.temp_name = temp_name;
        this.new_type = new_type;
        this.body = body;
        this.ctx = ctx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class Case extends Expression {
    Expression checked_expression;
    List<CaseBranch> branches;

    Case(Token token, Expression checked_expression, List<CaseBranch> branches, CoolParser.CaseContext ctx) {
        super(token);
        this.checked_expression = checked_expression;
        this.branches = branches;
        this.ctx = ctx;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {
    List<Expression> expressions;
    Block(Token token, List<Expression> expressions, CoolParser.BlockContext ctx) {
        super(token);
        this.expressions = expressions;
        this.ctx = ctx;
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}



