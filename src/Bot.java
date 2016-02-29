import java.util.Random;

public class Bot extends PlayGame{
	private static final char[] DIRECTIONS = {'n', 's', 'e', 'w'};
	private static boolean networkedMode = true;
	private Random random;
	private DiscoverableMap map;
	private int[] moveDelta = {0, 0};

	public Bot() {
		//logic = new GameLogicClient(false);
		super();
		random = new Random();
		map = new DiscoverableMap();
	}

	public static void main(String[] args) {
		Bot game = new Bot();
		if (!networkedMode) {
			System.out.println("Do you want to load a specific map?");
			System.out.println("Press enter for default map");
			game.selectMap(game.readUserInput());
		}

		game.update();
	}

	private String botAction(String lastAnswer) {
		String command;
		if (length(moveDelta) >= 2) {
			command = "LOOK";
		} else {
			command = "MOVE " + getMovement();
		}
		System.out.println("Sent command: " + command);
		return command;
	}

	private char getMovement() {
		Random random = new Random();
		int dir = random.nextInt(3);
		switch (dir) {
			case 0:
				moveDelta[0] -= 1; //North
				return 'N';
			case 1:
				moveDelta[1] += 1; //East
				return 'E';
			case 2:
				moveDelta[0] += 1; //South
				return 'S';
			case 3:
				moveDelta[1] -= 1; //West
				return 'W';
		}
		return 'N';
	}

	private double length(int[] vector) {
		return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
	}

	public void update(){
		String answer = "";
		while (logic.gameRunning()) {
			answer = parseCommand(botAction(answer).toLowerCase());
			printAnswer (answer);
			try {
				Thread.currentThread().sleep(random.nextInt(2000) + 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
