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

	RemoteClient(Socket clientSocket) {
		this.clientSocket = clientSocket;
		connected = true;
		collectedGold = 0;
		playerPosition = initialisePlayer();
		address = clientSocket.getInetAddress();
	}

	public void run() {
		System.out.println(clientSocket.getInetAddress() + "\t\tConnected");

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			String input;

			while (connected && Server.isGameRunning()) {
				Server.checkStalemate();
				input = reader.readLine();
				System.out.println(clientSocket.getInetAddress() + "\t\t" + input);
				if (input != null) {
					writer.println(parseInput(input));
				} else {
					closeConnection();
				}
			}

			System.out.println(clientSocket.getInetAddress() + "\t\tDisconnected");
			writer.close();
			reader.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//GameLogic
	public String hello() {
		return "GOLD: " + getGoldNeeded();
	}

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

		if (Server.getMap().lookAtTile(newPosition[0], newPosition[1]) != '#' && !Server.playerOnTile(newPosition[0], newPosition[1])) {
			playerPosition = newPosition;
			if (checkWin()) {
				Server.broadcastMessage("Somebody else just won the game");
				Server.broadcastMessage("Get out of my dungeon.");
				writer.println("Congratulations!!! \n You have escaped the Dungeon of Dooom!!!!!! \nThank you for playing!");
				closeConnection();
				Server.shutDown();
				return null;
			}
			return "SUCCESS";
		} else {
			return "FAIL";
		}
	}

	/**
	 * If gold is on the player's tile, the player's gold count goes up and the gold is replaced with an empty tile.
	 * No need for this to be synchronised, because movement is synchronised; two players cannot be on the same tile to pick up the same gold.
	 *
	 * @return The response to be sent back to the player.
	 */
	public String pickup() {
		if (Server.getMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			collectedGold++;
			Server.getMap().replaceTile(playerPosition[0], playerPosition[1], '.');
			return "SUCCESS, GOLD COINS: " + collectedGold;
		}

		return "FAIL\nThere is nothing to pick up...";
	}

	public String look() {
		String output = "";
		char[][] lookReply = Server.getMap().lookWindow(playerPosition[0], playerPosition[1], 5);
		lookReply[2][2] = 'P';

		for (int i = 0; i < lookReply.length; i++) {
			for (int j = 0; j < lookReply[0].length; j++) {
				if (Server.playerOnTile(playerPosition[0] - 2 + i, playerPosition[1] - 2 + j)) {
					output += 'P';
				} else {
					output += lookReply[j][i];
				}
			}
			if (i != lookReply.length - 1) {
				output += "\n";
			}
		}

		return output;
	}

	public boolean gameRunning() {
		return Server.isGameRunning();
	}

	public void quitGame() {
		closeConnection();
	}


	//Misc

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 *
	 * @return True if all conditions are met, false otherwise
	 */
	private boolean checkWin() {
		return collectedGold >= Server.getMap().getWin() && Server.getMap().lookAtTile(playerPosition[0], playerPosition[1]) == 'E';
	}

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

		pos[0] = rand.nextInt(Server.getMap().getMapHeight() - 1);
		pos[1] = rand.nextInt(Server.getMap().getMapWidth() - 1);
		int counter = 1;
		while (Server.getMap().lookAtTile(pos[0], pos[1]) == '#' && !Server.playerOnTile(pos[0], pos[1]) && counter < Server.getMap().getMapHeight() * Server.getMap().getMapWidth()) {
			pos[1] = (int) (counter * Math.cos(counter));
			pos[0] = (int) (counter * Math.sin(counter));
			counter++;
		}
		if (Server.getMap().lookAtTile(pos[0], pos[1]) == '#') {
			System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
			closeConnection();
			return null;
		} else {
			return pos;
		}
	}

	public PrintWriter getWriter() {
		return writer;
	}

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
				answer = look().replaceAll(".(?!$)", "$0  ");
				break;
			case "QUIT":
				closeConnection();
				break;
		}
		return answer;
	}

	public int getGoldNeeded() {
		return (Server.getMap().getWin() - collectedGold);
	}


	//Connection handling
	public void closeConnection() {
		connected = false;
	}

	public void reconnect(Socket clientSocket) {
		this.clientSocket = clientSocket;
		if (Server.playerOnTile(playerPosition[0], playerPosition[1])) {
			//There is a player on out tile! Re-initialise player position
			playerPosition = initialisePlayer();
		}
		if (playerPosition != null) {
			connected = true;
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public InetAddress getAddress() {
		return address;
	}

}
