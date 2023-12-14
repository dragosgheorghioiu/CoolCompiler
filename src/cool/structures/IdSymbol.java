package cool.structures;

public class IdSymbol extends FeatureSymbol {
    protected ClassSymbol type;

    public IdSymbol(String name) {
        super(name);
    }

    public void setType(ClassSymbol classs) {
        if (type != null) {
            throw new RuntimeException("Type already set");
        }
        this.type = classs;
    }

    public ClassSymbol getType() {
        return type;
    }
}
