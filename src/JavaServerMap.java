import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Game map
 * Stores level information
 * Can be loaded from txt files
 */
public class JavaServerMap implements IServerMap {

    private char[][] map;
    private String mapName;
    private int totalGoldOnMap;

    /**
     * Constructor
     */
    public JavaServerMap() {
        map = null;
        mapName = "";
        totalGoldOnMap = -1;
    }

    @Override
    public void saveMap(String filename) throws IOException {
    }

    /**
     * Reads a map from a given file with the format:
     * name <mapName>
     * win <totalGold>
     *
     * @param mapFile A File pointed to a correctly formatted map file
     */
    public void loadMap(String filename) {
        // a buffered reader for the map
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
        } catch (FileNotFoundException e) {
            try {
                reader = new BufferedReader(new FileReader(new File("maps", "example_map.txt")));
            } catch (FileNotFoundException e1) {
                System.err.println("no valid map name given and default file example_map.txt not found");
                System.exit(-1);
            }
        }

        try {
            map = readMap(reader);
        } catch (IOException e) {
            System.err.println("map file invalid or wrongly formatted");
            System.exit(-1);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    /**
     * Loads a map from a given BufferedReader
     *
     * @param reader Buffered reader to load map from
     * @return The map as a char array
     * @throws IOException Exception reading from BufferedReader
     */
    private char[][] readMap(BufferedReader reader) throws IOException {

        boolean error = false;
        ArrayList<char[]> tempMap = new ArrayList<>();
        int width = -1;

        String in = reader.readLine();
        if (in.startsWith("name")) {
            error = setName(in);
        }

        in = reader.readLine();
        if (in.startsWith("win")) {
            error = setWin(in);
        }

        in = reader.readLine();
        if (in.charAt(0) == '#' && in.length() > 1)
            width = in.trim().length();

        while (in != null && !error) {

            char[] row = new char[in.length()];
            if (in.length() != width)
                error = true;

            for (int i = 0; i < in.length(); i++) {
                row[i] = in.charAt(i);
            }

            tempMap.add(row);

            in = reader.readLine();
        }

        if (error) {
            setName("");
            setWin("");
            return null;
        }
        char[][] map = new char[tempMap.size()][width];

        for (int i = 0; i < tempMap.size(); i++) {
            map[i] = tempMap.get(i);
        }
        return map;
    }

    /**
     * Sets the amount of gold required by any player to win the game
     *
     * @param in The line of the map file that specified the win amount
     * @return True if the given win is valid and it has been set. False otherwise.
     */
    private boolean setWin(String in) {
        if (!in.startsWith("win "))
            return true;
        int win = 0;
        try {
            win = Integer.parseInt(in.split(" ")[1].trim());
        } catch (NumberFormatException n) {
            System.err.println("the map does not contain a valid win criteria!");
        }
        if (win < 0)
            return true;
        this.totalGoldOnMap = win;

        return false;
    }

    /**
     * Sets the name of the map. Used when reading a map file
     *
     * @param in The line of the file that specifies the name of the map
     * @return True if the given name is valid and the name has been set. False otherwise
     */
    private boolean setName(String in) {
        if (!in.startsWith("name ") && in.length() < 4)
            return true;
        String name = in.substring(4).trim();

        if (name.length() < 1)
            return true;

        this.mapName = name;

        return false;
    }


    /**
     * finds a random position for the player in the map.
     *
     * @return Return null; if no position is found or a position vector [y,x]
     */
    public int[] getFreeTile(Server server) {
        synchronized (map) {
            int[] pos = new int[2];
            Random rand = new Random();

            pos[0] = rand.nextInt(getMapHeight() - 1);
            pos[1] = rand.nextInt(getMapWidth() - 1);
            int counter = 1;
            while (getTile(pos[0], pos[1]) == '#' && !server.playerOnTile(pos[0], pos[1]) && counter < getMapHeight() * getMapWidth()) {
                pos[1] = (int) (counter * Math.cos(counter));
                pos[0] = (int) (counter * Math.sin(counter));
                counter++;
            }
            if (getTile(pos[0], pos[1]) == '#') {
                return null;
            } else {
                return pos;
            }
        }
    }


    /**
     * The method replaces a char at a given position of the map with a new char
     *
     * @param y    the vertical position of the tile to replace
     * @param x    the horizontal position of the tile to replace
     * @param tile the char character of the tile to replace
     * @return The old character which was replaced will be returned.
     */
    public char replaceTile(int y, int x, char tile) {
        synchronized (map) {
            char output = map[y][x];
            map[y][x] = tile;
            return output;
        }
    }

    /**
     * The method returns the Tile at a given location. The tile is not removed.
     *
     * @param y the vertical position of the tile to look at
     * @param x the horizontal position of the tile to look at
     * @return The character at the tile is returned
     */
    public char getTile(int y, int x) {
        if (y < 0 || x < 0 || y >= map.length || x >= map[0].length)
            return '#';

        return map[y][x];
    }

    /**
     * This method is used to retrieve a map view around a certain location.
     * The method should be used to get the look() around the player location.
     *
     * @param y        Y coordinate of the location
     * @param x        X coordinate of the location
     * @param lookSize The lookSize defining the area which will be returned.
     * @return Returns a view window as a 2D char array of tiles
     */
    public char[][] getLookWindow(int y, int x, int lookSize) {
        char[][] reply = new char[lookSize][lookSize];
        for (int i = 0; i < lookSize; i++) {
            for (int j = 0; j < lookSize; j++) {
                int posX = x + j - lookSize / 2;
                int posY = y + i - lookSize / 2;
                if (posX >= 0 && posX < getMapWidth() && posY >= 0 && posY < getMapHeight())
                    reply[j][i] = map[posY][posX];
                else
                    reply[j][i] = '#';
            }
        }
        reply[0][0] = 'X';
        reply[lookSize - 1][0] = 'X';
        reply[0][lookSize - 1] = 'X';
        reply[lookSize - 1][lookSize - 1] = 'X';
        return reply;
    }

    /**
     * @return The amount of gold required by a player to win the game
     */
    public int getWin() {
        return totalGoldOnMap;
    }

    /**
     * @return The map width
     */
    private int getMapWidth() {
        return map[0].length;
    }

    /**
     * @return The map height
     */
    private int getMapHeight() {
        return map.length;
    }

    /**
     * @return The total gold left on the map
     */
    public int countRemainingGold() {
        int goldCount = 0;
        for (char[] y : map) {
            for (char x : y) {
                if (x == 'G') {
                    goldCount++;
                }
            }
        }
        return goldCount;
    }
}