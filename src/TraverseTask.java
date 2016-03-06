import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by matt on 03/03/2016.
 */
public class TraverseTask implements AITask {
	private Bot bot;
	private AIMap map;
	private int[] destination;
	private boolean persistant;
	private boolean running;

	public TraverseTask(Bot bot, AIMap map, int[] destination, boolean persistant) {
		this.bot = bot;
		this.map = map;
		this.persistant = false;
		this.destination = destination;
		running = true;
	}

	private Queue<Character> generateMovements(Stack<MapTile> path) {
		Queue<Character> movements = new PriorityQueue<>();
		System.out.print("Movements: ");
		if (path != null) {
			while (path.size() > 1) {
				char movement = getMovement(path.pop(), path.peek());
				movements.add(movement);
				System.out.print(movement + ", ");
			}
			System.out.println();
		} else {
			System.err.println("Trying to generate movements from a null path.");
		}
		return movements;
	}

	private char getMovement(MapTile t1, MapTile t2) {
		char dir = 0;
		if (t2.getX() - t1.getX() > 0) {
			dir = 'E';
		} else if (t2.getX() - t1.getX() < 0) {
			dir = 'W';
		} else if (t2.getY() - t1.getY() > 0) {
			dir = 'S';
		} else if (t2.getY() - t1.getY() < 0) {
			dir = 'N';
		}
		return dir;
	}

	public String getNextCommand() {
		System.out.println("TraverseTask command get");

		if (!map.tileReachable(bot.getPosition(), destination)) {
			running = false;
			return null;
		} else {
			Stack<MapTile> path = map.getPath(bot.getPosition(), destination);
			return "MOVE " + getMovement(path.pop(), path.peek());
		}

		/*
		if (!movements.isEmpty()) {
			return "MOVE " + movements.remove();
		} else {
			if (persistant) {
				generateMovements(map.getPath(bot.getPosition(), destination));
				return getNextCommand();
			} else {
				running = false;
				return "HELLO";
			}
		}
		*/
	}

	public boolean hasNextCommand() {
		if ((bot.getPosition()[0] == destination[0] && bot.getPosition()[1] == destination[1]) || !map.tileWalkable(destination[0], destination[1])) {
			running = false;
		}
		return running;
	}
}
