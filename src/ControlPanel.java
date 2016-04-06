import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by matt on 21/03/2016.
 */
public class ControlPanel extends JPanel {
    private Controller controller;

    public ControlPanel(final Controller controller) {
        super();
        this.controller = controller;

        //Setup Buttons
        JButton pickupButton = new JButton("Pickup");
        JButton quitButton = new JButton("Quit");

        //Setup DPad
        JPanel dPadPanel = new JPanel(new GridBagLayout());
        GridBagConstraints cd = new GridBagConstraints();
        JButton moveNorth = new JButton(new ImageIcon(resizeImage("img/up.png", 32, 32)));
        JButton moveSouth = new JButton(new ImageIcon(resizeImage("img/down.png", 32, 32)));
        JButton moveEast = new JButton(new ImageIcon(resizeImage("img/right.png", 32, 32)));
        JButton moveWest = new JButton(new ImageIcon(resizeImage("img/left.png", 32, 32)));
        cd.gridx = 1;
        cd.gridy = 0;
        dPadPanel.add(moveNorth, cd);
        cd.gridx = 1;
        cd.gridy = 2;
        dPadPanel.add(moveSouth, cd);
        cd.gridx = 2;
        cd.gridy = 1;
        dPadPanel.add(moveEast, cd);
        cd.gridx = 0;
        cd.gridy = 1;
        dPadPanel.add(moveWest, cd);
        //dPadPanel.setPreferredSize(new Dimension(150, 150));


        //Put it all together
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 25;
        c.insets = new Insets(10, 20, 10, 20);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(pickupButton, c);
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 25;
        add(quitButton, c);
        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 50;
        c.fill = GridBagConstraints.NONE;
        add(dPadPanel, c);

        setVisible(true);
        validate();
        repaint();


        //Button Listeners
        moveNorth.addActionListener(new MovementButtonListener('N'));
        moveSouth.addActionListener(new MovementButtonListener('S'));
        moveEast.addActionListener(new MovementButtonListener('E'));
        moveWest.addActionListener(new MovementButtonListener('W'));
        pickupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.pickupPressed();
            }
        });
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.quitPressed();
            }
        });
    }

    // From http://stackoverflow.com/questions/6714045/how-to-resize-jlabel-imageicon
    private Image resizeImage(String imgFile, int w, int h) {
        try {
            Image srcImg = ImageIO.read(new File(imgFile));
            BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(srcImg, 0, 0, w, h, null);
            g2.dispose();

            return resizedImg;
        } catch (IOException e) {
            return null;
        }
    }

    private class MovementButtonListener implements ActionListener {
        private char dir;

        public MovementButtonListener(char dir) {
            this.dir = dir;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.movePressed(dir);
        }
    }
}
