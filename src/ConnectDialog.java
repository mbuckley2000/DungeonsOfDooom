import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Socket;

/**
 * GUI dialog for entering server information and connecting to server
 *
 * @author Matt Buckley
 * @since 23/03/2016
 */
public class ConnectDialog extends JFrame {
    private JTextField addressEntry;
    private JTextField portEntry;
    private JTextField nameEntry;
    private boolean connectButtonPressed;
    private boolean connected;
    private JLabel instructionLabel;
    private Socket socket;
    private String name;
    private double scale;
    private JCheckBox botCheckBox;

    public ConnectDialog() {
        super("Connect to server");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Couldn't use system default look and feel");
        }

        scale = 1.0;//DPI scaling
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        if (screenWidth > 1600) {
            scale = screenWidth / 1600;
        }

        instructionLabel = new JLabel("Please enter server details");

        JLabel addressLabel = new JLabel("Address: ");
        addressEntry = new JTextField("localhost", 20);

        JLabel portLabel = new JLabel("Port: ");
        portEntry = new JTextField("40004", 20);

        JLabel nameLabel = new JLabel("Display Name: ");
        nameEntry = new JTextField("Unnamed", 20);

        botCheckBox = new JCheckBox("Bot");

        JButton connectButton = new JButton("Connect");
        JButton quitButton = new JButton("Quit");

        int border = (int) (25 * scale); //From the edge of the window
        int spacing = (int) (5 * scale); //between each component

        //Build the gui
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(border, border, spacing, spacing);
        c.anchor = GridBagConstraints.EAST;
        add(nameLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(border, spacing, spacing, border);
        add(nameEntry, c);
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(spacing, border, spacing, spacing);
        add(addressLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(spacing, spacing, spacing, border);
        add(addressEntry, c);
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(spacing, border, spacing, spacing);
        add(portLabel, c);
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(spacing, spacing, spacing, border);
        add(portEntry, c);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(spacing, border, spacing, border);
        add(connectButton, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.insets = new Insets(spacing, border, border, spacing);
        c.anchor = GridBagConstraints.WEST;
        add(instructionLabel, c);
        c.gridx = 1;
        c.gridy = 4;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(spacing, spacing, border, border);
        add(botCheckBox, c);

        pack();
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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

    /**
     * @return The connection socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * @return The player's chosen name
     */
    public String getName() {
        return name;
    }

    /**
     * @return True if bot was selected
     */
    public boolean getBotMode() {
        return botCheckBox.isSelected();
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

    /**
     * Used to connect the socket
     */
    private class ConnectThread implements Runnable {
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
                            instructionLabel.setText("Unable to connect to server");
                        }
                    } else {
                        if (!isNameValid(name)) {
                            instructionLabel.setText("Invalid name");
                        } else if (!isAddressValid(address)) {
                            instructionLabel.setText("Invalid address");
                        } else if (!isPortValid(port)) {
                            instructionLabel.setText("Invalid port");
                        }
                    }
                    connectButtonPressed = false;
                }
            }
        }
    }

    /**
     * Listens for enter keypress
     */
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
