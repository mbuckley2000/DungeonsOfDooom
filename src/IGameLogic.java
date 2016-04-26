/**
 * GameLogic interface defines the functions needed to play the game.
 */
public interface IGameLogic {
    String hello();

    String move(char direction);

    String pickup();

    String look();

    int[] getPlayerPosition();

    void setPlayerPosition(int[] playerPosition);

    String getLastLookWindow();

    int getGoldNeeded();

    boolean checkWin();
}