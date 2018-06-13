#!/bin/bash

cd ..
jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

java Proj testsuite/sintatic/array2_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/array4_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval1_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval2_err.yal color

read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval3_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval4_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval5_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval6_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/aval7_err.yal color
read -p " Press any key to continue..." -n1 -s

java Proj testsuite/sintatic/err1.yal color
read -p " Press any key to continue..." -n1 -s