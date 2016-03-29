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
	private boolean lookNeeded;
	private JLabel progressLabel;
	private boolean helloNeeded;

	public GUIInterface(String title) {
		super(title);
		map = new ClientMap();
		positionTracker = new PlayerPositionTracker();
		controller = new KeyboardController(150);
		lastCommand = "";
		helloNeeded = true;

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
	public void giveWin() {
		JDialog winDialog = new JDialog();
		winDialog.add(new JLabel("You have won!"));
		winDialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@Override
	public void giveLose() {
		JDialog loseDialog = new JDialog();
		loseDialog.add(new JLabel("Another player has won!"));
		loseDialog.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/*
	///Messages from the server are given to the interface here
	 */
	@Override
	public void giveLookResponse(char[][] response) {
		map.update(response, positionTracker.getPosition());
	}

	@Override
	public void giveHelloResponse(int response) {
		progressLabel.setText("Gold left to win: " + response);
	}

	@Override
	public void givePickupResponse(boolean response) {

	}

	@Override
	public void giveMoveResponse(boolean response) {
		if (response) {
			positionTracker.step();
			lookNeeded = true;
		}
	}

	@Override
	public void giveMessage(String message) {
		chatPanel.println(message);
	}


	/*
	///Messages to the server are picked up from here
	 */
	@Override
	public boolean hasNextCommand() {
		return true;
	}

	public String getNextCommand() {
		String command = null;
		while (command == null) {
			if (lookNeeded) {
				command = "LOOK";
				lookNeeded = false;
				break;
			}
			if (helloNeeded) {
				command = "HELLO";
				helloNeeded = false;
				break;
			}
			if (controller.isMovePressed()) {
				char dir = controller.getMoveDirection();
				positionTracker.setDirection(dir);
				command = "MOVE " + dir;
				break;
			}
			if (controller.isQuitPressed()) {
				command = "QUIT";
				break;
			}
			if (controller.isPickupPressed()) {
				command = "PICKUP";
				helloNeeded = true;
				break;
			}
			if (controller.isLookPressed()) {
				command = "LOOK";
				break;
			}
			if (controller.isHelloPressed()) {
				command = "HELLO";
				break;
			}
			if (chatPanel.hasMessage()) {
				command = "SAY " + chatPanel.getMessage();
				break;
			}
		}
		lastCommand = command;
		return command;
	}
}