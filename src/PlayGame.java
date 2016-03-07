import java.util.Scanner;

public class PlayGame {
	protected Client client;
	private Scanner userInput;

	public PlayGame() {
		client = new Client();
		if (client.gameRunning()) {
			System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
			userInput = new Scanner(System.in);
		}
	}

	public static void main(String[] args) {
		PlayGame game = new PlayGame();
		game.update();
	}

	/**
	 * Returns the user input
	 *
	 * @return The user input
	 */
	public String readUserInput() {
		return userInput.nextLine();
	}

	public void update() {
		while (client.gameRunning()) {
			parseInput(readUserInput());
		}
		client.close();
	}

	/**
	 * Parsing and Evaluating the User Input.
	 *
	 * @param input input the user generates
	 */
	protected void parseInput(String input) {
		String[] command = input.trim().split(" ");

		switch (command[0].toUpperCase()) {
			case "HELLO":
				client.send("HELLO");
				break;
			case "MOVE":
				if (command.length == 2) {
					client.send("MOVE " + command[1].toUpperCase().charAt(0));
				}
				break;
			case "PICKUP":
				client.send("PICKUP");
				break;
			case "LOOK":
				client.send("LOOK");
				break;
			case "QUIT":
				client.send("QUIT");
				break;
			default:
				System.out.println("Invalid command: " + command[0]);
				break;
		}
	}
}