import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 21/03/2016.
 */
public class MapPanel extends JPanel {
	BotMap map;

	public MapPanel() {
		super();
		//this.map = map;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		//int[] bounds = map.getBounds();
		g.drawRect(0, 0, 300, 300);
	}
}
