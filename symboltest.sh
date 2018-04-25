#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java


java Proj yal-eval/MyFirstYalExamples/aval2.yal 
java Proj yal-eval/MyFirstYalExamples/aval4.yal