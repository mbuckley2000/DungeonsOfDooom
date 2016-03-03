import java.util.Stack;

/**
 * Created by matt on 03/03/2016.
 */
public class TraverseTask implements AITask {
	Stack<Character> movements;

	public TraverseTask(Stack<MapTile> path) {
		movements = new Stack<>();
		int dir = 0;
		for (int i = 0; i < path.size() - 1; i++) {
			MapTile nextTile = path.get(i + 1);
			MapTile currentTile = path.get(i);
			if (nextTile.getX() - currentTile.getX() > 0) {
				dir = 3;
			} else if (nextTile.getX() - currentTile.getX() < 0) {
				dir = 1;
			} else if (nextTile.getY() - currentTile.getY() > 0) {
				dir = 0;
			} else if (nextTile.getY() - currentTile.getY() < 0) {
				dir = 2;
			}
			movements.add(dirIntToChar(dir));
		}
	}

	public String getNextCommand() {
		return "MOVE " + movements.pop();
	}

	private char dirIntToChar(int dir) {
		switch (dir) {
			case 0:
				return 'N';
			case 1:
				return 'E';
			case 2:
				return 'S';
			case 3:
				return 'W';
		}
		return 'X';
	}

	public boolean hasNextCommand() {
		if (movements.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
