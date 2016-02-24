import java.io.*;
import java.net.Socket;

/**
 * Created by matt on 24/02/2016.
 */
public class GameLogicClient implements IGameLogic {
    static boolean debug = true;
    int port = 25543;
    String address = "127.0.0.1";
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
    boolean connected;

	public GameLogicClient() {
		try {
			System.out.println("Connecting to " + address + " on port " + port);
			socket = new Socket(address, port);

			System.out.println("Just connected to " + socket.getRemoteSocketAddress());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
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
        String lookWindow = "";
        for (int i = 0; i < 6; i++) {
            lookWindow += receive().replaceAll(".(?!$)", "$0  ");
            lookWindow += '\n';
        }
        return lookWindow;
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
        try {
            String string = reader.readLine();
            if (string == null) {
                connected = false;
            }
            return string;
        } catch (IOException e) {
            connected = false;
        }
        return null;
    }
}
