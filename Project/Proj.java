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
            fillSymbolTables(root);
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

    public void fillSymbolTables(SimpleNode root) {
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;
            SymbolTable globalSymbolTable = new SymbolTable();

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTDeclaration) {
                    ASTElement element = (ASTElement) module.jjtGetChild(i).jjtGetChild(0);

                    if (root.jjtGetChild(i).jjtGetNumChildren() == 2) {
                        globalSymbolTable.addVariable(element.name, "array");
                    } else {
                        globalSymbolTable.addVariable(element.name, "int");
                    }
                } else if (module.jjtGetChild(i) instanceof ASTFunction) {

                }
            }
            this.symbolTables.put(module.name, globalSymbolTable);
        }
        printSymbolTables();
    }

    public void printSymbolTables() {
        Iterator it = symbolTables.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            System.out.println("Scope: " + key);

            for (Symbol s : symbolTables.get(key).getParameters()) {
                System.out.println("Parameter Symbol: " + s.getName() + " - " + s.getType());
            }

            for (Symbol s : symbolTables.get(key).getVariables()) {
                System.out.println("Variable Symbol: " + s.getName() + " - " + s.getType());
            }

            System.out.println("Return Symbol: " + symbolTables.get(key).getReturnSymbol());
        }
    }
}//Look aheads