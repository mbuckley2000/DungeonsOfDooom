#include "CServerMap.h"
#include "CGameLogic.h"
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>
#include <errno.h>

char* name;
char** map;
int goldToWin;
int mapSize[] = {1, 1};

char **mapMalloc(int xSize, int ySize) {
    printf("Allocating map: %d, %d\n", xSize, ySize);
    fflush(stdout);
    char **tempMap;
    tempMap = malloc(sizeof(char *) * ySize);
    for (int y = 0; y < ySize; y++) {
        tempMap[y] = malloc(sizeof(char) * xSize);
    }
    return tempMap;
}

void resizeMap(int xSize, int ySize) {
    printf("Resizing map: %d, %d\n", xSize, ySize);
    fflush(stdout);
    if (ySize >= mapSize[1] && xSize >= mapSize[0]) {
        char **tempMap = mapMalloc(xSize, ySize);
        printf("Copying %d bytes of map\n", mapSize[1] * sizeof(char *));
        fflush(stdout);
        memcpy(tempMap, map, mapSize[1] * sizeof(char *));
        free(map);
        map = tempMap;
        mapSize[0] = xSize;
        mapSize[1] = ySize;
    } else {
        printf("ERROR: Cannot resize map to a smaller size\n");
        fflush(stdout);
    }
    printf("Resize successful\n");
    fflush(stdout);
}

int mapRowLength(char *mapRow) {
    bool done = false;
    int count = 1;
    int lastHash = 0;
    if (mapRow[0] == '#') {
        while (!done) {
            if (mapRow[count] == '\0') done = true;
            if (mapRow[count] == '#') lastHash = count + 1;
            count++;
        }
        return lastHash;
    } else {
        return 0;
    }
}

void addMapRow(char *tempLine, int mapRow) {
    int charsToCopy = (mapRowLength(tempLine));
    if (mapSize[1] < mapRow + 1) resizeMap(mapSize[0], mapRow + 1);
    if (mapSize[0] < strlen(tempLine) + 1) resizeMap(charsToCopy, mapSize[1]);
    printf("Copying line to map\n");
    fflush(stdout);
    memcpy(map[mapRow], tempLine, charsToCopy * sizeof(char));
}

char **readMap(FILE *file) {
    bool error = false;

    //Load map from file
    int mapRow = 0;
    char *buffer = malloc(sizeof(char) * 1000);

    while (!feof(file)) {
        char *tempLine = fgets(buffer, 1000, file);
        printf("Read line number %d: %s\n", mapRow, tempLine);
        fflush(stdout);

        if (tempLine[0] == '#') {
            addMapRow(tempLine, mapRow);
            mapRow++;
        }

        //Check for win and name
        if (tempLine[0] == 'w') {
            goldToWin = atoi(tempLine + 4);
            printf("Detected Win: %d\n", goldToWin);
        }
        if (tempLine[0] == 'n') {
            name = strdup(tempLine + 5);
            printf("Detected Name: %s\n", name);
        }
        //free(tempLine); //WHY DOES THIS GIVE AN ERROR
    }
    printf("Reached EOF\n");
    fflush(stdout);

    //free(buffer); //WHY DOES THIS GIVE AN ERROR
}

int writeMap(FILE *file) {
    fprintf(file, "name %s", name);
    fprintf(file, "win %d", goldToWin);

    for (int y = 0; y < mapSize[1]; y++) {
        fprintf(file, "\n%s", map[y]);
    }

}

void printMap() {
    printf("Map size: %d, %d\n", mapSize[0], mapSize[1]);
    for (int y = 0; y < mapSize[1]; y++) {
        for (int x = 0; x < mapSize[0]; x++) {
            printf("%c", map[y][x]);
        }
        printf("\n");
    }
    fflush(stdout);
}

int loadMap(const char *filename) {
    map = mapMalloc(1, 1);
    FILE *file = fopen(filename, "r");
    if (file != NULL) {
        readMap(file);
        printMap();
        fclose(file);
        return 0;
    } else {
        printf("Couldn't open map file: %s\n", strerror(errno));
        return 1;
    }
}

int saveMap(char *filename) {
    FILE *file = fopen(filename, "w");
    if (file != NULL) {
        int writeError = writeMap(file);
        fclose(file);
        return writeError;
    } else {
        printf("Couldn't open map file: %s\n", strerror(errno));
        return 1;
    }
}

char getTile(int x, int y) {
    if (y < 0 || x < 0 || y >= mapSize[1] - 1 || x >= mapSize[0] - 1) return '#';
    return map[y][x];
}

bool playerOnTile(int tileX, int tileY) {
    return false;
}

