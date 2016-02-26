import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by matt on 25/02/2016.
 */
public class OutputClient implements Runnable {
	BufferedReader reader;
	boolean running;
	boolean connected;

	public OutputClient(BufferedReader reader) {
		this.reader = reader;
		running = true;
		connected = true;
	}

	@Override
	public void run() {
		System.out.println("Reader is running");
		String string;
		while (running) {
			try {
				if (reader.ready()) {
					string = reader.readLine();
					if (string == null) {
						connected = false;
					} else {
						System.out.println(string);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void stop() {
		running = false;
	}
}