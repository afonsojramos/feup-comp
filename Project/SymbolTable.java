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

    public void setReturnSymbol(String name, String type, int register) {
        Symbol s = new Symbol(name, type);
        s.setRegister(register);
        this.returnSymbol = s;
    }

    public boolean addParameter(String name, String type, int register) {
        Symbol s = new Symbol(name, type);
        if(this.parameters.add(s)){
            s.setRegister(register);
            return true;
        }
        else return false;
    }

    public boolean addVariable(String name, String type, int register) {
        Symbol s = new Symbol(name, type);
        if(this.variables.add(s)){
            s.setRegister(register);
            return true;
        }
        else return false;
    }
}