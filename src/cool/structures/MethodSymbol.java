package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class MethodSymbol extends FeatureSymbol implements Scope {
    protected ClassSymbol returnType;
    protected Map<String , Symbol> formals = new LinkedHashMap<>();
    protected Scope parent;

    public MethodSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }

    public ClassSymbol getReturnType() {
        return returnType;
    }
    public void setReturnType(ClassSymbol returnType) {
        if (this.returnType != null) {
            throw new RuntimeException("Return type already set");
        }
        this.returnType = returnType;
    }

    @Override
    public boolean add(Symbol sym) {
        if (formals.containsKey(sym.getName()))
            return false;

        formals.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String s) {
        var sym = formals.get(s);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(s);

        return null;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    public Map<String, Symbol> getFormals() {
        return formals;
    }
}
