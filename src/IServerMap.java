import java.io.File;
import java.io.IOException;

/**
 * Created by Matt Buckley on 26/04/16.
 */
public interface IServerMap {
    /**
     * Loads a map from a given BufferedReader
     *
     * @param reader Buffered reader to load map from
     * @return The map as a char array
     * @throws IOException Exception reading from BufferedReader
     */
    void loadMap(File mapFile) throws IOException;

    /**
     * finds a random position for the player in the map.
     *
     * @return Return null; if no position is found or a position vector [y,x]
     */
    int[] getFreeTile(Server server);

    /**
     * The method replaces a char at a given position of the map with a new char
     *
     * @param y    the vertical position of the tile to replace
     * @param x    the horizontal position of the tile to replace
     * @param tile the char character of the tile to replace
     * @return The old character which was replaced will be returned.
     */
    char replaceTile(int y, int x, char tile);

    /**
     * The method returns the Tile at a given location. The tile is not removed.
     *
     * @param y the vertical position of the tile to look at
     * @param x the horizontal position of the tile to look at
     * @return The character at the tile is returned
     */
    char getTile(int y, int x);

    /**
     * This method is used to retrieve a map view around a certain location.
     * The method should be used to get the look() around the player location.
     *
     * @param y        Y coordinate of the location
     * @param x        X coordinate of the location
     * @param lookSize The lookSize defining the area which will be returned.
     * @return Returns a view window as a 2D char array of tiles
     */
    char[][] getLookWindow(int y, int x, int lookSize);

    /**
     * @return The amount of gold required by a player to win the game
     */
    int getWin();

    /**
     * @return The total gold left on the map
     */
    int countRemainingGold();
}
