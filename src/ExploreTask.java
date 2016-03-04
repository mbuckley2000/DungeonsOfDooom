/**
 * Created by matt on 03/03/2016.
 */
public class ExploreTask implements AITask {
	final char[] directions = new char[]{'N', 'E', 'S', 'W'};
	int dir;
	private OutputClient outputClient;
	private AIMap map;
	private String lastCommand;
	private Bot bot;

	public ExploreTask(AIMap map, OutputClient outputClient, Bot bot) {
		this.bot = bot;
		this.outputClient = outputClient;
		this.map = map;
		lastCommand = "";
		dir = 0;
	}

	public String getNextCommand() {
		//System.out.println("ExploreTask command get");
		if (lastCommand.contains("MOVE") && !outputClient.getLastBoolResponse()) {
			dir = (dir + 1) % 4; //Turn right and continue moving forward
		}

		String command;

		if (bot.getGoldNeeded() == 0 && map.findTile('E') != null) {
			//Moving to exit
			int[] exitPos = map.findTile('E');
			System.out.println("We found an exit! Exit pos: " + exitPos[1] + ", " + exitPos[0]);
			bot.addTask(new TraverseTask(bot, map, exitPos));
			return "HELLO";
		}

		if (map.findTile('G') != null) {
			//We have some gold!!
			int[] goldPos = map.findTile('G');
			System.out.println("We found gold! Gold pos: " + goldPos[1] + ", " + goldPos[0]);
			bot.addTask(new GetGoldTask(bot, map, goldPos));
			return "HELLO";
		}

		command = "MOVE " + getMovement();
		lastCommand = command;
		return command;
	}

	private char getMovement() {
		return directions[dir];
	}

	private double vectorLength(int[] vector) {
		return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
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