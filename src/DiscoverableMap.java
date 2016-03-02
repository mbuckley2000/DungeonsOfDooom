import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by matt on 29/02/2016.
 */
public class DiscoverableMap {
	final int lookSize = 5;
	private final int offset = 20;
	private int[] lastPos = {0, 0};
	private char[][] map = new char[offset * 2][offset * 2];
	private int[] bounds = new int[4];

	public DiscoverableMap() {
		bounds[0] = offset;
		bounds[1] = offset;
		bounds[2] = offset;
		bounds[3] = offset;
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

		map[offsetY][offsetX] = tile;
		if (offsetY < bounds[0]) bounds[0] = offsetY;
		if (offsetX < bounds[1]) bounds[1] = offsetX;
		if (offsetY > bounds[2]) bounds[2] = offsetY;
		if (offsetX > bounds[3]) bounds[3] = offsetX;
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
	public Queue<Character> getPath(int[] start, int[] end) {
		Queue<Character> path = new LinkedBlockingDeque<>();
		Queue<MapTile> closedList = new PriorityQueue<>();
		Queue<MapTile> openList = new PriorityQueue<>();

		closedList.add(new MapTile(start));
		//A* pathfinding

		return path;
	}

	private void addAdjacentTiles(MapTile tile, PriorityQueue<MapTile> openList) {
		int x = tile.getX();
		int y = tile.getY();
		if (tileWalkable(map[y][x + 1])) {
			openList.add(new MapTile(x + 1, y));
		}
		if (tileWalkable(map[y][x - 1])) {
			openList.add(new MapTile(x - 1, y));
		}
		if (tileWalkable(map[y + 1][x])) {
			openList.add(new MapTile(x, y + 1));
		}
		if (tileWalkable(map[y - 1][x])) {
			openList.add(new MapTile(x, y - 1));
		}
	}

	private boolean tileWalkable(char tile) {
		if (tile == '.' || tile == 'G' || tile == 'E') {
			return true;
		} else {
			return false;
		}
	}
}