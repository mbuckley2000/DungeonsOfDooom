import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by matt on 04/04/16.
 */
public class MessageDialog extends JFrame {
    private boolean done;

    public MessageDialog(String message) {
        super(message);
        setLayout(new FlowLayout());
        add(new JLabel(message));
        JButton button = new JButton("OK");
        add(button);

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
