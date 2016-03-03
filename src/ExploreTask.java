/**
 * Created by matt on 03/03/2016.
 */
public class ExploreTask implements AITask {
	final char[] directions = new char[]{'N', 'E', 'S', 'W'};
	int dir;
	private String lastCommand;
	private OutputClient outputClient;
	private AIMap map;
	private int[] moveDelta;
	private int[] position;

	public ExploreTask(AIMap map, OutputClient outputClient, int[] position) {
		this.outputClient = outputClient;
		this.map = map;
		this.position = position;
		moveDelta = new int[]{0, 0};
		lastCommand = "HELLO";
		dir = 0;
	}

	public String getNextCommand() {
		if (lastCommand.equals("LOOK")) {
			map.update(outputClient.getLastLookWindow(), moveDelta);
			position[0] += moveDelta[0];
			position[1] += moveDelta[1];
			moveDelta[0] = 0;
			moveDelta[1] = 0;
			System.out.println("Updated internal map: ");
			map.print();
		} else if (lastCommand.contains("MOVE")) {
			//Check that the move was successful
			if (outputClient.getLastBoolResponse()) {
				switch (dir) {
					case 0:
						moveDelta[0] -= 1; //North
						break;
					case 1:
						moveDelta[1] += 1; //East
						break;
					case 2:
						moveDelta[0] += 1; //South
						break;
					case 3:
						moveDelta[1] -= 1; //West
						break;
				}
			} else {
				dir = (dir + 1) % 4; //Turn right and continue moving forward
			}
		}
		String command;
		if (vectorLength(moveDelta) >= 2) {
			command = "LOOK";
		} else {
			command = "MOVE " + getMovement();
		}
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