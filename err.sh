#!/bin/bash

cd Project

javacc proj.jj
javac *.java

java Scanner yal-eval/MyFirstYalExamples_1/array2_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/array4_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval1_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval2_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval3_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval4_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval5_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval6_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/aval7_err.yal 
java Scanner yal-eval/MyFirstYalExamples_1/err1.yal 