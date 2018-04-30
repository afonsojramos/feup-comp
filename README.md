# feup-comp
Projects for the Compilers (COMP) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP). 

To build our compiler, one must run the following commands inside of the folder `Project`:

1. `jjtree proj.jjt`
2. `javacc proj.jj`
3. `javac *.java`

Optionally, one can just run our script `./build.sh`.

Our class name is `yal2jvm`, and, for that reason, running the compiler is essencialy like this:

`java yal2jvm yal-eval/MyFirstYalExamples/array1.yal`

You can also have color coding in the output to be easier to analyze by using the `color` flag like this:

`java yal2jvm yal-eval/MyFirstYalExamples/array1.yal color`

Made by [Afonso Ramos](https://github.com/AJRamos308), [Julieta Frade](https://github.com/julietafrade97), [Sofia Silva](https://github.com/literallysofia) and [David Falcão](https://github.com/davidrsfalcao).