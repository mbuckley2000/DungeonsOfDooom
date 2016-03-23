import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by matt on 21/03/2016.
 */
public class GUIInterface extends JFrame implements PlayerInterface {
	private DPadPanel dPadPanel;
	private JButton quitButton;
	private boolean quitButtonPressed;
	private boolean pickupButtonPressed;
	private BotMap map;
	private PlayerPositionTracker positionTracker;
	private String lastCommand;
	private ChatPanel chatPanel;
	private MapPanel mapPanel;

	public GUIInterface(String title) {
		super(title);
		map = new BotMap();
		positionTracker = new PlayerPositionTracker();
		lastCommand = "";

		//Setup GUIInterface
		BorderLayout gameWindowLayout = new BorderLayout();
		getContentPane().setLayout(gameWindowLayout);


		//Setup Chat Footer
		chatPanel = new ChatPanel();
		getContentPane().add(chatPanel, BorderLayout.PAGE_END);

		//Setup Map View
		mapPanel = new MapPanel(map, positionTracker);
		getContentPane().add(mapPanel, BorderLayout.LINE_START);

		//Setup Controls
		JPanel controlsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JButton pickupButton = new JButton("Pickup");
		dPadPanel = new DPadPanel();
		quitButton = new JButton("Quit");

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		//controlsPanel.add(namePanel, c);
		c.gridx = 0;
		c.gridy = 1;
		c.ipady = 25;

		c.fill = GridBagConstraints.HORIZONTAL;
		controlsPanel.add(pickupButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.ipady = 25;

		c.fill = GridBagConstraints.HORIZONTAL;
		controlsPanel.add(quitButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.ipady = 50;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(5, 5, 5, 5);
		controlsPanel.add(dPadPanel, c);
		getContentPane().add(controlsPanel, BorderLayout.CENTER);

		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);


		//Create button listeners
		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				quitButtonPressed = true;
			}
		});

		pickupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pickupButtonPressed = true;
			}
		});
	}

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
			}
		}
	}

	@Override
	public void giveMessage(String message) {
		chatPanel.println(message);
	}

	@Override
	public boolean hasNextCommand() {
		return true;
	}

	public String getNextCommand() {
		String command = null;

		while (command == null) {
			try {
				Thread.sleep(50);
				if (lastCommand.contains("MOVE")) {
					command = "LOOK";
					break;
				}
				if (dPadPanel.isClicked()) {
					dPadPanel.reset();
					command = "MOVE " + dPadPanel.getDirection();
					break;
				}
				char move = mapPanel.getMove();
				if (move != '.') {
					command = "MOVE " + move;
					break;
				}
				if (quitButtonPressed) {
					command = "QUIT";
					quitButtonPressed = false;
					break;
				}
				if (pickupButtonPressed) {
					command = "PICKUP";
					pickupButtonPressed = false;
					break;
				}
				if (chatPanel.hasMessage()) {
					command = "SAY " + chatPanel.getMessage();
					break;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("Unable to obtain new command from GUI");
			}
		}

		lastCommand = command;
		return command;
	}
}