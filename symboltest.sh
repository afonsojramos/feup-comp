#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java

java yal2jvm yal-eval/MyFirstYalExamples/programa1.yal 