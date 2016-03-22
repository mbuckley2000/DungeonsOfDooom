/**
 * Created by matt on 22/03/2016.
 */
public class PlayerPositionTracker {
	private int[] position;

	public PlayerPositionTracker() {
		position = new int[2];
	}

	/**
	 * Updates the bot's position (internal displacement from where it spawned), given the latest successful movement direction
	 *
	 * @param dir Latest successful movement direction
	 */
	public void step(char dir) {
		switch (dir) {
			case 'N':
				position[0] -= 1; //North
				break;
			case 'E':
				position[1] += 1; //East
				break;
			case 'S':
				position[0] += 1; //South
				break;
			case 'W':
				position[1] -= 1; //West
				break;
		}
		System.out.println("Position: " + position[1] + ", " + position[0]);
	}

	public int[] getPosition() {
		return position;
	}
}
