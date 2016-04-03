import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 21/03/2016.
 */
public class GUIInterface extends JFrame implements PlayerInterface {
	private ClientMap map;
	private PlayerPositionTracker positionTracker;
	private String lastCommand;
	private ChatPanel chatPanel;
	private MapPanel mapPanel;
	private Controller controller;
	private boolean finished;
	private boolean lookNeeded;
	private JLabel progressLabel;
	private boolean helloNeeded;
	private boolean lookResponseReceived;
	private boolean helloResponseReceived;
	private boolean pickupResponseReceived;
	private boolean moveResponseReceived;


	public GUIInterface(String title) {
		super(title);
		map = new ClientMap();
		positionTracker = new PlayerPositionTracker();
		controller = new KeyboardController(150);
		lastCommand = "";
		helloNeeded = true;
		lookResponseReceived = true;
		helloResponseReceived = true;
		pickupResponseReceived = true;
		moveResponseReceived = true;

		//Setup GUIInterface
		BorderLayout gameWindowLayout = new BorderLayout();
		getContentPane().setLayout(gameWindowLayout);

		//Setup instructions label
		JLabel instructionsLabel = new JLabel("Keyboard Controls:    Move: Arrow Keys    Pickup: Space    Quit: Escape");
		progressLabel = new JLabel("Gold left to win: ");
		JPanel labels = new JPanel(new FlowLayout());
		labels.add(instructionsLabel);
		labels.add(progressLabel);
		add(labels, BorderLayout.PAGE_START);


		//Setup controls panel
		ControlPanel controlPanel = new ControlPanel(controller);
		add(controlPanel, BorderLayout.LINE_END);

		//Setup Chat Footer
		chatPanel = new ChatPanel();
		getContentPane().add(chatPanel, BorderLayout.PAGE_END);

		//Setup ServerMap View
		mapPanel = new MapPanel(map, positionTracker, 4);
		getContentPane().add(mapPanel, BorderLayout.CENTER);

		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void giveWin() {
		JDialog winDialog = new JDialog();
		winDialog.add(new JLabel("You have won!"));
		winDialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
		finished = true;
	}

	@Override
	public void giveLose() {
		JDialog loseDialog = new JDialog();
		loseDialog.add(new JLabel("Another player has won!"));
		loseDialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
		finished = true;
	}

	/*
	///Messages from the server are given to the interface here
	 */
	@Override
	public void giveLookResponse(char[][] response) {
		map.update(response, positionTracker.getPosition());
		lookResponseReceived = true;
	}

	@Override
	public void giveHelloResponse(int response) {
		System.out.println("GUI: Received hello response: " + response);
		progressLabel.setText("Gold left to win: " + response);
		helloResponseReceived = true;
	}

	@Override
	public void givePickupResponse(boolean response) {
		System.out.println("GUI: Received pickup response: " + response);
		pickupResponseReceived = true;
	}

	@Override
	public void giveMoveResponse(boolean response) {
		System.out.println("GUI: Received move response: " + response);
		if (response) {
			positionTracker.step();
		}
		lookNeeded = true;
		moveResponseReceived = true;
	}

	@Override
	public void giveMessage(String message) {
		System.out.println("GUI: Received message: " + message);
		chatPanel.println(message);
	}


	/*
	///Messages to the server are picked up from here
	 */
	@Override
	public boolean hasNextCommand() {
		if (controller.hasAction()) System.out.println("GOT CONTROLLER ACTION");
		return controller.hasAction() || lookNeeded || helloNeeded;
	}

	public String getNextCommand() {
		if (lookNeeded && lookResponseReceived) {
			lookNeeded = false;
			lastCommand = "LOOK";
			lookResponseReceived = false;
			return lastCommand;
		}
		if (helloNeeded && helloResponseReceived) {
			lastCommand = "HELLO";
			helloNeeded = false;
			helloResponseReceived = false;
			return lastCommand;
		}
		if (controller.isMovePressed() && moveResponseReceived) {
			char dir = controller.getMoveDirection();
			positionTracker.setDirection(dir);
			lastCommand = "MOVE " + dir;
			moveResponseReceived = false;
			return lastCommand;
		}
		if (controller.isQuitPressed()) {
			lastCommand = "QUIT";
			return lastCommand;
		}
		if (controller.isPickupPressed() && pickupResponseReceived) {
			lastCommand = "PICKUP";
			helloNeeded = true;
			pickupResponseReceived = false;
			return lastCommand;
		}
		if (controller.isLookPressed() && lookResponseReceived) {
			lastCommand = "LOOK";
			lookResponseReceived = false;
			return lastCommand;
		}
		if (controller.isHelloPressed() && helloResponseReceived) {
			lastCommand = "HELLO";
			helloResponseReceived = false;
			return lastCommand;
		}
		if (chatPanel.hasMessage()) {
			lastCommand = "SAY " + chatPanel.getMessage();
			return lastCommand;
		}
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return getNextCommand();
	}
}