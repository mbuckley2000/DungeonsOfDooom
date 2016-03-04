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

	public AIMap() {
		bounds[0] = offset;
		bounds[1] = offset;
		bounds[2] = offset;
		bounds[3] = offset;
	}

	public int[] getBounds() {
		return bounds;
	}

	public void update(char[][] lookWindow, int[] botPos) {
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

	public void setTile(int y, int x, char tile) {
		int offsetY = y + offset;
		int offsetX = x + offset;

		if (offsetX >= 0 && offsetX < map.length && offsetY >= 0 && offsetY < map.length) {
			map[offsetY][offsetX] = tile;
			if (offsetY < bounds[0]) bounds[0] = offsetY;
			if (offsetX < bounds[1]) bounds[1] = offsetX;
			if (offsetY > bounds[2]) bounds[2] = offsetY;
			if (offsetX > bounds[3]) bounds[3] = offsetX;
		} else {
			System.err.println("Out of bounds");
		}
	}

	public char getTile(int y, int x) {
		return (map[y + offset][x + offset]);
	}

	public void print() {
		for (int y = bounds[0]; y < bounds[2] + 1; y++) {
			for (int x = bounds[1]; x < bounds[3] + 1; x++) {
				System.out.print(map[y][x]);
				/*
				if (map[y][x] == '#' || map[y][x] == '.' || map[y][x] == 'G' || map[y][x] == 'E' || map[y][x] == 'X' || map[y][x] == 'P') {
					System.out.print(map[y][x]);
				} else {
					System.out.print("-");
				}
				*/
			}
			System.out.println();
		}
	}


	public Stack<MapTile> getPath(int[] start, int[] end) {
		//A* pathfinding
		HashSet<MapTile> closedList = new HashSet<>();
		HashSet<MapTile> openList = new HashSet<>();

		//Init
		MapTile startTile = new MapTile(start);
		MapTile endTile = new MapTile(end);
		startTile.setH(startTile.getManhattenDistanceTo(endTile));
		startTile.setG(0);
		openList.add(startTile);

		while (!openList.isEmpty()) {
			//Get best tile and add to closed list
			MapTile bestTile = getBestTile(openList);
			closedList.add(bestTile);
			//System.out.println("Added to closedList:" + bestTile.toString());
			openList.remove(bestTile);

			if (listContains(closedList, endTile)) {
				break;
			}

			//Check and score adjacent tiles
			for (MapTile t : getAdjacentTiles(bestTile)) {
				if (!listContains(closedList, t)) {
					if (!listContains(openList, t)) {
						//Calculate Score
						t.setG(bestTile.getG() + 1);
						t.setH(t.getManhattenDistanceTo(endTile));
						t.setParent(bestTile);
						openList.add(t);
						//System.out.println("Added to openList:" + t.toString());
					} else {
						//If T is already in the open list: Check if the F score is lower when we use the current generated path to get there.
						//If it is, update its score and update its parent as well.
					}
				}
			}
		}

		//Reverse engineer the path
		Stack<MapTile> path = new Stack<>();
		MapTile lastTile = getFromList(closedList, end);
		if (lastTile != null) {
			while (lastTile != null) {
				path.add(lastTile);
				lastTile = lastTile.getParent();
			}
			System.out.println("Found path! Number of tiles:" + path.size());
			return path;
		} else {
			System.out.println("Unable to find path");
			return null;
		}
	}

	private MapTile getFromList(HashSet<MapTile> list, int[] tile) {
		for (MapTile t : list) {
			if (t.getX() == tile[1] && t.getY() == tile[0]) {
				return t;
			}
		}
		return null;
	}

	private boolean listContains(HashSet<MapTile> list, MapTile tile) {
		for (MapTile t : list) {
			if (t.getX() == tile.getX() && t.getY() == tile.getY()) {
				return true;
			}
		}
		return false;
	}

	private MapTile getBestTile(HashSet<MapTile> openList) {
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

	private Set<MapTile> getAdjacentTiles(MapTile tile) {
		Set set = new HashSet();
		int x = tile.getX();
		int y = tile.getY();
		if (tileWalkable(y, x + 1)) {
			set.add(new MapTile(x + 1, y));
		}
		if (tileWalkable(y, x - 1)) {
			set.add(new MapTile(x - 1, y));
		}
		if (tileWalkable(y + 1, x)) {
			set.add(new MapTile(x, y + 1));
		}
		if (tileWalkable(y - 1, x)) {
			set.add(new MapTile(x, y - 1));
		}
		//System.out.println("Found " + set.size() + " adjacent tiles.");
		return set;
	}

	private boolean tileWalkable(int y, int x) {
		if (getTile(y, x) == '.' || getTile(y, x) == 'G' || getTile(y, x) == 'E') {
			return true;
		} else {
			return false;
		}
	}
}