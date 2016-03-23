/**
 * Allows a human player to play the game using a Client object
 * Has a runnable static main method
 * Takes user input through the command line
 */
public class PlayGame {
	//Command line arguments
	private static String address;
	private static int port;
	private static boolean guiMode;
	private static boolean botMode;

	protected Client client;
	private PlayerInterface playerInterface;

	/**
	 * Constructor
	 */
	public PlayGame() {
		client = new Client(address, port);
		if (client.gameRunning()) {
			if (guiMode) {
				playerInterface = new GUIInterface("DoD");
			} else if (botMode) {
				playerInterface = new Bot();
			} else {
				System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
				playerInterface = new TextualInterface();
			}
		}

		new Thread(new InputHandlerThread()).start();
		new Thread(new ResponseHandlerThread()).start();
	}

	/**
	 * Main method. Program starts here.
	 * PlayGame object is created and updated
	 * IP address and/or port number may be taken as command line arguments
	 * Default is localhost:40004
	 */
	public static void main(String[] args) {
		guiMode = true; //DEBUG
		processCommandLineArguments(args);
		new PlayGame();
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
				if (string.toLowerCase().equals("bot")) {
					botMode = true;
				}
			}
		} else {
			System.err.println("Too many arguments! IP Address and/or Port Number may be specified, as well as GUI mode");
			System.exit(1);
		}
	}


	private class InputHandlerThread implements Runnable {
		public void run() {
			while (client.gameRunning()) {
				if (playerInterface.hasNextCommand()) {
					String input = playerInterface.getNextCommand();
					if (input != null) {
						client.send(input);
					} else {
						System.err.println("FATAL: null response from user input get command");
						System.exit(1);
					}
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	private class ResponseHandlerThread implements Runnable {
		public void run() {
			ServerListenerThread listener = client.getServerListenerThread();
			while (client.gameRunning()) {
				if (listener.hasSuccessResponse()) {
					boolean response = listener.getSuccessResponse();
					playerInterface.giveSuccessResponse(response);
					if (response)
						playerInterface.giveMessage("Success");
					else
						playerInterface.giveMessage("Fail");
				}
				if (listener.hasGoldResponse()) {
					playerInterface.giveHelloResponse(listener.getGoldResponse());
				}
				if (listener.hasLookResponse()) {
					playerInterface.giveLookResponse(listener.getLookResponse());
				}
			}
		}
	}
}