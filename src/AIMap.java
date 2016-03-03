import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by matt on 29/02/2016.
 */
public class AIMap {
	final int lookSize = 5;
	private final int offset = 50;
	private int[] lastPos = {0, 0};
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

	public void update(char[][] lookWindow, int[] moveDelta) {
		lastPos[0] += moveDelta[0];
		lastPos[1] += moveDelta[1];
		replace(lastPos[0], lastPos[1], lookWindow);
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

	private void setTile(int y, int x, char tile) {
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

	private char getTile(int y, int x) {
		return (map[y + offset][x + offset]);
	}

	public void print() {
		for (int y = bounds[0]; y < bounds[2]; y++) {
			for (int x = bounds[1]; x < bounds[3]; x++) {
				if (map[y][x] == '#' || map[y][x] == '.' || map[y][x] == 'G' || map[y][x] == 'E' || map[y][x] == 'X' || map[y][x] == 'P') {
					System.out.print(map[y][x]);
				} else {
					System.out.print("");
				}
			}
			System.out.println();
		}
	}


	/*
	-Get the square on the open list which has the lowest score. Let’s call this square S.
	-Remove S from the open list and add S to the closed list.
	-For each square T in S’s walkable adjacent tiles:
		*If T is in the closed list: Ignore it.
		*If T is not in the open list: Add it and compute its score.
		*If T is already in the open list: Check if the F score is lower when we use the current generated path to get there. If it is, update its score and update its parent as well.
	 */

	/*

	At initialization add the starting location to the open list and empty the closed list
	While there are still more possible next steps in the open list and we haven’t found the target:
		-Select the most likely next step (based on both the heuristic and path costs)
		-Remove it from the open list and add it to the closed
		-Consider each neighbor of the step. For each neighbor:
			-Calculate the path cost of reaching the neighbor
			-If the cost is less than the cost known for this location then remove it from the open or closed lists (since we’ve now found a better route)
			-If the location isn’t in either the open or closed list then record the costs for the location and add it to the open list (this means it’ll be considered in the next search). Record how we got to this location
	 */
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
			openList.remove(bestTile);

			if (listContains(closedList, endTile)) {
				break;
				//Found the path!
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
		path.add(lastTile);
		while (!path.contains(startTile) && lastTile != null) {
			lastTile = lastTile.getParent();
			path.add(lastTile);
		}
		return path;
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