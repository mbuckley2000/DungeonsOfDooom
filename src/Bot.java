import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class Bot extends PlayGame{
	Queue<AITask> taskBacklog;
	AITask exploreTask;
	private int sleepMax = 500;
	private Random random;
	private AIMap map;
	private int[] moveDelta = {0, 0};
	private int[] position = {0, 0};
	private String lastCommand;

	public Bot() {
		super();
		random = new Random();
		map = new AIMap();
		taskBacklog = new PriorityQueue<>();
		exploreTask = new ExploreTask(map, logic.getOutputClient(), position);
		taskBacklog.add(exploreTask);
	}

	public static void main(String[] args) {
		Bot game = new Bot();
		System.out.println("Bot is now running");
		game.update();

		/*
		HashSet<Bot> botArmy = new HashSet<>();
		for (int i=0; i<20; i++) {
			botArmy.add(new Bot());
		}

		while (true) {
			for (Bot b : botArmy) {
				b.update();
			}
		}
		*/

	}

	private String botAction() {
		if (!taskBacklog.isEmpty()) {
			if (taskBacklog.peek().hasNextCommand()) {
				return taskBacklog.peek().getNextCommand();
			} else {
				taskBacklog.remove();
			}
	}

		if (taskBacklog.isEmpty()) {
			//Make a new task please
			taskBacklog.add(new TraverseTask(map.getPath(position, new int[]{0, 0})));
			sleepMax = 1000;
		}
		return "nah";
	}


	public void update(){
		while (logic.gameRunning()) {
			String command = botAction().toUpperCase();
			parseCommand(command);
			System.out.println(command);
			try {
				Thread.currentThread().sleep(random.nextInt(sleepMax / 2) + sleepMax / 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}