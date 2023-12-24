import java.io.BufferedReader;
import java.io.PrintWriter;

public class CommandFactory {
    public static Command createCommand(String command, PrintWriter out, BufferedReader buffin, UnifiedClient client) {
        switch (command.toLowerCase()) {
            case "list":
                return new ListCommand(out);
            case "request":
                return new RequestCommand(out, buffin, client); // Pass the client instance
            case "info":
                return new InfoCommand(out, buffin, client); // If InfoCommand requires client, pass it as well
            case "exit":
                return new ExitCommand();
            default:
                return new InvalidCommand();
        }
    }
}
