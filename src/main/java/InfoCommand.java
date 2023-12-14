import java.io.PrintWriter;
import java.io.BufferedReader;

public class InfoCommand implements Command {
    private PrintWriter out;
    private BufferedReader buffin;

    public InfoCommand(PrintWriter out, BufferedReader buffin) {
        this.out = out;
        this.buffin = buffin;
    }

    @Override
    public void execute() {
        
    }
}
