import java.io.BufferedReader;
import java.io.PrintWriter;

public class CommandFactory {
    public static Command createCommand(String command, PrintWriter out, BufferedReader buffin) {
        switch (command.toLowerCase()) {
            case "list":
                return new ListCommand(out);
            case "request":
                // You can create a RequestCommand here if needed
                return new RequestCommand(out, buffin);
            case "info":
                // You can create an InfoCommand here if needed
                return new InfoCommand(out, buffin);
            case "exit":
                return new ExitCommand();
            default:
                return new InvalidCommand();
        }
    }
}
