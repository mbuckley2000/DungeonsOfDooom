/**
 * Created by Matt Buckley on 26/04/16.
 */
public class CGameLogic implements IGameLogic {
    static {
        System.load(System.getProperty("user.dir") + "jni/libCDoD.so");
    }

    @Override
    public native String hello();

    @Override
    public native synchronized String move(char direction);

    @Override
    public native String pickup();

    @Override
    public native String look();

    @Override
    public native int[] getPlayerPosition();

    @Override
    public native void setPlayerPosition(int[] playerPosition);

    @Override
    public native String getLastLookWindow();

    @Override
    public native int getGoldNeeded();

    @Override
    public native boolean checkWin();
}
