/**
 * Static server class.
 * Receives incoming connections and spawns a RemoteClient thread for each one
 * Keeps a collection of all connections to be used for player collision checking and broadcast messages etc..
 * Clients are PERSISTENT. When a client disconnects, all data is kept and their connected flag set to false
 * When they reconnect, they use the same RemoteClient object, with their old data and continue where they left off
 * If a player is standing where they used to be, they are re-initialised
 * Provides utility to check for stalemates (not enough gold left for anybody to win)
 *
 * @author mb2070
 * @since 24/02/2016
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Server {
	private int port = 40004;
	private Set<RemoteClient> remoteClients = new HashSet<>();
	private boolean gameRunning;
	private Map map;
	private ServerSocket serverSocket;

	public Server() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Listening for remoteClients");

			gameRunning = true;

			//Setup
			//Load map
			map = new Map();
			map.readMap(new File("maps/maze.txt"));

			update();
		} catch (IOException e) {
			System.err.println("Error starting server");
			System.exit(0);
		}
	}

	/**
	 * Initiates the server and detects new connections, spawning a new thread for each one
	 */
	public static void main(String args[]) {
		Server server = new Server();
	}

	private void update() {
		try {
			while (gameRunning) {
				Socket clientSocket = serverSocket.accept();
				System.out.println(clientSocket.getInetAddress() + "\t\tRequested to connect");

				RemoteClient client = findClient(clientSocket.getInetAddress());
				if (client == null) {
					client = new RemoteClient(clientSocket);
					remoteClients.add(client);
					new Thread(client).start();
				} else {
					if (!client.isConnected()) {
						client.reconnect(clientSocket);
						new Thread(findClient(clientSocket.getInetAddress())).start();
					} else {
						System.out.println(clientSocket.getInetAddress() + "\t\tConnection refused. Address already connected");
						new PrintWriter(clientSocket.getOutputStream(), true).println("Connection refused. Address already connected");
						clientSocket.close();
					}
				}
			}
		} catch (IOException e) {
			//Couldn't update server
		}
	}

	public boolean isGameRunning() {
		return gameRunning;
	}

	/**
	 * Returns the game map
	 *
	 * @return The game map
	 */
	public Map getMap() {
		return map;
	}

	/**
	 * Closes all connections on the server
	 */
	public void closeAllConnections() {
		for (RemoteClient connection : remoteClients) {
			if (connection.isConnected()) {
				connection.closeConnection();
			}
		}
	}

	/**
	 * Sends a message to everybody on the server
	 * @param message The message to send
	 */
	public void broadcastMessage(String message, RemoteClient... excludedClients) {
		for (RemoteClient client : remoteClients) {
			if (client.isConnected() && !Arrays.asList(excludedClients).contains(client)) {
				client.getWriter().println(message);
			}
		}
	}

	/**
	 * Finds the client object for a given InetAddress. Used to find a client's data if they disconnect and reconnect
	 * @param address InetAddress of the client
	 * @return The found client. Null if none exist
	 */
	private RemoteClient findClient(InetAddress address) {
		for (RemoteClient connection : remoteClients) {
			if (connection.getAddress().equals(address)) {
				return connection;
			}
		}
		return null;
	}

	/**
	 * Checks if a player is on the specified tile
	 *
	 * @param y Y ordinate of the tile
	 * @param x X ordinate of the tile
	 * @return True if a player is on the specified tile, false otherwise
	 */
	public boolean playerOnTile(int y, int x) {
		boolean hit = false;
		for (RemoteClient connection : remoteClients) {
			if (connection != null) {
				if (connection.isConnected() && connection.getPlayerPosition()[0] == y && connection.getPlayerPosition()[1] == x) {
					hit = true;
				}
			}
		}
		return hit;
	}

	/**
	 * Shuts down all connections and ends the game
	 */
	public void shutDown() {
		System.out.println("Game over. Shutting down");
		gameRunning = false;
		closeAllConnections();
		System.exit(0);
	}

	/**
	 * Checks if a stalemate has occurred (i.e. not enough gold for any connected player to finish)
	 * If it has occurred, the game ends
	 */
	public void checkStalemate() {
		boolean hit = false;
		for (RemoteClient client : remoteClients) {
			if (client.isConnected()) {
				if ((client.getGoldNeeded() <= map.goldLeft()) || client.getGoldNeeded() == 0) {
					hit = true;
				}
			}
		}
		if (!hit) {
			//Not enough gold left for any connected player to win.
			broadcastMessage("Not enough gold left for any connected player to win. Game over");
			System.out.println("Not enough gold left for any connected player to win. Shutting down");
			shutDown();
		}
	}
}