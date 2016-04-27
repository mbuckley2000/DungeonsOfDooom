#!/bin/bash
echo "Copying c from clion project"
rm CServerMap.c
cp /home/matt/ClionProjects/cDoD/src/CServerMap.c CServerMap.c

echo "Deleting old things"
rm CGameLogic.h
rm CServerMap.h
rm libCDoD.so

echo "Making headers"
javah -cp ../src CGameLogic
javah -cp ../src CServerMap

#Make CServerMap.o
#echo "Making CServerMap.o"
#gcc -I"/usr/java/jdk1.7.0_79/include" -I"/usr/java/jdk1.7.0_79/include/linux" -c CServerMap.c -o CServerMap.o
#Make libCDoD.so
#echo "Making libCDoD.so"
#gcc -I"/usr/java/jdk1.7.0_79/include" -I"/usr/java/jdk1.7.0_79/include/linux" -o libCDoD.so -shared -Wl,-soname,CServerMap.o -static -lc -lm

echo "Compiling the library"
gcc -fPIC -shared -I"/usr/java/jdk1.7.0_79/include" -I"/usr/java/jdk1.7.0_79/include/linux" -o libCDoD.so CServerMap.c -lm -lc

echo "Cleaning it all up"
rm CGameLogic.h
rm CServerMap.h
rm CServerMap.c

echo "Running"
cd ..
java -cp out/production/DungeonsOfDooom PlayGame&
java -cp out/production/DungeonsOfDooom Server nogui
