/**
 * Created by matt on 02/03/2016.
 * Used for AI pathfinding
 */
public class MapTile {
	private int g;
	private int h;
	private MapTile parent;
	private int x;
	private int y;

	public MapTile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public MapTile(int x, int y, MapTile parent) {
		this.x = x;
		this.y = y;
		this.parent = parent;

	}
	public MapTile(int[] position) {
		this.y = position[0];
		this.x = position[1];
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public void setH(int h) {
		this.h = h;
	}

	public MapTile getParent() {
		return parent;
	}

	public void setParent(MapTile parent) {
		this.parent = parent;
	}

	public int getManhattenDistanceTo(MapTile tile) {
		int distance = Math.abs(tile.x - x) + Math.abs(tile.y - y);
		return distance;
	}

	public int getScore() {
		return g + h;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String toString() {
		return x + ", " + y;
	}

	public int[] toIntArray() {
		return new int[]{y, x};
	}
}
