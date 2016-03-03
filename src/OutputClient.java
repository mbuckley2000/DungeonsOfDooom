import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by matt on 25/02/2016.
 */
public class OutputClient implements Runnable {
	final int lookSize = 5;
	BufferedReader reader;
	boolean running;
	boolean connected;
	char[][] lastLookWindow;
	int lookWindowYIndex = 0;
	boolean lastBoolResponse;

	public OutputClient(BufferedReader reader) {
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
						addToLookWindow(string);
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

	public void printLastLookWindow() {
		for (char[] y : lastLookWindow) {
			for (char x : y) {
				System.out.print(x + "  ");
			}
			System.out.println();
		}
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