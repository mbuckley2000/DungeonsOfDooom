import java.util.Random;



public class Bot extends PlayGame{
	private static final char [] DIRECTIONS = {'N','S','E','W'};
	private Random random;
	
	public Bot(){
		super();
		random = new Random();
	}

	public static void main(String[] args) {
		Bot game = new Bot();
		System.out.println("Do you want to load a specitic map?");
		System.out.println("Press enter for default map");
		game.selectMap(game.readUserInput());

		game.update();

	}

	private String botAction(String lastAnswer) {
		switch (lastAnswer.split(" ")[0]){
		case "":
			return "HELLO";
			case "GOLD:":
			case "FAIL":
			return "LOOK";
		default:
			return "MOVE " + DIRECTIONS[random.nextInt(4)];
		}
	}
	
	public void update(){
		String answer = "";
		while (logic.gameRunning()){

			answer = parseCommand(botAction(answer));
			printAnswer (answer);
		}
	}

}
