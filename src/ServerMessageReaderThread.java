import java.io.BufferedReader;

/**
 * ServerMessageReaderThread for the Client
 * Prints every line it receives from the given BufferedReader to STDOUT
 * Used by the Client class to print output from server in an asynchronous manner
 * Stores the last responses to each command to be publicly retrieved by the BOT
 * @author mb2070
 * @since 25/02/2016
 */
public class ServerMessageReaderThread implements Runnable {
	private final int lookSize = 5;
	private BufferedReader reader;
	private boolean running;
	private boolean connected;
	private char[][] lookResponse;
	private int lookWindowYIndex;
	private boolean successResponse;
	private int goldResponse;

	/**
	 * Constructs the reader
	 *
	 * @param reader
	 */
	public ServerMessageReaderThread(BufferedReader reader) {
		lookWindowYIndex = 0;
		goldResponse = -1;
		this.reader = reader;
		running = true;
		connected = true;
		lookResponse = new char[lookSize][lookSize];
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
					System.out.println(string);
				}
				if (string.length() > 0) {
					if (string.charAt(0) == '#' || string.charAt(0) == '.' || string.charAt(0) == 'G' || string.charAt(0) == 'E' || string.charAt(0) == 'X' || string.charAt(0) == 'P') {
						if (!string.contains("GOLD")) {
							addToLookWindow(string);
						} else {
							goldResponse = Integer.parseInt(string.replaceFirst("GOLD: ", ""));
						}
					}
					if (string.toUpperCase().equals("SUCCESS")) {
						successResponse = true;
					} else if (string.toUpperCase().equals("FAIL")) {
						successResponse = false;
					}
				}
			} catch (Exception e) {
				connected = false;
			}
		}
	}

	/**
	 * Adds a new line to the last look window
	 * Used because the look window is sent line by line
	 * When the window is full, it overwrites the first line and starts again
	 *
	 * @param line The received lookWindow line
	 */
	private void addToLookWindow(String line) {
		if (lookWindowYIndex == lookSize) lookWindowYIndex = 0;
		line = line.replaceAll(" ", "");
		lookResponse[lookWindowYIndex] = line.toCharArray();
		lookWindowYIndex++;
	}

	/**
	 * @return The look window in its current state. Usually a full look window but not guaranteed!
	 */
	public char[][] getLookResponse() {
		return lookResponse;
	}

	/**
	 * @return The most recently received response to HELLO
	 */
	public int getGoldResponse() {
		return goldResponse;
	}

	/**
	 * @return The most recently received SUCCESS / FAIL
	 */
	public boolean getSuccessResponse() {
		return successResponse;
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