import java.io.PrintWriter;

public class ListCommand implements Command {
    private PrintWriter out;

    public ListCommand(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void execute() {
        System.out.println("Listing music...");
        out.println("LIST_MUSIC");
    }
}
