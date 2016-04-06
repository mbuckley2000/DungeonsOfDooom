import javax.swing.*;

/**
 * A simple window that displays the graphical map and the bot's progress
 * @author mb2070
 * @since 21/03/2016
 */
public class BotWindow extends JFrame {
    public BotWindow(Bot bot) {
        super("Bot");

        MapPanel mapView = new MapPanel(bot.getMap(), bot.getPositionTracker(), 8);
        getContentPane().add(mapView);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //setResizable(false);

        pack();
        setVisible(true);
    }
}