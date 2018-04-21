import java.util.HashSet;

public class SymbolTable {
    private HashSet<Symbol> parameters;
    private HashSet<Symbol> variables;
    private Symbol returnSymbol = null;

    public SymbolTable() {
        this.parameters = new HashSet<Symbol>();
        this.variables = new HashSet<Symbol>();
    }

    public HashSet<Symbol> getParameters() {
        return this.parameters;
    }

    public HashSet<Symbol> getVariables() {
        return this.variables;
    }

    public Symbol getReturnSymbol() {
        return this.returnSymbol;
    }

    public void setReturnSymbol(String name, String type) {
        this.returnSymbol = new Symbol(name, type);
    }

    public void addParameter(String name, String type) {
        Symbol s = new Symbol(name, type);
        this.parameters.add(s);
    }

    public void addVariable(String name, String type) {
        Symbol s = new Symbol(name, type);
        this.variables.add(s);
    }
}