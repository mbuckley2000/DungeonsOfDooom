import java.util.*;


/**
 * Bot class for Dungeons of Doom
 * Runnable (has static main)
 * Connects to DoD server using the Client class
 */
public class Bot extends PlayGame {
	private final int sleepMax = 100;
	private Stack<BotTask> taskStack;
	private BotTask exploreTask;
	private String command;
	private Random random;
	private BotMap map;
	private int[] position;
	/**
	 * Used for sorting a list of int[] map positions from closest to the bot to furthest from the bot
	 */
	public final Comparator<int[]> distanceFromBot =
			new Comparator<int[]>() {
				public int compare(int[] t1, int[] t2) {
					int t1Dist = BotMap.getManhattanDistance(t1, position);
					int t2Dist = BotMap.getManhattanDistance(t2, position);
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

	/**
	 * Constructor.
	 * Creates the initial ExploreTask
	 */
	public Bot() {
		super();
		position = new int[]{0, 0};
		stepsSinceLastLook = 0;
		goldNeeded = 10;
		random = new Random();
		map = new BotMap();
		taskStack = new Stack<>();
		exploreTask = new BotExploreTask(map, this);
		taskStack.add(exploreTask);
		stepSize = 1;
		command = "HELLO";
	}

	/**
	 * Program runs from here
	 * Creates a bot object and starts it off updating
	 */
	public static void main(String[] args) {
		Bot game = new Bot();
		game.update();
	}

	/**
	 * @return The gold needed for the bot to win the game, from the last 'HELLO' call
	 */
	public int getGoldNeeded() {
		return goldNeeded;
	}

	/**
	 * Adds a BotTask to the task stack
	 * @param task The task to add
	 */
	public void addTask(BotTask task) {
		taskStack.add(task);
	}

	/**
	 * Gets the next command that the bot should call
	 * @return The next command
	 */
	private String botAction() {
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
			return botAction();
		}
	}

	/**
	 * Update loop for the bot
	 * Runs until the game is over
	 */
	public void update() {
		System.out.println("Bot is now running");
		while (client.gameRunning()) {
			updatePosition();
			updateMap();
			updateGoldToWin();
			command = botAction().toUpperCase();
			parseInput(command);
			System.out.println(command);
			try {
				Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return Current position of the bot
	 */
	public int[] getPosition() {
		return position;
	}

	/**
	 * If the last command was MOVE:
	 * Checks whether the last move command was successful
	 * If so, updates the bot's local position (relative to it's start position)
	 */
	private void updatePosition() {
		if (command.contains("MOVE")) {
			stepsSinceLastLook++;
			if (client.getServerMessageReaderThread().getSuccessResponse()) {
				stepped(command.charAt(5));
				System.out.println("Bot position: " + position[1] + ", " + position[0]);
			}
		}
	}

	/**
	 * If the last command was LOOK:
	 * Updates the bot's internal BotMap with the received look window
	 */
	private void updateMap() {
		if (command.equals("LOOK")) {
			map.update(client.getServerMessageReaderThread().getLookResponse(), position);
			System.out.println("Updated internal map: ");
			map.print(position);
		}
	}

	/**
	 * If the last command was HELLO:
	 * Updates the goldToWin with the latest number from the Server
	 */
	private void updateGoldToWin() {
		if (command.equals("HELLO")) {
			goldNeeded = client.getServerMessageReaderThread().getGoldResponse();
		}
	}

	/**
	 * @return True if the number of successful movements since the last LOOK call is more than the stepSize
	 */
	private boolean needToLook() {
		if (stepsSinceLastLook >= stepSize) {
			stepsSinceLastLook = 0;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Updates the bot's position (internal displacement from where it spawned), given the latest successful movement direction
	 * @param dir Latest successful movement direction
	 */
	private void stepped(char dir) {
		switch (dir) {
			case 'N':
				position[0] -= 1; //North
				break;
			case 'E':
				position[1] += 1; //East
				break;
			case 'S':
				position[0] += 1; //South
				break;
			case 'W':
				position[1] -= 1; //West
				break;
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
			if (map.tileReachable(position, tile)) {
				return tile;
			}
		}
		return null;
	}
}