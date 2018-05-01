#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

java Proj yal-eval/MyFirstYalExamples/array1.yal color
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/array2.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval1.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval2.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval3.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval4.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval5.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval6.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval7.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/aval8.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/library1.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/max_array.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/max.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/max1.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/maxmin.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/programa1.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/programa2.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/programa3.yal color 
read -p "Press any key to continue... " -n1 -s
java Proj yal-eval/MyFirstYalExamples/sqrt.yal color