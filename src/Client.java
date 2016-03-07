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
 * All output from the server is picked up in a ClientOutputThread to prevent blocking
 *
 * @author mb2070
 * @since 24/02/2016
 */
public class Client implements IGameLogic {
	private final int port = 40004;
	private final String address = "localhost";
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean connected;
	private ClientOutputThread clientOutputThread;

	public Client() {
		int connectionAttempts = 0;
		while (!connected && connectionAttempts < 5) {
			connectionAttempts++;
			try {
				System.out.println("Attempting to connect to " + address + ":" + port);
				socket = new Socket(address, port);

				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);

				connected = true;
				clientOutputThread = new ClientOutputThread(reader);
				new Thread(clientOutputThread).start();

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

	public boolean isConnected() {
		return connected;
	}

	public ClientOutputThread getClientOutputThread() {
		return clientOutputThread;
	}

	public String hello() {
		send("HELLO");
		return null;
	}

	public String move(char direction) {
		send("MOVE " + direction);
		return null;
	}

	public String pickup() {
		send("PICKUP");
		return null;
	}

	public String look() {
		send("LOOK");
		return null;
	}

	public boolean gameRunning() {
		return connected && clientOutputThread.isConnected();
	}

	public void close() {
		try {
			writer.close();
			clientOutputThread.stop();
			reader.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void quitGame() {
		try {
			writer.println("QUIT");
		} catch (Exception e) {
			e.printStackTrace();
		}
		connected = false;
	}

	private void send(String string) {
		writer.println(string);
	}
}