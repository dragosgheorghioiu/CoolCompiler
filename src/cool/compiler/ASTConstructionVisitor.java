package cool.compiler;

import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;

import java.util.ArrayList;

public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitId(CoolParser.IdContext ctx) {
        return new Id(ctx.ID().getSymbol(), ctx);
    }
    @Override
    public ASTNode visitInt(CoolParser.IntContext ctx) {
        return new IntType(ctx.INT().getSymbol(), ctx);
    }
    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        return new StringType(ctx.STRING().getSymbol(), ctx);
    }
    @Override
    public ASTNode visitBool(CoolParser.BoolContext ctx) {
        return new BoolType(ctx.BOOL().getSymbol(), ctx);
    }
    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        ArrayList<ClassDef> classes = new ArrayList<>();
        for (var child : ctx.classes) {
            var classNode = (ClassDef) visit(child);
            classes.add(classNode);
        }
        return new Program(ctx.start, classes, ctx);
    }
    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        ArrayList<Feature> features = new ArrayList<>();
        for (var feat : ctx.features) {
            var methodNode = (Feature) visit(feat);
            features.add(methodNode);
        }
        if (ctx.parent != null)
            return new ClassDef(ctx.start, new Id(ctx.name, ctx), new Type(ctx.parent, ctx), features, ctx);
        return new ClassDef(ctx.start, new Id(ctx.name, ctx), null, features, ctx);

    }
    @Override
    public ASTNode visitFeature(CoolParser.FeatureContext ctx) {
        if (ctx.method() != null) {
            return visit(ctx.method());
        }
        return visit(ctx.attribute());
    }
    @Override
    public ASTNode visitAttribute(CoolParser.AttributeContext ctx) {
        if (ctx.value != null)
            return new Attribute(ctx.start, new Id(ctx.name, ctx), new Type(ctx.type, ctx), (Expression)visit(ctx.value), ctx);
        return new Attribute(ctx.start, new Id(ctx.name, ctx), new Type(ctx.type, ctx), null, ctx);

    }
    @Override
    public ASTNode visitMethod(CoolParser.MethodContext ctx) {
        ArrayList<Formal> formals = new ArrayList<>();
        for (var formal : ctx.formals) {
            var formalNode = (Formal) visit(formal);
            formals.add(formalNode);
        }
        return new ClassMethod(ctx.start, new Id(ctx.name, ctx), new Type(ctx.type, ctx), formals, (Expression)visit(ctx.method_body), ctx);
    }
    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        return new Formal(ctx.start, new Type(ctx.type, ctx), new Id(ctx.name, ctx), ctx);
    }
    @Override
    public ASTNode visitParen(CoolParser.ParenContext ctx) {
        return new Paren(ctx.start, (Expression)visit(ctx.e), ctx);
    }
    @Override
    public ASTNode visitUnaryMinus(CoolParser.UnaryMinusContext ctx) {
        return new UnaryMinus(ctx.start, (Expression)visit(ctx.expr()), ctx);
    }
    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        return new Not(ctx.start, (Expression)visit(ctx.expr()), ctx);
    }
    @Override
    public ASTNode visitAddSub(CoolParser.AddSubContext ctx) {
        return new PlusMinus(ctx.start, (Expression)visit(ctx.left), (Expression)visit(ctx.right), ctx.op.getText(), ctx);
    }
    @Override
    public ASTNode visitMulDiv(CoolParser.MulDivContext ctx) {
        return new MultDiv(ctx.start, (Expression)visit(ctx.left), (Expression)visit(ctx.right), ctx.op.getText(), ctx);
    }
    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        return new RelationalComp(ctx.start, (Expression)visit(ctx.left), (Expression)visit(ctx.right), new Id(ctx.op, ctx), ctx);
    }
    @Override
    public ASTNode visitAssign(CoolParser.AssignContext ctx) {
        return new Assign(ctx.start, new Id(ctx.name, ctx), (Expression)visit(ctx.e), ctx);
    }
    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        return new New(ctx.start, new Type(ctx.name, ctx), ctx);
    }
    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        return new Isvoid(ctx.start, (Expression)visit(ctx.expr()), ctx);
    }
    @Override
    public ASTNode visitSelfDispatch(CoolParser.SelfDispatchContext ctx) {
        ArrayList<Expression> args = new ArrayList<>();
        for (var arg : ctx.parameters) {
            var argNode = (Expression) visit(arg);
            args.add(argNode);
        }
        return new SelfDispatch(ctx.start, new Id(ctx.method_name, ctx), args, ctx);
    }
    @Override
    public ASTNode visitExplicitDispatch(CoolParser.ExplicitDispatchContext ctx) {
        ArrayList<Expression> args = new ArrayList<>();
        for (var arg : ctx.parameters) {
            var argNode = (Expression) visit(arg);
            args.add(argNode);
        }
        if (ctx.parent != null)
            return new ExplicitDispatch(ctx.start, (Expression) visit(ctx.object), new Type(ctx.parent, ctx),new Id(ctx.method_name, ctx), args, ctx);
        return new ExplicitDispatch(ctx.start, (Expression) visit(ctx.object), null, new Id(ctx.method_name, ctx), args, ctx);
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        return new If(ctx.start, (Expression)visit(ctx.cond), (Expression)visit(ctx.thenBranch), (Expression)visit(ctx.elseBranch), ctx);
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        return new While(ctx.start, (Expression)visit(ctx.cond), (Expression)visit(ctx.body), ctx);
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        ArrayList<Attribute> locals = new ArrayList<>();
        for (var local : ctx.local_vars) {
            var localNode = (Attribute) visit(local);
            locals.add(localNode);
        }
        return new Let(ctx.start, locals, (Expression)visit(ctx.body), ctx);
    }

    @Override
    public ASTNode visitCase_branch(CoolParser.Case_branchContext ctx) {
        return new CaseBranch(ctx.start, new Id(ctx.temp_name, ctx), new Type(ctx.new_type, ctx), (Expression) visit(ctx.body), ctx);
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        ArrayList<CaseBranch> branches = new ArrayList<>();
        for (var branch : ctx.branches) {
            var caseBranchNode = (CaseBranch) visit(branch);
            branches.add(caseBranchNode);
        }
        return new Case(ctx.start, (Expression) visit(ctx.checked_expr), branches, ctx);
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        ArrayList<Expression> expressions = new ArrayList<>();
        for (var expr : ctx.exprs) {
            var exprNode = (Expression) visit(expr);
            expressions.add(exprNode);
        }
        return new Block(ctx.start, expressions, ctx);
    }
}
