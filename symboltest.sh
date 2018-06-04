#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

#java Proj yal-eval/SemanticTests/ftl.yal color
#java Proj yal-eval/all.yal color
#java Proj yal-eval/test.yal color


java Proj yal-eval/MyFirstYalExamples/array1.yal color
java Proj yal-eval/MyFirstYalExamples/array2.yal color
java Proj yal-eval/MyFirstYalExamples/aval1.yal color
java Proj yal-eval/MyFirstYalExamples/aval2.yal color





