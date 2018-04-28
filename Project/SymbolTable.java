import java.util.LinkedHashMap;

public class SymbolTable {
    private LinkedHashMap<String, Symbol> parameters;
    private LinkedHashMap<String, Symbol> variables;
    private Symbol returnSymbol = null;

    public SymbolTable() {
        this.parameters = new LinkedHashMap<String, Symbol>();
        this.variables = new LinkedHashMap<String, Symbol>();
    }

    public LinkedHashMap<String, Symbol> getParameters() {
        return this.parameters;
    }

    public LinkedHashMap<String, Symbol> getVariables() {
        return this.variables;
    }

    public Symbol getReturnSymbol() {
        return this.returnSymbol;
    }

    public void setReturnSymbol(String name, String type, int register) {
        Symbol s = new Symbol(name, type, register);
        this.returnSymbol = s;
    }

    public boolean addParameter(String name, String type, int register) {

        if(this.parameters.containsKey(name)){
            return false;
        }
        else{
            Symbol s = new Symbol(name, type, register);
            this.parameters.put(name, s);
            return true;
        }
       
    }

    public boolean addVariable(String name, String type, int register) {

        if(this.variables.containsKey(name)){
            return false;
        }
        else{
            Symbol s = new Symbol(name, type, register);
            this.variables.put(name, s);
            return true;
        }
       

    }

    public Symbol getFromAll(String name){

        if(this.returnSymbol!= null && name.equals(this.returnSymbol.getName()))
            return this.returnSymbol;
        else if(this.variables.get(name) != null)
            return this.variables.get(name);
        else if(this.parameters.get(name) != null)
            return this.parameters.get(name);
        else return null;
    }

}