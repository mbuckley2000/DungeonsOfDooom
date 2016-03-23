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
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JButton moveNorth = new JButton("N");
		JButton moveSouth = new JButton("S");
		JButton moveEast = new JButton("E");
		JButton moveWest = new JButton("W");
		c.gridx = 1;
		c.gridy = 0;
		add(moveNorth, c);
		c.gridx = 1;
		c.gridy = 2;
		add(moveSouth, c);
		c.gridx = 2;
		c.gridy = 1;
		add(moveEast, c);
		c.gridx = 0;
		c.gridy = 1;
		add(moveWest, c);

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
