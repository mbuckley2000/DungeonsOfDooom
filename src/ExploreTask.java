import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by matt on 03/03/2016.
 */
public class ExploreTask implements AITask {
	private AIMap map;
	private Bot bot;
	private TraverseTask traverseTask;

	public ExploreTask(AIMap map, Bot bot) {
		this.bot = bot;
		this.map = map;
	}

	public String getNextCommand() {
		System.out.println("ExploreTask command get");

		if (bot.getGoldNeeded() == 0 && map.findTile('E') != null) {
			//Moving to exit
			int[] exitPos = bot.getClosestReachableTile('E');
			if (exitPos != null) {
				System.out.println("We found an exit! Exit pos: " + exitPos[1] + ", " + exitPos[0]);
				TraverseTask tt = new TraverseTask(bot, map, exitPos);
				bot.addTask(tt);
				return tt.getNextCommand();
			}
		}

		if (map.findTile('G') != null && bot.getGoldNeeded() != 0) {
			//We have some gold!!
			int[] goldPos = bot.getClosestReachableTile('G');
			if (goldPos != null) {
				System.out.println("We found gold! Gold pos: " + goldPos[1] + ", " + goldPos[0]);
				GetGoldTask gt = new GetGoldTask(bot, map, goldPos);
				bot.addTask(gt);
				return gt.getNextCommand();
			}
		}

		if (map.isEmpty()) {
			return "LOOK";
		}

		if (traverseTask == null) {
			getNewDestination();
		}

		if (!traverseTask.hasNextCommand()) {
			getNewDestination();
		}

		return traverseTask.getNextCommand();
	}

	private void getNewDestination() {
		int[] bestTile = null;

		ArrayList<int[]> potentialDestinations = map.findAllTiles(' ');
		Collections.sort(potentialDestinations, bot.distanceFromBot);


		for (int[] tile : potentialDestinations) {
			if (bestTile != null) {
				break;
			}
			if (tile[0] != bot.getPosition()[0] || tile[1] != bot.getPosition()[1]) {
				for (MapTile adjTile : map.findAdjacentWalkableTiles(new MapTile(tile))) {
					if (map.tileReachable(bot.getPosition(), adjTile.toIntArray())) {
						bestTile = adjTile.toIntArray();
						break;
					}
				}
			}
		}

		System.out.println("Undiscovered tiles: " + (potentialDestinations.size() - 1)); //-1 for player position
		if (bestTile != null) {
			System.out.println("Dest: " + bestTile[1] + ", " + bestTile[0]);
			System.out.println("Bot: " + bot.getPosition()[1] + ", " + bot.getPosition()[0]);
			traverseTask = new TraverseTask(bot, map, bestTile);
		} else {
			//No good tile was found.
			System.err.println("FATAL: Couldn't find new dest in explore task");
			System.exit(0);
		}
	}

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