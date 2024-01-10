/**
 * Represents a command that displays the help menu.
 */
public class HelpCommand implements Command {

    /**
     * Executes the help command.
     */
    @Override
    public void execute() {
        System.out.println("\nMenu:");
        System.out.println("--------------------------------------------------------");
        System.out.println("'help'          - Show this menu.");
        System.out.println("'list'          - List all available songs.");
        System.out.println("'request'       - Request a song from another client.");
        System.out.println("'info'          - Get info about a specific client.");
        System.out.println("'exit'          - Quit the application.");
        System.out.println("--------------------------------------------------------");
        System.out.print("Enter command: ");
    }
}
