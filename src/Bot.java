import java.util.Random;



public class Bot extends PlayGame{
	private Random random;
	private static final char [] DIRECTIONS = {'N','S','E','W'};
	
	public Bot(){
		super();
		random = new Random();
	}

	private String botMovement(String lastAnswer){
		switch (lastAnswer.split(" ")[0]){
		case "":
			return "HELLO";
		case "GOLD":
			return "LOOK";
		default:
			return "MOVE" + DIRECTIONS[random.nextInt(4)];
		}
	}
	
	public void update(){
		String answer = "";
		while (logic.gameRunning()){
			
			answer = parseCommand(botMovement(answer));
			printAnswer (answer);
		}
	}
	
	public static void main(String [] args) {
		Bot game = new Bot();
		game.selectMap(game.readUserInput());
		
		game.update();
		
	}

}
