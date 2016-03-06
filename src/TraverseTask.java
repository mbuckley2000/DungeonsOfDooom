import java.util.Stack;

/**
 * Created by matt on 03/03/2016.
 */
public class TraverseTask implements AITask {
	private Bot bot;
	private AIMap map;
	private int[] destination;
	private boolean running;

	public TraverseTask(Bot bot, AIMap map, int[] destination) {
		this.bot = bot;
		this.map = map;
		this.destination = destination;
		running = true;
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
	}

	public boolean hasNextCommand() {
		if ((bot.getPosition()[0] == destination[0] && bot.getPosition()[1] == destination[1]) || !map.tileWalkable(destination[0], destination[1])) {
			running = false;
		}
		return running;
	}
}
