public class ExitCommand implements Command {
    @Override
    public void execute() {
        System.out.println("Exiting client...");
        System.exit(0);
    }
}
