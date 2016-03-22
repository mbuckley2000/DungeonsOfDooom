import java.util.*;

/**
 * Bot class for Dungeons of Doom
 * Runnable (has static main)
 * Connects to DoD server using the Client class
 */
public class Bot implements PlayerInterface {
	private Stack<BotTask> taskStack;
	private BotTask exploreTask;
	private String command;
	private Random random;
	private BotMap map;
	private PlayerPositionTracker positionTracker;
	/**
	 * Used for sorting a list of int[] map positions from closest to the bot to furthest from the bot
	 */
	public final Comparator<int[]> distanceFromBot =
			new Comparator<int[]>() {
				public int compare(int[] t1, int[] t2) {
					int t1Dist = BotMap.getManhattanDistance(t1, positionTracker.getPosition());
					int t2Dist = BotMap.getManhattanDistance(t2, positionTracker.getPosition());
					if (t1Dist == t2Dist) {
						return 0;
					} else if (t1Dist > t2Dist) {
						return 1;
					} else {
						return -1;
					}
				}
			};
	private int stepsSinceLastLook;
	private int goldNeeded;
	private int stepSize;
	private boolean pathBlocked;

	/**
	 * Constructor.
	 * Creates the initial ExploreTask
	 */
	public Bot() {
		positionTracker = new PlayerPositionTracker();
		stepsSinceLastLook = 0;
		goldNeeded = 10;
		random = new Random();
		map = new BotMap();
		taskStack = new Stack<>();
		exploreTask = new BotExploreTask(map, this);
		taskStack.add(exploreTask);
		stepSize = 2;
		command = "HELLO";
		pathBlocked = false;
	}

	/**
	 * @return The gold needed for the bot to win the game, from the last 'HELLO' call
	 */
	public int getGoldNeeded() {
		return goldNeeded;
	}

	/**
	 * Adds a BotTask to the task stack
	 *
	 * @param task The task to add
	 */
	public void addTask(BotTask task) {
		taskStack.add(task);
	}

	/**
	 * Clears all current tasks
	 */
	public void clearTasks() {
		taskStack.empty();
	}

	/**
	 * Gets the next command that the bot should call
	 *
	 * @return The next command
	 */
	private String getBotAction() {
		if (needToLook()) {
			return "LOOK";
		}

		if (taskStack.isEmpty()) {
			System.err.println("FATAL: Task stack empty!");
			System.exit(0);
		}

		if (taskStack.peek().hasNextCommand()) {
			return taskStack.peek().getNextCommand();
		} else {
			taskStack.pop();
			return getBotAction();
		}
	}


	/**
	 * @return Current position of the bot
	 */
	public int[] getPosition() {
		return positionTracker.getPosition();
	}

	/**
	 * If the last command was MOVE:
	 * Checks whether the last move command was successful
	 * If so, updates the bot's local position (relative to it's start position)
	 */
	private void updatePosition() {
		stepsSinceLastLook++;
		positionTracker.step(command.charAt(5));
	}

	/**
	 * @return True if the number of successful movements since the last LOOK call is more than the stepSize
	 */
	private boolean needToLook() {
		if (pathBlocked) return true;

		//Never look if we're racing to the exit
		if (goldNeeded == 0 && taskStack.peek().getClass() == BotTraverseTask.class) {
			return false;
		}

		//Or if we are going for gold
		if (taskStack.peek().getClass() == BotRetrieveGoldTask.class) {
			return false;
		}

		boolean hit = false;
		int[] position = positionTracker.getPosition();
		//Check if there are any undiscovered tiles in our look window
		for (int x = 0; x < 5; x++) {
			for (int y = 0; y < 5; y++) {
				int posY = y + position[0] - 2;
				int posX = x + position[1] - 2;
				if (map.tileEmpty(posY, posX)) {
					if (!((x == 0 && y == 0) || (x == 4 && y == 0) || (x == 0 && y == 4) || (x == 4 && y == 4) || (x == 2 && y == 2))) {
						hit = true;
					}
				}
			}
		}
		if (stepsSinceLastLook >= stepSize && hit) {
			stepsSinceLastLook = 0;
			return true;
		} else {
			return false;
		}
	}



	/**
	 * @param tileType Tile type to find
	 * @return The closest reachable tile of the given type
	 */
	public int[] getClosestReachableTile(char tileType) {
		ArrayList<int[]> tiles = map.findAllTiles(tileType);
		Collections.sort(tiles, distanceFromBot);
		for (int[] tile : tiles) {
			if (map.tileReachable(positionTracker.getPosition(), tile)) {
				return tile;
			}
		}
		return null;
	}

	@Override
	public void giveLookResponse(char[][] response) {
		map.update(response, positionTracker.getPosition());
	}

	@Override
	public void giveHelloResponse(int response) {
		goldNeeded = response;
	}

	@Override
	public void giveSuccessResponse(boolean response) {
		if (response) {
			if (command.contains("MOVE")) {
				updatePosition();
			}
		} else {
			if (command.contains("MOVE")) {
				//Movement failed. This shouldn't happen unless a player got in the way!!
				pathBlocked = true; //This makes the needToLook method return true
			}
		}
	}

	@Override
	public boolean hasNextCommand() {
		return true;
	}

	@Override
	public String getNextCommand() {
		final int sleepMax = 3000;
		try {
			Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		command = getBotAction().toUpperCase();
		System.out.println(command);
		return command;
	}
}