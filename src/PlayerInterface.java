/**
 * Created by matt on 22/03/2016.
 */
public interface PlayerInterface {
	//Output
	void giveLookResponse(char[][] response);

	void giveHelloResponse(int response);

	void giveSuccessResponse(boolean response);

	//Input
	boolean hasNextCommand();

	String getNextCommand();
}
