import java.util.Random;

public class Bot extends PlayGame{
	final int sleepMax = 100;
	private Random random;
	private DiscoverableMap map;
	private int[] moveDelta = {0, 0};
	private int[] position = {0, 0};
	private String lastCommand;

	public Bot() {
		super();
		random = new Random();
		map = new DiscoverableMap();
	}

	public static void main(String[] args) {
		Bot game = new Bot();
		System.out.println("Bot is now running");
		game.update();
	}

	private String botAction() {
		if (lastCommand.equals("LOOK")) {
			//logic.getOutputClient().printLastLookWindow();
			map.update(logic.getOutputClient().getLastLookWindow(), moveDelta);
			position[0] += moveDelta[0];
			position[1] += moveDelta[1];
			moveDelta[0] = 0;
			moveDelta[1] = 0;
			System.out.println("Updated internal map: ");
			map.print();
		} else if (lastCommand.contains("MOVE")) {
			//Check that the move was successful
			if (logic.getOutputClient().getLastBoolResponse()) {
				char dir = lastCommand.toUpperCase().charAt(5);
				switch (dir) {
					case 'N':
						moveDelta[0] -= 1; //North
						break;
					case 'E':
						moveDelta[1] += 1; //East
						break;
					case 'S':
						moveDelta[0] += 1; //South
						break;
					case 'W':
						moveDelta[1] -= 1; //West
						break;
				}
			}
		}

		String command;
		if (length(moveDelta) >= 2) {
			command = "LOOK";
		} else {
			command = "MOVE " + getMovement();
		}
		System.out.println("Sent command: " + command);
		lastCommand = command;
		return command;
	}

	private char getMovement() {
		Random random = new Random();
		int dir = random.nextInt(4);
		switch (dir) {
			case 0:
				return 'N';
			case 1:
				return 'E';
			case 2:
				return 'S';
			case 3:
				return 'W';
			default:
				System.out.println("AI generated an invalid movement. Trying again");
				return getMovement();
		}
	}

	private double length(int[] vector) {
		return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
	}

	public void update(){
		while (logic.gameRunning()) {
			if (lastCommand == null) lastCommand = "";
			parseCommand(botAction().toLowerCase());
			try {
				Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}