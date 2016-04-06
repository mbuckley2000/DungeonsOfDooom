import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Component of the GUI that allows users to send and receive chat, as well as messages from the server
 *
 * @author mb2070
 * @since 25/03/2016
 */
public class ChatPanel extends JPanel {
    private JTextField messageEntry;
    private JButton sendButton;
    private boolean sendButtonPressed;
    private JScrollPane receivePanel;
    private JList<String> messageList;
    private DefaultListModel messageListModel;

    public ChatPanel() {
        //Receive panel inside a scroll pane
        receivePanel = new JScrollPane();
        messageListModel = new DefaultListModel();
        messageList = new JList(messageListModel);
        receivePanel.setViewportView(messageList);
        receivePanel.setFocusable(false);

        //Send panel
        JPanel sendPanel = new JPanel(new FlowLayout());

        JLabel sendLabel = new JLabel("Enter your message: ");
        messageEntry = new JTextField("", 40);

        sendButton = new JButton("Send");

        sendPanel.add(sendLabel);
        sendPanel.add(messageEntry);
        sendPanel.add(sendButton);

        //And it all comes together!
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(receivePanel);
        add(sendPanel);

        //Button listener
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButtonPressed = true;
            }
        });

        messageEntry.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButtonPressed = true;
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButtonPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * @return True if a message is ready to be sent
     */
    public boolean hasMessage() {
        return sendButtonPressed && !messageEntry.getText().isEmpty();
    }

    /**
     * @return The message to be sent
     */
    public String getMessage() {
        sendButtonPressed = false;
        String message = messageEntry.getText();
        messageEntry.setText("");
        return message;
    }

    /**
     * Prints a string to the output area
     *
     * @param string String to be printed
     */
    public void println(String string) {
        messageListModel.addElement(string);
        receivePanel.revalidate();
        JScrollBar sb = receivePanel.getVerticalScrollBar();
        sb.setValue(sb.getMaximum());
    }
}