/**
 * Created by matt on 03/03/2016.
 */
public class GetGoldTask implements AITask {
	private boolean running;
	private AIMap map;
	private int[] goldPos;
	private Bot bot;
	private TraverseTask traverseTask;

	public GetGoldTask(Bot bot, AIMap map, int[] goldPos) {
		this.bot = bot;
		this.map = map;
		this.goldPos = goldPos;
		this.running = true;
		traverseTask = new TraverseTask(bot, map, goldPos);
		System.out.println("Moving to gold!");
	}

	public String getNextCommand() {
		System.out.println("GoldTask command get");

		if (map.getTile(goldPos[0], goldPos[1]) != 'G') {
			running = false;
			return "HELLO";
		}

		if (traverseTask.hasNextCommand()) {
			return traverseTask.getNextCommand();
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
