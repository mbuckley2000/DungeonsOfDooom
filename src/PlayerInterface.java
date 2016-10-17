/**
 * Interface for the player. Could be GUI, textual, or bot
 *
 * @author Matt Buckley
 * @since 22/03/2016
 */
public interface PlayerInterface {

    //These are all used to give the interface responses from the server
    //Output
    void giveLookResponse(char[][] response);

    void giveHelloResponse(int response);

    void givePickupResponse(boolean response);

    void giveMoveResponse(boolean response);

    void giveMessage(String message);

    void giveWin();

    void giveLose();

    //These get the next command from the interface
    //Input
    boolean hasNextCommand();

    String getNextCommand();

    boolean isFinished();
}
