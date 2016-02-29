
import java.io.File;
import java.util.Scanner;



public class PlayGame {

	private static boolean networkedMode = true;
	protected IGameLogic logic;
	protected Scanner userInput;


	public PlayGame(){
		logic = new GameLogicClient(true);
		userInput = new Scanner(System.in);
	}

	public static void main(String[] args) {
		PlayGame game = new PlayGame();
		if (!networkedMode) {
			System.out.println("Do you want to load a specitic map?");
			System.out.println("Press enter for default map");
			game.selectMap(game.readUserInput());
		}
		System.out.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
		game.update();
	}
	
	/**
	 * Returns the user input
	 * @return The user input
	 */
	public String readUserInput(){
		return userInput.nextLine();
	}
	
	public void update(){
		String answer;
		while (logic.gameRunning()) {
			answer = parseCommand(readUserInput());
			printAnswer (answer);
		}
	}

	protected void printAnswer(String answer) {
		//System.out.println(answer);
	}

	public void selectMap(String mapName){
		logic.setMap(new File(mapName));
	}

	/**
	 * Parsing and Evaluating the User Input.
	 * @param readUserInput input the user generates
	 * @return answer of GameLogic
	 */
	protected String parseCommand(String readUserInput) {

		String [] command = readUserInput.trim().split(" ");
		String answer = "FAIL";

		switch (command[0].toUpperCase()){
			case "HELLO":
				answer = hello();
				break;
			case "NAME":
				logic.name(command[1]);
				answer = "";
				break;
			case "SAY":
				logic.say(readUserInput.substring(4, readUserInput.length()));
				answer = "";
				break;
			case "MOVE":
				if (command.length == 2)
					answer = move(command[1].toUpperCase().charAt(0));
			break;
			case "PICKUP":
				answer = pickup();
				break;
			case "LOOK":
				answer = look();
				break;
			case "QUIT":
				answer = "Quitting game";
				logic.quitGame();
				break;
		}
		return answer;
	}

	public String hello() {
		return logic.hello();
	}

	public String move(char direction) {
		return logic.move(direction);
	}

	public String pickup() {
		return logic.pickup();
	}

	public String look() {
		return logic.look();
	}

}
