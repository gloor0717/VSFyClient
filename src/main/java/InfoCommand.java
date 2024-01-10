import java.io.PrintWriter;
import java.util.Scanner;

public class InfoCommand implements Command {
    private PrintWriter out;
    private UnifiedClient client;

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
        //System.out.println("Sending INFO command to server: " + command);
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
