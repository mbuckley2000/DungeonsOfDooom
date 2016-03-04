import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by matt on 03/03/2016.
 */
public class TraverseTask implements AITask {
	private Queue<Character> movements;
	private Bot bot;
	private AIMap map;
	private int[] destination;

	public TraverseTask(Bot bot, AIMap map, int[] destination) {
		this.bot = bot;
		this.map = map;
		this.destination = destination;
		generateMovements(map.getPath(bot.getPosition(), destination));
	}

	private void generateMovements(Stack<MapTile> path) {
		movements = new PriorityQueue<>();
		if (path != null) {
			while (path.size() > 1) {
				char movement = getMovement(path.pop(), path.peek());
				movements.add(movement);
				System.out.println(movement);
			}
		} else {
			System.err.println("Null path in traverse task.");
		}
	}

	private char getMovement(MapTile start, MapTile end) {
		char dir = 0;
		if (end.getX() - start.getX() > 0) {
			dir = 'E';
		} else if (end.getX() - start.getX() < 0) {
			dir = 'W';
		} else if (end.getY() - start.getY() > 0) {
			dir = 'S';
		} else if (end.getY() - start.getY() < 0) {
			dir = 'N';
		}
		return dir;
	}

	public String getNextCommand() {
		System.out.println("TraverseTask command get");
		if (!movements.isEmpty()) {
			return "MOVE " + movements.remove();
		} else {
			generateMovements(map.getPath(bot.getPosition(), destination));
			return getNextCommand();
		}
	}

	public boolean hasNextCommand() {
		if (bot.getPosition()[0] != destination[0] || bot.getPosition()[1] != destination[1]) {
			return true;
		} else {
			return false;
		}
	}
}
