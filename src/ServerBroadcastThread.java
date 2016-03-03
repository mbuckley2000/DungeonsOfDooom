import java.util.Scanner;

/**
 * Created by matt on 03/03/2016.
 */
public class ServerBroadcastThread implements Runnable {
	Scanner input;
	boolean running;

	public ServerBroadcastThread() {
		input = new Scanner(System.in);
		running = true;
	}

	@Override
	public void run() {
		while (running) {
			Server.broadcast("CoolBigDaddyB: " + input.nextLine());
		}
		input.close();
	}

	public void close() {
		running = false;
	}
}
