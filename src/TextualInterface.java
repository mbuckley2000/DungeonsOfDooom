import java.util.Scanner;

/**
 * Created by matt on 21/03/2016.
 */
public class TextualInterface implements PlayerInterface {
    private Scanner scanner;

    public TextualInterface() {
        scanner = new Scanner(System.in);
    }

    public String getNextCommand() {
        return parseInput(scanner.nextLine());
    }

    /**
     * Does very basic input filtering before calling the relevant GameLogic method (Client)
     * Checks for null or invalid commands
     * Converts everything to uppercase
     *
     * @param input input the user generates
     */
    private String parseInput(String input) {
        String[] command = input.trim().split(" ");

        switch (command[0].toUpperCase()) {
            case "HELLO":
                return "HELLO";
            case "MOVE":
                if (command.length == 2) {
                    return ("MOVE " + command[1].toUpperCase().charAt(0));
                }
            case "PICKUP":
                return "PICKUP";
            case "LOOK":
                return "LOOK";
            case "QUIT":
                return "QUIT";
            default:
                System.out.println("Invalid command: " + command[0]);
                break;
        }
        return null;
    }

    @Override
    public void giveLookResponse(char[][] response) {

    }

    @Override
    public void giveHelloResponse(int response) {

    }

    @Override
    public void givePickupResponse(boolean response) {

    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void giveMoveResponse(boolean response) {
    }

    @Override
    public void giveMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void giveWin() {
        System.out.println("You have escaped my dungeon! Well played...");
    }

    @Override
    public void giveLose() {
        System.out.println("Somebody else has escaped my dungeon! Get OUT!");
    }

    @Override
    public boolean hasNextCommand() {
        return false;
    }
}
