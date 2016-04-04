import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a client connected to the server
 * Each client gets their own thread, so operations can happen simultaneously
 * Only MOVE is synchronised. This ensures that only one player can be on a single tile, removing the need to synchronise other methods such as PICKUP
 *
 * @author mb2070
 * @since 07/03/2016
 */
public class RemoteClient implements Runnable, IGameLogic {
	private Socket clientSocket;
	private boolean connected;
	private int[] playerPosition;
	private int collectedGold;
	private InetAddress address;
	private PrintWriter writer;
	private BufferedReader reader;
	private Server server;
	private String lastLookWindow;
	private String name;
	private int lookSize;

	/**
	 * Constructs the RemoteClient given the Socket and Server it is connected to
	 *
	 * @param server       The Server the RemoteClient belongs to
	 * @param clientSocket The Socket that the client is connected to
	 */
	RemoteClient(Server server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		connected = true;
		collectedGold = 0;
		lookSize = 5;
		int[] freePos = server.getServerMap().getFreeTile(server);
		if (freePos == null) {
			System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
			closeConnection();
		} else {
			playerPosition = freePos;
		}
		address = clientSocket.getInetAddress();
	}

	/**
	 * Thread starts here.
	 * Initialises the reader and writer
	 * Sends welcome message
	 * Loops until the game is over or the client is disconnected, receiving input from client and processing it. Calls relevant methods for each valid command
	 * Closes everything down cleanly when the loop is done
	 */
	public void run() {
		//new Thread(new ViewUpdaterThread()).start();

		try {
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			String input;

			System.out.println(address + "\t\tConnected");
			sendChat("Welcome to the dungeon! Mwuahaha");

			while (connected && server.isGameRunning()) {
				input = reader.readLine();
				if (input != null) {
					String response = parseInput(input.toUpperCase());
					if (response != null) {
						writer.println(response);
						if (!input.equals("LOOK")) {
							System.out.println(address + "\t\t" + input + "\t\t" + response);
						} else {
							System.out.println(address + "\t\t" + input + "\t\tSent look window");
						}
					}
				} else {
					closeConnection();
				}
			}
		} catch (IOException e) {
			closeConnection();
		}
	}


	//GameLogic (Command processing)

	/**
	 * Processes the HELLO command
	 *
	 * @return String to be sent back to the Client (How much gold left they need to win)
	 */
	public String hello() {
		return "H" + getGoldNeeded();
	}

	/**
	 * Processes the MOVE command
	 * If the desired tile to move to is walkable and no player is on it, the move is allowed, otherwise it is denied
	 * Synchronised to prevent multiple players on the same tile
	 *
	 * @param direction The direction the client wants to move in
	 * @return The response to be sent back to the client: SUCCESS or FAIL, depending on if the move was allowed.
	 */
	public synchronized String move(char direction) {
		int[] newPosition = playerPosition.clone();
		switch (direction) {
			case 'N':
				newPosition[0] -= 1;
				break;
			case 'E':
				newPosition[1] += 1;
				break;
			case 'S':
				newPosition[0] += 1;
				break;
			case 'W':
				newPosition[1] -= 1;
				break;
			default:
				return ("MF");
		}

		if (server.getServerMap().lookAtTile(newPosition[0], newPosition[1]) != '#' && !server.playerOnTile(newPosition[0], newPosition[1])) {
			playerPosition = newPosition;
			if (checkWin()) {
				server.broadcastMessage("LOSE", this);
				System.out.println(address + "\t\t\t\t\tWIN");
				writer.println("WIN");
				server.shutDown();
				return null;
			}
			return "MS";
		} else {
			return "MF";
		}
	}

	/**
	 * Processes the PICKUP command
	 * If gold is on the player's tile, the player's gold count goes up and the gold is replaced with an empty tile.
	 * No need for this to be synchronised, because movement is synchronised; two players cannot be on the same tile to pick up the same gold.
	 *
	 * @return The response to be sent back to the player
	 */
	public String pickup() {
		if (server.getServerMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			collectedGold++;
			server.getServerMap().replaceTile(playerPosition[0], playerPosition[1], '.');
			return "PS" + collectedGold;
		}

		return "PF";
	}

