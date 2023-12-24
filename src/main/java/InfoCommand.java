import java.io.PrintWriter;
import java.util.Scanner;
import java.io.BufferedReader;

public class InfoCommand implements Command {
    private PrintWriter out;
    private BufferedReader buffin;
    private UnifiedClient client;

    public InfoCommand(PrintWriter out, BufferedReader buffin, UnifiedClient client) {
        this.out = out;
        this.buffin = buffin;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.print("Enter the client ID to get info: ");
        Scanner scanner = new Scanner(System.in);
        String clientId = scanner.nextLine();

        String command = "INFO " + clientId;
        System.out.println("Sending INFO command to server: " + command); // Debug log
        out.println(command);
    }
}

