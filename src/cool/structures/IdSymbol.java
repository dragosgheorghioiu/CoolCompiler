package cool.structures;

public class IdSymbol extends FeatureSymbol {
    protected ClassSymbol type;

    public IdSymbol(String name) {
        super(name);
    }

    public void setType(ClassSymbol classs) {
        this.type = classs;
    }

    public ClassSymbol getType() {
        return type;
    }
}
