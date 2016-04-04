/**
 * Created by matt on 23/03/2016.
 */
public class Stopwatch {
    private long startTime;

    public Stopwatch() {
        startTime = System.nanoTime();
    }

    public void restart() {
        startTime = System.nanoTime();
    }

    public long getElapsedTimeMillis() {
        return (System.nanoTime() - startTime) / 1000000;
    }
}
