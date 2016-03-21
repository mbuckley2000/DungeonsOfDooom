import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 21/03/2016.
 */
public class GameWindow extends JFrame implements IUserInput {
	private DPadPanel dPadPanel;

	public GameWindow(String title) {
		super(title);
		//Setup GameWindow
		BorderLayout gameWindowLayout = new BorderLayout();
		getContentPane().setLayout(gameWindowLayout);

		//Setup Map View
		MapPanel mapPanel = new MapPanel();
		getContentPane().add(mapPanel, BorderLayout.CENTER);

		//Setup Header
		JPanel headerPanel = new JPanel();
		FlowLayout headerLayout = new FlowLayout();
		JButton quitButton = new JButton("Quit");
		JTextField nameTextField = new JTextField("Enter your name");
		nameTextField.setPreferredSize(new Dimension(200, 25));
		JButton setNameButton = new JButton("Set Name");
		headerPanel.setLayout(headerLayout);
		headerPanel.add(quitButton);
		headerPanel.add(nameTextField);
		headerPanel.add(setNameButton);
		getContentPane().add(headerPanel, BorderLayout.PAGE_START);

		//Setup Chat Footer
		JPanel chatPanel = new JPanel();
		BoxLayout chatLayout = new BoxLayout(chatPanel, BoxLayout.PAGE_AXIS);
		JPanel chatEntry = new JPanel();
		chatEntry.setLayout(new FlowLayout());
		JButton chatSendButton = new JButton("Send");
		JTextField chatEntryTextField = new JTextField();
		chatEntryTextField.setPreferredSize(new Dimension(300, 25));
		chatEntry.add(chatEntryTextField);
		chatEntry.add(chatSendButton);
		chatPanel.setLayout(chatLayout);
		ServerListenerPanel serverListenerPanel = new ServerListenerPanel();
		chatPanel.add(serverListenerPanel);
		chatPanel.add(chatEntry);
		getContentPane().add(chatPanel, BorderLayout.PAGE_END);

		//Setup Controls
		JPanel controlsPanel = new JPanel();
		BoxLayout controlsLayout = new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS);
		controlsPanel.setLayout(controlsLayout);
		JButton pickupButton = new JButton("Pickup");
		controlsPanel.add(pickupButton);
		dPadPanel = new DPadPanel();
		controlsPanel.add(dPadPanel);
		getContentPane().add(controlsPanel, BorderLayout.LINE_END);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public String getNextCommand() {
		System.out.println("Waiting for command");

		String command = null;
		while (command == null) {
			if (dPadPanel.isClicked()) {
				System.out.println("Detected click");
				dPadPanel.reset();
				command = "MOVE " + dPadPanel.getDirection();
			}
		}
		System.out.println("Got command");

		return command;
	}
}