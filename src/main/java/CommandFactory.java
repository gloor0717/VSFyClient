import java.io.PrintWriter;

public class CommandFactory {
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
            default:
                return new InvalidCommand();
        }
    }
}
