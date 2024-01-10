import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Represents a command to retrieve information about a client.
 */
public class InfoCommand implements Command {
    private PrintWriter out;
    private UnifiedClient client;

    /**
     * Constructs an InfoCommand object.
     * 
     * @param out    the PrintWriter object used to send commands to the server
     * @param client the UnifiedClient object used to receive responses from the server
     */
    public InfoCommand(PrintWriter out, UnifiedClient client) {
        this.out = out;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.print("Enter the client ID to get info: ");
        Scanner scanner = new Scanner(System.in);
        String clientId = scanner.nextLine();

        String command = "INFO " + clientId;
        out.println(command);

        String response = client.waitForResponse("Client:");
        if (response != null) {
            System.out.println(response);
        } else {
            System.out.println("No response received.");
        }
        System.out.print("Enter command: ");
    }
}
