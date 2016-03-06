import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by matt on 29/02/2016.
 */
public class AIMap {
	final int lookSize = 5;
	private final int offset = 50;
	private char[][] map = new char[offset * 2][offset * 2];
	private int[] bounds = new int[4];
	private boolean empty = true;

	public AIMap() {
		bounds[0] = offset;
		bounds[1] = offset;
		bounds[2] = offset;
		bounds[3] = offset;
		/*
		for (char[] y : map) {
			for (char x : y) {
				x = ' ';
			}
		}
		*/
	}

	static int getManhattanDistance(int[] start, int[] end) {
		return Math.abs(start[0] - end[0]) + Math.abs(start[1] - end[1]);
	}

	private MapTile findInList(HashSet<MapTile> list, int[] tile) {
		for (MapTile t : list) {
			if (t.getX() == tile[1] && t.getY() == tile[0]) {
				return t;
			}
		}
		return null;
	}

	public int[] getBounds() {
		return bounds;
	}

	public void update(char[][] lookWindow, int[] botPos) {
		empty = false;
		replace(botPos[0] - lookSize / 2, botPos[1] - lookSize / 2, lookWindow);
	}

	private void replace(int posY, int posX, char[][] lookWindow) {
		for (int x = 0; x < lookSize; x++) {
			for (int y = 0; y < lookSize; y++) {
				if (lookWindow[y][x] != 'X' && lookWindow[y][x] != 'P') {
					setTile(posY + y, posX + x, lookWindow[y][x]);
				}
			}
		}
	}

	public int[] findTile(char tile) {
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[0].length; x++) {
				if (map[y][x] == tile) {
					return new int[]{y - offset, x - offset};
				}
			}
		}
		return null;
	}

	public ArrayList<int[]> findAllTiles(char tile) {
		ArrayList<int[]> list = new ArrayList<>();
		for (int y = bounds[0] - 1; y < bounds[2] + 2; y++) {
			for (int x = bounds[1] - 1; x < bounds[3] + 2; x++) {
				if (tile != ' ') {
					if (map[y][x] == tile) {
						list.add(new int[]{y - offset, x - offset});
					}
				} else {
					if (tileEmpty(y, x)) {
						list.add(new int[]{y - offset, x - offset});
					}
				}
			}
		}
		return list;
	}

	public boolean tileEmpty(int y, int x) {
		return map[y][x] != 'G' && map[y][x] != 'E' && map[y][x] != '#' && map[y][x] != '.';
	}

	public void setTile(int y, int x, char tile) {
		int offsetY = y + offset;
		int offsetX = x + offset;

		if (tileOnMap(y, x)) {
			map[offsetY][offsetX] = tile;
			//Adjust bounds
			if (offsetY < bounds[0]) bounds[0] = offsetY;
			if (offsetX < bounds[1]) bounds[1] = offsetX;
			if (offsetY > bounds[2]) bounds[2] = offsetY;
			if (offsetX > bounds[3]) bounds[3] = offsetX;
		} else {
			System.err.println("Trying to set tile out of bounds on map");
		}
	}

	public char getTile(int y, int x) {
		if (tileOnMap(y, x)) {
			return (map[y + offset][x + offset]);
		} else {
			return 'X';
		}
	}

	/**
	 * Prints the map in it's currently discovered state. Ignores all other players
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
		MapTile lastTile = findInList(closedList, end);
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
		return tileOnMap(y, x) && (getTile(y, x) == 'E' || getTile(y, x) == 'G' || getTile(y, x) == '.');
	}

	public boolean tileOnMap(int y, int x) {
		return (y + offset > 0 && x + offset > 0 && y + offset < map.length && x + offset < map.length);
	}

	public boolean isEmpty() {
		return empty;
	}
}