import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Client class implementing IGameLogic
 * Can be used in place of the old GameLogic class.
 * Connects to server, performs very basic input filtering, and sends commands
 * All output from the server is printed and interpreted in a ServerListenerThread to prevent blocking
 *
 * @author mb2070
 * @since 24/02/2016
 */
public class Client {
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean connected;
	private ServerListenerThread serverListenerThread;

	/**
	 * Constructor
	 */
	public Client(String address, int port) {
		int connectionAttempts = 0;

		while (!connected && connectionAttempts < 5) {
			connectionAttempts++;
			try {
				System.out.println("Attempting to connect to " + address + ":" + port);
				socket = new Socket(address, port);

				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);

				connected = true;
				serverListenerThread = new ServerListenerThread(reader);
				new Thread(serverListenerThread).start();

				Thread.sleep(1000); //Sleep to give outputClientThread time to validate connection
			} catch (ConnectException e) {
				System.err.println("Unable to connect: " + e.getMessage());
				System.err.println("Attempting to re-establish connection");
			} catch (IOException e) {
				System.err.println("Unable to establish IO streams");
				System.err.println("For debug:");
				if (socket != null) {
					System.err.println("Socket bound: " + socket.isBound());
					System.err.println("Socket connected: " + socket.isConnected());
					System.err.println("Socket closed: " + socket.isClosed());
					System.err.println("Input shutdown: " + socket.isInputShutdown());
					System.err.println("Output shutdown: " + socket.isOutputShutdown());
				}
				System.err.println("Attempting to re-establish connection");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (connectionAttempts > 4) {
			System.err.println("Maximum connection attempts exceeded");
			System.err.println("Please check that the server is running on the specified address & port");
			System.err.println("Please check that all ports are open and that no firewalls are blocking communication");
		}
	}

	public static boolean isAddressValid(String address) {
		try {
			if (address == null) return false;

			if (address.isEmpty()) return false;

			if (address.equals("localhost")) return true;

			String[] sections = address.split("\\.");
			for (String section : sections) {
				int sectionInt = Integer.parseInt(section);
				if (sectionInt > 255 || sectionInt < 0) return false;
			}

			if (sections.length != 4) return false;

			return !address.endsWith(".");

		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public static boolean isPortValid(int port) {
		return (port > 0 && port < 65535);
	}

	/**
	 * @return Returns the message reader that prints and interprets all messages from the server
	 */
	public ServerListenerThread getServerListenerThread() {
		return serverListenerThread;
	}

	/**
	 * @return True if the game is running
	 */
	public boolean gameRunning() {
		return connected && serverListenerThread.isConnected();
	}

	/**
	 * Closes the socket, the ServerListenerThread, and all streams.
	 */
	public void close() {
		try {
			writer.close();
			serverListenerThread.stop();
			reader.close();
			socket.close();
			System.exit(0);
		} catch (Exception e) {
			System.exit(0);
		}
	}

	/**
	 * Sends a string to the server
	 *
	 * @param string String to send
	 */
	public void send(String string) {
		writer.println(string);
	}
}