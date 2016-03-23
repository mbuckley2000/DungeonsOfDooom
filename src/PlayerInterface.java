/**
 * Created by matt on 22/03/2016.
 */
public interface PlayerInterface {
	//Output
	void giveLookResponse(char[][] response);

	void giveHelloResponse(int response);

	void giveSuccessResponse(boolean response);

	void giveMessage(String message);

	//Input
	boolean hasNextCommand();

	String getNextCommand();
}
