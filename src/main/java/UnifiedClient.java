import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;
import javazoom.jl.player.Player;

public class UnifiedClient {

    private static final int SERVER_PORT = 45000;
    private String clientName;
    private String serverName = "localhost"; // Change this to the server's IP or hostname
    private List<String> clientSongs = new ArrayList<>();

    // Constructor
    public UnifiedClient(String clientName, List<String> clientSongs) {
        this.clientName = clientName;
        this.clientSongs = clientSongs;
    }

    public void start() {
        System.out.println("Starting the client...");
    
        Socket clientSocket = null;
        BufferedReader buffin = null;
        PrintWriter out = null;
        Scanner scanner = null;
    
        try {
            clientSocket = new Socket(InetAddress.getByName(serverName), SERVER_PORT);
            buffin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            scanner = new Scanner(System.in);
    
            System.out.println("Attempting to connect to the server at " + serverName + ":" + SERVER_PORT);
            System.out.println("Connected to server. Ready for user input.");
    
            // Send the registration command to the server when the client starts
            out.println("REGISTER " + clientName + " " + String.join(" ", clientSongs));
    
            final BufferedReader finalBuffin = buffin;
            new Thread(() -> listenForServerResponses(finalBuffin)).start();
    
            showMenu();
    
            // Main command loop
            while (true) {
                String commandInput = scanner.nextLine();
                processCommand(commandInput, out, finalBuffin); // Process the user command
            }
    
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Connection lost or unable to connect. Exiting.");
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                }
                if (out != null) {
                    out.close();
                }
                if (buffin != null) {
                    buffin.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("Type 'help' to show this menu.");
        System.out.println("Type 'list' to list all available songs.");
        System.out.println("Type 'play' to play a song.");
        System.out.println("Type 'info' to request client info.");
        System.out.println("Type 'exit' to quit the application.");
        System.out.print("Enter command: ");
    }

    private void processCommand(String commandInput, PrintWriter out, BufferedReader buffin) {
        String[] commandParts = commandInput.split(" ");
        String command = commandParts[0].toLowerCase();

        Command cmd = CommandFactory.createCommand(command, out, buffin);
        cmd.execute();
    }

    private void playMP3Stream(InputStream stream) {
        try {
            Player player = new Player(stream);
            System.out.println("Streaming and playing the file...");
            player.play();
            System.out.println("Finished playing the file.");
        } catch (Exception e) {
            System.err.println("Problem playing stream");
            e.printStackTrace();
        }
    }

    private void listenForServerResponses(BufferedReader buffin) {
        try {
            String response;
            while ((response = buffin.readLine()) != null) {
                // Handle the response
                if (!"END_OF_LIST".equals(response)) {
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            System.err.println("Error listening for server responses: " + e.getMessage());
        }
        // Do not close buffin here as it's being managed by the caller method
    }

    // Load all songs from the specified folder
    private static List<String> loadSongsFromFolder(String folderPath) {
        List<String> songs = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    songs.add(path.getFileName().toString());
                }
            }
        } catch (IOException | DirectoryIteratorException e) {
            System.out.println("Error loading songs from folder: " + e.getMessage());
        }
        return songs;
    }

    // Main method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to VSFy, a simple audio streaming application.");

        System.out.println("Please enter a name for this client instance:");
        String clientName = scanner.nextLine();

        System.out.println("Please enter the path to your music folder:");
        String musicFolderPath = scanner.nextLine();

        // Handle song loading in a separate thread to avoid blocking the main thread
        new Thread(() -> {
            List<String> clientSongs = loadSongsFromFolder(musicFolderPath);
            UnifiedClient client = new UnifiedClient(clientName, clientSongs);
            client.start();
        }).start();
    }
}
