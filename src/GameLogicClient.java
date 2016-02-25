import java.io.*;
import java.net.Socket;

/**
 * Created by matt on 24/02/2016.
 */
public class GameLogicClient implements IGameLogic {
	int port = 40004;
	String address = "localhost";
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
    boolean connected;
	OutputClient outputClient;

	public GameLogicClient() {
		try {
			System.out.println("Connecting to " + address + ":" + port);
			socket = new Socket(address, port);

			System.out.println("Connected to server");
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
			outputClient = new OutputClient(reader);
			new Thread(outputClient).start();
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
        return receive();
    }

	@Override
	public String move(char direction) {
        send("MOVE " + direction);
        return receive();
    }

	@Override
	public String pickup() {
        send("PICKUP");
        return receive();
    }

	@Override
	public String look() {
        send("LOOK");
		return receive();
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
	        outputClient.stop();
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

    private String receive() {
	    /*
	    String string = "empty";
        try {
	        while (string.equals("empty")) {
		        string = reader.readLine() + "\n";
	        }
	        while (reader.ready()) {
		        string += reader.readLine() + "\n";
	        }
            if (string == null) {
                connected = false;
            }
            return string;
        } catch (IOException e) {
            connected = false;
        }
        return null;
        */
	    return "";
    }
}
