public interface IGameLogic {
	String hello();

	String move(char direction);

	String pickup();

	String look();

	boolean gameRunning();

	void quitGame();
}