	/**
	 * Processes the LOOK command
	 *
	 * @return The response to be sent back to the Client: A look window into the map, based on their current position
	 */
	public String look() {
		String output = "L" + lookSize;
		char[][] lookReply = server.getServerMap().getLookWindow(playerPosition[0], playerPosition[1], lookSize);

		for (int i = 0; i < lookSize; i++) {
			for (int j = 0; j < lookSize; j++) {
				if (server.playerOnTile(playerPosition[0] - (lookSize / 2) + i, playerPosition[1] - (lookSize / 2) + j)) {
					output += 'P';
				} else {
					output += lookReply[j][i];
				}
			}
			if (i != lookSize - 1) {
				output += "\nL" + lookSize;
			}
		}
		lastLookWindow = output;
		return output;
	}

	/**
	 * Parses the input from the Client
	 * Checks for valid commands and then calls the relevant method to get the response
	 *
	 * @param input Input from the client
	 * @return The message to be sent back to the client
	 */
	private String parseInput(String input) {
		String[] command = input.trim().split(" ");
		String answer = null;
		switch (command[0].toUpperCase()) {
			case "HELLO":
				answer = hello();
				break;
			case "MOVE":
				if (command.length == 2) {
					answer = move(command[1].toUpperCase().charAt(0));
				}
				break;
			case "PICKUP":
				answer = pickup();
				break;
			case "LOOK":
				answer = look();//.replaceAll(".(?!$)", "$0  ");  <-- This adds spacing to the window
				break;
			case "NAME":
				name = command[1];
				return null;
			case "SAY":
				server.broadcastMessage(name + ": " + input.substring(4));
				return null;
			case "QUIT":
				closeConnection();
				return "Thanks for playing!";
		}
		return answer;
	}

	public void sendChat(String message) {
		System.out.println(address + "\t\t\t\t\t" + message);
		writer.println("C" + message);
	}

	//Misc

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 *
	 * @return True if all conditions are met, false otherwise
	 */
	private boolean checkWin() {
		return collectedGold >= server.getServerMap().getWin() && server.getServerMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'E';
	}

	/**
	 * @return The player position as an int array
	 */
	public int[] getPlayerPosition() {
		return playerPosition;
	}

	/**
	 * @return The amount of gold extra that the client needs to win
	 */
	public int getGoldNeeded() {
		return (server.getServerMap().getWin() - collectedGold);
	}


	//Connection handling

	/**
	 *
	 */
	public void closeConnection() {
		try {
			clientSocket.close();
			System.out.println(address + "\t\t\t\t\tDisconnected");
			writer.close();
			reader.close();
		} catch (IOException e) {
			System.err.println(address + "\t\t\t\t\tError closing connection");
		}
	}

	/**
	 * Allows a RemoteClient object to reconnect on a different clientSocket, with the same state (mapPos, gold etc)
	 * If a player is in their position, they are re-initialised
	 *
	 * @param clientSocket The socket to reconnect on
	 */
	public void reconnect(Socket clientSocket) {
		this.clientSocket = clientSocket;
		if (server.playerOnTile(playerPosition[0], playerPosition[1])) {
			//There is a player on out tile! Re-initialise player position
			int[] freePos = server.getServerMap().getFreeTile(server);
			if (freePos == null) {
				System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
				closeConnection();
			} else {
				playerPosition = freePos;
			}
		}
		if (playerPosition != null) {
			connected = true;
		}
	}

	/**
	 * @return True if the client is connected, false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return The InetAddress of the client
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Detects changes in the look window around the player.
	 * Sends an updated look window if changes are detected.
	 * Runs on 500ms clock
	 */
	private class ViewUpdaterThread implements Runnable {
		public void run() {
			while (connected) {
				try {
					Thread.sleep(200); //run on 200ms ticks, no need to spam. This is only for if a player moves in their getLookWindow
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				String oldLookWindow = lastLookWindow;
				String newLookWindow = look();
				if (oldLookWindow != null) {
					if (!oldLookWindow.equals(newLookWindow)) {
						System.out.println("Sending lookwindow");
						writer.println(newLookWindow);
					}
				}
			}
		}
	}
}