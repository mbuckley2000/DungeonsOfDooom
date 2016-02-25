import java.io.*;
import java.net.Socket;

/**
 * Created by matt on 24/02/2016.
 */
public class GameLogicClient implements IGameLogic {
	int port = 40004;
	String address = "localhost"; //Ricky's IP: 138.38.171.121
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
    boolean connected;
	boolean output;
	OutputClient outputClient;

	public GameLogicClient(boolean output) {
		try {
			this.output = output;
			System.out.println("Connecting to " + address + ":" + port);
			socket = new Socket(address, port);

			System.out.println("Connected to server");
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
			if (output) {
				outputClient = new OutputClient(reader);
				new Thread(outputClient).start();
			}
		} catch (IOException e) {
            e.printStackTrace();
		}
	}

	@Override
	public void setMap(File file) {

	}

	@Override
	public String hello() {
        send("HELLO");
		return null;
	}

	@Override
	public String move(char direction) {
        send("MOVE " + direction);
		return null;
	}

	@Override
	public String pickup() {
        send("PICKUP");
		return null;
	}

	@Override
	public String look() {
        send("LOOK");
		return null;
	}

	@Override
	public boolean gameRunning() {
        return connected;
    }

	@Override
	public void quitGame() {
        try {
            writer.println("QUIT");
            writer.close();
	        if (output) outputClient.stop();
	        reader.close();
	        socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connected = false;
    }

    private void send(String string) {
        writer.println(string);
    }
}
