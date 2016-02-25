import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by matt on 25/02/2016.
 */
public class OutputClient implements Runnable {
	BufferedReader reader;
	boolean running;

	public OutputClient(BufferedReader reader) {
		this.reader = reader;
		running = true;
	}

	@Override
	public void run() {
		System.out.println("Reader is running");
		while (running) {
			try {
				if (reader.ready()) {
					System.out.println(reader.readLine());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		running = false;
	}
}
