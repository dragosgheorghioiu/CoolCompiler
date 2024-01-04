package cool.structures;

public class IdSymbol extends FeatureSymbol {
    protected ClassSymbol type;
    private boolean isSelfType = false;

    public IdSymbol(String name) {
        super(name);
    }

    public IdSymbol(IdSymbol id) {
        this(id.getName());
        this.type = new ClassSymbol(id.getType());
    }

    public void setType(ClassSymbol classs) {
        this.type = classs;
    }

    public ClassSymbol getType() {
        return type;
    }
    public void switchToSelfType() {
        isSelfType = true;
    }
    public boolean isSelfType() {
        return isSelfType;
    }

    public String getNameOrSelf() {
        if (isSelfType)
            return "SELF_TYPE";
        return getName();
    }
}
