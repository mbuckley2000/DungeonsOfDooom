

import java.io.File;
import java.util.Random;


public class GameLogic implements IGameLogic{

	private Map map = null;
	private int[] playerPosition;
	private int collectedGold;
	private boolean active;

	public GameLogic(){
		playerPosition = new int[2];
		collectedGold = 0;
		map = new Map();
	}

	protected Map getMap(){
		return map;
	}

	public void setMap(File file) {
		map.readMap(file);
		playerPosition = initiatePlayer();
		active = true;
	}

	/**
	 * Prints how much gold is still required to win!
	 */
	public String hello() {
		return "GOLD: " + (map.getWin() - collectedGold);
	}

	/**
	 * By proving a character direction from the set of {N,S,E,W} the gamelogic 
	 * checks if this location can be visited by the player. 
	 * If it is true, the player is moved to the new location.
	 * @return If the move was executed Success is returned. If the move could not execute Fail is returned.
	 */
	public String move(char direction) {
		
		int[] newPosition = playerPosition;
		switch (direction){
		case 'N':
			newPosition[0] -=1;
			break;
		case 'E':
			newPosition[1] +=1;
			break;
		case 'S':
			newPosition[0] +=1;
			break;
		case 'W':
			newPosition[1] -=1;
			break;
		default:
			break;
		}
		
		if(map.lookAtTile(newPosition[0], newPosition[1]) != '#'){
			playerPosition = newPosition;
			
			if (checkWin())
				quitGame();
			
			return "SUCCESS";
		} else {
			return "FAIL";
		}
	}

	public String pickup() {

		if (map.lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			collectedGold++;
			map.replaceTile(playerPosition[0], playerPosition[1], '.');
			return "SUCCESS, GOLD COINS: " + collectedGold;
		}

		return "FAIL" + "\n" + "There is nothing to pick up...";
	}

	/**
	 * The method shows the dungeon around the player location
	 */
	public String look() {
		String output = "";
		char [][] lookReply = map.lookWindow(playerPosition[0], playerPosition[1], 5);
		lookReply[2][2] = 'P';

		for (int i=0;i<lookReply.length;i++){
			for (int j=0;j<lookReply[0].length;j++){
				output += lookReply[j][i];
			}
			output += "\n";
		}
		return output;
	}

	/*
	 * Prints the whole map directly to Standard out.
	 */
	public void printMap() {
		map.printMap();
	}

	/**
	 * finds a random position for the player in the map.
	 * @return Return null; if no position is found or a position vector [y,x]
	 */
	private int[] initiatePlayer() {
		int[] pos = new int[2];
		Random rand = new Random();

		pos[0]=rand.nextInt(map.getMapHeight());
		pos[1]=rand.nextInt(map.getMapWidth());
		int counter = 1;
		while (map.lookAtTile(pos[0], pos[1]) == '#' && counter < map.getMapHeight() * map.getMapWidth()) {
			pos[1]= (int) ( counter * Math.cos(counter));
			pos[0]=(int) ( counter * Math.sin(counter));
			counter++;
		}
		return (map.lookAtTile(pos[0], pos[1]) == '#') ? null : pos;
	}

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 * @return True if all conditions are met, false otherwise
	 */
	protected boolean checkWin() {
		if (collectedGold >= map.getWin() && 
				map.lookAtTile(playerPosition[0], playerPosition[1]) == 'E') {
			System.out.println("Congratulations!!! \n You have escaped the Dungeon of Dooom!!!!!! \n"
					+ "Thank you for playing!");
			return true;
		}
		return false;
	}

	/**
	 * Quits the game when called
	 */
	public void quitGame() {
		System.out.println("The game will now exit");
		active = false;
	}
	
	public boolean gameRunning(){
		return active;
	}





}
