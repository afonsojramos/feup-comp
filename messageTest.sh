#!/bin/bash

cd Project
jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

java Proj yal-eval/MyFirstYalExamples_1/aval6_err.yal 