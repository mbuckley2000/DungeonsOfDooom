import javax.swing.*;
import java.awt.*;

/**
 * GUI interface for the game
 *
 * @author mb2070
 * @since 21/02/2016
 */
public class GUIInterface extends JFrame implements PlayerInterface {
    private ClientMap map;
    private PlayerPositionTracker positionTracker;
    private String lastCommand;
    private ChatPanel chatPanel;
    private MapPanel mapPanel;
    private Controller controller;
    private boolean finished;
    private boolean lookNeeded;
    private JLabel progressLabel;
    private boolean helloNeeded;
    private boolean lookResponseReceived;
    private boolean helloResponseReceived;
    private boolean pickupResponseReceived;
    private boolean moveResponseReceived;


    public GUIInterface(String title) {
        super(title);
        map = new ClientMap();
        positionTracker = new PlayerPositionTracker();
        controller = new KeyboardController(150);
        lastCommand = "";
        helloNeeded = true;
        lookResponseReceived = true;
        helloResponseReceived = true;
        pickupResponseReceived = true;
        moveResponseReceived = true;

        //Setup GUIInterface
        BorderLayout gameWindowLayout = new BorderLayout();
        getContentPane().setLayout(gameWindowLayout);

        //Setup instructions label
        JLabel instructionsLabel = new JLabel("Use arrow keys to move. Spacebar to pickup gold        ");
        instructionsLabel.setFont(new Font("Helvetica", NORMAL, 16));
        progressLabel = new JLabel("Gold left to win: ");
        progressLabel.setFont(new Font("Helvetica", NORMAL, 16));
        JPanel labels = new JPanel(new FlowLayout());
        labels.add(instructionsLabel);
        labels.add(progressLabel);
        add(labels, BorderLayout.PAGE_START);


        //Setup controls panel
        ControlPanel controlPanel = new ControlPanel(controller);
        add(controlPanel, BorderLayout.LINE_END);

        //Setup Chat Footer
        chatPanel = new ChatPanel();
        getContentPane().add(chatPanel, BorderLayout.PAGE_END);

        //Setup ServerMap View
        mapPanel = new MapPanel(map, positionTracker, 4);
        getContentPane().add(mapPanel, BorderLayout.CENTER);

        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * @return True if all GUI operations are done
     */
    @Override
    public boolean isFinished() {
        return finished;
    }


    /*
    ///Messages from the server are given to the interface here
     */
    @Override
    public void giveWin() {
        dispose();
        MessageDialog dialog = new MessageDialog("You have won!");
        while (!dialog.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        finished = true;
    }

    @Override
    public void giveLose() {
        dispose();
        MessageDialog dialog = new MessageDialog("Another player has won!");
        while (!dialog.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        finished = true;
    }

    @Override
    public void giveLookResponse(char[][] response) {
        map.update(response, positionTracker.getPosition());
        lookResponseReceived = true;
    }

    @Override
    public void giveHelloResponse(int response) {
        progressLabel.setText("Gold left to win: " + response);
        helloResponseReceived = true;
    }

    @Override
    public void givePickupResponse(boolean response) {
        pickupResponseReceived = true;
    }

    @Override
    public void giveMoveResponse(boolean response) {
        if (response) {
            positionTracker.step();
        }
        lookNeeded = true;
        moveResponseReceived = true;
    }

    @Override
    public void giveMessage(String message) {
        chatPanel.println(message);
    }


    /*
    ///Messages to the server are picked up from here
     */
    @Override
    public boolean hasNextCommand() {
        return controller.hasAction() || lookNeeded || helloNeeded || chatPanel.hasMessage();
    }

    public String getNextCommand() {
        if (lookNeeded && lookResponseReceived) {
            lookNeeded = false;
            lastCommand = "LOOK";
            lookResponseReceived = false;
            return lastCommand;
        }
        if (helloNeeded && helloResponseReceived) {
            lastCommand = "HELLO";
            helloNeeded = false;
            helloResponseReceived = false;
            return lastCommand;
        }
        if (controller.isMovePressed() && moveResponseReceived) {
            char dir = controller.getMoveDirection();
            positionTracker.setDirection(dir);
            lastCommand = "MOVE " + dir;
            moveResponseReceived = false;
            return lastCommand;
        }
        if (controller.isQuitPressed()) {
            lastCommand = "QUIT";
            finished = true;
            return lastCommand;
        }
        if (controller.isPickupPressed() && pickupResponseReceived) {
            lastCommand = "PICKUP";
            helloNeeded = true;
            pickupResponseReceived = false;
            return lastCommand;
        }
        if (controller.isLookPressed() && lookResponseReceived) {
            lastCommand = "LOOK";
            lookResponseReceived = false;
            return lastCommand;
        }
        if (controller.isHelloPressed() && helloResponseReceived) {
            lastCommand = "HELLO";
            helloResponseReceived = false;
            return lastCommand;
        }
        if (chatPanel.hasMessage()) {
            lastCommand = "SAY " + chatPanel.getMessage();
            return lastCommand;
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return getNextCommand();
    }
}