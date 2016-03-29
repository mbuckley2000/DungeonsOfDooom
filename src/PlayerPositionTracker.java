/**
 * Created by matt on 22/03/2016.
 */
public class PlayerPositionTracker {
	private int[] position;
	private char direction;

	public PlayerPositionTracker() {
		position = new int[2];
		direction = 'N';
	}

	public char getDirection() {
		return direction;
	}

	public void setDirection(char direction) {
		this.direction = direction;
	}

	/**
	 * Updates the bot's position (internal displacement from where it spawned), given the latest successful movement direction
	 *
	 */
	public void step() {
		switch (direction) {
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
	}

	public int[] getPosition() {
		return position;
	}
}
