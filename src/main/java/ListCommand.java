import java.io.PrintWriter;

/**
 * Represents a command to list music.
 */
public class ListCommand implements Command {
    private PrintWriter out;
    private UnifiedClient client;

    /**
     * Constructs a ListCommand object.
     * 
     * @param out    the PrintWriter object used for output
     * @param client the UnifiedClient object used for communication with the server
     */
    public ListCommand(PrintWriter out, UnifiedClient client) {
        this.out = out;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.println("\nMusic list : ");
        System.out.println("--------------------------------------------------------");
        out.println("LIST_MUSIC");

        String response;
        while (true) {
            response = client.waitForResponse("");
            if (response == null || response.equals("END_OF_LIST")) {
                break;
            }
            System.out.println(response);
        }
        System.out.print("Enter command: ");
    }
}
