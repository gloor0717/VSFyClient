import java.io.PrintWriter;
import java.io.BufferedReader;

public class RequestCommand implements Command {
    private PrintWriter out;
    private BufferedReader buffin;

    public RequestCommand(PrintWriter out, BufferedReader buffin) {
        this.out = out;
        this.buffin = buffin;
    }

    @Override
    public void execute() {
        // Implement the logic for the 'request' command here
        // You can use the 'out' and 'buffin' objects to interact with the server
        // Add your code here to send requests and handle responses
    }
}
