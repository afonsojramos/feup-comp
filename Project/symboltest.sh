#!/bin/bash

jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

#java Proj yal-eval/SemanticTests/ftl.yal color
#java Proj yal-eval/all.yal color
#java Proj yal-eval/test.yal color


java Proj yal-eval/MyFirstYalExamples/array1.yal color
java Proj yal-eval/MyFirstYalExamples/array2.yal color
java Proj yal-eval/MyFirstYalExamples/aval1.yal color
java Proj yal-eval/MyFirstYalExamples/aval2.yal color
java Proj yal-eval/MyFirstYalExamples/programa1.yal color
java Proj yal-eval/MyFirstYalExamples/programa2.yal color