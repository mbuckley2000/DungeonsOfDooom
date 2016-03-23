import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 22/03/2016.
 */
public class ChatPanel extends JPanel {
	private JTextField messageEntry;
	private JButton sendButton;
	private JList<String> messageList;
	private DefaultListModel<String> listModel;

	public ChatPanel() {
		//Receive panel inside a scroll pane
		JScrollPane receivePanel = new JScrollPane();
		listModel = new DefaultListModel<>();
		messageList = new JList<>(listModel);
		receivePanel.add(messageList);
		receivePanel.setPreferredSize(new Dimension(getWidth(), 150));

		//Send panel
		JPanel sendPanel = new JPanel(new FlowLayout());

		messageEntry = new JTextField("Enter your message");
		messageEntry.setPreferredSize(new Dimension(300, 25));

		sendButton = new JButton("Send");

		sendPanel.add(messageEntry);
		sendPanel.add(sendButton);

		//And it all comes together!
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(receivePanel);
		add(sendPanel);
	}

	public void println(String string) {
		listModel.addElement(string);
		repaint();
	}
}
