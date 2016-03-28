import javax.swing.*;

/**
 * Created by matt on 28/03/16.
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