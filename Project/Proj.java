import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import java.util.Iterator;

public class Proj {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static String fileName = "";

    private HashMap<String, SymbolTable> symbolTables = new HashMap<String, SymbolTable>();
    private SymbolTable globalSymbolTable;

    public static void main(String args[]) throws ParseException {
        yal2jvm parser;

        if (args.length == 0) {
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " Reading input ...");
            parser = new yal2jvm(System.in);
        } else if (args.length == 1) {
            fileName = args[0];
            System.out.println(
                    ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " Reading the file " + args[0] + " ..." + ANSI_RESET);
            try {
                parser = new yal2jvm(new java.io.FileInputStream(fileName));
            } catch (java.io.FileNotFoundException e) {
                System.out.println(
                        ANSI_CYAN + "yal2jvm:" + ANSI_RED + " The file " + args[0] + " was not found." + ANSI_RESET);
                return;
            }
        } else {
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " You must use one of the following:");
            System.out.println("         java yal2jvm < file");
            System.out.println("Or");
            System.out.println("         java yal2jvm file");
            return;
        }

        new Proj(parser);

    }

    public Proj(yal2jvm parser) {
        try {
            SimpleNode root = parser.Module();
            root.dump("");
            buildSymbolTables(root);
            fillFunctionSymbolTables(root);
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_GREEN + " The input was read sucessfully." + ANSI_RESET);
        } catch (ParseException e) {
            System.out
                    .println(ANSI_CYAN + "yal2jvm:" + ANSI_RED + " There was an error during the parse." + ANSI_RESET);
            System.out.println(e.getMessage());
        } catch (TokenMgrError e) {
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_RED + " There was an error." + ANSI_RESET);
            System.out.println(e.getMessage());
        }
    }

    public void buildSymbolTables(SimpleNode root) {
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;
            this.globalSymbolTable = new SymbolTable();

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTDeclaration) {
                    ASTElement element = (ASTElement) module.jjtGetChild(i).jjtGetChild(0);

                    if (root.jjtGetChild(i).jjtGetNumChildren() == 2) {
                        globalSymbolTable.addVariable(element.name, "array");
                    } else {
                        globalSymbolTable.addVariable(element.name, "int");
                    }
                } else if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);

                    this.symbolTables.put(function.name, new SymbolTable());
                }
            }
            this.symbolTables.put(module.name, this.globalSymbolTable);
        }
    }

    public void fillFunctionSymbolTables(SimpleNode root){
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    SymbolTable functionSymbolTable = this.symbolTables.get(function.name);

                    for (int j = 0; j < function.jjtGetNumChildren(); j++) {

                        //Return Symbol
                        if (function.jjtGetChild(j) instanceof ASTElement) {
                            ASTElement element = (ASTElement) function.jjtGetChild(j);
            
                            if (element.jjtGetNumChildren() == 1) {
                                functionSymbolTable.setReturnSymbol(element.name, "array");
                            } else {
                                functionSymbolTable.setReturnSymbol(element.name, "int");
                            }
                        }
            
                        //Parameters
                        if (function.jjtGetChild(j) instanceof ASTVarlist) {
                            for (int k = 0; k < function.jjtGetChild(j).jjtGetNumChildren(); k++) {
                                ASTElement element = (ASTElement) function.jjtGetChild(j).jjtGetChild(k);
                                if (element.jjtGetNumChildren() == 1) {
                                    functionSymbolTable.addParameter(element.name, "array");
                                } else {
                                    functionSymbolTable.addParameter(element.name, "int");
                                }
                            }
                        }
            
                        //Variables
                        if (function.jjtGetChild(j) instanceof ASTAssign || function.jjtGetChild(j) instanceof ASTWhile
                                || function.jjtGetChild(j) instanceof ASTIf || function.jjtGetChild(j) instanceof ASTElse)
                            saveFunctionVariables(functionSymbolTable, function.jjtGetChild(j));
                    }
                    
                }
            }
        }
        printSymbolTables();
    }

    public void saveFunctionVariables(SymbolTable functionSymbolTable, Node node) {

        if (node instanceof ASTAssign) {
            ASTAssign assign = (ASTAssign) node;
            String name = "";
            String type = "int";

            for (int i = 0; i < assign.jjtGetNumChildren(); i++) {
                if (assign.jjtGetChild(i) instanceof ASTAccess) {
                    ASTAccess access = (ASTAccess) assign.jjtGetChild(i);
                    name = access.name;
                }

                else if (assign.jjtGetChild(i) instanceof ASTRhs) {
                    ASTRhs rhs = (ASTRhs) assign.jjtGetChild(i);

                    for (int j = 0; j < rhs.jjtGetNumChildren(); j++) {
                        if (rhs.jjtGetChild(j) instanceof ASTArraySize) {
                            type = "array";
                        }

                        if (rhs.jjtGetChild(j) instanceof ASTTerm) {
                            ASTTerm term = (ASTTerm) rhs.jjtGetChild(j);

                            if(term.jjtGetNumChildren()>0 && term.jjtGetChild(0) instanceof ASTCall){
                                ASTCall call = (ASTCall) term.jjtGetChild(0);
                                String functionName = call.object;

                                if(this.symbolTables.containsKey(functionName)) //if the functions belongs to this module
                                    type=symbolTables.get(functionName).getReturnSymbol().getType(); //gets that function return type
                                else type = "int"; //otherwise it's int
                            }
                        }
                    }
                }
            }

            if(canAddVariable(functionSymbolTable, name, type))
                functionSymbolTable.addVariable(name, type);

        }

        else if (node instanceof ASTWhile || node instanceof ASTIf || node instanceof ASTElse) {
            SimpleNode simpleNode = (SimpleNode) node;

            for (int i = 0; i < simpleNode.jjtGetNumChildren(); i++) {
                saveFunctionVariables(functionSymbolTable, simpleNode.jjtGetChild(i));
            }
        }

    }

    public boolean canAddVariable(SymbolTable functionSymbolTable, String name, String type){

        if (!this.globalSymbolTable.getVariables().contains(new Symbol(name, type))){//verify if the new symbol isn't on the module symbol table already
            if(!functionSymbolTable.getParameters().contains(new Symbol(name, type))){ //verify if the new symbol isn't on the function's parameters already
                if(functionSymbolTable.getReturnSymbol()!=null){ //if the function returns a symbol
                    if(!functionSymbolTable.getReturnSymbol().equals(new Symbol(name, type))) // if the return symbol isnt't the new one
                    return true;
                }
                else{
                    return true;
                }
            }
        }
        return false;   
    }
    

    public void printSymbolTables() {
        Iterator it = symbolTables.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            System.out.println(" > SCOPE: " + key);

            for (Symbol s : symbolTables.get(key).getParameters()) {
                System.out.println("   - Parameter Symbol: " + s.getName() + " - " + s.getType());
            }

            for (Symbol s : symbolTables.get(key).getVariables()) {
                System.out.println("   - Variable Symbol: " + s.getName() + " - " + s.getType());
            }
            if (symbolTables.get(key).getReturnSymbol() != null)
                System.out.println("   - Return Symbol: " + symbolTables.get(key).getReturnSymbol().getName() + " - "
                        + symbolTables.get(key).getReturnSymbol().getType());
        }
    }
}