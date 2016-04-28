import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Server Class
 * Receives incoming connections and spawns a RemoteClient thread for each one
 * Keeps a collection of all connections to be used for player collision checking and broadcast messages etc..
 * Clients are PERSISTENT. When a client disconnects, all data is kept and their connected flag set to false
 * When they reconnect, they use the same RemoteClient object, with their old data and continue where they left off
 * If a player is standing where they used to be, they are re-initialised
 * Provides utility to check for stalemates (not enough gold left for anybody to win)
 *
 * @author mb2070
 * @since 24/02/2016
 */
public class Server {
    private static boolean guiDisabled;
    private Set<RemoteClient> remoteClients;
    private boolean gameRunning;
    private IServerMap serverMap;
    private ServerSocket serverSocket;

    /**
     * Constructor
     * Starts the ServerSocket to listen from client connections
     * Initialises and loads the serverMap
     */
    public Server(int port, File mapFile) {
        remoteClients = new HashSet<>();
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Listening for remote client connections on port: " + serverSocket.getLocalPort());

            gameRunning = true;

            //Setup
            //Load serverMap
            serverMap = new CServerMap();

            if (!guiDisabled) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("DoD Map File", "txt");
                chooser.setFileFilter(filter);
                chooser.setCurrentDirectory(new File("maps"));
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    mapFile = chooser.getSelectedFile();
                } else {
                    System.out.println("Using default map");
                }
            }

            serverMap.loadMap(mapFile.getAbsolutePath());

            new Thread(new StalemateCheckingThread()).start();
        } catch (IOException e) {
            System.err.println("Error starting server");
            System.exit(0);
        }
    }

    /**
     * Initiates the server and starts the update loop
     */
    public static void main(String args[]) {
        int port = 40004;
        File mapFile = new File("maps/example_map.txt");

        for (String s : args) {
            try {
                int argInt = Integer.parseInt(s);
                if (Client.isPortValid(argInt)) {
                    port = argInt;
                }
            } catch (NumberFormatException e) {
                if (s.equals("nogui")) {
                    guiDisabled = true;
                }
                if (s.contains(".txt")) {
                    File f = new File(s);
                    if (f.exists() && !f.isDirectory()) {
                        mapFile = new File(s);
                    }
                }
            }
        }

        Server server = new Server(port, mapFile);
        server.update();
    }

    /**
     * The update loop: detects new connections, spawning a new thread for each one
     */
    private void update() {
        try {
            while (gameRunning) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket.getInetAddress() + "\t\tRequested to connect");

                RemoteClient remoteClient = findClient(clientSocket.getInetAddress());
                if (remoteClient == null) {
                    remoteClient = new RemoteClient(this, clientSocket);
                    remoteClients.add(remoteClient);
                    new Thread(remoteClient).start();
                } else {
                    if (!remoteClient.isConnected()) {
                        remoteClient.reconnect(clientSocket);
                        new Thread(findClient(clientSocket.getInetAddress())).start();
                    } else {
                        System.out.println(clientSocket.getInetAddress() + "\t\tConnection refused. Address already connected");
                        new PrintWriter(clientSocket.getOutputStream(), true).println("Connection refused. Address already connected");
                        clientSocket.close();
                    }
                }
            }
            broadcastMessage("Game is over. Server is shutting down");
            closeAllConnections();
            serverSocket.close();
        } catch (IOException e) {
            //Couldn't update server
            e.printStackTrace();
        }
    }

    /**
     * Checks if game is running
     *
     * @return True if game is running, false otherwise
     */
    public boolean isGameRunning() {
        return gameRunning;
    }

    /**
     * Returns the game serverMap
     *
     * @return The game serverMap
     */
    public IServerMap getServerMap() {
        return serverMap;
    }

    /**
     * Closes all connections on the server
     */
    private void closeAllConnections() {
        for (RemoteClient connection : remoteClients) {
            if (connection.isConnected()) {
                connection.closeConnection();
            }
        }
    }

    /**
     * Sends a message to everybody on the server
     *
     * @param message The message to send
     */
    public void broadcastMessage(String message, RemoteClient... excludedClients) {
        for (RemoteClient client : remoteClients) {
            if (client.isConnected() && !Arrays.asList(excludedClients).contains(client)) {
                client.sendLine(message);
            }
        }
    }

    /**
     * Finds the client object for a given InetAddress. Used to find a client's data if they disconnect and reconnect
     *
     * @param address InetAddress of the client
     * @return The found client. Null if none exist
     */
    private RemoteClient findClient(InetAddress address) {
        for (RemoteClient connection : remoteClients) {
            if (connection.getAddress().equals(address)) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Checks if a player is on the specified tile
     *
     * @param y Y ordinate of the tile
     * @param x X ordinate of the tile
     * @return True if a player is on the specified tile, false otherwise
     */
    public boolean playerOnTile(int y, int x) {
        boolean hit = false;
        for (RemoteClient connection : remoteClients) {
            if (connection != null) {
                if (connection.isConnected() && connection.getGameLogic().getPlayerPosition()[0] == y && connection.getGameLogic().getPlayerPosition()[1] == x) {
                    hit = true;
                }
            }
        }
        return hit;
    }

    /**
     * Shuts down all connections and ends the game
     */
    public void shutDown() {
        System.out.println("Game over. Shutting down");
        gameRunning = false;
        closeAllConnections();
        System.exit(0);
    }

    /**
     * Checks if a stalemate has occurred (i.e. not enough gold for any connected player to finish)
     * If it has occurred, the game ends
     */
    private boolean isItAStalemate() {
        boolean hit = false;
        boolean clientConnected = false;
        for (RemoteClient client : remoteClients) {
            if (client.isConnected()) {
                if ((client.getGameLogic().getGoldNeeded() <= serverMap.countRemainingGold()) || client.getGameLogic().getGoldNeeded() == 0) {
                    hit = true;
                }
                clientConnected = true;
            }
        }
        return !hit && clientConnected;
    }

    private void spawnGold(int amount) {
        for (int i = 0; i < amount; i++) {
            int[] freeTile = serverMap.getFreeTile(this);
            if (freeTile != null) {
                serverMap.replaceTile(freeTile[0], freeTile[1], 'G');
                System.out.println("Adding gold piece at " + freeTile[1] + ", " + freeTile[0]);
            }
        }
    }

    public void saveGame(String filename) {
        try {
            serverMap.saveMap(new File(filename).getAbsolutePath());
            //File gameFile = new File(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadGame(String filename) {
        try {
            serverMap.loadMap(new File(filename).getAbsolutePath());
            //File gameFile = new File(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class StalemateCheckingThread implements Runnable {
        @Override
        public void run() {
            while (gameRunning) {
                if (isItAStalemate()) {
                    spawnGold(1);
                }
            }
        }
    }
}