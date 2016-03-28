import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	private final int targetFramerate = 60;
	private ClientMap map;
	private Image tileSet;
	private Image playerSpriteSheet;
	private PlayerPositionTracker positionTracker;
	private int screenOffsetX;
	private int screenOffsetY;
	private Stopwatch vSyncTimer;
	private double scale;


	public MapPanel(ClientMap map, PlayerPositionTracker positionTracker) {
		super();

		scale = 1.0;//DPI scaling
		double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		if (screenWidth > 1600) {
			scale = screenWidth / 1600;
		}
		this.map = map;
		this.positionTracker = positionTracker;
		vSyncTimer = new Stopwatch();
		try {
			tileSet = ImageIO.read(new File("img/spritesheet.png"));
			playerSpriteSheet = ImageIO.read(new File("img/player.png"));
		} catch (IOException e) {
			System.err.println("Couldn't load images");
			System.exit(0);
		}

		//Filter player image to be transparent
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
		playerSpriteSheet = Toolkit.getDefaultToolkit().createImage(filteredImgProd);

		setFocusable(true);
		requestFocusInWindow();

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension((int) (650 * scale), (int) (500 * scale)));

		//Focus Listener
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				requestFocusInWindow();
			}
		});
	}

	@Override
	public void update(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		paintComponent(g);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.scale(scale, scale);

		super.paintComponent(g2);
		int tileSize = 32; //px unscaled
		int targetScreenOffsetX = (650 - tileSize) / 2 - (positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]) * tileSize;
		int targetScreenOffsetY = (500 - tileSize) / 2 - (positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0]) * tileSize;
		if (screenOffsetX < targetScreenOffsetX) screenOffsetX += tileSize / 8;
		if (screenOffsetX > targetScreenOffsetX) screenOffsetX -= tileSize / 8;
		if (screenOffsetY < targetScreenOffsetY) screenOffsetY += tileSize / 8;
		if (screenOffsetY > targetScreenOffsetY) screenOffsetY -= tileSize / 8;
		int[] playerSpritePos = getPlayerSprite(positionTracker.getDirection());

		char[][] knownMap = map.getAsArray();
		for (int y = 0; y < knownMap.length; y++) {
			for (int x = 0; x < knownMap[0].length; x++) {
				int screenX = x * tileSize + screenOffsetX;
				int screenY = y * tileSize + screenOffsetY;
				if (screenX >= -tileSize && screenX < 650 && screenY >= -tileSize && screenY < 500) {
					if (knownMap[y][x] == '.') {
						g2.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 0, tileSize, tileSize, null);
					} else if (knownMap[y][x] == 'G') {
						g2.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, tileSize, 9 * tileSize, 2 * tileSize, 10 * tileSize, null);
					} else if (knownMap[y][x] == '#') {
						g2.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 5 * tileSize, 0, 6 * tileSize, tileSize, null);
					} else if (knownMap[y][x] == 'E') {
						g2.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 3 * tileSize, tileSize, 4 * tileSize, null);
					}
					//Drawing other players
					if (knownMap[y][x] == 'P') {
						g2.drawImage(tileSet, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 0, tileSize, tileSize, null);
						if (Math.sqrt(Math.pow(y - (positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0]), 2) + Math.pow(x - (positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]), 2)) < 3) { //If they are close
							g2.drawImage(playerSpriteSheet, screenX, screenY, screenX + tileSize, screenY + tileSize, 7 * 32, 4 * 32, 7 * 32 + tileSize, 4 * 32 + tileSize, null);
						}
					}
				}
			}
		}

		//Draw player
		g2.drawImage(playerSpriteSheet, (650 - tileSize) / 2, (500 - tileSize) / 2, (650 + tileSize) / 2, (500 + tileSize) / 2, playerSpritePos[0], playerSpritePos[1], playerSpritePos[0] + tileSize, playerSpritePos[1] + tileSize, null);

		setVisible(true);
		validate();
		repaint();

		//VSYNC @ 60fps
		long frameTime = vSyncTimer.getElapsedTimeMillis();
		int targetFrameTime = 1000 / targetFramerate;
		if (frameTime < targetFrameTime) {
			try {
				//System.out.println("Frametime: " + frameTime + ", sleeping for " + (targetFrameTime-frameTime) + "ms");
				Thread.sleep(targetFrameTime - frameTime);
			} catch (InterruptedException e) {
				System.exit(1);
				Thread.currentThread().interrupt();
			}
		}
		vSyncTimer.restart();
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