/**
 * Used for Bot pathfinding
 * Contains an x and y position and a scoring system for pathfinding
 *
 * @author mb2070
 * @since 02/03/2016
 */
public class BotMapTile {
	private int g; //Used for pathfinding score
	private int h; //Used for pathfinding score
	private BotMapTile parent;
	private int x;
	private int y;

	public BotMapTile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public BotMapTile(int[] position) {
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

	public BotMapTile getParent() {
		return parent;
	}

	public void setParent(BotMapTile parent) {
		this.parent = parent;
	}

	public int getManhattanDistanceTo(BotMapTile tile) {
		return Math.abs(tile.x - x) + Math.abs(tile.y - y);
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
