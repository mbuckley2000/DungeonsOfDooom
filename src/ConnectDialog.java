import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

/**
 * Created by matt on 23/03/2016.
 */
public class ConnectDialog extends JFrame {
	private JTextField addressEntry;
	private JTextField portEntry;
	private boolean connectButtonPressed;
	private boolean connected;
	private JLabel unableToConnectLabel;
	private Socket socket;

	public ConnectDialog() {
		super("Connect to server");

		unableToConnectLabel = new JLabel("Unable to connect... Please try again");

		JLabel addressLabel = new JLabel("Address: ");
		addressEntry = new JTextField("localhost");
		addressEntry.setPreferredSize(new Dimension(200, 25));

		JLabel portLabel = new JLabel("Port: ");
		portEntry = new JTextField("40004");
		portEntry.setPreferredSize(new Dimension(200, 25));

		JButton connectButton = new JButton("Connect");
		JButton quitButton = new JButton("Quit");

		//Build the gui
		setLayout(new FlowLayout());
		add(addressLabel);
		add(addressEntry);
		add(portLabel);
		add(portEntry);
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


		//Start the thread
		new Thread(new ConnectThread()).start();
	}

	public boolean isConnected() {
		return connected;
	}

	public Socket getSocket() {
		return socket;
	}

	private boolean isPortValid(int port) {
		return Client.isPortValid(port);
	}

	private boolean isAddressValid(String address) {
		return Client.isAddressValid(address);
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
					int port = Integer.parseInt(portEntry.getText());
					if (isAddressValid(address) && isPortValid(port)) {
						try {
							socket = new Socket(address, port);
							connected = true;
							dispose();
						} catch (Exception e) {
							connected = false;
							unableToConnectLabel.setVisible(true);
						}
					}
					connectButtonPressed = false;
				}
			}
		}
	}
}
