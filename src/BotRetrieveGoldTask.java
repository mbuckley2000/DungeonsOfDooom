/**
 * Bot task to retrieve a found piece of gold.
 * Spawns a TraverseTask to get there, then attempts to pick the gold up
 * Terminates if the gold is picked up by the bot or somebody else, or if the gold is unreachable
 *
 * @author mb2070
 * @since 03/03/2016
 */
public class BotRetrieveGoldTask implements BotTask {
	private boolean running;
	private ClientMap map;
	private int[] goldPos;
	private Bot bot;
	private BotTraverseTask botTraverseTask;

	/**
	 * Constructor
	 *
	 * @param bot     The Bot that owns the task
	 * @param map     The ClientMap that the bot is playing on
	 * @param goldPos Position of the gold to collect on the ClientMap
	 */
	public BotRetrieveGoldTask(Bot bot, ClientMap map, int[] goldPos) {
		this.bot = bot;
		this.map = map;
		this.goldPos = goldPos;
		this.running = true;
		botTraverseTask = new BotTraverseTask(bot, map, goldPos);
		System.out.println("Moving to gold!");
	}

	/**
	 * @return The next command for the bot
	 */
	public String getNextCommand() {
		System.out.println("GoldTask command get");

		if (map.getTile(goldPos[0], goldPos[1]) != 'G' && !arrived()) {
			running = false;
		}

		if (botTraverseTask.hasNextCommand()) {
			return botTraverseTask.getNextCommand();
		} else {
			if (arrived()) {
				map.setTile(bot.getPosition()[0], bot.getPosition()[1], '.');
				running = false;
				return "PICKUP";
			} else {
				return null;
			}
		}
	}

	private boolean arrived() {
		return bot.getPosition()[0] == goldPos[0] && bot.getPosition()[1] == goldPos[1];
	}

	/**
	 * @return True if the task is still running, false otherwise
	 */
	public boolean hasNextCommand() {
		return running;
	}
}
