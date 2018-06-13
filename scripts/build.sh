#!/bin/bash

cd ..
jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java