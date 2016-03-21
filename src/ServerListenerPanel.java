import javax.swing.*;

/**
 * Created by matt on 21/03/2016.
 */
public class ServerListenerPanel extends JScrollPane {
	JList<String> messages;

	public ServerListenerPanel() {
		super();
		messages = new JList<>(new String[]{"Hi", "Hey", "Strings", "WOO!"});
		add(messages);
	}

}