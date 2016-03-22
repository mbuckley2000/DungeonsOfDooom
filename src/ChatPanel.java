import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 22/03/2016.
 */
public class ChatPanel extends JPanel {
	private JTextField messageEntry;
	private JButton sendButton;

	public ChatPanel() {

		//Receive panel
		ServerListenerPanel receivePanel = new ServerListenerPanel();

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

}
