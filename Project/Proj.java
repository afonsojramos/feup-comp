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
    private String moduleName;
    private int registerCounter = 0;

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

                    if (module.jjtGetChild(i).jjtGetNumChildren() == 2) {
                        globalSymbolTable.addVariable(element.name, "array",-1);
                    } else {
                        globalSymbolTable.addVariable(element.name, "int",-1);
                    }
                //functions
                } else if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);

                    this.symbolTables.put(function.name, new SymbolTable());
                }
            }
            this.symbolTables.put(module.name, globalSymbolTable);
        }
    }

    public void fillFunctionSymbolTables(SimpleNode root){
        if (root != null && root instanceof ASTModule) {
            ASTModule module = (ASTModule) root;

            for (int i = 0; i < module.jjtGetNumChildren(); i++) {
                if (module.jjtGetChild(i) instanceof ASTFunction) {
                    ASTFunction function = (ASTFunction) module.jjtGetChild(i);
                    SymbolTable functionSymbolTable = this.symbolTables.get(function.name);

                    System.out.println("FUNCTION - > " + function.name);  //Debug

                    this.registerCounter = 0;

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
                                if (element.jjtGetNumChildren() == 1){
                                    if(functionSymbolTable.addParameter(element.name, "array", this.registerCounter))
                                        this.registerCounter++;
                                }   
                                else{
                                    if(functionSymbolTable.addParameter(element.name, "int", this.registerCounter))
                                        this.registerCounter++;
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

                            if(term.jjtGetNumChildren() > 0 && term.jjtGetChild(0) instanceof ASTCall) {
                                ASTCall call = (ASTCall) term.jjtGetChild(0);
                                String functionName = call.function;
                                String functionModule = call.module;

                                if(functionModule.equals("")) //if the functions belongs to this module
                                    type=symbolTables.get(functionName).getReturnSymbol().getType(); //gets that function return type
                                else type = "int"; //otherwise it's int
                            } else {
								for( int k = 0; k < term.jjtGetNumChildren(); k++) {

									if (term.jjtGetChild(k) instanceof ASTAccess) {
										ASTAccess access = (ASTAccess) term.jjtGetChild(k);
										
										if (functionSymbolTable.getParameters().get(access.name) == null && functionSymbolTable.getVariables().get(access.name) == null && !functionSymbolTable.getReturnSymbol().getName().equals(access.name)){
											System.out.println("STOP RIGHT THERE YOU CRIMINAL SCUM ---> " + access.name);  //Debug
										}
									}
								}
							}
                        }
                    }
                }
            }

            if(canAddVariable(functionSymbolTable, name, type, this.registerCounter)){
                if(functionSymbolTable.addVariable(name, type, this.registerCounter))
                    this.registerCounter++;
            }
		} 
		
		else if (node instanceof ASTExprtest) {
			ASTExprtest exprtest = (ASTExprtest) node;

			for (int i = 0; i < exprtest.jjtGetNumChildren(); i++) {
				if (exprtest.jjtGetChild(i) instanceof ASTAccess) {
					ASTAccess access = (ASTAccess) exprtest.jjtGetChild(i);
					
					if (functionSymbolTable.getParameters().get(access.name) == null && functionSymbolTable.getVariables().get(access.name) == null && !functionSymbolTable.getReturnSymbol().getName().equals(access.name)){
						System.out.println("STOP RIGHT THERE YOU CRIMINAL SCUM ---> " + access.name);  //Debug
					}
				}

				else if (exprtest.jjtGetChild(i) instanceof ASTRhs) {
					ASTRhs rhs = (ASTRhs) exprtest.jjtGetChild(i);

					for (int j = 0; j < rhs.jjtGetNumChildren(); j++) {

						if (rhs.jjtGetChild(j) instanceof ASTTerm) {
							ASTTerm term = (ASTTerm) rhs.jjtGetChild(j);

							for( int k = 0; k < term.jjtGetNumChildren(); k++) {

								if (term.jjtGetChild(k) instanceof ASTAccess) {
									ASTAccess access = (ASTAccess) term.jjtGetChild(k);
									
									if (functionSymbolTable.getParameters().get(access.name) == null && functionSymbolTable.getVariables().get(access.name) == null && !functionSymbolTable.getReturnSymbol().getName().equals(access.name)){
										System.out.println("STOP RIGHT THERE YOU CRIMINAL SCUM ---> " + access.name);  //Debug
									}
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

    public boolean canAddVariable(SymbolTable functionSymbolTable, String name, String type, int registerCounter){

        if (!this.symbolTables.get(this.moduleName).getVariables().containsValue(new Symbol(name, type, registerCounter))){//verify if the new symbol isn't on the module symbol table already
            if(!functionSymbolTable.getParameters().containsValue(new Symbol(name, type, registerCounter))){ //verify if the new symbol isn't on the function's parameters already
                if(functionSymbolTable.getReturnSymbol()!=null){ //if the function returns a symbol
                    if(!functionSymbolTable.getReturnSymbol().equals(new Symbol(name, type, registerCounter))) // if the return symbol isnt't the new one
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
            if(key.equals(this.moduleName))
                System.out.println(" > MODULE: " + key);
            else System.out.println(" > SCOPE: " + key);
            
            for (String parkey : symbolTables.get(key).getParameters().keySet()) {
                Symbol s = symbolTables.get(key).getParameters().get(parkey);
                System.out.println("   - Parameter Symbol: " + s.getName() + " - " + s.getType()  + " - " + s.getRegister());
            }

            for (String varkey : symbolTables.get(key).getVariables().keySet()) {
                Symbol s = symbolTables.get(key).getVariables().get(varkey);
                System.out.print("   - Variable Symbol: " + s.getName() + " - " + s.getType());
                if(s.getRegister()!=-1)
                    System.out.print(" - " + s.getRegister());
                System.out.print('\n');
            }

            if (symbolTables.get(key).getReturnSymbol() != null)
                System.out.println("   - Return Symbol: " + symbolTables.get(key).getReturnSymbol().getName() + " - "
                        + symbolTables.get(key).getReturnSymbol().getType()  + " - " + symbolTables.get(key).getReturnSymbol().getRegister());
        } 
    }

    public PrintWriter getFile(){

        try {
            File dir = new File("jvm");
            if (!dir.exists()) dir.mkdirs();

            File file = new File("jvm/" + this.moduleName + ".jvm");
            if(!file.exists()) file.createNewFile();

            PrintWriter writer = new PrintWriter(file);

            return writer;

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


    public void yalToJasmin(SimpleNode root){
        
        
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

    public void declarationsToJvm(PrintWriter file, ASTDeclaration declaration){

        ASTElement element = (ASTElement) declaration.jjtGetChild(0);

        String type = this.symbolTables.get(this.moduleName).getVariables().get(element.name).getType();

        if(type.equals("int")){
            file.print(".field static " + element.name + " I");

            if(!declaration.integer.equals("")){

                file.print(" = ");
                if(declaration.operator.equals("-")) file.print("-"); //negative number
                file.print(declaration.integer);
            }
            
            file.print("\n");
        }
            
        else if (type.equals("array")){
            file.println(".field static " + element.name + " [I");
        }
            
    }


    public void functionToJvm(PrintWriter file, ASTFunction function){

        SymbolTable functionTable = this.symbolTables.get(function.name);
        
        //function header

        file.println("\n.method public static ");
        if(function.name.equals("main")){
            file.println("main([Ljava/lang/String;)V");
        }
        else{
            file.println(functionHeader(function.name));
        }

        //function limits

        int nrParameters = (new ArrayList(functionTable.getParameters().keySet())).size();
        int nrVariables = (new ArrayList(functionTable.getVariables().keySet())).size();
        int nrReturn = functionTable.getReturnSymbol() != null ? 1 : 0;

        int nrLocals = nrParameters + nrVariables + nrReturn;
        int nrStack = nrLocals; //TODO: alterar para número correto

        file.println("  .limit stack " + nrStack);
        file.println("  .limit locals " + nrLocals +"\n");


        //function statements
        for (int i = 0; i < function.jjtGetNumChildren(); i++) {
            statementToJvm(file, functionTable, function.jjtGetChild(i));
        }
        
        //function return

        if(functionTable.getReturnSymbol()!= null){
            if(functionTable.getReturnSymbol().getType()=="int"){

                file.println("\n  iload " + functionTable.getReturnSymbol().getRegister());
                file.println("  ireturn");

            }else{ //array

                file.println("\n  aload " + functionTable.getReturnSymbol().getRegister());
                file.println("  areturn");

            }
        }
        else{ //void
            file.println("\n  return"); 
        }

        file.println(".end method");

  
    }

    public String functionHeader(String functionName){

        SymbolTable functionTable = this.symbolTables.get(functionName);

        String functionHeader = functionName + "(";

        
        for (Map.Entry<String, Symbol> entry : functionTable.getParameters().entrySet()) {
            String type = entry.getValue().getType();
                
            if(type.equals("array"))
                functionHeader=functionHeader+"[I";
            else
                functionHeader=functionHeader+"I";
        }


        Symbol returnSymbol = functionTable.getReturnSymbol();

        if(returnSymbol != null){
            if(returnSymbol.getType().equals("int"))
                functionHeader=functionHeader+")I";
            else if (returnSymbol.getType().equals("array"))
                functionHeader=functionHeader+")[I";
            }
            else 
                functionHeader=functionHeader+")V";

         return functionHeader;
    }

    public void statementToJvm(PrintWriter file, SymbolTable functionTable, Node node){

        if (node instanceof ASTAssign) {
            ASTAssign assign = (ASTAssign) node;

            if(assign.jjtGetNumChildren()==2){

                if(assign.jjtGetChild(0) instanceof ASTAccess && assign.jjtGetChild(1) instanceof ASTRhs){
                    ASTAccess access = (ASTAccess) assign.jjtGetChild(0);
                    ASTRhs rhs = (ASTRhs) assign.jjtGetChild(1);

                    if(rhs.jjtGetNumChildren()==1 ){

                        if(rhs.jjtGetChild(0) instanceof ASTTerm){
                            
                            ASTTerm term = (ASTTerm) rhs.jjtGetChild(0);

                            if(term.jjtGetNumChildren()==0 && term.integer!=""){
                                
                                int number =  Integer.parseInt(term.integer);

                                if(number < 6)
                                    file.println("  iconst_"+number);
                                else
                                    file.println("  bipush " + number);
                                

                                Symbol variable = functionTable.getFromAll(access.name);

                                if(variable != null){
                                    file.println("  istore " + variable.getRegister() );
                                }
                                else{
                                    //TODO: Global variable
                                }

                                
                            }

                            if(term.jjtGetNumChildren()==1 && term.jjtGetChild(0) instanceof ASTAccess){
                                
                                ASTAccess termAccess = (ASTAccess) term.jjtGetChild(0);

                                //i=b.size
                               

                            }

                        }
                        else if(rhs.jjtGetChild(0) instanceof ASTArraySize){
                            
                            ASTArraySize arraySize = (ASTArraySize) rhs.jjtGetChild(0);
                            
                             //a=[N]


                        }

                    }
                    
                }

            }

        
        }else if (node instanceof ASTCall) {
            ASTCall call = (ASTCall) node;

            if(call.module.equals("")){
                file.println("  invokestatic " + this.moduleName +"/" + functionHeader(call.function));
            }
            else{

                if(call.module.equals("io") && call.function.equals("println")){
                    if(call.jjtGetChild(0).jjtGetNumChildren()==2)
                        file.println("  invokestatic io/println(Ljava/lang/String;I)V");  
                    else
                        file.println("  invokestatic io/println(Ljava/lang/String)V");  
                    
                }

            }   
        }

    }
}