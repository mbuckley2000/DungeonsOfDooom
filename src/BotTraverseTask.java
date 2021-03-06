import java.util.Stack;

/**
 * Bot task for travelling to a destination tile on the ClientMap
 * Terminates if tile is unreachable, unwalkable, or if tile has been reached
 * Generates a new path after every move, to account for newly discovered map areas or obstacles
 *
 * @author Matt Buckley
 * @since 03/03/2016
 */
public class BotTraverseTask implements BotTask {
    private Bot bot;
    private ClientMap map;
    private int[] destination;
    private boolean running;

    /**
     * Constructor
     *
     * @param bot         The bot the task belongs to
     * @param map         The ClientMap the bot is playing in
     * @param destination The destination tile on the ClientMap
     */
    public BotTraverseTask(Bot bot, ClientMap map, int[] destination) {
        this.bot = bot;
        this.map = map;
        this.destination = destination;
        running = true;
    }

    /**
     * Gets the next direction to move in to get from one adjacent tile (t1) to another (t2)
     *
     * @param t1 Start tile
     * @param t2 End tile
     * @return The direction to move in
     */
    private char getMovement(BotMapTile t1, BotMapTile t2) {
        char dir = 0;
        if (t2.getX() - t1.getX() > 0) {
            dir = 'E';
        } else if (t2.getX() - t1.getX() < 0) {
            dir = 'W';
        } else if (t2.getY() - t1.getY() > 0) {
            dir = 'S';
        } else if (t2.getY() - t1.getY() < 0) {
            dir = 'N';
        }
        return dir;
    }

    /**
     * @return The next command for the bot
     */
    public String getNextCommand() {
        System.out.println("BotTraverseTask command get");

        if (!map.tileReachable(bot.getPosition(), destination)) {
            running = false;
            return null;
        } else {
            Stack<BotMapTile> path = map.getPath(bot.getPosition(), destination);
            return "MOVE " + getMovement(path.pop(), path.peek());
        }
    }

    /**
     * @return True if the task is still running, false otherwise
     */
    public boolean hasNextCommand() {
        if ((bot.getPosition()[0] == destination[0] && bot.getPosition()[1] == destination[1]) || !map.tileWalkable(destination[0], destination[1])) {
            running = false;
        }
        return running;
    }
}