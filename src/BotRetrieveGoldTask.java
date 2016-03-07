/**
 * Bot task to retrieve a found piece of gold.
 * Spawns a TraverseTask to get there, then attempts to pick the gold up
 * Terminates if the gold is picked up by the bot or somebody else, or if the gold is unreachable
 *
 * @since 03/03/2016
 * @author mb2070
 */
public class BotRetrieveGoldTask implements BotTask {
	private boolean running;
	private BotMap map;
	private int[] goldPos;
	private Bot bot;
	private BotTraverseTask botTraverseTask;

	public BotRetrieveGoldTask(Bot bot, BotMap map, int[] goldPos) {
		this.bot = bot;
		this.map = map;
		this.goldPos = goldPos;
		this.running = true;
		botTraverseTask = new BotTraverseTask(bot, map, goldPos);
		System.out.println("Moving to gold!");
	}

	public String getNextCommand() {
		//System.out.println("GoldTask command get");

		if (map.getTile(goldPos[0], goldPos[1]) != 'G') {
			running = false;
			return "HELLO";
		}

		if (botTraverseTask.hasNextCommand()) {
			return botTraverseTask.getNextCommand();
		} else {
			if (bot.getPosition()[0] == goldPos[0] && bot.getPosition()[1] == goldPos[1]) {
				map.setTile(goldPos[0], goldPos[1], '.');
				return "PICKUP";
			} else {
				return null;
			}
		}
	}

	public boolean hasNextCommand() {
		return running;
	}
}
