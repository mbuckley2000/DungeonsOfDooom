import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Displays a message to the player
 *
 * @author Matt Buckley
 * @since 04/04/2016
 */
public class MessageDialog extends JFrame {
    private boolean done;

    public MessageDialog(String message) {
        super(message);
        setLayout(new FlowLayout());
        JLabel label = new JLabel(message);
        label.setFont(new Font("Helvetica", NORMAL, 16));
        add(label);
        JButton button = new JButton("OK");
        add(button);

        setPreferredSize(new Dimension(300, 150));
        setLocation(800, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                done = true;
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        done = true;
    }

    public boolean isDone() {
        return done;
    }
}
