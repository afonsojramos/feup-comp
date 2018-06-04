#!/bin/bash

cd Project

jjtree proj.jjt

cd AST

javacc proj.jj

cd ..

javac Proj.java