import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Created by matt on 24/02/2016.
 */
public class GameLogicClient implements IGameLogic {
	private final int port = 40004;
	private final String address = "localhst"; //Ricky's IP: 138.38.153.79  James' IP 138.38.159.174   My ip 138.38.193.197
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean connected;
	private OutputClient outputClient;

	public GameLogicClient() {
		int connectionAttempts = 0;
		while (!connected && connectionAttempts < 5) {
			connectionAttempts++;
			try {
				System.out.println("Attempting to connect to " + address + ":" + port);
				socket = new Socket(address, port);

				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);

				connected = true;
				outputClient = new OutputClient(reader);
				new Thread(outputClient).start();

				Thread.sleep(1000); //Sleep to give outputClient thread time to validate connection
			} catch (ConnectException e) {
				System.err.println("Unable to connect: " + e.getMessage());
				System.err.println("Attempting to re-establish connection");
			} catch (IOException e) {
				System.err.println("Unable to establish IO streams");
				System.err.println("For debug:");
				System.err.println("Socket bound: " + socket.isBound());
				System.err.println("Socket connected: " + socket.isConnected());
				System.err.println("Socket closed: " + socket.isClosed());
				System.err.println("Input shutdown: " + socket.isInputShutdown());
				System.err.println("Output shutdown: " + socket.isOutputShutdown());
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

	public OutputClient getOutputClient() {
		return outputClient;
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
		return connected && outputClient.isConnected();
	}

	public void close() {
		try {
			writer.close();
			outputClient.stop();
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