import java.util.ArrayList;
import java.util.Collections;

/**
 * A BotTask for exploring the map
 * Will head toward the nearest reachable undiscovered area using a TraverseTask
 * If reachable gold is found and needed, it will spawn a RetrieveGoldTask to pick it up
 * If reachable exit is found and needed, it will spawn a TraverseTask to get there
 *
 * @author mb2070
 * @since 03/03/2016
 */
public class BotExploreTask implements BotTask {
	private BotMap map;
	private Bot bot;
	private BotTraverseTask botTraverseTask;

	/**
	 * Constructor
	 *
	 * @param map BotMap the bot is playing on
	 * @param bot Bot that owns the task
	 */
	public BotExploreTask(BotMap map, Bot bot) {
		this.bot = bot;
		this.map = map;
	}

	/**
	 * Returns a movement command from the TraverseTask.
	 * Gets a new destination when required
	 * Spawns gold get tasks and traverse tasks to exits when needed.
	 *
	 * @return The next movement command from the TraverseTask
	 */
	public String getNextCommand() {
		System.out.println("BotExploreTask command get");

		if (bot.getGoldNeeded() == 0 && map.findTile('E') != null) {
			//Moving to exit
			int[] exitPos = bot.getClosestReachableTile('E');
			if (exitPos != null) {
				System.out.println("We found an exit! Exit pos: " + exitPos[1] + ", " + exitPos[0]);
				BotTraverseTask tt = new BotTraverseTask(bot, map, exitPos);
				bot.clearTasks();
				bot.addTask(tt);
				return tt.getNextCommand();
			}
		}

		if (map.findTile('G') != null && bot.getGoldNeeded() != 0) {
			//We have some gold!!
			int[] goldPos = bot.getClosestReachableTile('G');
			if (goldPos != null) {
				System.out.println("We found gold! Gold pos: " + goldPos[1] + ", " + goldPos[0]);
				BotRetrieveGoldTask gt = new BotRetrieveGoldTask(bot, map, goldPos);
				bot.addTask(gt);
				return gt.getNextCommand();
			}
		}

		if (map.isEmpty()) {
			return "LOOK";
		}

		if (botTraverseTask == null) {
			getNewDestination();
		}

		if (!botTraverseTask.hasNextCommand()) {
			getNewDestination();
		}

		return botTraverseTask.getNextCommand();
	}

	/**
	 * Picks the nearest discovered, reachable tile that is adjacent to an undiscovered one, and updates the TraverseTask accordingly
	 */
	private void getNewDestination() {
		int[] bestTile = null;

		ArrayList<int[]> potentialDestinations = map.findAllTiles(' ');
		Collections.sort(potentialDestinations, bot.distanceFromBot);


		for (int[] tile : potentialDestinations) {
			if (bestTile != null) {
				break;
			}
			if (tile[0] != bot.getPosition()[0] || tile[1] != bot.getPosition()[1]) {
				for (BotMapTile adjTile : map.findAdjacentWalkableTiles(new BotMapTile(tile))) {
					if (map.tileReachable(bot.getPosition(), adjTile.getPositionAsIntArray())) {
						bestTile = adjTile.getPositionAsIntArray();
						break;
					}
				}
			}
		}

		System.out.println("Undiscovered tiles: " + (potentialDestinations.size() - 1)); //-1 for player position
		if (bestTile != null) {
			System.out.println("Dest: " + bestTile[1] + ", " + bestTile[0]);
			System.out.println("Bot: " + bot.getPosition()[1] + ", " + bot.getPosition()[0]);
			botTraverseTask = new BotTraverseTask(bot, map, bestTile);
		} else {
			//No good tile was found.
			System.err.println("FATAL: Couldn't find new dest in explore task");
			System.exit(0);
		}
	}

	/**
	 * Checks if a new command is available
	 *
	 * @return True if a new command is available
	 */
	public boolean hasNextCommand() {
		int[] mapSize = new int[2];
		mapSize[0] = map.getBounds()[3] - map.getBounds()[1];
		mapSize[1] = map.getBounds()[2] - map.getBounds()[0];
		if (mapSize[0] > 18 && mapSize[1] > 8) {
			return true; //Should be false
		} else {
			return true;
		}
	}
}