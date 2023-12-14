package cool.compiler;

public interface ASTVisitor<T> {
    T visit(Program program);
    T visit(ClassDef classDef);
    T visit(Attribute attribute);
    T visit(ClassMethod method);
    T visit(Formal formal);
    T visit(Id id);
    T visit(Type type);
    T visit(IntType intt);
    T visit(StringType str);
    T visit(BoolType bool);
    T visit(Paren paren);
    T visit(UnaryMinus uMinus);
    T visit(PlusMinus plusMinus);
    T visit(MultDiv multDiv);
    T visit(RelationalComp rel);
    T visit(Not not);
    T visit(Assign assign);
    T visit(New neww);
    T visit(Isvoid isvoid);
    T visit(ExplicitDispatch dispatch);
    T visit(SelfDispatch dispatch);
    T visit(If iff);
    T visit(While whilee);
    T visit(Let let);
    T visit(Case casee);
    T visit(CaseBranch caseBranch);
    T visit(Block block);
}
