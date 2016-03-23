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
	private boolean lookButtonPressed;
	private NamePanel namePanel;
	private BotMap map;
	private PlayerPositionTracker positionTracker;
	private String lastCommand;
	private ChatPanel chatPanel;

	public GUIInterface(String title) {
		super(title);
		map = new BotMap();
		positionTracker = new PlayerPositionTracker();
		lastCommand = "";

		//Setup GUIInterface
		BorderLayout gameWindowLayout = new BorderLayout();
		getContentPane().setLayout(gameWindowLayout);

		//Setup Map View
		MapPanel mapPanel = new MapPanel(map, positionTracker);
		getContentPane().add(mapPanel, BorderLayout.CENTER);

		//Setup Header
		JPanel headerPanel = new JPanel();
		quitButton = new JButton("Quit");
		quitButton.setAlignmentX(JButton.LEFT_ALIGNMENT);
		namePanel = new NamePanel();
		namePanel.setAlignmentX(NamePanel.RIGHT_ALIGNMENT);
		headerPanel.add(quitButton);
		headerPanel.add(namePanel);
		getContentPane().add(headerPanel, BorderLayout.PAGE_START);

		//Setup Chat Footer
		chatPanel = new ChatPanel();
		getContentPane().add(chatPanel, BorderLayout.PAGE_END);

		//Setup Controls
		JPanel controlsPanel = new JPanel();
		BoxLayout controlsLayout = new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS);
		controlsPanel.setLayout(controlsLayout);
		JButton pickupButton = new JButton("Pickup");
		JButton lookButton = new JButton("Look");
		controlsPanel.add(pickupButton);
		controlsPanel.add(lookButton);
		dPadPanel = new DPadPanel();
		controlsPanel.add(dPadPanel);
		getContentPane().add(controlsPanel, BorderLayout.LINE_END);

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

		lookButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lookButtonPressed = true;
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
				if (quitButtonPressed) {
					command = "QUIT";
					quitButtonPressed = false;
					break;
				}
				if (lookButtonPressed) {
					command = "LOOK";
					lookButtonPressed = false;
					break;
				}
				if (pickupButtonPressed) {
					command = "PICKUP";
					pickupButtonPressed = false;
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