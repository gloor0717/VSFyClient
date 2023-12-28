import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;
import javazoom.jl.player.Player;

public class UnifiedClient {

    private static final int SERVER_PORT = 45000;
    private ServerSocket p2pServerSocket;
    private String clientName;
    private String serverName = "localhost"; // Change this to the server's IP or hostname
    private List<String> clientSongs = new ArrayList<>();
    private int p2pPort;
    private String musicFolderPath;
    private PrintWriter out;

    // Updated Constructor
    public UnifiedClient(String clientName, String musicFolderPath, List<String> clientSongs, int p2pPort) {
        this.clientName = clientName;
        this.musicFolderPath = musicFolderPath; // Set the music folder path
        this.clientSongs = clientSongs;
        this.p2pPort = 0;
    }

    public void start() {
        System.out.println("Starting the client...");
    
        Socket clientSocket = null;
        BufferedReader buffin = null;
        PrintWriter out = null;
        Scanner scanner = null;

        // Start P2P server in a new thread
        new Thread(this::startP2PServer).start();
    
        try {
            clientSocket = new Socket(InetAddress.getByName(serverName), SERVER_PORT);
            buffin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            scanner = new Scanner(System.in);
    
            System.out.println("Attempting to connect to the server at " + serverName + ":" + SERVER_PORT);
            System.out.println("Connected to server. Ready for user input.");
    
            out.println("REGISTER " + clientName + " " + p2pPort + " " + String.join(" ", clientSongs));
    
            final BufferedReader finalBuffin = buffin;
            new Thread(() -> listenForServerResponses(finalBuffin)).start();
    
            showMenu();
    
            while (true) {
                String commandInput = scanner.nextLine();
                processCommand(commandInput, out, finalBuffin);
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

    private void startP2PServer() {
        try {
            p2pServerSocket = new ServerSocket(0); // 0 allows the system to pick an available port
            p2pPort = p2pServerSocket.getLocalPort(); // Save the port for later use
            System.out.println("P2P Server started on port: " + p2pServerSocket.getLocalPort());

            out.println("UPDATE_PORT " + clientName + " " + p2pPort);

            while (!p2pServerSocket.isClosed()) {
                Socket clientSocket = p2pServerSocket.accept();
                new Thread(() -> handleP2PClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error in P2P Server: " + e.getMessage());
        }
    }

    private void handleP2PClient(Socket clientSocket) {
        try {
            System.out.println("handleP2PClient: New P2P connection established from " + clientSocket.getInetAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outStream = clientSocket.getOutputStream();

            String requestedSong = reader.readLine(); 
            System.out.println("handleP2PClient: Requested song: " + requestedSong);

            Path songPath = Paths.get(musicFolderPath, requestedSong);
            if (Files.exists(songPath)) {
                System.out.println("handleP2PClient: Found song, streaming: " + songPath);
                try (InputStream songStream = Files.newInputStream(songPath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = songStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("handleP2PClient: Finished streaming song.");
            } else {
                System.out.println("handleP2PClient: Song not found: " + songPath);
            }
        } catch (IOException e) {
            System.err.println("handleP2PClient: Error in handling P2P client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("handleP2PClient: Closed P2P connection.");
            } catch (IOException e) {
                System.err.println("handleP2PClient: Error closing client socket: " + e.getMessage());
            }
        }
    }    
    
             

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("'help' - Show this menu.");
        System.out.println("'list' - List all available songs.");
        System.out.println("'request' - Request a song from another client.");
        System.out.println("'info <client_id>' - Get info about a specific client.");
        System.out.println("'exit' - Quit the application.");
        System.out.print("Enter command: ");
    } 

    private void processCommand(String commandInput, PrintWriter out, BufferedReader buffin) {
        String[] commandParts = commandInput.split(" ");
        String commandString = commandParts[0].toLowerCase();
    
        System.out.println("Processing command: " + commandString); // Debug log
    
        Command command = CommandFactory.createCommand(commandString, out, buffin, this);
        if (command != null) {
            command.execute();
        } else {
            System.out.println("Invalid command, please try again.");
        }
    }    
    
    public void playMP3Stream(InputStream stream) {
        try {
            System.out.println("Attempting to play stream..."); // Log attempt
            Player player = new Player(stream);
            System.out.println("Starting playback..."); // Log start
            player.play();
            System.out.println("Finished playing the file."); // Log finish
        } catch (Exception e) {
            System.err.println("Problem playing stream");
            e.printStackTrace();
        }
    }
    
        

    private void listenForServerResponses(BufferedReader buffin) {
        try {
            String response;
            while ((response = buffin.readLine()) != null) {
                System.out.println(response); // Just print the server's response
            }
        } catch (IOException e) {
            System.err.println("Error listening for server responses: " + e.getMessage());
        }
    }      

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
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to VSFy, a simple audio streaming application.");
    
        System.out.print("Please enter a name for this client instance: ");
        String clientName = scanner.nextLine();
    
        System.out.print("Please enter the path to your music folder: ");
        String musicFolderPath = scanner.nextLine();
    
        List<String> clientSongs = loadSongsFromFolder(musicFolderPath);
        UnifiedClient client = new UnifiedClient(clientName, musicFolderPath, clientSongs, 0);
        client.start();
    }    
}
