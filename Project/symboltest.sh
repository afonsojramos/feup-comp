#!/bin/bash

jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

java Proj yal-eval/MyFirstYalExamples/array0.yal color