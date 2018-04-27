#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java


java Proj yal-eval/MyFirstYalExamples/programa1.yal