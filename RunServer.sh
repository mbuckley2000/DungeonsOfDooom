#!/bin/sh
echo "Updating from GitHub..."
git pull https://mbuckley2000:OrganicFruit97@github.com/mbuckley2000/DungeonsOfDooom.git master
echo "Compiling server..."
cd src
javac -d ../out/ *.java
echo "Running server..."
cd ..
java -cp out Server nogui
