#!/bin/bash

cd Project/AST

jjtree proj.jjt
javacc proj.jj

cd ..

javac *.java