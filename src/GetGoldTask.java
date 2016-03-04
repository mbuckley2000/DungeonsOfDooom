/**
 * Created by matt on 03/03/2016.
 */
public class GetGoldTask implements AITask {
	private boolean running;
	private AIMap map;
	private Bot bot;
	private int[] goldPos;
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
		if (traverseTask.hasNextCommand()) {
			return traverseTask.getNextCommand();
		} else {
			running = false;
			map.setTile(goldPos[0], goldPos[1], '.');
			return "PICKUP";
		}
	}

	public boolean hasNextCommand() {
		return running;
	}
}
