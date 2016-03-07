import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * A discoverable map class used by the Bot to keep track of the map structure as well as gold and exit positions
 * Can be updated by being given the bot position and a look window received at that positions
 * Includes tile searching and pathfinding functionality
 *
 * @author mb2070
 * @since 29/02/2016
 */
public class AIMap {
	private final int lookSize = 5;
	private final int offset = 200; //Large array to handle any map size;
	private char[][] map;
	private int[] bounds;
	private boolean empty;

	/**
	 * Constructs the AI Map
	 */
	public AIMap() {
		map = new char[offset * 2][offset * 2];
		bounds = new int[4];
		empty = true;
		bounds[0] = offset;
		bounds[1] = offset;
		bounds[2] = offset;
		bounds[3] = offset;
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

	/**
	 * Finds a MapTile object in a given Set using it's co-ordinates. Used for pathfinding
	 *
	 * @param set     Set of MapTiles to search
	 * @param tilePos Coordinates of MapTile to find
	 * @return The found MapTile object, or null if there is no such tile in the set
	 */
	private MapTile findInSet(HashSet<MapTile> set, int[] tilePos) {
		for (MapTile t : set) {
			if (t.getX() == tilePos[1] && t.getY() == tilePos[0]) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Returns the current bounds of the map. Used because most of the map is empty, and grows from the middle outwards as the bot discovers more tiles
	 * @return The bounds as an array: (top, left, bottom, right)
	 */
	public int[] getBounds() {
		return bounds;
	}

	/**
	 * Updates the map with a given lookWindow and bot position
	 * @param lookWindow The look window to use
	 * @param botPos The bot's position when the look window was received
	 */
	public void update(char[][] lookWindow, int[] botPos) {
		empty = false;
		replace(botPos[0] - lookSize / 2, botPos[1] - lookSize / 2, lookWindow);
	}

	/**
	 * Replaces a section of the map with a given lookWindow at a given position
	 * @param posY Given position
	 * @param posX Given position
	 * @param lookWindow Given lookWindow
	 */
	private void replace(int posY, int posX, char[][] lookWindow) {
		for (int x = 0; x < lookSize; x++) {
			for (int y = 0; y < lookSize; y++) {
				if (lookWindow[y][x] != 'X' && lookWindow[y][x] != 'P') {
					setTile(posY + y, posX + x, lookWindow[y][x]);
				}
			}
		}
	}

	/**
	 * Finds the first occurrence of a tileType in the map
	 * @param tileType The type of tile to find
	 * @return The found tile, or null if no such tile is found
	 */
	public int[] findTile(char tileType) {
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[0].length; x++) {
				if (map[y][x] == tileType) {
					return new int[]{y - offset, x - offset};
				}
			}
		}
		return null;
	}

	/**
	 * Finds all occurrences of the given tileType in the map
	 * @param tileType The type of tile to find
	 * @return A list of all occurrences of the tile
	 */
	public ArrayList<int[]> findAllTiles(char tileType) {
		ArrayList<int[]> list = new ArrayList<>();
		for (int y = bounds[0] - 1; y < bounds[2] + 2; y++) {
			for (int x = bounds[1] - 1; x < bounds[3] + 2; x++) {
				if (tileType != ' ') {
					if (map[y][x] == tileType) {
						list.add(new int[]{y - offset, x - offset});
					}
				} else {
					if (tileDiscovered(y, x)) {
						list.add(new int[]{y - offset, x - offset});
					}
				}
			}
		}
		return list;
	}

	/**
	 * Checks if the given tile has been discovered
	 * @param y Tile position
	 * @param x Tile position
	 * @return True if the tile has been discovered, false otherwise
	 */
	public boolean tileDiscovered(int y, int x) {
		return map[y][x] != 'G' && map[y][x] != 'E' && map[y][x] != '#' && map[y][x] != '.';
	}

	/**
	 * Sets the tile at given coordinates to the given tileType
	 * Also adjusts the map bounds
	 * @param y Position of tile
	 * @param x Position of tile
	 * @param tileType Given tileType
	 */
	public void setTile(int y, int x, char tileType) {
		int offsetY = y + offset;
		int offsetX = x + offset;

		if (tileInBounds(y, x)) {
			map[offsetY][offsetX] = tileType;
			//Adjust bounds
			if (offsetY < bounds[0]) bounds[0] = offsetY;
			if (offsetX < bounds[1]) bounds[1] = offsetX;
			if (offsetY > bounds[2]) bounds[2] = offsetY;
			if (offsetX > bounds[3]) bounds[3] = offsetX;
		} else {
			System.err.println("Trying to set tile out of bounds on map");
		}
	}

	/**
	 * Returns the tileType at the given location
	 * @param y Given location
	 * @param x Given location
	 * @return The tile type, or X if the tile is out of bounds
	 */
	public char getTile(int y, int x) {
		if (tileInBounds(y, x)) {
			return (map[y + offset][x + offset]);
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
				if (y == botPosition[0] + offset && x == botPosition[1] + offset) {
					System.out.print("B");
				} else {
					System.out.print(map[y][x]);
				}
			}
			System.out.println();
		}
	}


