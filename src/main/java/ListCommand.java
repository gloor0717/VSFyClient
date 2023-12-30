import java.io.PrintWriter;

public class ListCommand implements Command {
    private PrintWriter out;
    private UnifiedClient client;

    public ListCommand(PrintWriter out, UnifiedClient client) {
        this.out = out;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.println("Listing music...");
        out.println("LIST_MUSIC");

        String response;
        while (true) {
            response = client.waitForResponse("");
            if (response == null || response.equals("END_OF_LIST")) {
                break;
            }
            System.out.println(response);
        }
    }
}
