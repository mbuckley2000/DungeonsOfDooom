/**
 * Created by Matt Buckley on 26/04/16.
 */
public class JavaGameLogic implements IGameLogic {
    private int lookSize;
    private String lastLookWindow;
    private int[] playerPosition;
    private int collectedGold;
    private Server server;

    public JavaGameLogic(Server server) {
        this.server = server;
        collectedGold = 0;
        lookSize = 5;
    }

    /**
     * Processes the HELLO command
     *
     * @return String to be sent back to the Client (How much gold left they need to win)
     */
    @Override
    public String hello() {
        return "H" + getGoldNeeded();
    }

    /**
     * Processes the MOVE command
     * If the desired tile to move to is walkable and no player is on it, the move is allowed, otherwise it is denied
     * Synchronised to prevent multiple players on the same tile
     *
     * @param direction The direction the client wants to move in
     * @return The response to be sent back to the client: SUCCESS or FAIL, depending on if the move was allowed.
     */
    @Override
    public synchronized String move(char direction) {
        int[] newPosition = playerPosition.clone();
        switch (direction) {
            case 'N':
                newPosition[0] -= 1;
                break;
            case 'E':
                newPosition[1] += 1;
                break;
            case 'S':
                newPosition[0] += 1;
                break;
            case 'W':
                newPosition[1] -= 1;
                break;
            default:
                return ("MF");
        }

        if (server.getServerMap().getTile(newPosition[0], newPosition[1]) != '#' && !server.playerOnTile(newPosition[0], newPosition[1])) {
            playerPosition = newPosition;
            return "MS";
        } else {
            return "MF";
        }
    }

    /**
     * Processes the PICKUP command
     * If gold is on the player's tile, the player's gold count goes up and the gold is replaced with an empty tile.
     * No need for this to be synchronised, because movement is synchronised; two players cannot be on the same tile to pick up the same gold.
     *
     * @return The response to be sent back to the player
     */
    @Override
    public String pickup() {
        if (server.getServerMap().getTile(playerPosition[0], playerPosition[1]) == 'G') {
            collectedGold++;
            server.getServerMap().replaceTile(playerPosition[0], playerPosition[1], '.');
            return "PS" + collectedGold;
        }

        return "PF";
    }

    /**
     * Processes the LOOK command
     *
     * @return The response to be sent back to the Client: A look window into the map, based on their current position
     */
    @Override
    public String look() {
        String output = "L" + lookSize;
        char[][] lookReply = server.getServerMap().getLookWindow(playerPosition[0], playerPosition[1], lookSize);

        for (int i = 0; i < lookSize; i++) {
            for (int j = 0; j < lookSize; j++) {
                if (server.playerOnTile(playerPosition[0] - (lookSize / 2) + i, playerPosition[1] - (lookSize / 2) + j)) {
                    output += 'P';
                } else {
                    output += lookReply[j][i];
                }
            }
            if (i != lookSize - 1) {
                output += "\nL" + lookSize;
            }
        }
        lastLookWindow = output;
        return output;
    }

    /**
     * checks if the player collected all GOLD and is on the exit tile
     *
     * @return True if all conditions are met, false otherwise
     */
    public boolean checkWin() {
        return collectedGold >= server.getServerMap().getWin() && server.getServerMap().getTile(playerPosition[0], playerPosition[1]) == 'E';
    }

    //Misc

    /**
     * @return The player position as an int array
     */
    public int[] getPlayerPosition() {
        return playerPosition;
    }

    public void setPlayerPosition(int[] playerPosition) {
        this.playerPosition = playerPosition;
    }

    public String getLastLookWindow() {
        return lastLookWindow;
    }

    /**
     * @return The amount of gold extra that the client needs to win
     */
    public int getGoldNeeded() {
        return (server.getServerMap().getWin() - collectedGold);
    }
}
