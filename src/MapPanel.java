import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by matt on 21/03/2016.
 */
public class MapPanel extends JPanel {
	private BotMap map;
	private Image tileSet;
	private PlayerPositionTracker positionTracker;

	public MapPanel(BotMap map, PlayerPositionTracker positionTracker) {
		super();
		this.map = map;
		this.positionTracker = positionTracker;
		try {
			tileSet = ImageIO.read(new File("img/spritesheet.png"));
		} catch (IOException e) {
			System.err.println("Couldn't find tileset image");
			System.exit(0);
		}

		setBackground(Color.BLACK);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int tileSize = 32; //px

		char[][] knownMap = map.getAsArray();
		//if (knownMap.length !=0) {
		for (int y = 0; y < knownMap.length; y++) {
			for (int x = 0; x < knownMap[0].length; x++) {
				if (y == positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0] && x == positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]) {
					g.drawImage(tileSet, x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, 6 * 32, 9 * 32, 7 * 32, 10 * 32, null);
				} else if (knownMap[y][x] == '.') {
					g.drawImage(tileSet, x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, 0, 0, 32, 32, null);
				} else if (knownMap[y][x] == 'G') {
					g.drawImage(tileSet, x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, 32, 9 * 32, 2 * 32, 10 * 32, null);
				} else if (knownMap[y][x] == '#') {
					g.drawImage(tileSet, x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, 5 * 32, 0, 6 * 32, 32, null);
				} else if (knownMap[y][x] == 'E') {
					g.drawImage(tileSet, x * tileSize, y * tileSize, (x + 1) * tileSize, (y + 1) * tileSize, 0, 3 * 32, 32, 4 * 32, null);
				}
			}
		}

		//setMaximumSize(new Dimension(knownMap[0].length * tileSize, knownMap.length * tileSize));
		setVisible(true);
		validate();
		repaint();
		//}
	}
}