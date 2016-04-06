import javax.swing.*;
import java.awt.*;

/**
 * Diplays player progress towards winning
 * Deprecated.
 *
 * @author mb2070
 * @since 28/03/2016
 * @deprecated 01/04/2016
 */
public class ProgressPanel extends JPanel {
    private int goldToWin;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    public ProgressPanel(int goldToWin) {
        super();
        this.goldToWin = goldToWin;

        setLayout(new FlowLayout());
        progressBar = new JProgressBar(0, goldToWin);
        progressLabel = new JLabel();

        add(progressLabel);
        add(progressBar);
    }

    void update(int goldCollected) {
        progressLabel.setText(goldCollected + "/" + goldToWin + " gold collected");
        progressBar.setValue(goldCollected);
    }

    void setGoldToWin(int goldToWin) {
        this.goldToWin = goldToWin;
        progressBar.setMaximum(goldToWin);
    }
}
