/**
 * Represents an invalid command.
 */
public class InvalidCommand implements Command {

    /**
     * Executes the invalid command.
     */
    @Override
    public void execute() {
        System.out.println("Invalid command, please try again.");
        System.out.print("Enter command: ");
    }
}
