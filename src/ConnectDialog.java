import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Socket;

/**
 * Created by matt on 23/03/2016.
 */
public class ConnectDialog extends JFrame {
	private JTextField addressEntry;
	private JTextField portEntry;
	private JTextField nameEntry;
	private boolean connectButtonPressed;
	private boolean connected;
	private JLabel unableToConnectLabel;
	private Socket socket;
	private String name;

	public ConnectDialog() {
		super("Connect to server");

		unableToConnectLabel = new JLabel("Unable to connect... Please try again");

		JLabel addressLabel = new JLabel("Address: ");
		addressEntry = new JTextField("localhost");
		addressEntry.setPreferredSize(new Dimension(200, 25));

		JLabel portLabel = new JLabel("Port: ");
		portEntry = new JTextField("40004");
		portEntry.setPreferredSize(new Dimension(200, 25));

		JLabel nameLabel = new JLabel("Display Name: ");
		nameEntry = new JTextField("Unnamed");
		nameEntry.setPreferredSize(new Dimension(200, 25));

		JButton connectButton = new JButton("Connect");
		JButton quitButton = new JButton("Quit");

		//Build the gui
		setLayout(new FlowLayout());
		add(addressLabel);
		add(addressEntry);
		add(portLabel);
		add(portEntry);
		add(nameLabel);
		add(nameEntry);
		add(connectButton);
		add(unableToConnectLabel);

		pack();
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		unableToConnectLabel.setVisible(false);

		//Button Listeners
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connectButtonPressed = true;
			}
		});

		quitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		//Key Listeners
		addressEntry.addKeyListener(new SubmitOnEnter());
		portEntry.addKeyListener(new SubmitOnEnter());
		nameEntry.addKeyListener(new SubmitOnEnter());

		//Start the thread
		new Thread(new ConnectThread()).start();
	}

	public boolean isConnected() {
		return connected;
	}

	public Socket getSocket() {
		return socket;
	}

	public String getName() {
		return name;
	}

	private boolean isPortValid(int port) {
		return Client.isPortValid(port);
	}

	private boolean isAddressValid(String address) {
		return Client.isAddressValid(address);
	}

	private boolean isNameValid(String name) {
		return name.length() >= 3 && name.length() <= 10;
	}

	public class ConnectThread implements Runnable {
		public void run() {
			while (!connected) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if (connectButtonPressed) {
					String address = addressEntry.getText();
					name = nameEntry.getText();
					int port = -1;
					if (!portEntry.getText().isEmpty()) {
						port = Integer.parseInt(portEntry.getText());
					}
					if (isAddressValid(address) && isPortValid(port) && isNameValid(name)) {
						try {
							socket = new Socket(address, port);
							connected = true;
							dispose();
						} catch (Exception e) {
							connected = false;
							unableToConnectLabel.setText("Unable to connect to server");
							unableToConnectLabel.setVisible(true);
						}
					} else {
						if (!isNameValid(name)) {
							unableToConnectLabel.setText("Invalid name");
							unableToConnectLabel.setVisible(true);
						} else if (!isAddressValid(address)) {
							unableToConnectLabel.setText("Invalid address");
							unableToConnectLabel.setVisible(true);
						} else if (!isPortValid(port)) {
							unableToConnectLabel.setText("Invalid port");
							unableToConnectLabel.setVisible(true);
						}
					}
					connectButtonPressed = false;
				}
			}
		}
	}

	private class SubmitOnEnter implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				connectButtonPressed = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
}
