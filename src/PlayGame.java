import java.util.Scanner;

public class PlayGame {
	protected Client logic;
	protected Scanner userInput;

	public PlayGame(){
		logic = new Client();
		if (logic.isConnected()) {
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
	 * @return The user input
	 */
	public String readUserInput(){
		return userInput.nextLine();
	}

	public void update(){
		while (logic.gameRunning()) {
			parseCommand(readUserInput());
		}
		logic.close();
	}

	/**
	 * Parsing and Evaluating the User Input.
	 * @param readUserInput input the user generates
	 */
	protected void parseCommand(String readUserInput) {
		String [] command = readUserInput.trim().split(" ");

		switch (command[0].toUpperCase()){
			case "HELLO":
				logic.hello();
				break;
			case "MOVE":
				if (command.length == 2) {
					logic.move(command[1].toUpperCase().charAt(0));
				}
			break;
			case "PICKUP":
				logic.pickup();
				break;
			case "LOOK":
				logic.look();
				break;
			case "QUIT":
				logic.quitGame();
				break;
			default:
				System.out.println("Invalid command: " + command[0]);
				break;
		}
	}

}
