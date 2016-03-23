import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by matt on 21/03/2016.
 */
public class DPadPanel extends JPanel {
	private boolean clicked;
	private char direction;

	public DPadPanel() {
		super();
		setLayout(new BorderLayout());
		JButton moveNorth = new JButton("N");
		JButton moveSouth = new JButton("S");
		JButton moveEast = new JButton("E");
		JButton moveWest = new JButton("W");
		add(moveNorth, BorderLayout.PAGE_START);
		add(moveSouth, BorderLayout.PAGE_END);
		add(moveEast, BorderLayout.LINE_END);
		add(moveWest, BorderLayout.LINE_START);

		//Button listeners
		moveNorth.addActionListener(new MovementButtonListener('N'));
		moveSouth.addActionListener(new MovementButtonListener('S'));
		moveEast.addActionListener(new MovementButtonListener('E'));
		moveWest.addActionListener(new MovementButtonListener('W'));

		setPreferredSize(new Dimension(150, 150));
	}

	public boolean isClicked() {
		return clicked;
	}

	public char getDirection() {
		return direction;
	}

	public void reset() {
		clicked = false;
	}

	private class MovementButtonListener implements ActionListener {
		private char dir;

		public MovementButtonListener(char dir) {
			this.dir = dir;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!clicked) {
				clicked = true;
				direction = dir;
			}
		}
	}
}
