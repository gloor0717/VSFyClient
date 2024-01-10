import java.io.PrintWriter;

/**
 * A factory class for creating Command objects based on the given command string.
 */
public class CommandFactory {
    /**
     * Creates a Command object based on the given command string.
     *
     * @param command the command string
     * @param out the PrintWriter object for output
     * @param client the UnifiedClient object
     * @return a Command object based on the given command string
     */
    public static Command createCommand(String command, PrintWriter out, UnifiedClient client) {
        switch (command.toLowerCase()) {
            case "list":
                return new ListCommand(out, client);
            case "request":
                return new RequestCommand(out, client);
            case "info":
                return new InfoCommand(out, client);
            case "exit":
                return new ExitCommand();
            case "help":
                return new HelpCommand(); 
            default:
                return new InvalidCommand();
        }
    }
}