	/**
	 * A* pathfinding implementation to find a path on the AIMap from the specified start location to the specified end location
	 * @param start Start location
	 * @param end End location
	 * @return The path int he form of a stack of map tiles. Null if no path is found
	 */
	public Stack<MapTile> getPath(int[] start, int[] end) {
		//A* pathfinding
		HashSet<MapTile> closedList = new HashSet<>();
		HashSet<MapTile> openList = new HashSet<>();

		//Init
		MapTile startTile = new MapTile(start);
		MapTile endTile = new MapTile(end);
		startTile.setH(startTile.getManhattanDistanceTo(endTile));
		startTile.setG(0);
		openList.add(startTile);

		while (!openList.isEmpty()) {
			//Get best tile and add to closed list
			MapTile bestTile = findLowestScore(openList);
			closedList.add(bestTile);
			//System.out.println("Added to closedList:" + bestTile.toString());
			openList.remove(bestTile);

			if (listContains(closedList, endTile)) {
				//System.out.println("Found the last tile.");
				break;
			}

			//Check and score adjacent tiles
			for (MapTile t : findAdjacentWalkableTiles(bestTile)) {
				if (!listContains(closedList, t)) {
					if (!listContains(openList, t)) {
						//Calculate Score
						t.setG(bestTile.getG() + 1);
						t.setH(t.getManhattanDistanceTo(endTile));
						t.setParent(bestTile);
						openList.add(t);
						//System.out.println("Added to openList:" + t.toString());
					}
				}
			}
		}

		//Reverse engineer the path
		Stack<MapTile> path = new Stack<>();
		MapTile lastTile = findInSet(closedList, end);
		if (lastTile != null) {
			while (lastTile != null) {
				path.add(lastTile);
				lastTile = lastTile.getParent();
			}
			//System.out.println("Found path! Number of tiles:" + path.size());
			if (path.size() > 1) {
				return path;
			} else {
				return null;
			}
		} else {
			//System.out.println("Unable to find path");
			return null;
		}
	}

	private boolean listContains(HashSet<MapTile> list, MapTile tile) {
		for (MapTile t : list) {
			if (t.getX() == tile.getX() && t.getY() == tile.getY()) {
				return true;
			}
		}
		return false;
	}

	public boolean tileReachable(int[] start, int[] end) {
		return (getPath(start, end) != null);
	}

	private MapTile findLowestScore(HashSet<MapTile> openList) {
		MapTile lowest = null;
		for (MapTile tile : openList) {
			if (lowest == null) {
				lowest = tile;
			} else if (lowest.getScore() > tile.getScore()) {
				lowest = tile;
			}
		}
		return lowest;
	}

	public Set<MapTile> findAdjacentWalkableTiles(MapTile tile) {
		Set<MapTile> set = new HashSet<>();
		int x = tile.getX();
		int y = tile.getY();

		if (tileWalkable(y, x + 1))
				set.add(new MapTile(x + 1, y));
		if (tileWalkable(y, x - 1))
				set.add(new MapTile(x - 1, y));
		if (tileWalkable(y + 1, x))
				set.add(new MapTile(x, y + 1));
		if (tileWalkable(y - 1, x))
				set.add(new MapTile(x, y - 1));

		return set;
	}

	public boolean tileWalkable(int y, int x) {
		return tileInBounds(y, x) && (getTile(y, x) == 'E' || getTile(y, x) == 'G' || getTile(y, x) == '.');
	}

	public boolean tileInBounds(int y, int x) {
		return (y + offset > 0 && x + offset > 0 && y + offset < map.length && x + offset < map.length);
	}

	public boolean isEmpty() {
		return empty;
	}
}