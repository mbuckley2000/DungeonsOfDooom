import javax.swing.*;
import java.awt.*;

/**
 * Created by matt on 28/03/16.
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
