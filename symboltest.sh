#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java


java Proj yal-eval/MyFirstYalExamples/programa1.yal
java Proj yal-eval/MyFirstYalExamples/programa2.yal

