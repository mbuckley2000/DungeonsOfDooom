import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by matt on 22/03/2016.
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
		receivePanel.setPreferredSize(new Dimension(getWidth(), 150));
		receivePanel.setFocusable(false);

		//Send panel
		JPanel sendPanel = new JPanel(new FlowLayout());

		JLabel sendLabel = new JLabel("Enter your message: ");
		messageEntry = new JTextField();
		messageEntry.setPreferredSize(new Dimension(300, 25));

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

	public boolean hasMessage() {
		return sendButtonPressed && !messageEntry.getText().isEmpty();
	}

	public String getMessage() {
		sendButtonPressed = false;
		String message = messageEntry.getText();
		messageEntry.setText("");
		return message;
	}

	public void println(String string) {
		messageListModel.addElement(string);
		receivePanel.revalidate();
		JScrollBar sb = receivePanel.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}
}
