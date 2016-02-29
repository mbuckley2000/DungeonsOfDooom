/**
 * Created by matt on 29/02/2016.
 */
public class DiscoverableMap {
	private final int OFFSET = 100;
	private int[] lastPos = new int[2];
	private char[][] map = new char[200][200];

	public DiscoverableMap() {
	}

	public void update(char[][] lookWindow, int[] moveDelta) {
		lastPos[0] += moveDelta[0];
		lastPos[1] += moveDelta[1];
		replace(lastPos[0], lastPos[1], lookWindow);
	}

	private void replace(int posY, int posX, char[][] lookWindow) {
		for (int x = 0; x < 6; x++) {
			for (int y = 0; y < 6; y++) {
				setTile(posY + y, posX + x, lookWindow[y][x]);
			}
		}
	}

	private void setTile(int y, int x, char tile) {
		map[y + OFFSET][x + OFFSET] = tile;
	}

	private char getTile(int y, int x) {
		return (map[y + OFFSET][x + OFFSET]);
	}
}