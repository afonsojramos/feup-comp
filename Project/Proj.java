import java.io.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import AST.*;
import java.nio.file.Paths;
import java.util.List;

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
    private int errorCount = 0;

    public static void main(String args[]) throws ParseException {
        yal2jvm parser;

        if (args.length == 0) {
            System.out.println(ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " Reading input ...");
            parser = new yal2jvm(System.in);
        } else if (args.length == 1 || args.length == 2) {
            if (args.length == 2 && args[1].toString().equals("color")){
                ANSI_RESET = "\u001B[0m";
                ANSI_RED = "\u001B[31m";
                ANSI_GREEN = "\u001B[32m";
                ANSI_CYAN = "\u001B[36m";
                ANSI_YELLOW = "\u001B[33m";
            }
            ParseException.filename = args[0];
            System.out.println(
                    ANSI_CYAN + "yal2jvm:" + ANSI_RESET + " Reading the file " + args[0] + " ..." + ANSI_RESET);
            try {
                parser = new yal2jvm(new java.io.FileInputStream(args[0]));
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
            if(errorCount == 0) //if there weren't any semantic erros
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
            this.moduleName = "module - " + module.name;
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
							globalSymbolTable.addVariable(element.name, "array");
                        } else {
                            globalSymbolTable.addVariable(element.name, "int");
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

                    fillFunctionParametersReturn(function);
                }
            }
            this.symbolTables.put(this.moduleName, globalSymbolTable);
        }
    }

    public void fillFunctionParametersReturn(ASTFunction function){

            SymbolTable functionSymbolTable = this.symbolTables.get(function.name);

            for (int j = 0; j < function.jjtGetNumChildren(); j++) {

                //Return Symbol
                if (function.jjtGetChild(j) instanceof ASTElement) {
                    ASTElement element = (ASTElement) function.jjtGetChild(j);

                    if (element.jjtGetNumChildren() == 1)
                        functionSymbolTable.setReturnSymbol(element.name, "array");
                    else
                        functionSymbolTable.setReturnSymbol(element.name, "int");

                }

                //Parameters
                if (function.jjtGetChild(j) instanceof ASTVarlist) {
                    ArrayList<String> names = new ArrayList<String>();
                    for (int k = 0; k < function.jjtGetChild(j).jjtGetNumChildren(); k++) {
                        ASTElement element = (ASTElement) function.jjtGetChild(j).jjtGetChild(k);
                        
                        if(names.contains(element.name))
                            printSemanticError(element.name, element.line, "Repeated argument.");

                        names.add(element.name);

                        if (element.jjtGetNumChildren() == 1) {
                            functionSymbolTable.addParameter(element.name, "array");
                               
                        } else {
                            functionSymbolTable.addParameter(element.name, "int");
                        }

                        if(functionSymbolTable.getReturnSymbol()!=null && element.name.equals(functionSymbolTable.getReturnSymbol().getName()))
                            functionSymbolTable.setReturned(true);

                    }
                }
            }

    }

    public void fillFunctionSymbolTables(SimpleNode root) {
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    SymbolTable functionSymbolTable = this.symbolTables.get(function.name);

                    for (int j = 0; j < function.jjtGetNumChildren(); j++) {

                        //Variables
                        if (function.jjtGetChild(j) instanceof ASTAssign || function.jjtGetChild(j) instanceof ASTWhile || function.jjtGetChild(j) instanceof ASTIf || function.jjtGetChild(j) instanceof ASTElse || function.jjtGetChild(j) instanceof ASTCall)
                            saveFunctionVariables(functionSymbolTable, function.jjtGetChild(j));
                    }

                    if(functionSymbolTable.getReturnSymbol()!= null){
                        String returnName = functionSymbolTable.getReturnSymbol().getName();
                        String returnType = functionSymbolTable.getReturnSymbol().getType();
                        

                        if(this.symbolTables.get(this.moduleName).getFromAll(returnName)!= null){    
    
                            if(!this.symbolTables.get(this.moduleName).getFromAll(returnName).getType().equals(returnType))
                                printSemanticError(returnName, function.line, "Return type and global variable type don't match");
                            
                        }else{
                            if(!functionSymbolTable.getReturned())
                                printSemanticError(returnName, function.line, "Return type not declared");
                        }

                    } 
                       
                }
            }
        }
        printSymbolTables();
    }

    public SymbolTable saveFunctionVariables(SymbolTable functionSymbolTable, Node node) {

        if (node instanceof ASTAssign) {
            ASTAssign assign = (ASTAssign) node;
            String name = "";
            String type = "int";
            boolean arrayIndex = false;

            for (int i = 0; i < assign.jjtGetNumChildren(); i++) {
                if (assign.jjtGetChild(i) instanceof ASTAccess) {
                    ASTAccess access = (ASTAccess) assign.jjtGetChild(i);
                    name = access.name;

                    if (functionSymbolTable.getReturnSymbol() != null && functionSymbolTable.getReturnSymbol().getName().equals(name) && functionSymbolTable.getReturnSymbol().getType().equals("int")){
                        functionSymbolTable.getReturnSymbol().setInit();
                    }

                    for (int j = 0; j < access.jjtGetNumChildren(); j++) { //In Array Accesses an int must be inside the brackets
                        if (access.jjtGetChild(j) instanceof ASTArrayAccess) {
                            ASTArrayAccess arrayAccess = (ASTArrayAccess) access.jjtGetChild(j);

                            if (functionSymbolTable.getFromAll(name)==null && this.symbolTables.get(this.moduleName).getFromAll(name)==null){
                                printSemanticError(access.name, access.line, "Undefined array");
                            }

                            for (int k = 0; k < arrayAccess.jjtGetNumChildren(); k++) {
                                if (arrayAccess.jjtGetChild(k) instanceof ASTIndex) {
                                    ASTIndex index = (ASTIndex) arrayAccess.jjtGetChild(k);
                                    
                                    arrayIndex = true;
        
                                    if (index.value.isEmpty()){ //Case of VARIABLE has "name" but does not have "value"
                                        if (functionSymbolTable.getFromAll(index.name) == null && this.symbolTables.get(moduleName).getFromAll(index.name) == null)
                                            printSemanticError(index.name, index.line, "Undefined variable.");
                                        else if (functionSymbolTable.getAcessType(index.name) != "int" && this.symbolTables.get(this.moduleName).getAcessType(index.name) != "int") //Variable must represent an int
                                            printSemanticError(index.name, index.line, "Type mismatch.");
                                    }
                                }
                            }
                        }
                        if (access.jjtGetChild(j) instanceof ASTSizeAccess) {

                            printSemanticError(access.name, access.line, "Size is a property, not a variable.");
                            
                        }
                    }
                }

                else if (assign.jjtGetChild(i) instanceof ASTRhs) {
                    ASTRhs rhs = (ASTRhs) assign.jjtGetChild(i);

                    for (int j = 0; j < rhs.jjtGetNumChildren(); j++) {
                        if (rhs.jjtGetChild(j) instanceof ASTArraySize) {
                            ASTArraySize arraySize = (ASTArraySize) rhs.jjtGetChild(j);

                            if (functionSymbolTable.getReturnSymbol() != null && functionSymbolTable.getReturnSymbol().getName().equals(name) && functionSymbolTable.getReturnSymbol().getType().equals("array")){
                                functionSymbolTable.getReturnSymbol().setInit();
                            }

                            if (arrayIndex)
                                printSemanticError(arraySize.name, arraySize.line, "Undefined variable.");
                           
                            if (functionSymbolTable.getAcessType(name) == "int" || this.symbolTables.get(this.moduleName).getAcessType(name) == "int") //Variable previously defined as integer
                                printSemanticError(name, arraySize.line, "Variable previously defined as another type.");

                            type = "array";

                            if (arraySize.value.isEmpty()) { //Case of VARIABLE has "name" but does not have "value"
                                if (functionSymbolTable.getFromAll(arraySize.name) == null && this.symbolTables.get(moduleName).getFromAll(arraySize.name) == null)
                                    printSemanticError(arraySize.name, arraySize.line, "Undefined variable.");
                                else {
                                    for (int k = 0; k < arraySize.jjtGetNumChildren(); k++) {
                                        if (arraySize.jjtGetChild(k) instanceof ASTSizeAccess) {
                                            arrayIndex = true;
                                        }
                                    }
                                    if (functionSymbolTable.getAcessType(arraySize.name) != "int" && arrayIndex == false) //Variable must represent an int
                                        printSemanticError(arraySize.name, arraySize.line, "Type mismatch..");
                                }
                                
                                
                                
                                
                            }
                        }

                        if (rhs.jjtGetChild(j) instanceof ASTTerm) {
                            ASTTerm term = (ASTTerm) rhs.jjtGetChild(j);

                            if (term.integer != "" && functionSymbolTable.getFromAll(name) != null && !functionSymbolTable.getFromAll(name).getInit()) {
                                functionSymbolTable.getFromAll(name).setInit();
                            }

                            if (functionSymbolTable.getReturnSymbol() != null && functionSymbolTable.getReturnSymbol().getName().equals(name) && functionSymbolTable.getReturnSymbol().getType().equals("array") && !functionSymbolTable.getReturnSymbol().getInit()){
                                printSemanticError(name, assign.line, "Function type mismatch...");
                            }

                            if (term.jjtGetNumChildren() > 0 && term.jjtGetChild(0) instanceof ASTCall) {
                                ASTCall call = (ASTCall) term.jjtGetChild(0);

                                if (this.symbolTables.get(this.moduleName) != null && this.symbolTables.get(call.function) != null && this.symbolTables.get(call.function).getReturnSymbol() != null && (this.symbolTables.get(this.moduleName).getAcessType(name) == "int" || functionSymbolTable.getAcessType(name) == "int") && this.symbolTables.get(call.function).getReturnSymbol().getType() == "array"){
                                    printSemanticError(call.function, call.line, "Function type mismatch.");
                                }

                                argumentsAnalysis(functionSymbolTable, call);

                                if (this.symbolTables.get(call.function) != null){
                                    if (this.symbolTables.get(call.function).getReturnSymbol() != null)
                                        if (call.module.equals("")){ //if the functions belongs to this module
                                            type = this.symbolTables.get(call.function).getReturnSymbol().getType(); //gets that function return type
                                            if(!rhs.operator.equals("")){
                                                if(type.equals("array")){
                                                    printSemanticError(call.function, call.line, "This variable is an array, operations can only be done with scalars.");
                                                }
                                            }    
                                        } 
                                        else
                                            type = "int"; //otherwise it's int
                                }

                               
                               
                            } else {
                                for (int k = 0; k < term.jjtGetNumChildren(); k++) {

                                    if (term.jjtGetChild(k) instanceof ASTAccess) {
                                        ASTAccess access = (ASTAccess) term.jjtGetChild(k);

                                        if (functionSymbolTable.getFromAll(access.name) == null && this.symbolTables.get(moduleName).getFromAll(access.name) == null)
                                            printSemanticError(access.name, access.line, "Variable not previously defined.");

                                        for (int l = 0; l < access.jjtGetNumChildren(); l++) {
                                            if (access.jjtGetChild(l) instanceof ASTArrayAccess) {
                                                ASTArrayAccess arrayAccess = (ASTArrayAccess) access.jjtGetChild(l);
                    
                                                for (int m = 0; m < arrayAccess.jjtGetNumChildren(); m++) {
                                                    if (arrayAccess.jjtGetChild(m) instanceof ASTIndex) {
                                                        ASTIndex index = (ASTIndex) arrayAccess.jjtGetChild(m);

                                                        if (functionSymbolTable.getFromAll(access.name) != null && !functionSymbolTable.getFromAll(access.name).getInit()){
                                                            printSemanticError(access.name, index.line, "Empty Array.");
                                                        }
                                                                                    
                                                        if (index.value.isEmpty()){ //Case of VARIABLE has "name" but does not have "value"
                                                            if (functionSymbolTable.getFromAll(index.name) == null && this.symbolTables.get(moduleName).getFromAll(index.name) == null)
                                                                printSemanticError(index.name, index.line, "Undefined index.");
                                                            else if (functionSymbolTable.getAcessType(index.name) != "int") //Variable must represent an int
                                                                printSemanticError(index.name, index.line, "Type mismatch.");
                                                        }
                                                    }
                                                }
                                            }
                                            if (access.jjtGetChild(l) instanceof ASTSizeAccess) {

                                                if (functionSymbolTable.getAcessType(access.name) == "int" || this.symbolTables.get(this.moduleName).getAcessType(access.name) == "int") //Variable previously defined as integer
                                                    printSemanticError(access.name, access.line, "This variable is a scalar, not an array.");
                                            }
                                        }       
                                        
                                        //TODO: Just if not working
                                        if (functionSymbolTable.getFromAll(access.name) != null && functionSymbolTable.getVariables() != null && functionSymbolTable.getVariables().get(access.name) != null)
                                            System.out.println(access.name + " " + functionSymbolTable.getVariables().get(access.name).getInit() + assign.jjtGetParent());

                                        if (functionSymbolTable.getFromAll(access.name) != null && !functionSymbolTable.getFromAll(access.name).getInit())
                                            printSemanticError(access.name, access.line, "Variable may not be defined.");
                                    } 
                                } 
                            }
                        }
                    }
                }
            }
            
            if(functionSymbolTable.getReturnSymbol()!=null && name.equals(functionSymbolTable.getReturnSymbol().getName())){
                functionSymbolTable.setReturned(true);
                if(!type.equals(functionSymbolTable.getReturnSymbol().getType()) && functionSymbolTable.getReturnSymbol().getType() == "int")
                    printSemanticError(name, assign.line,"Return type mismatch.");                    
            } 

            if (canAddVariable(functionSymbolTable, name, type)) {
                if (assign.jjtGetParent() instanceof ASTIf){
                    functionSymbolTable.addVariable(name, type);
                    functionSymbolTable.getFromAll(name).setNotInit();
                    System.out.println("Adding this dude: " + name + functionSymbolTable.getFromAll(name).getInit());
                }
                else if (assign.jjtGetParent() instanceof ASTElse){
                    if (functionSymbolTable.getFromAll(name) != null && functionSymbolTable.getFromAll(name).getType() == type){
                        functionSymbolTable.getFromAll(name).setInit();
                    }
                    else if (functionSymbolTable.getFromAll(name) == null){
                        functionSymbolTable.addVariable(name, type);
                        functionSymbolTable.getFromAll(name).setNotInit();
                    }
                }  
                else if (assign.jjtGetParent() instanceof ASTWhile) {
                    functionSymbolTable.addVariable(name, type);
                    functionSymbolTable.getFromAll(name).setNotInit();
                }             
                else {
                    functionSymbolTable.addVariable(name, type);
                    if (type == "array"){
                        functionSymbolTable.getVariables().get(name).setNotInit();
                    }
                }

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

        else if (node instanceof ASTCall){
            ASTCall call = (ASTCall) node;
            argumentsAnalysis(functionSymbolTable, call);
        }

        else if (node instanceof ASTWhile || node instanceof ASTIf || node instanceof ASTElse) {
            SimpleNode simpleNode = (SimpleNode) node;

            for (int i = 0; i < simpleNode.jjtGetNumChildren(); i++) {
                try {
                    SymbolTable newTable = functionSymbolTable.clone();
                    SymbolTable nestedTable = saveFunctionVariables(newTable, simpleNode.jjtGetChild(i));
                    mergeIf(nestedTable, functionSymbolTable);
                } catch (CloneNotSupportedException e) {
                    System.out.println("Clone not supported!");
                }
            }
        }

        return functionSymbolTable;

    }

    public void mergeIf(SymbolTable ifTable, SymbolTable parent) {
        LinkedHashMap<String, Symbol> ifVariables = ifTable.getVariables();
        for (Map.Entry<String, Symbol> entry : ifVariables.entrySet()) {
            if ((parent.getFromAll(entry.getKey())) == null) {
                parent.addVariable(entry.getKey(), entry.getValue().getType());
                //entry.getValue().setInit(); Just MAY BE
            }
        }
    }

    public void argumentsAnalysis(SymbolTable functionSymbolTable, Node node) {

        if (node instanceof ASTCall) {
			ASTCall call = (ASTCall) node;

            if (this.symbolTables.get(call.function) == null && call.module == "")//Excludes io module
                printSemanticError(call.function, call.line, "Function not declared."); 

            for (int i = 0; i < call.jjtGetNumChildren(); i++) {
                if (call.jjtGetChild(i) instanceof ASTArgumentList) {

					if (this.symbolTables.get(call.function) != null && call.jjtGetChild(i).jjtGetNumChildren() != this.symbolTables.get(call.function).getParameters().size()){
                        printSemanticError(call.function, call.line, "Wrong number of arguments.");
                    }

                    ASTArgumentList argumentList = (ASTArgumentList) call.jjtGetChild(i);
					for (int j = 0; j < argumentList.jjtGetNumChildren(); j++) {
						if (argumentList.jjtGetChild(j) instanceof ASTArgument) {
							ASTArgument argument = (ASTArgument) argumentList.jjtGetChild(j);

							if (argument.type.equals("ID") && functionSymbolTable.getFromAll(argument.name) == null && this.symbolTables.get(this.moduleName).getFromAll(argument.name) == null)
								printSemanticError(argument.name, argument.line, "Argument undefined."); 

							if (argument.type.equals("ID") && (functionSymbolTable.getFromAll(argument.name) != null || this.symbolTables.get(this.moduleName).getFromAll(argument.name) != null)){
								if (this.symbolTables.get(call.function) != null){
									Iterator<Symbol> it = this.symbolTables.get(call.function).getParameters().values().iterator();
									int paramCount = 0;
									while (it.hasNext())
									{
										Symbol currentSymbol = it.next();
	
										/* System.out.println(currentSymbol.getType() + " = " + argument.name + " " + this.symbolTables.get(this.moduleName).getAcessType(argument.name) + "\n in " + paramCount + " = " + j); */
	
										if ((currentSymbol.getType() != functionSymbolTable.getAcessType(argument.name) && currentSymbol.getType() != this.symbolTables.get(this.moduleName).getAcessType(argument.name)) && paramCount == j)
											printSemanticError(argument.name, argument.line, "Wrong type of argument.");      
										paramCount++;        
									}
								}
							}
						}
					}
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

    public boolean canAddVariable(SymbolTable functionSymbolTable, String name, String type) {
        if (this.symbolTables.get(this.moduleName).getFromAll(name) == null) {//verify if the new symbol isn't on the module symbol table already
            if (!functionSymbolTable.getParameters().containsValue(new Symbol(name, type))) { //verify if the new symbol isn't on the function's parameters already
                if (functionSymbolTable.getReturnSymbol() != null) { //if the function returns a symbol
                    if (!functionSymbolTable.getReturnSymbol().equals(new Symbol(name, type))) // if the return symbol isnt't the new one
                        return true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void printSemanticError(String var, int line, String error) {
        System.out.println( ANSI_RED + "Semantic Error nº" + ++errorCount + "!\n" + ANSI_YELLOW + "Line " + ANSI_CYAN + line + ANSI_RESET + " : " + var + " -> " + error);
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
                        "   - Parameter Symbol: " + s.getName() + " - " + s.getType());
            }

            for (String varkey : symbolTables.get(key).getVariables().keySet()) {
                Symbol s = symbolTables.get(key).getVariables().get(varkey);
                System.out.println("   - Variable Symbol: " + s.getName() + " - " + s.getType());
            }

            if (symbolTables.get(key).getReturnSymbol() != null)
                System.out.println("   - Return Symbol: " + symbolTables.get(key).getReturnSymbol().getName() + " - "
                        + symbolTables.get(key).getReturnSymbol().getType());
        }
    }

    //////////////////////JASMIN//////////////////////

    public PrintWriter getFile() {

        try {
            File dir = new File("jasmin");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File("jasmin/" + this.moduleName.substring(9) + ".j");
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

    
        if (root != null && root instanceof ASTModule) {
            PrintWriter file = getFile();

            ASTModule module = (ASTModule) root;

            file.println(".class public " +  this.moduleName.substring(9));
            file.println(".super java/lang/Object\n");

            HashMap<String, String> staticArrays = new HashMap<>();

            //declarations
            for (int i = 0; i < module.jjtGetNumChildren(); i++) {   
              
                if (module.jjtGetChild(i) instanceof ASTDeclaration) {
                    ASTDeclaration declaration = (ASTDeclaration) module.jjtGetChild(i);
                    declarationsToJvm(file, declaration, staticArrays);

                } else if (module.jjtGetChild(i) instanceof ASTFunction) {
                    break;
                }
            }

            if(!staticArrays.isEmpty()){
                initDeclarationsArrays(file, staticArrays);
            }

            //functions
            for (int i = 0; i < module.jjtGetNumChildren(); i++) {

                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    functionToJvm(file, function);
                }
            }

            file.close();

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {

                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    SymbolTable functionTable = this.symbolTables.get(function.name);
                    writeStackNumber(functionTable, function.name);
                }
            } 

        }

       
    }

    public void declarationsToJvm(PrintWriter file, ASTDeclaration declaration, HashMap<String, String> staticArrays) {

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

            if(declaration.jjtGetChild(1) instanceof ASTArraySize){

                ASTArraySize arraysize = (ASTArraySize) declaration.jjtGetChild(1);

                if(!arraysize.value.equals("")){ //Size is an integer
                    staticArrays.put(element.name, arraysize.value);

                } else if(!arraysize.name.equals("")){ //Size is a variable
                    staticArrays.put(element.name, arraysize.name);
                }
                
            }
        }

    }

    public void initDeclarationsArrays(PrintWriter file, HashMap<String,String> staticArrays){

        file.println("\n.method static public <clinit>()V");
        file.println("  .limit locals " + staticArrays.size());        
        file.println("  .limit stack " + staticArrays.size());

        for (Map.Entry<String, String> entry : staticArrays.entrySet()) {

            if(entry.getValue().matches("-?\\d+(\\.\\d+)?")){ //it's a int
                printVariableLoad(file, null, entry.getValue(), "Integer");
            }
            else{//it's a variable
                printVariableLoad(file, null, entry.getValue(), "ID");
            }

            file.println("  newarray int");
            printVariableStore(file, null, entry.getKey());

            file.print("\n");

        }

        file.println("  return");            
        file.println(".end method\n");

    }

    public void functionToJvm(PrintWriter file, ASTFunction function) {

        SymbolTable functionTable = this.symbolTables.get(function.name);

        functionTable.setRegisters(function.name);

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
            file.print(functionHeader(function.name)+"\n");
        }

        //function limits

        int nrParameters = (new ArrayList(functionTable.getParameters().keySet())).size();
        int nrVariables = (new ArrayList(functionTable.getVariables().keySet())).size();
        int nrReturn = functionTable.getReturnSymbol() != null ? 1 : 0;

        /*int nrLocals = nrParameters + nrVariables + nrReturn;
        if(function.name.equals("main")) nrLocals++;
        int nrStack = 6;*/

        file.println("locals_" + function.name);       
        file.println("stack_" + function.name);


        //function statements
        for (int i = 0; i < function.jjtGetNumChildren(); i++) {
           
            if(!(function.jjtGetChild(i) instanceof ASTElement || function.jjtGetChild(i) instanceof ASTVarlist)){
                //file.print("\n");
                statementToJvm(file, functionTable, function.jjtGetChild(i));
                //file.print("\n");
            }
        }

        //function return
        
        if (functionTable.getReturnSymbol() != null) {
            printVariableLoad(file, functionTable,functionTable.getReturnSymbol().getName(), "ID");
            if (functionTable.getReturnSymbol().getType() == "int") {
                file.println("  ireturn");

            } else { //array
                file.println("  areturn");

            }
        } else { //void
            file.println("  return");
        }
        file.println(".end method\n");

    }


    public void writeStackNumber(SymbolTable functionTable, String functionName){
        int stackNr = functionTable.getStack();
        int localsNr = functionTable.getLocals();
        


        try {
            File dir = new File("jasmin");
            if (!dir.exists())
                dir.mkdirs();

            File file = new File("jasmin/" + this.moduleName.substring(9) + ".j");
            if (!file.exists())
                file.createNewFile();


            List<String> lines = Files.readAllLines(file.toPath());
            
            for(int i = 0; i < lines.size(); i++){

                if(lines.get(i).equals("stack_"+functionName)){
                    lines.set(i, "  .limit stack " + stackNr);
                }

                if(lines.get(i).equals("locals_"+functionName)){
                    lines.set(i, "  .limit locals " + localsNr);
                }

            }

            Files.write(file.toPath(), lines);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

       
    }

    public String functionHeader(String functionName){

        SymbolTable functionTable = this.symbolTables.get(functionName);

        String functionHeader = functionName + "(";

        for (Map.Entry<String, Symbol> entry : functionTable.getParameters().entrySet()) {
            Symbol symbol = entry.getValue();
            if(symbol.getType().equals("int")){
                functionHeader = functionHeader + "I";
            }else if(symbol.getType().equals("array")){
                functionHeader = functionHeader + "[I";
            }
        }


        if(functionTable.getReturnSymbol() != null){
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


    public void statementToJvm(PrintWriter file, SymbolTable functionTable, Node node){

        if (node instanceof ASTAssign) { 
            assignToJvm(file, functionTable, node);
        }else if (node instanceof ASTCall){
            callToJvm(file, functionTable, node, "void");
        }else if (node instanceof ASTWhile){
            whileToJvm(file, functionTable, node);
        }else if (node instanceof ASTIf){
            ifToJvm(file, functionTable, node);
        }

        file.print("\n");
    }

    public void assignToJvm(PrintWriter file, SymbolTable functionTable, Node node){
        ASTAssign assign = (ASTAssign) node;

        if (assign.jjtGetChild(0) instanceof ASTAccess && assign.jjtGetChild(1) instanceof ASTRhs){
            ASTRhs rhs = (ASTRhs) assign.jjtGetChild(1);
            ASTAccess access = (ASTAccess) assign.jjtGetChild(0);

            if(access.jjtGetNumChildren() == 1 && access.jjtGetChild(0) instanceof ASTArrayAccess){ //arrayaccess special case         
                printVariableLoad(file, functionTable, access.name, "ID"); //reference
                arrayaccessToJvm(file,functionTable, access.jjtGetChild(0));
                rhsToJvm(file, functionTable, assign.jjtGetChild(1));
                file.println("  iastore");
            }else if(rhs.jjtGetNumChildren() == 2){
               if(!isInc(file, functionTable, rhs, access.name)){
                rhsToJvm(file, functionTable, assign.jjtGetChild(1));
                accessToJvm(file, functionTable, assign.jjtGetChild(0), "Store");
               }  
            }
            else if(initializeAllArray(file,functionTable, assign)){

             
            }else if(externalFunctionArray(file, functionTable, rhs, access)){ //external call returns array special case
            
            }else{
                rhsToJvm(file, functionTable, assign.jjtGetChild(1));
                accessToJvm(file, functionTable, assign.jjtGetChild(0), "Store");
            }
        }
    }

    public boolean externalFunctionArray(PrintWriter file, SymbolTable functionTable, ASTRhs rhs, ASTAccess access){
        if(rhs.jjtGetNumChildren() == 1 && rhs.jjtGetChild(0) instanceof ASTTerm){ 
            ASTTerm term = (ASTTerm) rhs.jjtGetChild(0);

            if(term.jjtGetNumChildren() == 1 && term.jjtGetChild(0) instanceof ASTCall){

                ASTCall call = (ASTCall) term.jjtGetChild(0);

                String name = access.name;
                if (functionTable != null && functionTable.getFromAll(name) != null) { 
                    Symbol variable = functionTable.getFromAll(name);
                    if(variable.getType().equals("array")){ 
                        callToJvm(file, functionTable, call, "array");
                        accessToJvm(file, functionTable, access, "Store");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean initializeAllArray(PrintWriter file, SymbolTable functionTable,ASTAssign assign){

        if (assign.jjtGetChild(0) instanceof ASTAccess && assign.jjtGetChild(1) instanceof ASTRhs){
            ASTRhs rhs = (ASTRhs) assign.jjtGetChild(1);
            ASTAccess access = (ASTAccess) assign.jjtGetChild(0);

            if(access.jjtGetNumChildren() == 0 && rhs.jjtGetNumChildren() == 1 && rhs.jjtGetChild(0) instanceof ASTTerm){
                ASTTerm term = (ASTTerm) rhs.jjtGetChild(0);
                String name = access.name;
                String value = getTerm(term);
                String regex = "\\d+";
                if(value.matches(regex)){
                    if (functionTable != null && functionTable.getFromAll(name) != null && functionTable.getFromAll(name).getType().equals("array")) { //Local Variables
                        
                        int indexRegister = functionTable.getLastRegister();
                        int arrayRegister = functionTable.getFromAll(name).getRegister();

                        functionTable.incLoopCounter();
                        int loop_nr = functionTable.getLoopCounter();

                        file.println("  iconst_0");
                        file.println("  istore " + indexRegister);
                        file.println("loop"+loop_nr+":");
                        file.println("  iload " + indexRegister);
                        file.println("  aload " + arrayRegister);
                        file.println("  arraylength");
                        file.println("  if_icmpge loop" + loop_nr+"_end");
                        file.println("  aload " + arrayRegister);
                        file.println("  iload " + indexRegister);
                        file.println("  bipush " + value);
                        file.println("  iastore");
                        file.println("  iinc " + indexRegister +" 1");
                        file.println("  goto loop"+ loop_nr);
                        file.println("loop"+ loop_nr+ "_end:");

                        indexRegister ++;
                        functionTable.setLastRegister(indexRegister);
                        functionTable.setLocals(indexRegister);
                        functionTable.setMaxStack(3);
                        
                            
                        return true;
                    }
                    else{

                        Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(name);

                        if(globalVariable != null){
                            String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                    
                            int indexRegister = functionTable.getLastRegister();
                            //int arrayRegister = functionTable.getFromAll(name).getRegister();
    
                            functionTable.incLoopCounter();
                            int loop_nr = functionTable.getLoopCounter();
    
                            file.println("  iconst_0");
                            file.println("  istore " + indexRegister);
                            file.println("loop"+loop_nr+":");
                            file.println("  iload " + indexRegister);
    
                            //file.println("  aload " + arrayRegister);
                            file.println("  getstatic " + this.moduleName.substring(9) + "/" + globalVariable.getName() + globalVariableType);
    
                            file.println("  arraylength");
                            file.println("  if_icmpge loop" + loop_nr+"_end");
                            
                            //file.println("  aload " + arrayRegister);
                            file.println("  getstatic " + this.moduleName.substring(9) + "/" + globalVariable.getName() + globalVariableType);
                            
                            file.println("  iload " + indexRegister);
                            file.println("  bipush " + value);
                            
                            //file.println("  iastore");
                            file.println("  putstatic " + this.moduleName.substring(9) + "/" + globalVariable.getName() + globalVariableType);

                            file.println("  iinc " + indexRegister + " 1");
                            file.println("  goto loop"+ loop_nr);
                            file.println("loop"+ loop_nr+ "_end:");
    
                            indexRegister ++;
                            functionTable.setLastRegister(indexRegister);
                            functionTable.setLocals(indexRegister);                            
                            functionTable.setMaxStack(3);
                            
                                
                            return true;

                        }

                       

                    }

                }
            }
        }
        return false;
    }

    public boolean isInc(PrintWriter file, SymbolTable functionTable, ASTRhs rhs, String accessName){
        ASTTerm term1 = (ASTTerm) rhs.jjtGetChild(0);
        String value1 = getTerm(term1);
        ASTTerm term2 = (ASTTerm) rhs.jjtGetChild(1);
        String value2 = getTerm(term2);

        String regex = "\\d+";

        if ((accessName.equals(value1) || accessName.equals(value2)) 
                && (value1.matches(regex) || value2.matches(regex))
                && (rhs.operator.equals("+") || rhs.operator.equals("-"))) {

            if (value2.matches(regex)) { // iinc term1 term2
                return printVariableInc(file, functionTable, value1, rhs.operator, value2);
            } else { // iinc term2 term1
                return printVariableInc(file, functionTable, value2, rhs.operator, value1);
            }   
        }

        return false;
    }

    public String getTerm(ASTTerm term) {
        if (term.jjtGetNumChildren() == 0 && term.integer != "") { // term is a number
            return term.integer;
        } else if (term.jjtGetNumChildren() == 1) {
            if (term.jjtGetChild(0) instanceof ASTAccess) { // term is a variable
                ASTAccess termAccess = (ASTAccess) term.jjtGetChild(0);
                return termAccess.name;
            }
        }
        return "";
    }

    public boolean printVariableInc(PrintWriter file, SymbolTable functionTable, String termVariable, String operator, String termNumber) {

        Symbol variable = functionTable.getFromAll(termVariable);
        if (variable != null) { //Local Variables

            if (operator.equals("+"))
                operator = " ";
            else
                operator = " -";

            file.println("  iinc " + variable.getRegister() + operator + termNumber);
            return true;
        } else
            return false;
    }

   

    public void rhsToJvm(PrintWriter file, SymbolTable functionTable, Node node){
        ASTRhs rhs = (ASTRhs) node;

        if(rhs.jjtGetNumChildren() == 1){
            if (rhs.jjtGetChild(0) instanceof ASTTerm) {
                termToJvm(file, functionTable, rhs.jjtGetChild(0));
            }
            else if(rhs.jjtGetChild(0) instanceof ASTArraySize){
                arraySizeToJvm(file, functionTable, rhs.jjtGetChild(0));
            }

        }else if(rhs.jjtGetNumChildren() == 2){ //operations
            ASTTerm term1 = (ASTTerm) rhs.jjtGetChild(0);
            ASTTerm term2 = (ASTTerm) rhs.jjtGetChild(1);
            
            termToJvm(file, functionTable, term1);
            termToJvm(file, functionTable, term2);
            
            switch (rhs.operator) {
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
                case "<<":
                    file.println("  ishl");
                    break;
                case ">>":
                    file.println("  ishr");
                    break;
                case ">>>":
                    file.println("  iushr");
                    break;
                case "&":
                    file.println("  iand");
                    break;
                case "|":
                    file.println("  ior");
                    break;
                case "^":
                    file.println("  ixor");
                    break;
                default:
                    break;
            }

            functionTable.setMaxStack(2);            
        
        }
    }

    public void arraySizeToJvm(PrintWriter file, SymbolTable functionTable, Node node){
        ASTArraySize arraysize = (ASTArraySize) node;

        if(arraysize.jjtGetNumChildren()==1){
            if(arraysize.jjtGetChild(0) instanceof ASTSizeAccess){
                printVariableLoad(file, functionTable, arraysize.name, "ID"); //reference
                file.println("  arraylength");
            }
        } else{
            if(!arraysize.value.equals("")){ //Size is an integer
                printVariableLoad(file, functionTable, arraysize.value, "Integer");
            } else { //Size is a variable
                printVariableLoad(file, functionTable, arraysize.name, "ID");
            }
        }

        file.println("  newarray int");
    }

    

    public void termToJvm(PrintWriter file, SymbolTable functionTable, Node node){

        ASTTerm term = (ASTTerm) node;

        if (term.jjtGetNumChildren() == 0){
            if(term.integer != "") { //simple integer
                
                String name = term.integer;
                if (term.operator.equals("-"))
                    name = '-' + name; //Negative number

                printVariableLoad(file, functionTable, name, "Integer");                

            }

        } else if(term.jjtGetNumChildren() == 1){
            if (term.jjtGetChild(0) instanceof ASTAccess) {
                accessToJvm(file, functionTable, term.jjtGetChild(0), "Load");
            } else if(term.jjtGetChild(0) instanceof ASTCall){
                callToJvm(file, functionTable, term.jjtGetChild(0), "int");
            }
        }


    }


    public void accessToJvm(PrintWriter file, SymbolTable functionTable, Node node, String mode){
        ASTAccess access = (ASTAccess) node;

        if(access.jjtGetNumChildren() == 0){
            if(mode.equals("Store")) //store
                printVariableStore(file, functionTable, access.name); 
            else //load
                printVariableLoad(file, functionTable, access.name, "ID");
        }
        else if(access.jjtGetNumChildren() == 1){
            if(access.jjtGetChild(0) instanceof ASTArrayAccess){
                if(mode.equals("Load")){ //Load
                    printVariableLoad(file, functionTable, access.name, "ID"); //reference
                    arrayaccessToJvm(file,functionTable, access.jjtGetChild(0));
                    file.println("  iaload");
                }
            }
            else if(access.jjtGetChild(0) instanceof ASTSizeAccess){
                printVariableLoad(file, functionTable, access.name, "ID"); //reference
                file.println("  arraylength");
            } 
        }
    }    

    public void arrayaccessToJvm(PrintWriter file, SymbolTable functionTable, Node node){
        ASTArrayAccess arrayAccess = (ASTArrayAccess) node;

        if(arrayAccess.jjtGetNumChildren() == 1){

            if(arrayAccess.jjtGetChild(0) instanceof ASTIndex){

                ASTIndex index = (ASTIndex) arrayAccess.jjtGetChild(0);

                functionTable.setMaxStack(3);

                if(!index.value.equals("")){ //Size is an integer
                    printVariableLoad(file, functionTable, index.value, "Integer");

                } else if(!index.name.equals("")){ //Size is a variable
                    printVariableLoad(file, functionTable, index.name, "ID");
                }

               
            }
        }
    }

    public void callToJvm(PrintWriter file, SymbolTable functionTable, Node node, String returnMode){

        ASTCall call = (ASTCall) node;
        ASTArgumentList argumentList = null;

        if(call.jjtGetNumChildren() > 0 && call.jjtGetChild(0) instanceof ASTArgumentList){

            argumentList = (ASTArgumentList) call.jjtGetChild(0);

            for(int i = 0; i < argumentList.jjtGetNumChildren(); i++){
                ASTArgument argument = (ASTArgument) argumentList.jjtGetChild(i);
                functionTable.setMaxStack(argumentList.jjtGetNumChildren() + 1);
                printVariableLoad(file,functionTable,argument.name, argument.type);
            }
        } 
        if(call.jjtGetNumChildren() == 0 && call.function.equals("main")){
            file.println("  aconst_null");
            file.println("  invokestatic " + this.moduleName.substring(9) + "/main([Ljava/lang/String;)V");
        } else if (call.module.equals("") && symbolTables.get(call.function)!=null) { //function belongs to this module
            file.println("  invokestatic " + this.moduleName.substring(9) + "/" + functionHeaderInvoke(call.function, argumentList, returnMode));
        } else {
            file.println("  invokestatic " + call.module +"/" + functionHeaderInvoke(call.function, argumentList, returnMode));
        }

    }

    public String functionHeaderInvoke(String functionName, ASTArgumentList arguments, String returnMode) {

        SymbolTable functionTable = this.symbolTables.get(functionName);
        String functionHeader = functionName + "(";

        if(arguments!= null){
            for (int i = 0; i < arguments.jjtGetNumChildren(); i++){
                ASTArgument argument = (ASTArgument) arguments.jjtGetChild(i);
                if (argument.type.equals("ID")){
                    if(functionTable != null){
                        ArrayList<Symbol> l = new ArrayList(functionTable.getParameters().values());
                        String type = l.get(i).getType();
                        //String type =  functionTable.getParameters().get(argument.name).getType();
                        if(type.equals("array")){
                            functionHeader = functionHeader + "[I";
                        }
                        else functionHeader = functionHeader + "I";
                    }
                    else  functionHeader = functionHeader + "I";
                }  
                else if (argument.type.equals("String"))
                    functionHeader = functionHeader + "Ljava/lang/String;";
                else if(argument.type.equals("Integer"))
                    functionHeader = functionHeader + "I";

            }
        }
       

        if(functionTable != null){ //form this mdule
            Symbol returnSymbol = functionTable.getReturnSymbol();
            if (returnSymbol != null) {
                if (returnSymbol.getType().equals("int"))
                    functionHeader = functionHeader + ")I";
                else if (returnSymbol.getType().equals("array"))
                    functionHeader = functionHeader + ")[I";
            } else
                functionHeader = functionHeader + ")V";
        } else{ //from external module
            if(returnMode.equals("void")){
                functionHeader = functionHeader + ")V";                
            }else if(returnMode.equals("int")){
                functionHeader = functionHeader + ")I";                                
            } else if(returnMode.equals("array")){
                functionHeader = functionHeader + ")[I";                                
            }
        } 


        return functionHeader;
    } 


    public void whileToJvm(PrintWriter file, SymbolTable functionTable, Node node){

        ASTWhile whileNode = (ASTWhile) node;

        functionTable.incLoopCounter();        
        int loop_nr=functionTable.getLoopCounter();

        file.println("loop"+loop_nr+":");
        for (int i = 0; i < whileNode.jjtGetNumChildren(); i++) {
            if(whileNode.jjtGetChild(i) instanceof ASTExprtest){
                exprtestToJvm(file, functionTable,  whileNode.jjtGetChild(i), loop_nr);    
                file.print("\n"); 
            }
            else{                 
                statementToJvm(file, functionTable, whileNode.jjtGetChild(i));
                //file.print("\n");   
            } 
        }

        file.println("  goto loop"+loop_nr + "\n");
        file.print("loop"+loop_nr+"_end:");


    }

    public void ifToJvm(PrintWriter file, SymbolTable functionTable, Node node){
        ASTIf ifNode = (ASTIf) node;

        boolean elseExists = false;

        functionTable.incLoopCounter();
        int loop_nr=functionTable.getLoopCounter();

        for (int i = 0; i < ifNode.jjtGetNumChildren(); i++) {
            if(ifNode.jjtGetChild(i) instanceof ASTExprtest){
                exprtestToJvm(file, functionTable,  ifNode.jjtGetChild(i), loop_nr);    
                file.print("\n"); 
            }
            else if(ifNode.jjtGetChild(i) instanceof ASTElse){
                elseExists = true;
                ASTElse elseNode = (ASTElse) ifNode.jjtGetChild(i);

                file.println("  goto loop"+loop_nr+"_next\n");
                file.println("loop"+loop_nr+"_end:");

                for (int j = 0; j < elseNode.jjtGetNumChildren(); j++) {
                    statementToJvm(file, functionTable, elseNode.jjtGetChild(j));
                    //file.print("\n");
                }

                file.print("loop"+loop_nr+"_next:");
            }
            else{                   
                statementToJvm(file, functionTable, ifNode.jjtGetChild(i));
                //file.print("\n"); 
            } 
        }

        if(!elseExists)
            file.print("loop"+loop_nr+"_end:");

    }

    public void exprtestToJvm(PrintWriter file, SymbolTable functionTable, Node node, int loop){

        ASTExprtest exprtest = (ASTExprtest) node;

        if (exprtest.jjtGetChild(0) instanceof ASTAccess && exprtest.jjtGetChild(1) instanceof ASTRhs){
            accessToJvm(file, functionTable, exprtest.jjtGetChild(0), "Load");            
            rhsToJvm(file, functionTable, exprtest.jjtGetChild(1));
        }


         //">" | "<" | "<=" | ">=" | "==" | "!=">
         switch(exprtest.operator){
            case ">":
                file.println("  if_icmple loop" + loop + "_end" );
                break;
            case "<":
                file.println("  if_icmpge loop" + loop + "_end" );
                break;
            case "<=":
                file.println("  if_icmpgt loop" + loop + "_end" );
                break;
            case ">=":
                file.println("  if_icmplt loop" + loop + "_end" );
                break;
            case "==":
                file.println("  if_icmpne loop" + loop + "_end" );
                break;
            case "!=":
                file.println("  if_icmpeq loop" + loop + "_end" );
                break;
            default:
                break;   
        }

        functionTable.setMaxStack(2);
        

    }

    public void printVariableStore(PrintWriter file, SymbolTable functionTable, String name) {
        
        if (functionTable != null && functionTable.getFromAll(name) != null) { //Local Variables
            Symbol variable = functionTable.getFromAll(name);
            if(variable.getType().equals("int")){ //ints
                if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                    file.println("  istore_" + variable.getRegister());
                else file.println("  istore " + variable.getRegister());
            }else{ //arrays
                if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                    file.println("  astore_" + variable.getRegister());
                else file.println("  astore " + variable.getRegister());
            }

        } else { //Global variable             
            Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(name);
            if (globalVariable != null) {
                String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                file.println("  putstatic " + this.moduleName.substring(9) + "/" + globalVariable.getName() + globalVariableType);
            }

        }
    }

    public void printVariableLoad(PrintWriter file, SymbolTable functionTable, String name, String type) {

        if (type.equals("ID")) {
            if (functionTable != null && functionTable.getFromAll(name) != null) { //Local Variables
                Symbol variable = functionTable.getFromAll(name);

                if(variable.getType().equals("int")){ //ints
                    if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                        file.println("  iload_" + variable.getRegister());
                    else file.println("  iload " + variable.getRegister());
                }
                else{//arrays
                    if(variable.getRegister() >= 0 && variable.getRegister()<=3)
                        file.println("  aload_" + variable.getRegister());
                    else file.println("  aload " + variable.getRegister());
                }
                

            } else { //Global variable             
                Symbol globalVariable = symbolTables.get(this.moduleName).getFromAll(name);

                if (globalVariable != null) {
                    String globalVariableType = globalVariable.getType() == "array" ? " [I" : " I";

                    file.println("  getstatic " + this.moduleName.substring(9) + "/" + globalVariable.getName() + globalVariableType);
                }

            }
        } else if (type.equals("Integer")) {

            int number = Integer.parseInt(name);
            if(number == -1){
                file.println("  iconst_m1");                
            }else if (number >= 0 && number <= 5)
                file.println("  iconst_" + number);
            else if(number >= -128 && number <= 127)
                file.println("  bipush " + number);
            else if(number >= -32768 && number <= 32767)
                file.println("  sipush " + number);
            else
                file.println("  ldc " + number);

        } else if (type.equals("String")) {

            file.println("  ldc " + name);
        }

    }


}