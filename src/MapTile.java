/**
 * Created by matt on 02/03/2016.
 * Used for AI pathfinding
 */
public class MapTile {
	private int score;
	private int x;
	private int y;

	public MapTile(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public MapTile(int[] position) {
		this.y = position[0];
		this.x = position[1];
	}

	public int getManhattenDistanceTo(MapTile tile) {
		score = Math.abs((tile.x - x) + (tile.y - y));
		return score;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