int *getFreeTile() {
    int *pos = malloc(sizeof(int) * 2);
    int searchLimit = mapSize[0] * mapSize[1];

    pos[0] = rand() % (mapSize[0] - 1);
    pos[1] = rand() % (mapSize[1] - 1);

    bool failed = false;

    int counter = 1;
    while (getTile(pos[0], pos[1]) == '#' && !playerOnTile(pos[0], pos[1])) {
        pos[0] = rand() % (mapSize[0] - 1);
        pos[1] = rand() % (mapSize[1] - 1);
        if (counter++ > searchLimit) {
            failed = true;
            break;
        }
    }

    if (!failed) {
        return pos;
    } else {
        return NULL;
    }
}

char replaceTile(int x, int y, char tile) {
    if (y < 0 || x < 0 || y >= mapSize[1] || x >= mapSize[0]) return '#';
    char output = map[y][x];
    map[y][x] = tile;
    return output;
}

char **getLookWindow(int tileX, int tileY, int lookSize) {
    char **reply = mapMalloc(lookSize, lookSize);
    for (int x = 0; x < lookSize; x++) {
        for (int y = 0; y < lookSize; y++) {
            int posX = tileX + x - lookSize / 2;
            int posY = tileY + y - lookSize / 2;
            if (posX >= 0 && posX < mapSize[0] && posY >= 0 && posY < mapSize[1])
                reply[y][x] = getTile(posX, posY);
            else
                reply[y][x] = '#';
        }
    }
    reply[0][0] = 'X';
    reply[lookSize - 1][0] = 'X';
    reply[0][lookSize - 1] = 'X';
    reply[lookSize - 1][lookSize - 1] = 'X';
    return reply;
}

int countRemainingGold() {
    int goldCount = 0;
    for (int x = 0; x < mapSize[0]; x++) {
        for (int y = 0; y < mapSize[1]; y++) {
            if (getTile(x, y) == 'G') goldCount++;
        }
    }
    return goldCount;
}


////////////////////////////////////////////////////LINKS///////////////////////////////////////////////////////////////////
/*
 * Class:     CServerMap
 * Method:    loadMap
 * Signature: (Ljava/io/File;)V
 */
JNIEXPORT void JNICALL Java_CServerMap_loadMap(JNIEnv *env, jobject obj, jstring jFilename) {
    const char *filename = (*env)->GetStringUTFChars(env, jFilename, 0);
    loadMap(filename);
}

/*
 * Class:     CServerMap
 * Method:    getFreeTile
 * Signature: (LServer;)[I
 */
JNIEXPORT jintArray JNICALL Java_CServerMap_getFreeTile(JNIEnv *env, jobject obj1, jobject obj2) {
    int *freeTile = getFreeTile();
    int response[] = {freeTile[1], freeTile[0]};
    jintArray outJNIArray = (*env)->NewIntArray(env, 2);
    if (outJNIArray == NULL) return NULL;
    (*env)->SetIntArrayRegion(env, outJNIArray, 0, 2, response);
}

/*
 * Class:     CServerMap
 * Method:    replaceTile
 * Signature: (IIC)C
 */
JNIEXPORT jchar JNICALL Java_CServerMap_replaceTile(JNIEnv *env, jobject obj, jint y, jint x, jchar c) {
    char response = replaceTile((int) x, (int) y, (char) c);
    return (jchar) response;
}

/*
 * Class:     CServerMap
 * Method:    getTile
 * Signature: (II)C
 */
JNIEXPORT jchar JNICALL Java_CServerMap_getTile(JNIEnv *env, jobject obj, jint y, jint x) {
    char response = getTile(x, y);
    return (jchar) response;
}

/*
 * Class:     CServerMap
 * Method:    getLookWindow
 * Signature: (III)[[C
 */
JNIEXPORT jobjectArray JNICALL Java_CServerMap_getLookWindow(JNIEnv *env, jobject obj, jint y, jint x, jint lookSize) {
    char **response = getLookWindow(x, y, lookSize);
    //TODO: return
}

