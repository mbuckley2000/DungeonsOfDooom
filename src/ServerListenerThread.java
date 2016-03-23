import java.io.BufferedReader;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * ServerListenerThread for the Client
 * Prints every line it receives from the given BufferedReader to STDOUT
 * Used by the Client class to print output from server in an asynchronous manner
 * Stores the last responses to each command to be publicly retrieved by the BOT
 *
 * @author mb2070
 * @since 25/02/2016
 */
public class ServerListenerThread implements Runnable {
	private final int lookSize = 5;
	private BufferedReader reader;
	private boolean running;
	private boolean connected;
	private char[][] lookResponse;
	private int lookWindowYIndex;
	private boolean successResponse;
	private int goldResponse;
	private boolean hasSuccessResponse;
	private boolean hasGoldResponse;
	private boolean hasLookResponse;
	private boolean hasMessage;
	private Queue<String> messages;


	/**
	 * Constructs the Reader
	 *
	 * @param bufferedReader The buffered reader the Reader object should read from
	 */
	public ServerListenerThread(BufferedReader bufferedReader) {
		lookWindowYIndex = 0;
		goldResponse = -1;
		this.reader = bufferedReader;
		running = true;
		connected = true;
		lookResponse = new char[lookSize][lookSize];
		messages = new PriorityQueue<>();
	}

	/**
	 * Runs the reader thread
	 */
	public void run() {
		String string;
		while (running && connected) {
			try {
				string = reader.readLine();
				if (string == null) {
					connected = false;
					break;
				} else {
					//parse the string
					if (string.length() > 0) {
						if (string.charAt(0) == '#' || string.charAt(0) == '.' || string.charAt(0) == 'G' || string.charAt(0) == 'E' || string.charAt(0) == 'X' || string.charAt(0) == 'P') {
							if (!string.contains("GOLD")) {
								addToLookWindow(string);
							} else {
								goldResponse = Integer.parseInt(string.replaceFirst("GOLD: ", ""));
								hasGoldResponse = true;
							}
						}
						if (string.toUpperCase().equals("SUCCESS")) {
							hasSuccessResponse = true;
							successResponse = true;
						} else if (string.toUpperCase().equals("FAIL")) {
							hasSuccessResponse = true;
							successResponse = false;
						}
						if (string.startsWith("MESSAGE")) {
							hasMessage = true;
							messages.add(string.replaceFirst("MESSAGE", ""));
						}
					}
				}
			} catch (Exception e) {
				connected = false;
			}
		}

		System.out.flush();
		System.exit(0);
	}

	/**
	 * Adds a new line to the last look window
	 * Used because the look window is sent line by line
	 * When the window is full, it overwrites the first line and starts again
	 *
	 * @param line The received lookWindow line
	 */
	private void addToLookWindow(String line) {
		line = line.replaceAll(" ", "");
		lookResponse[lookWindowYIndex] = line.toCharArray();
		lookWindowYIndex++;
		if (lookWindowYIndex == lookSize) {
			lookWindowYIndex = 0;
			hasLookResponse = true;
		}
	}

	/**
	 * @return The look window in its current state. Usually a full look window but not guaranteed!
	 */
	public char[][] getLookResponse() {
		hasLookResponse = false;
		return lookResponse;
	}

	/**
	 * @return The most recently received response to HELLO
	 */
	public int getGoldResponse() {
		hasGoldResponse = false;
		return goldResponse;
	}

	/**
	 * @return The most recently received SUCCESS / FAIL
	 */
	public boolean getSuccessResponse() {
		hasSuccessResponse = false;
		return successResponse;
	}

	public String getMessage() {
		hasMessage = false;
		return messages.remove();
	}

	public boolean hasSuccessResponse() {
		return hasSuccessResponse;
	}

	public boolean hasLookResponse() {
		return hasLookResponse;
	}

	public boolean hasGoldResponse() {
		return hasGoldResponse;
	}

	public boolean hasMessage() {
		return !messages.isEmpty();
	}

	/**
	 * @return True if no disconnection from the server has been detected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Stops the reader
	 */
	public void stop() {
		running = false;
		connected = false;
	}
}