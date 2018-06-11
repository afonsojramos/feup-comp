PROJECT TITLE: Compiler of the yal0.4 language to Java Bytecodes

GROUP: G54

NAME1: Afonso Jorge Ramos, NR1: 201506239, GRADE1: 19, CONTRIBUTION1: 33,3%

NAME2: Bárbara Sofia Silva, NR2: 201505628, GRADE2: 19, CONTRIBUTION2: 33,3%

NAME3: David Falcão, NR3: 201506571, GRADE3: null, CONTRIBUTION3: 0%

NAME4: Julieta Frade, NR4: 201506530, GRADE4: 19, CONTRIBUTION4: 33,3%

...

GLOBAL Grade of the project: 19

--- IN DEVELOPMENT ---

SUMMARY:
The goal of this project was to apply the knowledge acquired in the course unit Compilers by building a compiler for programs in the yal language. The compiler produces valid Java Virtual Machine (JVM) instructions to Jasmin, a tool to generate Java bytecodes given assembly programs with JVM instructions.
The main features of the tool we developed are:
- Syntactic error controller
- Semantic analysis
- Code generation


EXECUTE:
To execute our tool you basically need to run the provided .jar using the following.
java -jar yal2jvm.jar <file>
Additionally you can add the argument "color" if your console supports it.
If you want to compile the project itself you can use our script build.sh. If you want to compile and run on of the provided examples, inside the Project folder, you can either run one of our scripts semantic.sh, jasmin.sh or sintatic.sh.
 
DEALING WITH SYNTACTIC ERRORS:
When there's something wrong with the syntactic of the yal code, the program has the capacity to tell the line and the specific "token" that was considered an error. There are no error limits, the program analyses the full file and indicates every found error. The tool also extracts from the original file the exact line where the error ocurred to show exactly where it is located.
 
SEMANTIC ANALYSIS:
We detect most semantic errors if not all, either inside nested operations, like ifs and whiles, or outside them. A file with over 40 semantic errors was delivered to demonstrate our tool's capacity. 
For displaying these errors we indicate the line, variable and type of error.

INTERMEDIATE REPRESENTATIONS (IRs):
None.
 
CODE GENERATION: 
If there aren't any semantic errors, it is generated the jasmin code for the yal file. If the generation is desired even with these type of errors, it will be necessary to comment the line 70 of Proj.java. 
As the AST is being analyzed for the code generation, each instruction is written straight into the corresponding file created in jasmin folder with the module name (saving it all in memory and write it in the file after everything could have scalability problems).
Firstly it's printed the module header and declarations. If there are global arrays to initialize, it's created the public method <clinit> to initialize all arrays necessary immediately after all the declarations. To accomplish this task, it's used a HashMap where the key is the variable name and the value the array size.
Next, the code for each yal function is generated, iterating every statement. The group tried to use the best possible instruction for each case, relatively to each instruction cost. An example of this is using 'iinc r n' when the add operations used the same variable in the right and left part of the formula. Another example is using the instructions of load and store until their limits. When loading constants were used the following instructions depending on the constant value:  'iconst_m1', 'iconst_n', 'bipush n', 'sipush n' and 'ldc n'. 
While generating the code for the function nodes, it's calculated the stack and the locals' number for each one. In the end, after iterating all of the AST, the calculated numbers are printed in the file in the correct place.
 
OVERVIEW:
In the end, we feel like we have made a highly funcitional tool with a lot of capabilities, however, if given the possibility and time, we would definitely taken a different approach for code structure, since after we realised that there was another way, it was too late for a refactor.

TESTSUITE AND TEST INFRASTRUCTURE:
There are three scripts to test each phase of the compiler: sintatic.sh, semantic.sh and jasmin.sh. These scripts try to compile a set of files present on the testsuits folder. The examples used for syntactic and jasmin were the ones available on Moodle, the semantic examples are files that the group created. The error files have the error signalized in the respective error lines and the jasmin files have the println expected value commented on the file. There aren't automated tests, the examination was made manually.

TASK DISTRIBUTION:
Afonso Jorge Ramos: Dealing with syntactic errors, Semantic analysis
Bárbara Sofia Silva: Code generation
Julieta Frade: Dealing with syntactic errors, Code generation

PROS:
Our tool does most of the requirements, only lacking on some parts of the optimizations.
When it comes to code generation, the pros of the functions are mainly the consideration of the instructions' cost and not simply use the one with a bigger range. Also, it is possible to fill both local and global arrays inside a function. Another positive aspect is the effort to calculate precisely the stack function, working in some hard cases as 'a[i]=b[i]' and 'c = d * g(e,f,g,h)'.

CONS:
The adjustments that can be made to the project are the optimizations proposed in the specification: register allocation, constant propagation, constant folding and while loops.
The existence of the instruction to compare a variable to zero was unknown at the time, therefore it wasn't used. However, because of the modular code structure, it would be easy to implement this improvement. Something else that we could have taken advantage of was polymorphism, however, due to lacking time, we weren't able to implement it.