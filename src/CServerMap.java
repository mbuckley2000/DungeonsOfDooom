import java.io.IOException;

/**
 * Created by Matt Buckley on 26/04/16.
 */
public class CServerMap implements IServerMap {
    static {
        System.load(System.getProperty("user.dir") + "/jni/libCDoD.so");
    }

    @Override
    public native void loadMap(String filename) throws IOException;

    @Override
    public native void saveMap(String filename) throws IOException;

    @Override
    public native int[] getFreeTile(Server server);

    @Override
    public native char replaceTile(int y, int x, char tile);

    @Override
    public native char getTile(int y, int x);

    @Override
    public native char[][] getLookWindow(int y, int x, int lookSize);

    @Override
    public native int getWin();

    @Override
    public native int countRemainingGold();
}
