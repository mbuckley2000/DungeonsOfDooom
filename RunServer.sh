#!/bin/sh
echo "Updating from GitHub..."
git pull https://github.com/mbuckley2000/DungeonsOfDooom.git master
echo "Compiling server..."
mkdir out
cd src
javac -d ../out/ *.java
echo "Running server..."
cd ..
java -cp out Server nogui
