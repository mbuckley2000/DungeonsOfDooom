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
 * A subclass of JPanel that draws a given ClientMap.
 * Requires a PlayerPositionTracker
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
    private int smoothness;

    public MapPanel(ClientMap map, PlayerPositionTracker positionTracker, int smoothness) {
        super();
        this.smoothness = smoothness;
        scale = 1.0;//DPI scaling
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        if (screenWidth > 2000) {
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
    }

    @Override
    public void update(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        paintComponent(g);
    }

    /**
     * Draws the map
     * Iterates through all onscreen tiles and displays their relevant image
     * Darkens tiles outside of the look window
     * Movement of the player is smoothed
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(scale, scale);

        super.paintComponent(g2);
        int width = (int) (getWidth() / scale);
        int height = (int) (getHeight() / scale);
        int tileSize = 32; //px unscaled
        int targetScreenOffsetX = (width - tileSize) / 2 - (positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]) * tileSize;
        int targetScreenOffsetY = (height - tileSize) / 2 - (positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0]) * tileSize;
        if (screenOffsetX < targetScreenOffsetX) screenOffsetX += smoothness;
        if (screenOffsetX > targetScreenOffsetX) screenOffsetX -= smoothness;
        if (screenOffsetY < targetScreenOffsetY) screenOffsetY += smoothness;
        if (screenOffsetY > targetScreenOffsetY) screenOffsetY -= smoothness;
        int[] playerSpritePos = getPlayerSprite(positionTracker.getDirection());

        char[][] knownMap = map.getAsArray();
        for (int y = 0; y < knownMap.length; y++) {
            for (int x = 0; x < knownMap[0].length; x++) {
                int screenX = x * tileSize + screenOffsetX;
                int screenY = y * tileSize + screenOffsetY;
                double distFromLocalPlayer = Math.sqrt(Math.pow(y - (positionTracker.getPosition()[0] + map.getOffset()[0] - map.getBounds()[0]), 2) + Math.pow(x - (positionTracker.getPosition()[1] + map.getOffset()[1] - map.getBounds()[1]), 2));
                if (screenX >= -tileSize && screenX < width && screenY >= -tileSize && screenY < height) {
                    Composite composite;
                    if (distFromLocalPlayer > (map.getLookSize() / 2)) { //If they are outside the lookwindow
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5);
                    } else {
                        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1);
                    }
                    g2.setComposite(composite);
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
                        if (distFromLocalPlayer < map.getLookSize() / 2) { //If they are within the lookwindow
                            g2.drawImage(playerSpriteSheet, screenX, screenY, screenX + tileSize, screenY + tileSize, 7 * 32, 4 * 32, 7 * 32 + tileSize, 4 * 32 + tileSize, null);
                        }
                    }
                }
            }
        }

        //Draw player
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 1));

        g2.drawImage(playerSpriteSheet, (width - tileSize) / 2, (height - tileSize) / 2, (width + tileSize) / 2, (height + tileSize) / 2, playerSpritePos[0], playerSpritePos[1], playerSpritePos[0] + tileSize, playerSpritePos[1] + tileSize, null);

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