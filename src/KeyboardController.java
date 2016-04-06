import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Keyboard controller for the game.
 * Arrow keys move, spacebar picks up, escape exits
 * @since 23/03/2016
 * @author mb2070
 */
public class KeyboardController extends Controller {
    private Stopwatch stopwatch;

    public KeyboardController(final long timeout) {
        stopwatch = new Stopwatch();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (stopwatch.getElapsedTimeMillis() > timeout) {
                    stopwatch.restart();
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        pickupPressed = true;
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        quitPressed = true;
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        movePressed = true;
                        moveDirection = 'N';
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        movePressed = true;
                        moveDirection = 'S';
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        movePressed = true;
                        moveDirection = 'W';
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        movePressed = true;
                        moveDirection = 'E';
                    }
                }
                return false;
            }
        });
    }


}
