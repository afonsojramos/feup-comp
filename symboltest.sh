#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

#java Proj yal-eval/SemanticTests/ftl.yal color
#java Proj yal-eval/all.yal color
#java Proj yal-eval/MyFirstYalExamples/programa1.yal color
java Proj yal-eval/MyFirstYalExamples/programa2.yal color
