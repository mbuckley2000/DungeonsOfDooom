#!/bin/sh
echo "Updating from GitHub..."
git pull https://github.com/mbuckley2000/DungeonsOfDooom.git master
echo "Compiling client..."
mkdir out
cd src
javac -d ../out/ *.java
echo "Running client..."
cd ..
java -cp out PlayGame
