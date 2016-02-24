import java.io.*;
import java.net.Socket;

/**
 * Created by matt on 24/02/2016.
 */
public class GameLogicClient implements IGameLogic {
	int port = 25543;
	String address = "127.0.0.1";
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;

	public GameLogicClient() {
		try {
			System.out.println("Connecting to " + address + " on port " + port);
			socket = new Socket(address, port);

			System.out.println("Just connected to " + socket.getRemoteSocketAddress());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setMap(File file) {

	}

	@Override
	public String hello() {
		try {
			writer.write("HELLO");
			return (reader.readLine());
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public String move(char direction) {
		return null;
	}

	@Override
	public String pickup() {
		return null;
	}

	@Override
	public String look() {
		return null;
	}

	@Override
	public boolean gameRunning() {
		return false;
	}

	@Override
	public void quitGame() {

	}
}
