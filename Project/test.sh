#!/bin/bash

jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java


java Proj yal-eval/MyFirstYalExamples/aval5.yal color
cd jasmin
java -jar jasmin.jar aval5.j
java aval5
cd ..
read -p " Press any key to continue..." -n1 -s