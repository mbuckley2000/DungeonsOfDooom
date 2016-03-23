import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

/**
 * Created by matt on 21/03/2016.
 */
public class MapPanel extends JPanel {
	private BotMap map;
	private Image tileSet;
	private Image playerSpriteSheet;
	private PlayerPositionTracker positionTracker;

	public MapPanel(BotMap map, PlayerPositionTracker positionTracker) {
		super();
		this.map = map;
		this.positionTracker = positionTracker;
		try {
			tileSet = ImageIO.read(new File("img/spritesheet.png"));
			playerSpriteSheet = ImageIO.read(new File("img/player.png"));
		} catch (IOException e) {
			System.err.println("Couldn't load images");
			System.exit(0);
		}

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(650, 500));
	}

	@Override
	public void update(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		paintComponent(g);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int tileSize = 32; //px
		int screenOffsetX = 300 - (positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]) * tileSize;
		int screenOffsetY = 220 - (positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0]) * tileSize;
		int[] playerSpritePos = getPlayerSprite(positionTracker.getDirection());


		ImageFilter filter = new RGBImageFilter() {
			int transparentColor = Color.white.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == transparentColor) {
					return 0x00FFFFFF & rgb;
				} else {
					return rgb;
				}
			}
		};

		ImageProducer filteredImgProd = new FilteredImageSource(playerSpriteSheet.getSource(), filter);
		Image transparentImg = Toolkit.getDefaultToolkit().createImage(filteredImgProd);



		char[][] knownMap = map.getAsArray();
		for (int y = 0; y < knownMap.length; y++) {
			for (int x = 0; x < knownMap[0].length; x++) {
				int screenX = x * tileSize + screenOffsetX;
				int screenY = y * tileSize + screenOffsetY;
				if (screenX > 0 && screenX < 650 && screenY > 0 && screenY < 500) {
					if (knownMap[y][x] == '.') {
						g.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 0, 32, 32, null);
					} else if (knownMap[y][x] == 'G') {
						g.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 32, 9 * 32, 2 * 32, 10 * 32, null);
					} else if (knownMap[y][x] == '#') {
						g.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 5 * 32, 0, 6 * 32, 32, null);
					} else if (knownMap[y][x] == 'E') {
						g.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 3 * 32, 32, 4 * 32, null);
					}
					if (y == positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0] && x == positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]) {
						g.drawImage(transparentImg, screenX, screenY, screenX + tileSize, screenY + tileSize, playerSpritePos[0], playerSpritePos[1], playerSpritePos[0] + 32, playerSpritePos[1] + 32, null);
					}
				}
			}
		}

		setVisible(true);
		validate();
		repaint();
	}

	private int[] getPlayerSprite(char dir) {
		switch (dir) {
			case 'N':
				return new int[]{6 * 32, 32 * 3};
			case 'S':
				return new int[]{6 * 32, 0};
			case 'E':
				return new int[]{6 * 32, 32 * 2};
			case 'W':
				return new int[]{6 * 32, 32};
			default:
				return new int[]{6 * 32, 32 * 3};
		}
	}
}