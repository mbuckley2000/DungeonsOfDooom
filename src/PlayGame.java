/**
 * Allows a human player to play the game using a Client object
 * Has a runnable static main method
 * Takes user input through the command line
 */
public class PlayGame {
	protected static String address;
	protected static int port;
	protected static boolean guiMode;
	protected Client client;
	private IUserInput userInput;

	/**
	 * Constructor
	 */
	public PlayGame(String address, int port) {
		client = new Client(address, port);
		if (client.gameRunning()) {
			if (guiMode) {
				userInput = new GameWindow("DoD");
			} else {
				System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
				userInput = new TextualInput();
			}
		}
	}

	/**
	 * Main method. Program starts here.
	 * PlayGame object is created and updated
	 * IP address and/or port number may be taken as command line arguments
	 * Default is localhost:40004
	 */
	public static void main(String[] args) {
		guiMode = true;
		processCommandLineArguments(args);
		PlayGame game = new PlayGame(address, port);
		game.update();
	}

	protected static void processCommandLineArguments(String[] args) {
		address = "localhost";
		port = 40004;

		if (args.length < 4) {
			for (String string : args) {
				if (Client.isAddressValid(string)) {
					address = string;
					continue;
				}
				try {
					if (Client.isPortValid(Integer.parseInt(string))) {
						port = Integer.parseInt(string);
					}
				} catch (NumberFormatException e) {
					//Not a valid port
					//This is handled by setting default values at the top of this method. (localhost:40004)
				}

				if (string.toLowerCase().equals("gui")) {
					guiMode = true;
				}
			}
		} else {
			System.err.println("Too many arguments! IP Address and/or Port Number may be specified, as well as GUI mode");
			System.exit(1);
		}
	}

	/**
	 *
	 */
	public void update() {
		while (client.gameRunning()) {
			String input = userInput.getNextCommand();
			if (input != null) {
				client.send(input);
			}
		}
		client.close();
	}
}