import java.util.*;

public class Bot extends PlayGame {
	private final int sleepMax = 2000;
	Stack<BotTask> taskStack;
	BotTask exploreTask;
	String command;
	private Random random;
	private BotMap map;
	private int[] position = {0, 0};
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
	private int stepsSinceLastLook = 0;
	private int goldNeeded = 10;

	public Bot() {
		super();
		random = new Random();
		map = new BotMap();
		taskStack = new Stack<>();
		exploreTask = new BotExploreTask(map, this);
		taskStack.add(exploreTask);
		command = "HELLO";
	}

	public static void main(String[] args) {
		Bot game = new Bot();
		game.update();
	}

	public int getGoldNeeded() {
		return goldNeeded;
	}

	public void addTask(BotTask task) {
		taskStack.add(task);
	}

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

	public void update(){
		System.out.println("Bot is now running");
		while (logic.gameRunning()) {
			updatePosition();
			updateMap();
			updateGoldToWin();
			command = botAction().toUpperCase();
			parseCommand(command);
			System.out.println(command);
			try {
				Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int[] getPosition() {
		return position;
	}

	private void updatePosition() {
		if (command.contains("MOVE")) {
			stepsSinceLastLook++;
			if (logic.getClientOutputThread().getLastBoolResponse()) {
				stepped(command.charAt(5));
				System.out.println("Bot position: " + position[1] + ", " + position[0]);
			}
		}
	}

	private void updateMap() {
		if (command.equals("LOOK")) {
			map.update(logic.getClientOutputThread().getLastLookWindow(), position);
			System.out.println("Updated internal map: ");
			map.print(position);
		}
	}

	private void updateGoldToWin() {
		if (command.equals("HELLO")) {
			goldNeeded = logic.getClientOutputThread().getLastGoldResponse();
		}
	}

	private boolean needToLook() {
		if (stepsSinceLastLook > 0) {
			stepsSinceLastLook = 0;
			return true;
		} else {
			return false;
		}
	}

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