/**
 * Created by matt on 23/03/2016.
 */
public abstract class Controller {
    protected boolean quitPressed;
    protected boolean pickupPressed;
    protected boolean movePressed;
    protected char moveDirection;
    private boolean lookPressed;
    private boolean helloPressed;

    public boolean hasAction() {
        return quitPressed || pickupPressed || lookPressed || helloPressed || movePressed;
    }

    public boolean isQuitPressed() {
        if (quitPressed) {
            quitPressed = false;
            return true;
        }
        return false;
    }

    public boolean isPickupPressed() {
        if (pickupPressed) {
            pickupPressed = false;
            return true;
        }
        return false;
    }

    public boolean isLookPressed() {
        if (lookPressed) {
            lookPressed = false;
            return true;
        }
        return false;
    }

    public boolean isHelloPressed() {
        if (helloPressed) {
            helloPressed = false;
            return true;
        }
        return false;
    }

    public boolean isMovePressed() {
        if (movePressed) {
            movePressed = false;
            return true;
        }
        return false;
    }

    public char getMoveDirection() {
        return moveDirection;
    }

    public void movePressed(char dir) {
        moveDirection = dir;
        movePressed = true;
    }

    public void lookPressed() {
        lookPressed = true;
    }

    public void helloPressed() {
        helloPressed = true;
    }

    public void pickupPressed() {
        pickupPressed = true;
    }

    public void quitPressed() {
        quitPressed = true;
    }
}
