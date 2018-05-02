public class Symbol {
    private String name = null;
    private String type;
    private int register; //if it is -1, means it's a global variable
    private boolean temporary = false;

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type; //"array" or "int"
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

    public boolean getTemporary() {
        return this.temporary;
    }

    public void setRegister(int register) {
        this.register = register;
    }

    public void setTemporary(){
        this.temporary = true;
    }

    public void setPermanent(){
        this.temporary = false;
    }

    public boolean equals(Object symbol) {
        Symbol s = (Symbol) symbol;
        return this.name.equals(s.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }
}