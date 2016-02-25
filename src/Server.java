/**
 * Created by matt on 24/02/2016.
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Server implements Runnable, IGameLogic {
	static int port = 40004;
	static Map map;
	static Set<Server> connections = new HashSet<>();
    static boolean gameRunning;
    Socket clientSocket;
    boolean connected;
	int[] playerPosition;
	int collectedGold;
	PrintWriter writer;

	Server(Socket clientSocket) {
		this.clientSocket = clientSocket;
		connected = true;
		collectedGold = 0;
		playerPosition = initialisePlayer();
		connections.add(this);
	}

	public static void main(String args[]) throws Exception {
		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Listening for connections");

        gameRunning = true;

		//Setup
		//Load map
		map = new Map();
		map.readMap(new File("maps/example_map.txt"));


        while (gameRunning) {
            Socket clientSocket = serverSocket.accept();
	        System.out.println(clientSocket.getInetAddress() + "\t\tConnected");
	        broadcast(clientSocket.getInetAddress() + " has joined the game");
			new Thread(new Server(clientSocket)).start();
		}
	}

	static private void closeAllConnections() {
		for (Server connection : connections) {
			connection.close();
		}
	}

	static void broadcast(String message) {
		for (Server connection : connections) {
			connection.getWriter().println(message);
		}
	}

	public void run() {
		try {
			// Get input from client
			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			String input;

			while (connected && gameRunning) {
				input = reader.readLine();
				System.out.println(clientSocket.getInetAddress() + "\t\t" + input);
				if (input != null) {
	                writer.println(parseInput(input.toUpperCase()));
				} else {
                    connected = false;
                }
            }

			System.out.println(clientSocket.getInetAddress() + "\t\tDisconnected");
			broadcast(clientSocket.getInetAddress() + " has left the game");
			writer.close();
            reader.close();
            clientSocket.close();
			connections.remove(this);
		}
		// handle exception (more work needed here)
		catch (IOException e) {
            e.printStackTrace();
        }
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
					answer = move(command[1].charAt(0));
				}
				break;
			case "PICKUP":
				answer = pickup();
				break;
			case "LOOK":
				answer = look().replaceAll(".(?!$)", "$0  ");
				break;
			case "QUIT":
                connected = false;
                break;
            default:
				answer = "FAIL";
				break;
		}
		return answer;
	}

	@Override
	public void setMap(File file) {
		map.readMap(file);
	}

	@Override
	public String hello() {
		return Integer.toString(map.getWin());
	}

	@Override
	public String move(char direction) {
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

        synchronized (map) {
            if (map.lookAtTile(newPosition[0], newPosition[1]) != '#' && !playerOnTile(newPosition[0], newPosition[1])) {
                playerPosition = newPosition;
	            if (checkWin()) {
		            broadcast("Player x just won! Game is now over.");
		            broadcast("Seriously, get out.");
		            gameRunning = false;
		            return "Congratulations!!! \n You have escaped the Dungeon of Dooom!!!!!! \nThank you for playing!";
	            }
                return "SUCCESS";
            } else {
                return "FAIL";
            }
        }
    }

	public PrintWriter getWriter() {
		return writer;
	}

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 *
	 * @return True if all conditions are met, false otherwise
	 */
	protected boolean checkWin() {
		if (collectedGold >= map.getWin() && map.lookAtTile(playerPosition[0], playerPosition[1]) == 'E') {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String pickup() {
		if (map.lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			collectedGold++;
			map.replaceTile(playerPosition[0], playerPosition[1], '.');
			return "SUCCESS, GOLD COINS: " + collectedGold;
		}

		return "FAIL\nThere is nothing to pick up...";
	}

	@Override
	public String look() {
		String output = "";
		char[][] lookReply = map.lookWindow(playerPosition[0], playerPosition[1], 5);
		lookReply[2][2] = 'P';

		for (int i = 0; i < lookReply.length; i++) {
			for (int j = 0; j < lookReply[0].length; j++) {
                if (playerOnTile(playerPosition[0] - 2 + i, playerPosition[1] - 2 + j)) {
                    output += 'P';
                } else {
                    output += lookReply[j][i];
                }
            }
            output += "\n";
		}
		return output;
	}

	@Override
	public boolean gameRunning() {
		return gameRunning;
	}

	@Override
	public void quitGame() {
		connected = false;
	}

	private void close() {
		connected = false;
	}

	/**
	 * Checks if a player is on the specified tile
	 *
	 * @param y Y ordinate of the tile
	 * @param x X ordinate of the tile
	 * @return True if a player is on the specified tile, false otherwise
	 */
	private boolean playerOnTile(int y, int x) {
		boolean hit = false;
		for (Server connection : connections) {
			if (connection != null) {
				if (connection.getPlayerPosition()[0] == y && connection.getPlayerPosition()[1] == x) {
					hit = true;
				}
			}
		}
		return hit;
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

		pos[0] = rand.nextInt(map.getMapHeight() - 1);
		pos[1] = rand.nextInt(map.getMapWidth() - 1);
		int counter = 1;
		while (map.lookAtTile(pos[0], pos[1]) == '#' && !playerOnTile(pos[0], pos[1]) && counter < map.getMapHeight() * map.getMapWidth()) {
			pos[1] = (int) (counter * Math.cos(counter));
			pos[0] = (int) (counter * Math.sin(counter));
			counter++;
		}
		return (map.lookAtTile(pos[0], pos[1]) == '#') ? null : pos;
	}

}