#!/bin/bash

jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

java Proj yal-eval/SemanticTests/ftl.yal color