import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by matt on 22/03/2016.
 */
public class NamePanel extends JPanel {
    private JTextField nameTextField;
    private String name;

    public NamePanel() {
        super();
        setLayout(new FlowLayout());

        nameTextField = new JTextField("Enter your name");
        //nameTextField.setPreferredSize(new Dimension(200, 25));

        JButton setNameButton = new JButton("Set Name");

        add(nameTextField);
        add(setNameButton);

        //Listener
        setNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String string = nameTextField.getText();
                if (isNameValid(string)) {
                    name = string;
                }
            }
        });
    }

    private boolean isNameValid(String name) {
        return (name.length() > 3 && name.length() < 10);
    }

    public String getName() {
        return name;
    }
}
