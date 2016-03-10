# DungeonsOfDooom
DungeonsOfDooom Prog Coursework Semester 2

Usage:
1. Compile all files from the src folder into the out folder
2. Run Server.class in the background from the root of the zip (where the maps folder is located)
3. Run Bot.class if you would like a bot
4. Run PlayGame.class if you would like to play


Default port 40004
Default address localhost


To Compile:
    From 'src' directory:
        mkdir ../out
        javac *.java -d ../out



To Run:
    From root of extracted zip:
        COMMAND:                    OPTIONAL ARGUMENTS:         WHAT IT DOES:
        java -cp out Server         PORT                        ##RUN SERVER WITH SPECIFIED PORT (0-65535 exclusive)
        java -cp out PlayGame       ADDRESS PORT                ##RUN PLAYABLE CLIENT WITH SPECIFIED ADDRESS AND PORT
        java -cp out Bot            ADDRESS PORT                ##RUN BOT WITH SPECIFIED ADDRESS AND PORT


    Example running server and bot on localhost:12343:
    java -cp out Server 12343&      (Ampersand runs server in background)
    java -cp Bot localhost 12343