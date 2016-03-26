import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

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
	private Server server;
	private String lastLookWindow;
	private String name;

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
		playerPosition = initialisePlayer();
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
		new Thread(new ViewUpdaterThread()).start();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			String input;

			System.out.println(address + "\t\tConnected");
			writer.println("Welcome to the dungeon! Mwuahaha");

			while (connected && server.isGameRunning()) {
				server.checkStalemate();
				input = reader.readLine();
				System.out.println(address + "\t\t" + input);
				if (input != null) {
					writer.println(parseInput(input));
				} else {
					closeConnection();
				}
			}

			System.out.println(address + "\t\tDisconnected");
			writer.close();
			reader.close();
			clientSocket.close();
		} catch (IOException e) {
			connected = false;
			System.out.println(address + "\t\tDisconnected");
		}
	}


	//GameLogic (Command processing)

	/**
	 * Processes the HELLO command
	 *
	 * @return String to be sent back to the Client (How much gold left they need to win)
	 */
	public String hello() {
		return "GOLD: " + getGoldNeeded();
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
				return ("FAIL");
		}

		if (server.getMap().lookAtTile(newPosition[0], newPosition[1]) != '#' && !server.playerOnTile(newPosition[0], newPosition[1])) {
			playerPosition = newPosition;
			if (checkWin()) {
				server.broadcastMessage("Somebody else just won the game", this);
				server.broadcastMessage("Get out of my dungeon.", this);
				writer.println("Congratulations!!! \n You have escaped the Dungeon of Dooom!!!!!! \nThank you for playing!");
				closeConnection();
				server.shutDown();
				return null;
			}
			return "SUCCESS";
		} else {
			return "FAIL";
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
		if (server.getMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			collectedGold++;
			server.getMap().replaceTile(playerPosition[0], playerPosition[1], '.');
			return "SUCCESS, GOLD COINS: " + collectedGold;
		}

		return "FAIL\nThere is nothing to pick up...";
	}

	/**
	 * Processes the LOOK command
	 *
	 * @return The response to be sent back to the Client: A look window into the map, based on their current position
	 */
	public String look() {
		String output = "";
		char[][] lookReply = server.getMap().lookWindow(playerPosition[0], playerPosition[1], 5);
		lookReply[2][2] = 'P';

		for (int i = 0; i < lookReply.length; i++) {
			for (int j = 0; j < lookReply[0].length; j++) {
				if (server.playerOnTile(playerPosition[0] - 2 + i, playerPosition[1] - 2 + j)) {
					output += 'P';
				} else {
					output += lookReply[j][i];
				}
			}
			if (i != lookReply.length - 1) {
				output += "\n";
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
		String answer = "FAIL";
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
				return "SUCCESS";
			case "SAY":
				server.broadcastMessage("MESSAGE" + name + ": " + input.replaceFirst("SAY ", ""));
				return "SUCCESS";
			case "QUIT":
				closeConnection();
				return "Thanks for playing!";
		}
		return answer;
	}


	//Misc

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 *
	 * @return True if all conditions are met, false otherwise
	 */
	private boolean checkWin() {
		return collectedGold >= server.getMap().getWin() && server.getMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'E';
	}

	/**
	 * @return The player position as an int array
	 */
	public int[] getPlayerPosition() {
		return playerPosition;
	}

	/**
	 * finds a random position for the player in the map.
	 *
	 * @return Return null; if no position is found or a position vector [y,x]
	 */
	private int[] initialisePlayer() {
		int[] pos = new int[2];
		Random rand = new Random();

		pos[0] = rand.nextInt(server.getMap().getMapHeight() - 1);
		pos[1] = rand.nextInt(server.getMap().getMapWidth() - 1);
		int counter = 1;
		while (server.getMap().lookAtTile(pos[0], pos[1]) == '#' && !server.playerOnTile(pos[0], pos[1]) && counter < server.getMap().getMapHeight() * server.getMap().getMapWidth()) {
			pos[1] = (int) (counter * Math.cos(counter));
			pos[0] = (int) (counter * Math.sin(counter));
			counter++;
		}
		if (server.getMap().lookAtTile(pos[0], pos[1]) == '#') {
			System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
			closeConnection();
			return null;
		} else {
			return pos;
		}
	}

	/**
	 * @return The PrintWriter that sends messages to the Client
	 */
	public PrintWriter getWriter() {
		return writer;
	}

	/**
	 * @return The amount of gold extra that the client needs to win
	 */
	public int getGoldNeeded() {
		return (server.getMap().getWin() - collectedGold);
	}


	//Connection handling

	/**
	 *
	 */
	public void closeConnection() {
		connected = false;
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
			playerPosition = initialisePlayer();
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
					Thread.sleep(200); //run on 200ms ticks, no need to spam. This is only for if a player moves in their lookWindow
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