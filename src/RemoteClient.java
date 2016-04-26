import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Represents a client connected to the server
 * Each client gets their own thread, so operations can happen simultaneously
 * Only MOVE is synchronised. This ensures that only one player can be on a single tile, removing the need to synchronise other methods such as PICKUP
 *
 * @author mb2070
 * @since 07/03/2016
 */
public class RemoteClient implements Runnable {
    private Socket clientSocket;
    private boolean connected;
    private InetAddress address;
    private PrintWriter writer;
    private BufferedReader reader;
    private Server server;
    private String name;
    private IGameLogic gameLogic;

    /**
     * Constructs the RemoteClient given the Socket and Server it is connected to
     *
     * @param server       The Server the RemoteClient belongs to
     * @param clientSocket The Socket that the client is connected to
     */
    RemoteClient(Server server, Socket clientSocket) {
        this.server = server;
        gameLogic = new JavaGameLogic(server);
        this.clientSocket = clientSocket;
        connected = true;
        int[] freePos = server.getServerMap().getFreeTile(server);
        if (freePos == null) {
            System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
            closeConnection();
        } else {
            gameLogic.setPlayerPosition(freePos);
        }
        address = clientSocket.getInetAddress();
    }

    /**
     * Thread starts here.
     * Initialises the reader and writer
     * Sends welcome message
     * Loops until the game is over or the client is disconnected, receiving input from client and processing it. Calls relevant methods for each valid command
     * Closes everything down cleanly when the loop is done
     */
    public void run() {
        new Thread(new ViewUpdaterThread()).start();

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            String input;

            System.out.println(address + "\t\tConnected");
            sendLine("Welcome to the dungeon! Mwuahaha");

            while (connected && server.isGameRunning()) {
                //Check for win
                if (gameLogic.checkWin()) {
                    server.broadcastMessage("LOSE", this);
                    System.out.println(address + "\t\t\t\t\tWIN");
                    writer.println("WIN");
                    server.shutDown();
                }

                input = reader.readLine();
                if (input != null) {
                    String response = parseInput(input);
                    if (response != null) {
                        writer.println(response);
                        if (!input.equals("LOOK")) {
                            System.out.println(address + "\t\t" + input + "\t\t" + response);
                        } else {
                            System.out.println(address + "\t\t" + input + "\t\tSent look window");
                        }
                    }
                } else {
                    closeConnection();
                }
            }
        } catch (IOException e) {
            closeConnection();
        }
    }

    /**
     * Parses the input from the Client
     * Checks for valid commands and then calls the relevant method to get the response
     *
     * @param input Input from the client
     * @return The message to be sent back to the client
     */
    private String parseInput(String input) {
        String[] command = input.trim().split(" ");
        String answer = null;
        switch (command[0].toUpperCase()) {
            case "HELLO":
                answer = gameLogic.hello();
                break;
            case "MOVE":
                if (command.length == 2) {
                    answer = gameLogic.move(command[1].toUpperCase().charAt(0));
                }
                break;
            case "PICKUP":
                answer = gameLogic.pickup();
                break;
            case "LOOK":
                answer = gameLogic.look();//.replaceAll(".(?!$)", "$0  ");  <-- This adds spacing to the window
                break;
            case "NAME":
                name = command[1];
                return null;
            case "SAY":
                server.broadcastMessage("C" + name + ": " + input.substring(4));
                return null;
            case "QUIT":
                closeConnection();
                return "Thanks for playing!";
        }
        return answer;
    }

    public void sendLine(String message) {
        System.out.println(address + "\t\t\t\t\t" + message);
        writer.println(message);
    }


    //Connection handling

    /**
     *
     */
    public void closeConnection() {
        try {
            connected = false;
            clientSocket.close();
            System.out.println(address + "\t\t\t\t\tDisconnected");
            writer.close();
            reader.close();
        } catch (IOException e) {
            System.err.println(address + "\t\t\t\t\tError closing connection");
        }
    }

    /**
     * Allows a RemoteClient object to reconnect on a different clientSocket, with the same state (mapPos, gold etc)
     * If a player is in their position, they are re-initialised
     *
     * @param clientSocket The socket to reconnect on
     */
    public void reconnect(Socket clientSocket) {
        this.clientSocket = clientSocket;
        if (server.playerOnTile(gameLogic.getPlayerPosition()[0], gameLogic.getPlayerPosition()[1])) {
            //There is a player on out tile! Re-initialise player position
            int[] freePos = server.getServerMap().getFreeTile(server);
            if (freePos == null) {
                System.err.println(clientSocket.getInetAddress() + "\t\tUnable to find empty tile for player. Closing connection");
                closeConnection();
            } else {
                gameLogic.setPlayerPosition(freePos);
            }
        }
        if (gameLogic.getPlayerPosition() != null) {
            connected = true;
        }
    }

    public IGameLogic getGameLogic() {
        return gameLogic;
    }

    /**
     * @return True if the client is connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return The InetAddress of the client
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Detects changes in the look window around the player.
     * Sends an updated look window if changes are detected.
     * Runs on 500ms clock
     */
    private class ViewUpdaterThread implements Runnable {
        public void run() {
            while (connected) {
                try {
                    Thread.sleep(50); //run on 50ms ticks, no need to spam. This is only for if a player moves in their getLookWindow
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String oldLookWindow = gameLogic.getLastLookWindow();
                String newLookWindow = gameLogic.look();
                if (oldLookWindow != null) {
                    if (!oldLookWindow.equals(newLookWindow)) {
                        System.out.println("Sending lookwindow");
                        writer.println(newLookWindow);
                    }
                }
            }
        }
    }
}