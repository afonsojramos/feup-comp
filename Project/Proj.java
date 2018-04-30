import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import java.util.Iterator;

public class Proj {

    public static String ANSI_RESET = "";
    public static String ANSI_RED = "";
    public static String ANSI_GREEN = "";
    public static String ANSI_CYAN = "";
    public static String ANSI_YELLOW = "";

    public static String fileName = "";

    private HashMap<String, SymbolTable> symbolTables = new HashMap<String, SymbolTable>();
    private String moduleName;
    private int registerCounter = 0;

    public static void main(String args[]) throws ParseException {
        yal2jvm parser;

        if (args.length == 0) {
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " Reading input ...");
            parser = new yal2jvm(System.in);
        } else if (args.length == 1 || args.length == 2) {
            if (args[1].toString().equals("color")){
                ANSI_RESET = "\u001B[0m";
                ANSI_RED = "\u001B[31m";
                ANSI_GREEN = "\u001B[32m";
                ANSI_CYAN = "\u001B[36m";
                ANSI_YELLOW = "\u001B[33m";
            }
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
            yalToJasmin(root);
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
            this.moduleName = module.name;
            SymbolTable globalSymbolTable = new SymbolTable();

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {

                //declarations
                if (module.jjtGetChild(i) instanceof ASTDeclaration) {
                    ASTElement element = (ASTElement) module.jjtGetChild(i).jjtGetChild(0);

                    if (globalSymbolTable.getFromAll(element.name) != null && (globalSymbolTable.getAcessType(element.name) == "array" && module.jjtGetChild(i).jjtGetNumChildren() == 2))
                        printSemanticError(element.name, element.line, "Redefinition of global variable.");
                    else if (globalSymbolTable.getFromAll(element.name) != null && (globalSymbolTable.getAcessType(element.name) == "int" && module.jjtGetChild(i).jjtGetNumChildren() != 2))
                        printSemanticError(element.name, element.line, "Redefinition of global variable.");
                    else {
                        if (module.jjtGetChild(i).jjtGetNumChildren() == 2) {
                            globalSymbolTable.addVariable(element.name, "array", -1);
                        } else {
                            globalSymbolTable.addVariable(element.name, "int", -1);
                        }
                    }
                    //functions
                } else if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);

                    if (this.symbolTables.get(function.name) == null)
                        this.symbolTables.put(function.name, new SymbolTable());
                    else {
                        printSemanticError(function.name, function.line, "Duplicate function.");
                    }
                }
            }
            this.symbolTables.put(module.name, globalSymbolTable);
        }
    }

    public void fillFunctionSymbolTables(SimpleNode root) {
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    SymbolTable functionSymbolTable = this.symbolTables.get(function.name);

                    System.out.println("FUNCTION - > " + function.name); //Debug

                    if(function.name.equals("main"))
                        this.registerCounter = 1;
                    else this.registerCounter = 0;

                    for (int j = 0; j < function.jjtGetNumChildren(); j++) {

                        //Return Symbol
                        if (function.jjtGetChild(j) instanceof ASTElement) {
                            ASTElement element = (ASTElement) function.jjtGetChild(j);

                            if (element.jjtGetNumChildren() == 1)
                                functionSymbolTable.setReturnSymbol(element.name, "array", this.registerCounter++);
                            else
                                functionSymbolTable.setReturnSymbol(element.name, "int", this.registerCounter++);
                        }

                        //Parameters
                        if (function.jjtGetChild(j) instanceof ASTVarlist) {
                            for (int k = 0; k < function.jjtGetChild(j).jjtGetNumChildren(); k++) {
                                ASTElement element = (ASTElement) function.jjtGetChild(j).jjtGetChild(k);
                                if (element.jjtGetNumChildren() == 1) {
                                    if (functionSymbolTable.addParameter(element.name, "array", this.registerCounter))
                                        this.registerCounter++;
                                } else {
                                    if (functionSymbolTable.addParameter(element.name, "int", this.registerCounter))
                                        this.registerCounter++;
                                }

                            }
                        }

                        //Variables
                        if (function.jjtGetChild(j) instanceof ASTAssign || function.jjtGetChild(j) instanceof ASTWhile || function.jjtGetChild(j) instanceof ASTIf || function.jjtGetChild(j) instanceof ASTElse)
                            saveFunctionVariables(functionSymbolTable, function.jjtGetChild(j));

                        //Additional Semantic Analysis
                        if (function.jjtGetChild(j) instanceof ASTCall)
                            argumentsAnalysis(functionSymbolTable, function.jjtGetChild(j));
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
            boolean arrayIndex = false;

            for (int i = 0; i < assign.jjtGetNumChildren(); i++) {
                if (assign.jjtGetChild(i) instanceof ASTAccess) {
                    ASTAccess access = (ASTAccess) assign.jjtGetChild(i);
                    name = access.name;

                    for (int j = 0; j < access.jjtGetNumChildren(); j++) { //In Array Accesses an int must be inside the brackets
                        if (access.jjtGetChild(j) instanceof ASTArrayAccess) {
                            ASTArrayAccess arrayAccess = (ASTArrayAccess) access.jjtGetChild(j);

                            for (int k = 0; k < arrayAccess.jjtGetNumChildren(); k++) {
                                if (arrayAccess.jjtGetChild(k) instanceof ASTIndex) {
                                    ASTIndex index = (ASTIndex) arrayAccess.jjtGetChild(k);
                                    
                                    arrayIndex = true;
        
                                    if (index.value.isEmpty()){ //Case of VARIABLE has "name" but does not have "value"
                                        if (functionSymbolTable.getFromAll(index.name) == null && symbolTables.get(moduleName).getFromAll(index.name) == null)
                                            printSemanticError(index.name, index.line, "Undefined variable.");
                                        else if (functionSymbolTable.getAcessType(index.name) != "int") //Variable must represent an int
                                            printSemanticError(index.name, index.line, "Type mismatch.");
                                    }
                                }
                            }
                        }
                    }
                }

                else if (assign.jjtGetChild(i) instanceof ASTRhs) {
                    ASTRhs rhs = (ASTRhs) assign.jjtGetChild(i);

                    for (int j = 0; j < rhs.jjtGetNumChildren(); j++) {
                        if (rhs.jjtGetChild(j) instanceof ASTArraySize) { 
                            ASTArraySize arraySize = (ASTArraySize) rhs.jjtGetChild(j);

                            if (arrayIndex)
                                printSemanticError(arraySize.name, arraySize.line, "Undefined variable.");
                           
                            if (functionSymbolTable.getAcessType(name) == "int") //Variable previously defined as integer
                                printSemanticError(arraySize.name, arraySize.line, "Variable previously defined.");

                            type = "array";

                            if (arraySize.value.isEmpty()){ //Case of VARIABLE has "name" but does not have "value"
                                if (functionSymbolTable.getFromAll(arraySize.name) == null && symbolTables.get(moduleName).getFromAll(arraySize.name) == null)
                                    printSemanticError(arraySize.name, arraySize.line, "Undefined variable.");
                                else if (functionSymbolTable.getAcessType(arraySize.name) != "int") //Variable must represent an int
                                    printSemanticError(arraySize.name, arraySize.line, "Type mismatch.");
                            }
                        }

                        if (rhs.jjtGetChild(j) instanceof ASTTerm) {
                            ASTTerm term = (ASTTerm) rhs.jjtGetChild(j);

                            if (term.jjtGetNumChildren() > 0 && term.jjtGetChild(0) instanceof ASTCall) {
                                ASTCall call = (ASTCall) term.jjtGetChild(0);

                                if (this.symbolTables.get(this.moduleName) != null && this.symbolTables.get(call.function) != null && this.symbolTables.get(call.function).getReturnSymbol() != null && (this.symbolTables.get(this.moduleName).getAcessType(name) == "int" || functionSymbolTable.getAcessType(name) == "int") && this.symbolTables.get(call.function).getReturnSymbol().getType() == "array"){
                                    printSemanticError(call.function, call.line, "Function type mismatch.");
                                }

                                argumentsAnalysis(functionSymbolTable, call);

                                if (this.symbolTables.get(call.function) != null){
                                    if (this.symbolTables.get(call.function).getReturnSymbol() != null)
                                        if (call.module.equals("")) //if the functions belongs to this module
                                            type = this.symbolTables.get(call.function).getReturnSymbol().getType(); //gets that function return type
                                        else
                                            type = "int"; //otherwise it's int
                                }
                               
                            } else {
                                for (int k = 0; k < term.jjtGetNumChildren(); k++) {

                                    if (term.jjtGetChild(k) instanceof ASTAccess) {
                                        ASTAccess access = (ASTAccess) term.jjtGetChild(k);
                                        boolean deepAccess = false;

                                        if (functionSymbolTable.getFromAll(access.name) == null && symbolTables.get(moduleName).getFromAll(access.name) == null)
                                            printSemanticError(access.name, access.line, "Variable not previously defined.");

                                        for (int l = 0; l < access.jjtGetNumChildren(); l++) {
                                            if (access.jjtGetChild(l) instanceof ASTArrayAccess) {
                                                ASTArrayAccess arrayAccess = (ASTArrayAccess) access.jjtGetChild(l);
                                                deepAccess = true;
                    
                                                for (int m = 0; m < arrayAccess.jjtGetNumChildren(); m++) {
                                                    if (arrayAccess.jjtGetChild(m) instanceof ASTIndex) {
                                                        ASTIndex index = (ASTIndex) arrayAccess.jjtGetChild(m);
                                                                                    
                                                        if (index.value.isEmpty()){ //Case of VARIABLE has "name" but does not have "value"
                                                            if (functionSymbolTable.getFromAll(index.name) == null && symbolTables.get(moduleName).getFromAll(index.name) == null)
                                                                printSemanticError(index.name, index.line, "Undefined variable.");
                                                            else if (functionSymbolTable.getAcessType(index.name) != "int") //Variable must represent an int
                                                                printSemanticError(index.name, index.line, "Type mismatch.");
                                                        }
                                                    }
                                                }
                                            }
                                            if (access.jjtGetChild(l) instanceof ASTSizeAccess) 
                                                deepAccess = true;
                                        }

                                        if (functionSymbolTable.getAcessType(access.name) != type && !deepAccess)
                                            printSemanticError(access.name, access.line, "Type mismatch.");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (canAddVariable(functionSymbolTable, name, type, this.registerCounter)) {
                if (functionSymbolTable.addVariable(name, type, this.registerCounter))
                    this.registerCounter++;
            }
        }

        else if (node instanceof ASTExprtest) {
            ASTExprtest exprtest = (ASTExprtest) node;

            for (int i = 0; i < exprtest.jjtGetNumChildren(); i++) {
                if (exprtest.jjtGetChild(i) instanceof ASTAccess) {
                    ASTAccess access = (ASTAccess) exprtest.jjtGetChild(i);

                    if (functionSymbolTable.getFromAll(access.name) == null && symbolTables.get(moduleName).getFromAll(access.name) == null)
                        printSemanticError(access.name, access.line, "Undefined variable.");
                }

                else if (exprtest.jjtGetChild(i) instanceof ASTRhs) {
                    ASTRhs rhs = (ASTRhs) exprtest.jjtGetChild(i);

                    for (int j = 0; j < rhs.jjtGetNumChildren(); j++) {

                        if (rhs.jjtGetChild(j) instanceof ASTTerm) {
                            ASTTerm term = (ASTTerm) rhs.jjtGetChild(j);

                            for (int k = 0; k < term.jjtGetNumChildren(); k++) {

                                if (term.jjtGetChild(k) instanceof ASTAccess) {
                                    ASTAccess access = (ASTAccess) term.jjtGetChild(k);

                                    if (functionSymbolTable.getFromAll(access.name) == null && symbolTables.get(moduleName).getFromAll(access.name) == null)
                                        printSemanticError(access.name, access.line, "Undefined variable.");
                                }
                            }
                        }
                    }
                }
            }
        }

        else if (node instanceof ASTWhile || node instanceof ASTIf || node instanceof ASTElse) {
            SimpleNode simpleNode = (SimpleNode) node;

            for (int i = 0; i < simpleNode.jjtGetNumChildren(); i++) {
                saveFunctionVariables(functionSymbolTable, simpleNode.jjtGetChild(i));
            }
        }

    }

    public void argumentsAnalysis(SymbolTable functionSymbolTable, Node node) {

        if (node instanceof ASTCall) {
            ASTCall call = (ASTCall) node;

            if (this.symbolTables.get(call.function) == null && call.module.compareTo("io") != 0)//Excludes io module
                printSemanticError(call.function, call.line, "Function not declared."); 

            for (int i = 0; i < call.jjtGetNumChildren(); i++) {
                if (call.jjtGetChild(i) instanceof ASTArgumentList) {
                    argumentsAnalysis(functionSymbolTable, call.jjtGetChild(i));
                }
            }
        } else if (node instanceof ASTArgumentList) {
            ASTArgumentList argumentList = (ASTArgumentList) node;
            for (int i = 0; i < argumentList.jjtGetNumChildren(); i++) {
                if (argumentList.jjtGetChild(i) instanceof ASTArgument) {
                    ASTArgument argument = (ASTArgument) argumentList.jjtGetChild(i);

                    if (argument.type.equals("ID") && functionSymbolTable.getFromAll(argument.name) == null && symbolTables.get(moduleName).getFromAll(argument.name) == null)
                        printSemanticError(argument.name, argument.line, "Argument not previously defined.");                   
                }
            }
        }

    }

    public boolean canAddVariable(SymbolTable functionSymbolTable, String name, String type, int registerCounter) {
        if (!this.symbolTables.get(this.moduleName).getVariables().containsValue(new Symbol(name, type, registerCounter))) {//verify if the new symbol isn't on the module symbol table already
            if (!functionSymbolTable.getParameters().containsValue(new Symbol(name, type, registerCounter))) { //verify if the new symbol isn't on the function's parameters already
                if (functionSymbolTable.getReturnSymbol() != null) { //if the function returns a symbol
                    if (!functionSymbolTable.getReturnSymbol().equals(new Symbol(name, type, registerCounter))) // if the return symbol isnt't the new one
                        return true;
                } else {
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
            if (key.equals(this.moduleName))
                System.out.println(" > MODULE: " + key);
            else
                System.out.println(" > SCOPE: " + key);

            for (String parkey : symbolTables.get(key).getParameters().keySet()) {
                Symbol s = symbolTables.get(key).getParameters().get(parkey);
                System.out.println(
                        "   - Parameter Symbol: " + s.getName() + " - " + s.getType() + " - " + s.getRegister());
            }

            for (String varkey : symbolTables.get(key).getVariables().keySet()) {
                Symbol s = symbolTables.get(key).getVariables().get(varkey);
                System.out.print("   - Variable Symbol: " + s.getName() + " - " + s.getType());
                if (s.getRegister() != -1)
                    System.out.print(" - " + s.getRegister());
                System.out.print('\n');
            }

            if (symbolTables.get(key).getReturnSymbol() != null)
                System.out.println("   - Return Symbol: " + symbolTables.get(key).getReturnSymbol().getName() + " - "
                        + symbolTables.get(key).getReturnSymbol().getType() + " - "
                        + symbolTables.get(key).getReturnSymbol().getRegister());
        }
    }

    public PrintWriter getFile() {

        try {
            File dir = new File("jvm");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File("jvm/" + this.moduleName + ".jvm");
            if (!file.exists())
                file.createNewFile();

            PrintWriter writer = new PrintWriter(file);

            return writer;

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public void yalToJasmin(SimpleNode root) {

        PrintWriter file = getFile();

        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;

            file.println(".class public " + module.name);
            file.println(".super java/lang/Object\n");

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {

                //declarations
                if (module.jjtGetChild(i) instanceof ASTDeclaration) {

                    ASTDeclaration declaration = (ASTDeclaration) module.jjtGetChild(i);
                    declarationsToJvm(file, declaration);

                    //functions
                } else if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    functionToJvm(file, function);
                }
            }

        }

        file.close();
    }

    public void declarationsToJvm(PrintWriter file, ASTDeclaration declaration) {

        ASTElement element = (ASTElement) declaration.jjtGetChild(0);

        String type = this.symbolTables.get(this.moduleName).getVariables().get(element.name).getType();

        if (type.equals("int")) {
            file.print(".field static " + element.name + " I");

            if (!declaration.integer.equals("")) {

                file.print(" = ");
                if (declaration.operator.equals("-"))
                    file.print("-"); //negative number
                file.print(declaration.integer);
            }

            file.print("\n");
        }

        else if (type.equals("array")) {
            file.println(".field static " + element.name + " [I");
        }

    }

    public void functionToJvm(PrintWriter file, ASTFunction function) {

        SymbolTable functionTable = this.symbolTables.get(function.name);

        //function header

        ASTArgumentList arguments = null;

        for(int i = 0; i < function.jjtGetNumChildren(); i++){
            if(function.jjtGetChild(i) instanceof ASTArgumentList)
                arguments = (ASTArgumentList) function.jjtGetChild(i);
        }

        file.print("\n.method public static ");
        if (function.name.equals("main")) {
            file.print("main([Ljava/lang/String;)V\n");
        } else {
            file.print(functionHeader(function.name,arguments)+"\n");
        }

        //function limits

        int nrParameters = (new ArrayList(functionTable.getParameters().keySet())).size();
        int nrVariables = (new ArrayList(functionTable.getVariables().keySet())).size();
        int nrReturn = functionTable.getReturnSymbol() != null ? 1 : 0;

        int nrLocals = nrParameters + nrVariables + nrReturn;
        if(function.name.equals("main")) nrLocals++;
        int nrStack = nrLocals; //TODO: alterar para nÃºmero correto

        file.println("  .limit stack " + "10");
        file.println("  .limit locals " + nrLocals);


        //function statements
        for (int i = 0; i < function.jjtGetNumChildren(); i++) {
           
            if(!(function.jjtGetChild(i) instanceof ASTElement || function.jjtGetChild(i) instanceof ASTVarlist)){
                file.print("\n");
                statementToJvm(file, functionTable, function.jjtGetChild(i));
                file.print("\n");
            }
        }

        //function return

        if (functionTable.getReturnSymbol() != null) {
            if (functionTable.getReturnSymbol().getType() == "int") {
                file.println("\n  iload_" + functionTable.getReturnSymbol().getRegister());
                file.println("  ireturn");

            } else { //array

                file.println("\n  aload " + functionTable.getReturnSymbol().getRegister());
                file.println("  areturn");

            }
        } else { //void
            file.println("\n  return");
        }

        file.println(".end method\n");

    }

    public String functionHeader(String functionName, ASTArgumentList arguments) {

        SymbolTable functionTable = this.symbolTables.get(functionName);

        String functionHeader = functionName + "(";

        if(arguments!= null){
            for (int i = 0; i < arguments.jjtGetNumChildren(); i++){

                ASTArgument argument = (ASTArgument) arguments.jjtGetChild(i);
    
                if (argument.type.equals("ID")){
    
                    if(functionTable != null && functionTable.getParameters().get(argument.name)!=null){
    
                        String type =  functionTable.getParameters().get(argument.name).getType();
    
                        if(type.equals("array")){
                            functionHeader = functionHeader + "[I";
                        }
                        else functionHeader = functionHeader + "I";
                    }
                    else  functionHeader = functionHeader + "I";
                }  
                else if (argument.type.equals("String"))
                    functionHeader = functionHeader + "java/lang/String;";
    
            }
        }
       

        if(functionTable != null && functionTable.getReturnSymbol() != null){
            Symbol returnSymbol = functionTable.getReturnSymbol();

            if (returnSymbol != null) {
                if (returnSymbol.getType().equals("int"))
                    functionHeader = functionHeader + ")I";
                else if (returnSymbol.getType().equals("array"))
                    functionHeader = functionHeader + ")[I";
            } else
                functionHeader = functionHeader + ")V";
        }
        else functionHeader = functionHeader + ")V";


        return functionHeader;
    }

    public void statementToJvm(PrintWriter file, SymbolTable functionTable, Node node) {

        if (node instanceof ASTAssign) { //ASSIGNS
            ASTAssign assign = (ASTAssign) node;

            if (assign.jjtGetNumChildren() == 2) {

                if (assign.jjtGetChild(0) instanceof ASTAccess && assign.jjtGetChild(1) instanceof ASTRhs) {
                    ASTAccess access = (ASTAccess) assign.jjtGetChild(0);
                    ASTRhs rhs = (ASTRhs) assign.jjtGetChild(1);

                    if (access.jjtGetNumChildren() == 0) { //just ints and entire arrays, no array accesses
                        if (rhs.jjtGetNumChildren() == 1) { //just simple assigns, no operations
                            simpleAssign(file, functionTable, access.name, rhs);
                        } else if (rhs.jjtGetNumChildren() == 2) { //operations
                            String term1 = getTerm((ASTTerm) rhs.jjtGetChild(0));
                            String term2 = getTerm((ASTTerm) rhs.jjtGetChild(1));
                            printOperation(file, functionTable, access.name, rhs.operator, term1, term2);
                        }

                        //assign of array accesses
                    } else if (access.jjtGetNumChildren() == 1 && access.jjtGetChild(0) instanceof ASTArrayAccess) { //eg.:a[i]=10 / a[4]=10

                        ASTArrayAccess arrayAccess = (ASTArrayAccess) access.jjtGetChild(0);

                        //TODO:array       
                    }

                }
            }

        } else if (node instanceof ASTCall) { //CALLS
            ASTCall call = (ASTCall) node;

            ASTArgumentList argumentList = null;

                if(call.jjtGetNumChildren() > 0 && call.jjtGetChild(0) instanceof ASTArgumentList){ //function has arguments

                    argumentList = (ASTArgumentList) call.jjtGetChild(0);
    
                    for(int i = 0; i < argumentList.jjtGetNumChildren(); i++){
    
                        ASTArgument argument = (ASTArgument) argumentList.jjtGetChild(i);
    
                        printVariableLoad(file,functionTable,argument.name, argument.type);
    
                    }
                }

                if (call.module.equals("") && symbolTables.get(call.function)!=null) {
                    file.println("  invokestatic " + this.moduleName + "/" + functionHeader(call.function, argumentList));
                } else {
                    file.print("  invokestatic " + call.module +"/" + functionHeader(call.function, argumentList));
                }     

        } else if (node instanceof ASTWhile) { // WHILE
            for (int i = 1; i < node.jjtGetNumChildren(); i++) { //TODO: comecar em 0 e analidar o expression
                statementToJvm(file, functionTable, node.jjtGetChild(i));
            }

        } else if (node instanceof ASTExprtest) {
            statementToJvm(file, functionTable, node.jjtGetChild(0));
        }
    }

    public void printOperation(PrintWriter file, SymbolTable functionTable, String accessName, String operator,
            String term1, String term2) {
        String regex = "\\d+";
        if ((accessName.equals(term1) || accessName.equals(term2)) && (term1.matches(regex) || term2.matches(regex))
                && (operator.equals("+") || operator.equals("-"))) {

            if (operator.equals("+"))
                operator = " ";
            else
                operator = " -";

            if (term2.matches(regex)) { // iinc term1 term2
                printVariableInc(file, functionTable, term1, operator, term2);
            } else { // iinc term2 term1
                printVariableInc(file, functionTable, term2, operator, term1);
            }
        } else {
            if (term2.matches(regex)) {
                printVariableLoad(file, functionTable, term1, "ID");
                printVariableLoad(file, functionTable, term2, "Integer");
            } else if (term1.matches(regex)) {
                printVariableLoad(file, functionTable, term1, "Integer");
                printVariableLoad(file, functionTable, term2, "ID");
            } else {
                printVariableLoad(file, functionTable, term1, "ID");
                printVariableLoad(file, functionTable, term2, "ID");
            }

            switch (operator) {
            case "+":
                file.println("  iadd");
                break;
            case "-":
                file.println("  isub");
                break;
            case "*":
                file.println("  imul");
                break;
            case "/":
                file.println("  idiv");
                break;
            default:
                break;
            }

            printVariableStore(file, functionTable, accessName);
        }
    }

    public String getTerm(ASTTerm term) {
        if (term.jjtGetNumChildren() == 0 && term.integer != "") { // term is a number
            return term.integer;
        } else if (term.jjtGetNumChildren() == 1) {
            if (term.jjtGetChild(0) instanceof ASTAccess) { // term is a variable

                ASTAccess termAccess = (ASTAccess) term.jjtGetChild(0);
                return termAccess.name;

                //TODO: array

            } else if (term.jjtGetChild(0) instanceof ASTCall) { // term is a call
                //statementToJvm(file, functionTable, term.jjtGetChild(0));
                //printVariableStore(file, functionTable, accessName);

                //TODO: array (se a for array)
            }
        }
        return "";
    }

    public void simpleAssign(PrintWriter file, SymbolTable functionTable, String accessName, ASTRhs rhs) {
        if (rhs.jjtGetChild(0) instanceof ASTTerm) {

            ASTTerm term = (ASTTerm) rhs.jjtGetChild(0);

            if (term.jjtGetNumChildren() == 0 && term.integer != "") { //eg.: a = 0
                
                String name = term.integer;
                if (term.operator.equals("-"))
                    name = '-' + name; //Negative number

                printVariableLoad(file, functionTable, name, "Integer");                
                printVariableStore(file, functionTable, accessName);

                //TODO: array (se a for array)

            }

            if (term.jjtGetNumChildren() == 1) {
                if (term.jjtGetChild(0) instanceof ASTAccess) { //eg.: i=b.size

                    ASTAccess termAccess = (ASTAccess) term.jjtGetChild(0);

                    //TODO: array

                } else if (term.jjtGetChild(0) instanceof ASTCall) { //eg.: a=f1(b)
                    statementToJvm(file, functionTable, term.jjtGetChild(0));
                    printVariableStore(file, functionTable, accessName);

                    //TODO: array (se a for array)
                }

            }

        } else if (rhs.jjtGetChild(0) instanceof ASTArraySize) { //eg.: a=[N]

            ASTArraySize arraySize = (ASTArraySize) rhs.jjtGetChild(0);

            //TODO:array
        }
    }

    public void printVariableInc(PrintWriter file, SymbolTable functionTable, String termVariable, String operator, String termNumber) {

        Symbol variable = functionTable.getFromAll(termVariable);
        if (variable != null) { //Local Variables
            file.println("  iinc " + variable.getRegister() + operator + termNumber);

        } else { //Global variable             
            Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(termVariable);
            if (globalVariable != null) {
                String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                file.println("  iinc " + this.moduleName + "/" + globalVariable.getName() + operator + termNumber);
            }

        }
    }

    public void printVariableStore(PrintWriter file, SymbolTable functionTable, String name) {

        Symbol variable = functionTable.getFromAll(name);
        if (variable != null) { //Local Variables
            if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                file.println("  istore_" + variable.getRegister());
            else file.println("  istore " + variable.getRegister());

        } else { //Global variable             
            Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(name);
            if (globalVariable != null) {
                String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                file.println("  putstatic " + this.moduleName + "/" + globalVariable.getName() + globalVariableType);
            }

        }
    }

    public void printVariableLoad(PrintWriter file, SymbolTable functionTable, String name, String type) {

        if (type.equals("ID")) {
            Symbol variable = functionTable.getFromAll(name);
            if (variable != null) { //Local Variables
                if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                    file.println("  iload_" + variable.getRegister());
                else file.println("  iload " + variable.getRegister());

                //TODO: array

            } else { //Global variable             
                Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(name);

                if (globalVariable != null) {
                    String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                    file.println("  getstatic " + this.moduleName + "/" + globalVariable.getName() + globalVariableType);
                }

            }
        } else if (type.equals("Integer")) {

            int number = Integer.parseInt(name);

            if (number >= 0 && number <= 5)
                file.println("  iconst_" + number);
            else
                file.println("  bipush " + number);

        } else if (type.equals("String")) {

            file.println("  ldc " + name);
        }

    }

    public void printSemanticError(String var, int line, String error) {
        System.out.println( ANSI_RED + "Semantic Error!\n" + ANSI_YELLOW + "Line " + line + ANSI_RESET + " : " + var + " -> " + error);
    }
}