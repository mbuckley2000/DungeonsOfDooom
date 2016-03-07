/**
 * Static server class.
 * Receives incoming connections and spawns a RemoteClient thread for each one
 * Keeps a collection of all connections to be used for player collision checking and broadcast messages etc..
 * Clients are PERSISTENT. When a client disconnects, all data is kept and their connected flag set to false
 * When they reconnect, they use the same RemoteClient object, with their old data and continue where they left off
 * If a player is standing where they used to be, they are re-initialised
 * Provides utility to check for stalemates (not enough gold left for anybody to win)
 *
 * @since 24/02/2016
 * @author mb2070
 */

import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
	private static int port = 40004;
	private static Set<RemoteClient> remoteClients = new HashSet<>();
	private static boolean gameRunning;
	private static Map map;

	public static boolean isGameRunning() {
		return gameRunning;
	}

	public static Map getMap() {
		return map;
	}

	public static void main(String args[]) throws Exception {
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Listening for remoteClients");

		gameRunning = true;

		//Setup
		//Load map
		map = new Map();
		map.readMap(new File("maps/example_map.txt"));

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
					clientSocket.close();
				}
			}
		}
	}

	public static void closeAllConnections() {
		for (RemoteClient connection : remoteClients) {
			if (connection.isConnected()) {
				connection.closeConnection();
			}
		}
	}

	public static void broadcastMessage(String message) {
		for (RemoteClient connection : remoteClients) {
			if (connection.isConnected()) {
				connection.getWriter().println(message);
			}
		}
	}

	private static RemoteClient findClient(InetAddress address) {
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
	public static boolean playerOnTile(int y, int x) {
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

	public static void shutDown() {
		System.out.println("Game over. Shutting down");
		gameRunning = false;
		closeAllConnections();
		System.exit(0);
	}

	public static void checkStalemate() {
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