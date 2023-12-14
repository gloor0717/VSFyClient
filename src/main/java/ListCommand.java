import java.io.PrintWriter;

public class ListCommand implements Command {
    private PrintWriter out;

    public ListCommand(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void execute() {
        System.out.println("Listing music...");
        out.println("LIST_MUSIC"); // Send the command to the server
        // You can add more code to handle the response from the server if needed
    }
}
