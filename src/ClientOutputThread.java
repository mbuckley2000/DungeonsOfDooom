import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

/**
 * ClientOutputThread for the Client
 * Prints every line it receives from the given BufferedReader to STDOUT
 * Used by the Client class to print output from server in an asynchronous manner
 *
 * @author mb2070
 * @since 25/02/2016
 */
public class ClientOutputThread implements Runnable {
	private final int lookSize = 5;
	private BufferedReader reader;
	private boolean running;
	private boolean connected;
	private char[][] lastLookWindow;
	private int lookWindowYIndex;
	private boolean lastBoolResponse;
	private int lastGoldResponse;

	public ClientOutputThread(BufferedReader reader) {
		lookWindowYIndex = 0;
		lastGoldResponse = -1;
		this.reader = reader;
		running = true;
		connected = true;
		lastLookWindow = new char[lookSize][lookSize];
	}

	@Override
	public void run() {
		String string;
		while (running && connected) {
			try {
				string = reader.readLine();
				if (string == null) {
					connected = false;
					System.err.println("Lost connection to server. Shutting down.");
					break;
				} else {
					System.out.println(string);
				}
				if (string.length() > 0) {
					if (string.charAt(0) == '#' || string.charAt(0) == '.' || string.charAt(0) == 'G' || string.charAt(0) == 'E' || string.charAt(0) == 'X' || string.charAt(0) == 'P') {
						if (!string.contains("GOLD")) {
							addToLookWindow(string);
						} else {
							lastGoldResponse = Integer.parseInt(string.replaceFirst("GOLD: ", ""));
						}
					}
					if (string.toUpperCase().equals("SUCCESS")) {
						lastBoolResponse = true;
					} else if (string.toUpperCase().equals("FAIL")) {
						lastBoolResponse = false;
					}
				}
			} catch (SocketException e) {
				connected = false;
				System.err.println("Lost connection to server. Shutting down.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addToLookWindow(String row) {
		if (lookWindowYIndex == lookSize) lookWindowYIndex = 0;
		row = row.replaceAll(" ", "");
		lastLookWindow[lookWindowYIndex] = row.toCharArray();
		lookWindowYIndex++;
	}

	public char[][] getLastLookWindow() {
		return lastLookWindow;
	}

	public int getLastGoldResponse() {
		return lastGoldResponse;
	}

	public boolean getLastBoolResponse() {
		return lastBoolResponse;
	}

	public boolean isConnected() {
		return connected;
	}

	public void stop() {
		running = false;
		connected = false;
	}
}