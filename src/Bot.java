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
	private ClientMap map;
	private PlayerPositionTracker positionTracker;

	/**
	 * Used for sorting a list of int[] map positions from closest to the bot to furthest from the bot
	 */
	public final Comparator<int[]> distanceFromBot =
			new Comparator<int[]>() {
				public int compare(int[] t1, int[] t2) {
					int t1Dist = ClientMap.getManhattanDistance(t1, positionTracker.getPosition());
					int t2Dist = ClientMap.getManhattanDistance(t2, positionTracker.getPosition());
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
		map = new ClientMap();
		taskStack = new Stack<>();
		exploreTask = new BotExploreTask(map, this);
		taskStack.add(exploreTask);
		stepSize = 1;
		command = "HELLO";
		pathBlocked = false;
		new BotWindow(this);
	}

	public PlayerPositionTracker getPositionTracker() {
		return positionTracker;
	}

	public ClientMap getMap() {
		return map;
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
		final int sleepMax = 100;

		if (needToLook()) {
			return "LOOK";
		}

		try {
			Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		if (stepsSinceLastLook >= stepSize || pathBlocked) {
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
		if (command.contains("MOVE")) {
			if (response) {
				updatePosition();
			} else {
				//Movement failed. This shouldn't happen unless a player got in the way!!
				pathBlocked = true; //This makes the needToLook method return true
			}
		}
	}

	@Override
	public void giveMessage(String message) {
		System.out.println(message);
	}

	@Override
	public void giveWin() {

	}

	@Override
	public void giveLose() {

	}

	@Override
	public boolean hasNextCommand() {
		return true;
	}

	@Override
	public String getNextCommand() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		command = getBotAction();
		if (command != null) {
			System.out.println("Bot Command: " + command);
			return command;
		} else {
			return "HELLO";
		}
	}
}