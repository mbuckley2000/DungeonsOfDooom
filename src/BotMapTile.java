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

	/**
	 * Constructor
	 *
	 * @param x X position of the tile
	 * @param y Y position of the tile
	 */
	public BotMapTile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructor
	 *
	 * @param position Position of the tile
	 */
	public BotMapTile(int[] position) {
		this.y = position[0];
		this.x = position[1];
	}

	/**
	 * G is the number of steps from the first tile (+1 for every parent) for pathfinding
	 *
	 * @return G component of the score used for pathfinding
	 */
	public int getG() {
		return g;
	}

	/**
	 * @param g G component of the score used for pathfinding
	 */
	public void setG(int g) {
		this.g = g;
	}

	/**
	 * H is the manhattan distance from the destination tile for pathfinding
	 *
	 * @param h H component of the score used for pathfinding
	 */
	public void setH(int h) {
		this.h = h;
	}

	/**
	 * The parent tile is the previous tile in the path
	 *
	 * @return The tile's parent tile
	 */
	public BotMapTile getParent() {
		return parent;
	}

	/**
	 * @param parent The tile's parents
	 */
	public void setParent(BotMapTile parent) {
		this.parent = parent;
	}

	/**
	 * Manhattan distance is the sum of the X and Y distances between two tiles
	 *
	 * @param destTile Tile to calculate distance to
	 * @return The manhattan distance from this tile to the specified tile
	 */
	public int getManhattanDistanceTo(BotMapTile destTile) {
		return Math.abs(destTile.x - x) + Math.abs(destTile.y - y);
	}

	/**
	 * @return The tile score. Sum of G score and H score
	 */
	public int getScore() {
		return g + h;
	}

	/**
	 * @return X position of tile
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return Y position of tile
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return The tile position as a string (x, y)
	 */
	public String toString() {
		return x + ", " + y;
	}

	/**
	 * @return The tile position as an int array
	 */
	public int[] getPositionAsIntArray() {
		return new int[]{y, x};
	}
}