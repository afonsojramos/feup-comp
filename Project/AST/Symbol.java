package AST;

public class Symbol {
    private String name = null;
    private String type;
    private int register;
    private boolean init = false;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type; //"array" or "int"
    }

    public Symbol(String name, String type, boolean init) {
        this.name = name;
        this.type = type; //"array" or "int"
        this.init = init;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public int getRegister() {
        return this.register;
    }

    public boolean getInit() {
        return this.init;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setInit(){
        this.init = true;
    }

    public void setNotInit(){
        this.init = false;
    }

    public boolean equals(Object symbol) {
        Symbol s = (Symbol) symbol;
        return this.name.equals(s.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }
}