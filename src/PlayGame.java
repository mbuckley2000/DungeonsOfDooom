import java.util.Scanner;

/**
 * Allows a human player to play the game using a Client object
 * Has a runnable static main method
 * Takes user input through the command line
 */
public class PlayGame {
	protected Client client;
	private Scanner userInput;

	/**
	 * Constructor
	 */
	public PlayGame() {
		client = new Client();
		if (client.gameRunning()) {
			System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
			userInput = new Scanner(System.in);
		}
	}

	/**
	 * Main method. Program starts here.
	 * PlayGame object is created and updated
	 * No command line arguments are taken
	 */
	public static void main(String[] args) {
		PlayGame game = new PlayGame();
		game.update();
	}

	/**
	 * Returns the user input
	 *
	 * @return The user input
	 */
	private String readUserInput() {
		return userInput.nextLine();
	}

	/**
	 *
	 */
	public void update() {
		while (client.gameRunning()) {
			parseInput(readUserInput());
		}
		client.close();
	}

	/**
	 * Does very basic input filtering before calling the relevant GameLogic method (Client)
	 * Checks for null or invalid commands
	 * Converts everything to uppercase
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