import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * A discoverable map class used by the Client to keep track of the map structure as well as gold and exit positions
 * Can be updated given the current client position and a look window received at that position
 * Includes tile searching and pathfinding functionality
 *
 * @author Matt Buckley
 * @since 29/02/2016
 */
public class ClientMap {
    private int[] offset; //Large array to handle any map size;
    private char[][] map;
    private int[] bounds;
    private boolean empty;
    private int lookSize;

    /**
     * Constructs the AI JavaServerMap
     * Initialises the bounds to the middle of the map
     */
    public ClientMap() {
        lookSize = 5;
        offset = new int[]{20, 20};
        map = new char[offset[0] * 2][offset[1] * 2];
        bounds = new int[4];
        empty = true;
        bounds[0] = offset[0];
        bounds[1] = offset[1];
        bounds[2] = offset[0];
        bounds[3] = offset[0];
    }

    /**
     * Gets the manhattan distance between two tiles (X dist + Y dist)
     * This is the heuristic used for pathfinding
     *
     * @param t1 Tile 1
     * @param t2 Tile 2
     * @return Manhattan distance between t1 and t2
     */
    static int getManhattanDistance(int[] t1, int[] t2) {
        return Math.abs(t1[0] - t2[0]) + Math.abs(t1[1] - t2[1]);
    }

    public int[] getOffset() {
        return offset;
    }

