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
	private boolean needToLook;

	public GUIInterface(String title) {
		super(title);
		map = new ClientMap();
		positionTracker = new PlayerPositionTracker();
		controller = new KeyboardController(150);
		lastCommand = "";

		//Setup GUIInterface
		BorderLayout gameWindowLayout = new BorderLayout();
		getContentPane().setLayout(gameWindowLayout);

		//Setup instructions label
		JLabel instructionsLabel = new JLabel("Keyboard Controls:    Move: Arrow Keys    Pickup: Space    Quit: Escape");
		add(instructionsLabel, BorderLayout.PAGE_START);

		//Setup controls panel
		ControlPanel controlPanel = new ControlPanel(controller);
		add(controlPanel, BorderLayout.LINE_END);

		//Setup Chat Footer
		chatPanel = new ChatPanel();
		getContentPane().add(chatPanel, BorderLayout.PAGE_END);

		//Setup ServerMap View
		mapPanel = new MapPanel(map, positionTracker);
		getContentPane().add(mapPanel, BorderLayout.CENTER);

		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
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
	}

	@Override
	public void giveSuccessResponse(boolean response) {
		if (response) {
			if (lastCommand.contains("MOVE")) {
				positionTracker.step(lastCommand.charAt(5));
				needToLook = true;
			}
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
			if (needToLook) {
				command = "LOOK";
				needToLook = false;
				break;
			}
			if (controller.isMovePressed()) {
				command = "MOVE " + controller.getMoveDirection();
				break;
			}
			if (controller.isQuitPressed()) {
				command = "QUIT";
				break;
			}
			if (controller.isPickupPressed()) {
				command = "PICKUP";
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