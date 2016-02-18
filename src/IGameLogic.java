

import java.io.File;

public interface IGameLogic {
	
	
	
	public void setMap(File file);
	

	public String hello();
	

	public String move(char direction);
	

	public String pickup();
	

	public String look();
	
	public boolean gameRunning();
	
	
	public void quitGame();
	
}
