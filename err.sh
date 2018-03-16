#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

java yal2jvm yal-eval/MyFirstYalExamples_1/array2_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/array4_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval1_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval2_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval3_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval4_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval5_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval6_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/aval7_err.yal 
read -p "Press any key to continue... " -n1 -s
java yal2jvm yal-eval/MyFirstYalExamples_1/err1.yal 