    /**
     * Finds a BotMapTile object in a given Set using it's co-ordinates. Used for pathfinding
     *
     * @param set     Set of MapTiles to search
     * @param tilePos Coordinates of BotMapTile to find
     * @return The found BotMapTile object, or null if there is no such tile in the set
     */
    private BotMapTile findInSet(HashSet<BotMapTile> set, int[] tilePos) {
        for (BotMapTile t : set) {
            if (t.getX() == tilePos[1] && t.getY() == tilePos[0]) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns the current bounds of the map. Used because most of the map is empty, and grows from the middle outwards as the bot discovers more tiles
     *
     * @return The bounds as an array: (top, left, bottom, right)
     */
    public int[] getBounds() {
        return bounds;
    }

    /**
     * Updates the map with a given getLookWindow and bot position
     *
     * @param lookWindow The look window to use
     * @param botPos     The bot's position when the look window was received
     */
    public void update(char[][] lookWindow, int[] botPos) {
        synchronized (map) {
            empty = false;
            lookSize = lookWindow.length + 1;
            replace(botPos[0] - lookWindow.length / 2, botPos[1] - lookWindow.length / 2, lookWindow);
        }
    }

    public int getLookSize() {
        return lookSize;
    }

    /**
     * Replaces a section of the map with a given getLookWindow at a given position
     *
     * @param posY       Given position
     * @param posX       Given position
     * @param lookWindow Given getLookWindow
     */
    private void replace(int posY, int posX, char[][] lookWindow) {
        if (lookWindow[0].length == lookWindow[lookWindow.length - 1].length) {
            for (int x = 0; x < lookWindow.length; x++) {
                for (int y = 0; y < lookWindow.length; y++) {
                    if (lookWindow[y][x] != 'X' && !(x == lookWindow.length / 2 && y == lookWindow.length / 2)) {
                        setTile(posY + y, posX + x, lookWindow[y][x]);
                    }
                }
            }
        }
    }

    /**
     * Finds the first occurrence of a tileType in the map
     *
     * @param tileType The type of tile to find
     * @return The found tile, or null if no such tile is found
     */
    public int[] findTile(char tileType) {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] == tileType) {
                    return new int[]{y - offset[0], x - offset[1]};
                }
            }
        }
        return null;
    }

    /**
     * Finds all occurrences of the given tileType in the map
     * ' ' (space) represents an empty tile
     *
     * @param tileType The type of tile to find
     * @return A list of all occurrences of the tile
     */
    public ArrayList<int[]> findAllTiles(char tileType) {
        if (bounds[0] - 2 < 0 || bounds[1] - 2 < 0 || bounds[2] + 2 > map.length || bounds[3] + 2 > map[0].length) {
            expandMapArray(new int[]{6, 6});
        }
        ArrayList<int[]> list = new ArrayList<>();
        for (int y = bounds[0] - 2; y < bounds[2] + 2; y++) {
            for (int x = bounds[1] - 2; x < bounds[3] + 2; x++) {
                if (tileType != ' ') {
                    if (map[y][x] == tileType) {
                        list.add(new int[]{y - offset[1], x - offset[0]});
                    }
                } else {
                    if (!tileDiscovered(y, x)) {
                        list.add(new int[]{y - offset[1], x - offset[0]});
                    }
                }
            }
        }
        return list;
    }

    public void clearFloor() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map[y][x] == '.') {
                    map[y][x] = ' ';
                }
            }
        }
    }

    /**
     * Checks if the given tile has been discovered
     *
     * @param y Tile position
     * @param x Tile position
     * @return True if the tile has been discovered, false otherwise
     */
    private boolean tileDiscovered(int y, int x) {
        return map[y][x] == 'G' || map[y][x] == 'E' || map[y][x] == '#' || map[y][x] == '.' || map[y][x] == 'P';
    }

    /**
     * Sets the tile at given coordinates to the given tileType
     * Also adjusts the map bounds
     *
     * @param y        Position of tile
     * @param x        Position of tile
     * @param tileType Given tileType
     */
    public void setTile(int y, int x, char tileType) {
        int offsetY = y + offset[0];
        int offsetX = x + offset[1];

        if (tileInArrayBounds(y, x)) {
            map[offsetY][offsetX] = tileType;
            //Adjust bounds
            if (offsetY < bounds[0]) bounds[0] = offsetY;
            if (offsetX < bounds[1]) bounds[1] = offsetX;
            if (offsetY > bounds[2]) bounds[2] = offsetY;
            if (offsetX > bounds[3]) bounds[3] = offsetX;
        } else {
            expandMapArray(new int[]{5, 5});
            setTile(y, x, tileType);
        }
    }

    /**
     * Returns the tileType at the given location
     *
     * @param y Given location
     * @param x Given location
     * @return The tile type, or X if the tile is out of bounds
     */
    public char getTile(int y, int x) {
        if (tileInArrayBounds(y, x)) {
            return (map[y + offset[0]][x + offset[1]]);
        } else {
            return 'X';
        }
    }

    /**
     * Prints the map in it's currently discovered state. Ignores all other players
     * Includes the bot at given position
     *
     * @param botPosition Current position of the bot
     */
    public void print(int[] botPosition) {
        for (int y = bounds[0]; y < bounds[2] + 1; y++) {
            for (int x = bounds[1]; x < bounds[3] + 1; x++) {
                if (y == botPosition[0] + offset[0] && x == botPosition[1] + offset[1]) {
                    System.out.print("B");
                } else {
                    System.out.print(map[y][x]);
                }
            }
            System.out.println();
        }
    }

    /**
     * A* pathfinding implementation to find a path from the specified start location to the specified end location
     *
     * @param start Start location
     * @param end   End location
     * @return The path in the form of a stack of MapTiles. Null if no path is found
     */
    public Stack<BotMapTile> getPath(int[] start, int[] end) {
        //A* pathfinding
        HashSet<BotMapTile> closedList = new HashSet<>();
        HashSet<BotMapTile> openList = new HashSet<>();

        //Init
        BotMapTile startTile = new BotMapTile(start);
        BotMapTile endTile = new BotMapTile(end);
        startTile.setH(startTile.getManhattanDistanceTo(endTile));
        startTile.setG(0);
        openList.add(startTile);

        while (!openList.isEmpty()) {
            //Get best tile and add to closed list
            BotMapTile bestTile = findLowestScore(openList);
            closedList.add(bestTile);
            openList.remove(bestTile);

            if (setContains(closedList, endTile)) {
                //Found the destination tile
                break;
            }

            //Check and score adjacent tiles
            for (BotMapTile t : findAdjacentWalkableTiles(bestTile)) {
                if (!setContains(closedList, t)) {
                    if (!setContains(openList, t)) {
                        //Calculate Score
                        t.setG(bestTile.getG() + 1);
                        t.setH(t.getManhattanDistanceTo(endTile));
                        t.setParent(bestTile);
                        openList.add(t);
                    }
                }
            }
        }

        //Reverse engineer the path
        Stack<BotMapTile> path = new Stack<>();
        BotMapTile lastTile = findInSet(closedList, end);
        if (lastTile != null) {
            while (lastTile != null) {
                path.add(lastTile);
                lastTile = lastTile.getParent();
            }
            if (path.size() > 1) {
                return path;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns true if the given set contains a tile with the same co-ordinates as the given tile. Used for pathfinding
     *
     * @param set  Set of tiles to search
     * @param tile Tile to search for
     * @return True if the given set contains a tile with the same co-ordinates as the given tile. False otherwise
     */
    private boolean setContains(HashSet<BotMapTile> set, BotMapTile tile) {
        for (BotMapTile t : set) {
            if (t.getX() == tile.getX() && t.getY() == tile.getY()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a path can be calculated from the given start tile to the given end tile
     * NOTE: if a tile is not reachable with the map in its current state, it may be reachable after more of the map is discovered
     *
     * @param start Start of the desired path
     * @param end   End of the desired path
     * @return True if a path is possible, false otherwise
     */
    public boolean tileReachable(int[] start, int[] end) {
        return (getPath(start, end) != null);
    }

    /**
     * Finds the tile with the lowest score in the given set. Used for pathfinding
     *
     * @param set The set of tiles to search
     * @return The tile with the lowest score in the given set
     */
    private BotMapTile findLowestScore(HashSet<BotMapTile> set) {
        BotMapTile lowest = null;
        for (BotMapTile tile : set) {
            if (lowest == null) {
                lowest = tile;
            } else if (lowest.getScore() > tile.getScore()) {
                lowest = tile;
            }
        }
        return lowest;
    }

    /**
     * Returns a set of all walkable tiles adjacent to the given tile. Used for pathfinding
     *
     * @param tile The given tile
     * @return Set of all walkable tiles adjacent to the given tile. Used for pathfinding
     */
    public Set<BotMapTile> findAdjacentWalkableTiles(BotMapTile tile) {
        Set<BotMapTile> set = new HashSet<>();
        int x = tile.getX();
        int y = tile.getY();

        if (tileWalkable(y, x + 1))
            set.add(new BotMapTile(x + 1, y));
        if (tileWalkable(y, x - 1))
            set.add(new BotMapTile(x - 1, y));
        if (tileWalkable(y + 1, x))
            set.add(new BotMapTile(x, y + 1));
        if (tileWalkable(y - 1, x))
            set.add(new BotMapTile(x, y - 1));

        return set;
    }

    /**
     * Checks if a tile is walkable. Used for pathfinding
     * NOTE: Undiscovered tiles are assumed to be walkable!
     *
     * @param y Position of tile
     * @param x Position of tile
     * @return True if the tile is walkable, false otherwise
     */
    public boolean tileWalkable(int y, int x) {
        return tileInArrayBounds(y, x) && (getTile(y, x) == 'E' || getTile(y, x) == 'G' || getTile(y, x) == '.');
    }

    /**
     * Checks if a tile is inside the bounds of the map array
     *
     * @param y Position of tile
     * @param x Position of tile
     * @return True if the tile is inside the bounds of the map array, false otherwise
     */
    private boolean tileInArrayBounds(int y, int x) {
        return (y + offset[0] > 0 && x + offset[1] > 0 && y + offset[0] < map.length && x + offset[1] < map[0].length);
    }

    /**
     * Checks if the map has been updated, or if it is still in an empty state
     *
     * @return True if the map is empty, false otherwise
     */
    public boolean isEmpty() {
        return empty;
    }

    public char[][] getAsArray() {
        synchronized (map) {
            char[][] filledMap = new char[bounds[2] - bounds[0]][bounds[3] - bounds[1]];
            for (int i = bounds[0]; i < bounds[2]; i++) {
                System.arraycopy(map[i], bounds[1], filledMap[i - bounds[0]], 0, bounds[3] - bounds[1]);
            }
            return filledMap;
        }
    }

    private void expandMapArray(int[] offsetDelta) {
        synchronized (map) {
            char[][] oldMap = map;
            this.offset[0] += offsetDelta[0];
            this.offset[1] += offsetDelta[1];
            map = new char[offset[0] * 2][offset[1] * 2];
            for (int i = 0; i < oldMap.length; i++) {
                System.arraycopy(oldMap[i], 0, map[i + offsetDelta[0]], offsetDelta[1], oldMap[i].length);
            }
            bounds[0] += offsetDelta[0];
            bounds[2] += offsetDelta[0];
            bounds[1] += offsetDelta[1];
            bounds[3] += offsetDelta[1];
        }
    }
}