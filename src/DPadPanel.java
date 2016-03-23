import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

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

/*
		JButton moveNorth = new JButton("N");
		JButton moveSouth = new JButton("S");
		JButton moveEast = new JButton("E");
		JButton moveWest = new JButton("W");
*/
		try {
			JLabel moveNorth = new JLabel(new ImageIcon(ImageIO.read(new File("img/up.png"))));
			JLabel moveSouth = new JLabel(new ImageIcon(ImageIO.read(new File("img/down.png"))));
			JLabel moveEast = new JLabel(new ImageIcon(ImageIO.read(new File("img/right.png"))));
			JLabel moveWest = new JLabel(new ImageIcon(ImageIO.read(new File("img/left.png"))));

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
			moveNorth.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					clicked = true;
					direction = 'N';
				}
			});
			moveSouth.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					clicked = true;
					direction = 'S';
				}
			});
			moveEast.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					clicked = true;
					direction = 'E';
				}
			});
			moveWest.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					super.mouseClicked(e);
					clicked = true;
					direction = 'W';
				}
			});
		} catch (IOException e) {
			System.err.println("Couldn't load images");
			System.exit(1);
		}

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
