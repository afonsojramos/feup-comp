#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

java yal2jvm yal-eval/MyFirstYalExamples_1/aval2_err.yal