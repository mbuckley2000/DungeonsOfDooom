/**
 * Interface for bot tasks. Explained in Bot
 *
 * @author Matt Buckley
 * @since 03/03/2016
 */
public interface BotTask {
    boolean hasNextCommand();

    String getNextCommand();
}
