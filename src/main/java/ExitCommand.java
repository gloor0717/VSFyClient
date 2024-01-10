/**
 * Represents a command to exit the client.
 */
public class ExitCommand implements Command {

    /**
     * Executes the exit command.
     */
    @Override
    public void execute() {
        System.out.println("Exiting client...");
        System.exit(0);
    }
}
