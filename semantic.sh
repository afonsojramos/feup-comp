#!/bin/bash

jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

java Proj testsuite/semantic/all.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/semantic/test1.yal color
read -p " Press any key to continue..." -n1 -s


java Proj testsuite/semantic/test2.yal color
read -p " Press any key to continue..." -n1 -s