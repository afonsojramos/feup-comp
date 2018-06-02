#!/bin/bash

cd Project

jjtree proj.jjt
javacc proj.jj
javac *.java