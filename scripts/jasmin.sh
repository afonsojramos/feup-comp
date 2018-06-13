#!/bin/bash

cd ..
jjtree proj.jjt
cd AST
javacc proj.jj
cd ..
javac Proj.java

#java Proj examples/test.yal color
#cd jasmin
#java -jar jasmin.jar test.j
#java test
#cd ..
#read -p " Press any key to continue..." -n1 -s


java Proj examples/array1.yal color
cd jasmin
java -jar jasmin.jar array1.j
java array1
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/array2.yal color
cd jasmin
java -jar jasmin.jar array2.j
java array2
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/aval1.yal color
cd jasmin
java -jar jasmin.jar aval1.j
java aval1
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval2.yal color
cd jasmin
java -jar jasmin.jar aval2.j
java aval2
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval3.yal color
cd jasmin
java -jar jasmin.jar aval3.j
java aval3
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval4.yal color
cd jasmin
java -jar jasmin.jar aval4.j
java aval4
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval5.yal color
cd jasmin
java -jar jasmin.jar aval5.j
java aval5
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval6.yal color
cd jasmin
java -jar jasmin.jar aval6.j
java aval6
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval7.yal color
cd jasmin
java -jar jasmin.jar aval7.j
java aval7
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/aval8.yal color
cd jasmin
java -jar jasmin.jar aval8.j
java aval8
cd ..
read -p " Press any key to continue..." -n1 -s

java Proj examples/library1.yal color
cd jasmin
java -jar jasmin.jar library1.j
#java library1
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/max_array.yal color
cd jasmin
java -jar jasmin.jar max_array.j
java max_array
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/max.yal color
cd jasmin
java -jar jasmin.jar max.j
java max
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/max1.yal color
cd jasmin
java -jar jasmin.jar max1.j
java max1
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/maxmin.yal color
cd jasmin
java -jar jasmin.jar maxmin.j
java maxmin
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/programa1.yal color
cd jasmin
java -jar jasmin.jar programa1.j
java programa1
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/programa2.yal color
cd jasmin
java -jar jasmin.jar programa2.j
java programa2
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/programa3.yal color
cd jasmin
java -jar jasmin.jar programa3.j
java programa3
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/sqrt.yal color
cd jasmin
java -jar jasmin.jar sqrt.j
java sqrt
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/array-init.yal color
cd jasmin
java -jar jasmin.jar arrayInit.j
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/constant-ranges.yal color
cd jasmin
java -jar jasmin.jar constantRanges.j
java constantRanges
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/call-main.yal color
cd jasmin
java -jar jasmin.jar callMain.j
java callMain
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/input-as-output.yal color
cd jasmin
java -jar jasmin.jar inputAsOutput.j
java inputAsOutput
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/nested-branch.yal color
cd jasmin
java -jar jasmin.jar nestedBranch.j
java nestedBranch
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/quicksort.yal color
cd jasmin
java -jar jasmin.jar quicksort.j
java quicksort
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/register-test.yal color
cd jasmin
java -jar jasmin.jar registerTest.j
java registerTest
cd ..
read -p " Press any key to continue..." -n1 -s


java Proj examples/stack-size.yal color
cd jasmin
java -jar jasmin.jar stackSize.j
java stackSize
cd ..
read -p " Press any key to continue..." -n1 -s