/*
 * Class:     CServerMap
 * Method:    getWin
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_CServerMap_getWin(JNIEnv *env, jobject obj) {
    return (jint) goldToWin;
}

/*
 * Class:     CServerMap
 * Method:    countRemainingGold
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_CServerMap_countRemainingGold(JNIEnv *env, jobject obj) {
    return (jint) countRemainingGold();
}




























































































//Gamelogic stuff
struct Player *players;
int playerCount = 0;

struct Player {
    int id;
    int lookSize;
    char *lastLookWindow;
    int *position;
    int collectedGold;
    jobject *obj;
};

struct Player *createPlayer() {
    int id = playerCount++;
    players = realloc(players, sizeof(struct Player *) * (id + 1));
    players[id].id = id;
    return players + id;
}

struct Player *getPlayer(jobject *obj) {
    for (int i = 0; i < playerCount; i++) {
        if (players[i].obj == obj) {
            return players + i;
        }
    }
    return createPlayer();
}

char *strintcat(char *s, int i) {
    char *buffer = malloc(1024);
    sprintf(buffer, "%d", i);
    char *output = strcat(s, buffer);
    free(buffer);
    return output;
}

char *strcharcat(char *s, char c) {
    char *buffer = malloc(1024);
    sprintf(buffer, "%c", c);
    char *output = strcat(s, buffer);
    free(buffer);
    return output;
}

int getGoldNeeded(struct Player *player) {
    return (goldToWin - player->collectedGold);
}

char *hello(struct Player *player) {
    return strintcat("H", getGoldNeeded(player));
}


char *move(struct Player *player, char direction) {
    int newPosition[] = {player->position[0], player->position[1]};

    switch (direction) {
        case 'N':
            newPosition[0] -= 1;
            break;
        case 'E':
            newPosition[1] += 1;
            break;
        case 'S':
            newPosition[0] += 1;
            break;
        case 'W':
            newPosition[1] -= 1;
            break;
        default:
            return ("MF");
    }

    if (getTile(newPosition[0], newPosition[1]) != '#' && !playerOnTile(newPosition[0], newPosition[1])) {
        player->position[0] = newPosition[0];
        player->position[1] = newPosition[1];
        return "MS";
    } else {
        return "MF";
    }
}


char *pickup(struct Player *player) {
    int *pos = player->position;
    if (getTile(pos[0], pos[1]) == 'G') {
        player->collectedGold++;
        replaceTile(pos[0], pos[1], '.');
        return strintcat("PS", player->collectedGold);
    }
    return "PF";
}


char *look(struct Player *player) {
    int *pos = player->position;
    int lookSize = player->lookSize;
    char *response = strintcat("L", lookSize);
    char **lookReply = getLookWindow(pos[0], pos[1], lookSize);

    for (int i = 0; i < lookSize; i++) {
        for (int j = 0; j < lookSize; j++) {
            if (playerOnTile(pos[0] - (lookSize / 2) + i, pos[1] - (lookSize / 2) + j)) {
                response = strcat(response, "P");
            } else {
                response = strcharcat(response, lookReply[j][i]);
            }
        }
        if (i != lookSize - 1) {
            response = strcat(response, strintcat("\nL", lookSize));
        }
    }

    player->lastLookWindow = response;
    return response;
}


bool checkWin(struct Player *player) {
    return (player->collectedGold >= goldToWin && getTile(player->position[0], players->position[1]) == 'E');
}



//////////////////////////////////////////LINKS/////////////////////////////////////////////////////
/*
 * Class:     CGameLogic
 * Method:    hello
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_CGameLogic_hello(JNIEnv *env, jobject obj) {
    char *response = hello(getPlayer(&obj));
    return (*env)->NewStringUTF(env, response);
}

/*
 * Class:     CGameLogic
 * Method:    move
 * Signature: (C)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_CGameLogic_move(JNIEnv *env, jobject obj, jchar dir) {
    char *response = move(getPlayer(&obj), (char) dir);
    return (*env)->NewStringUTF(env, response);
}

/*
 * Class:     CGameLogic
 * Method:    pickup
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_CGameLogic_pickup(JNIEnv *env, jobject obj) {
    char *response = pickup(getPlayer(&obj));
    return (*env)->NewStringUTF(env, response);
}

/*
 * Class:     CGameLogic
 * Method:    look
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_CGameLogic_look(JNIEnv *env, jobject obj) {
    char *response = look(getPlayer(&obj));
    return (*env)->NewStringUTF(env, response);
}

/*
 * Class:     CGameLogic
 * Method:    getPlayerPosition
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_CGameLogic_getPlayerPosition(JNIEnv *env, jobject obj) {
    struct Player *player = getPlayer(&obj);
    int response[] = {player->position[1], player->position[0]};
    jintArray outJNIArray = (*env)->NewIntArray(env, 2);
    if (outJNIArray == NULL) return NULL;
    (*env)->SetIntArrayRegion(env, outJNIArray, 0, 2, response);
}

/*
 * Class:     CGameLogic
 * Method:    setPlayerPosition
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL Java_CGameLogic_setPlayerPosition(JNIEnv *env, jobject obj, jintArray pos) {
    struct Player *player = getPlayer(&obj);
    jint *cPos = (*env)->GetIntArrayElements(env, pos, NULL);
    if (cPos != NULL) {
        player->position[0] = cPos[1];
        player->position[1] = cPos[0];
    }
}

/*
 * Class:     CGameLogic
 * Method:    getLastLookWindow
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_CGameLogic_getLastLookWindow(JNIEnv *env, jobject obj) {
    char *response = getPlayer(&obj)->lastLookWindow;
    return (*env)->NewStringUTF(env, response);
}

/*
 * Class:     CGameLogic
 * Method:    getGoldNeeded
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_CGameLogic_getGoldNeeded(JNIEnv *env, jobject obj) {
    return (jint) getGoldNeeded(getPlayer(&obj));
}

/*
 * Class:     CGameLogic
 * Method:    checkWin
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_CGameLogic_checkWin(JNIEnv *env, jobject obj) {
    return (jboolean) checkWin(getPlayer(&obj));
}