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
	private boolean helloNeeded;
	private boolean lookNeeded;
	private int goldNeeded;

	/**
	 * Constructor.
	 * Creates the initial ExploreTask
	 */
	public Bot() {
		positionTracker = new PlayerPositionTracker();
		goldNeeded = -1;
		random = new Random();
		map = new ClientMap();
		taskStack = new Stack<>();
		exploreTask = new BotExploreTask(map, this);
		taskStack.add(exploreTask);
		command = "HELLO";
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
		System.out.println("Gold Needed: " + goldNeeded);
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

		try {
			Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (lookNeeded) {
			lookNeeded = false;
			return "LOOK";
		}

		if (helloNeeded) {
			helloNeeded = false;
			return "HELLO";
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
	public void givePickupResponse(boolean response) {
		helloNeeded = true;
	}

	@Override
	public void giveMoveResponse(boolean response) {
		lookNeeded = true;
		if (response) {
			positionTracker.step();
		}
	}


	@Override
	public boolean isFinished() {
		return true;
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
		command = getBotAction();
		if (command != null) {
			System.out.println("Bot Command: " + command);
			if (command.contains("MOVE")) {
				positionTracker.setDirection(command.charAt(5));
			}
			return command;
		} else {
			return "HELLO";
		}
	}
}