/**
 * Interface for bot tasks. Explained in Bot
 *
 * @author mb2070
 * @since 03/03/2016
 */
public interface BotTask {
	boolean hasNextCommand();

	String getNextCommand();
}
