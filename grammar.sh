#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

java yal2jvm yal-eval/MyFirstYalExamples/array1.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/array2.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval1.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval2.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval3.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval4.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval5.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval6.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval7.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/aval8.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/library1.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/max_array.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/max.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/max1.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/maxmin.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/programa1.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/programa2.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/programa3.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples/sqrt.yal