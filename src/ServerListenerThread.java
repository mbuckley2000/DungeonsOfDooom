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
	private BufferedReader reader;
	private boolean running;
	private boolean connected;

	private boolean holdingLookResponse;
	private char[][] lookResponse;
	private int lookWindowYIndex;
	private int lookSize;

	private boolean holdingHelloResponse;
	private int helloResponse;

	private Queue<String> messages;

	private boolean winReceived;
	private boolean loseReceived;

	private boolean holdingMoveResponse;
	private boolean moveSuccessful;

	private boolean holdingPickupResponse;
	private boolean pickupSuccessful;

	/**
	 * Constructs the Reader

	 *
	 * @param bufferedReader The buffered reader the Reader object should read from
	 */
	public ServerListenerThread(BufferedReader bufferedReader) {
		lookWindowYIndex = 0;
		helloResponse = -1;
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
					parseResponse(string);
				}
			} catch (Exception e) {
				e.printStackTrace();
				connected = false;
			}
		}

		System.out.flush();
		System.exit(0);
	}

	private void parseResponse(String response) {
		if (response.equals("WIN")) {
			winReceived = true;
			return;
		}
		if (response.equals("LOSE")) {
			loseReceived = true;
			return;
		}
		switch (response.charAt(0)) {
			case 'M':
				//Move response
				holdingMoveResponse = true;
				moveSuccessful = response.charAt(1) == 'S';
				System.out.println("LISTENER RECEIVED MOVE RESPONSE: " + moveSuccessful);
				break;
			case 'P':
				//Pickup response
				holdingPickupResponse = true;
				pickupSuccessful = response.charAt(1) == 'S';
				break;
			case 'H':
				//Hello response
				holdingHelloResponse = true;
				helloResponse = Integer.parseInt(response.substring(1));
				break;
			case 'L':
				//Look response
				int size = Character.getNumericValue(response.charAt(1));
				if (lookSize != size) lookSize = size;
				addToLookWindow(response.substring(2));
				break;
			case 'C':
				//Chat response
				messages.add(response.substring(1));
				break;
		}
	}

	public boolean isPickupSuccessful() {
		return pickupSuccessful;
	}

	public boolean isHoldingPickupResponse() {
		if (holdingPickupResponse) {
			holdingPickupResponse = false;
			return true;
		}
		return false;
	}

	public boolean isMoveSuccessful() {
		return moveSuccessful;
	}

	public boolean isHoldingMoveResponse() {
		if (holdingMoveResponse) {
			holdingMoveResponse = false;
			return true;
		}
		return false;
	}

	public boolean isLoseReceived() {
		if (loseReceived) {
			loseReceived = false;
			return true;
		}
		return false;
	}

	public boolean isWinReceived() {
		if (winReceived) {
			winReceived = false;
			return true;
		}
		return false;
	}

	/**
	 * Adds a new line to the last look window
	 * Used because the look window is sent line by line
	 * When the window is full, it overwrites the first line and starts again
	 *
	 * @param line The received getLookWindow line
	 */
	private void addToLookWindow(String line) {
		if (!holdingLookResponse) {
			if (lookResponse.length != lookSize) {
				lookResponse = new char[lookSize][lookSize];
			}
			lookResponse[lookWindowYIndex] = line.toCharArray();
			lookWindowYIndex++;
			if (lookWindowYIndex == lookSize) {
				lookWindowYIndex = 0;
				holdingLookResponse = true;
			}
		}
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
	public int getHelloResponse() {
		return helloResponse;
	}

	public boolean isHoldingLookResponse() {
		if (holdingLookResponse) {
			holdingLookResponse = false;
			return true;
		}
		return false;
	}

	public boolean isHoldingHelloResponse() {
		if (holdingHelloResponse) {
			holdingHelloResponse = false;
			return true;
		}
		return false;
	}

	public String getMessage() {
		return messages.remove();
	}

	public boolean isHoldingMessage() {
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