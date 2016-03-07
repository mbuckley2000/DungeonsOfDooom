/**
 * Interface for bot tasks
 *
 * @author mb2070
 * @since 03/03/2016
 */
public interface BotTask {
	boolean hasNextCommand();

	String getNextCommand();
}
