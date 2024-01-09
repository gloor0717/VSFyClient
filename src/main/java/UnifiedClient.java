import java.io.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;
import javazoom.jl.player.*;
import javazoom.jl.decoder.*;

public class UnifiedClient {

    private static final int SERVER_PORT = 45000;
    private ServerSocket p2pServerSocket;
    private String clientName;
    private String serverName = "localhost"; // Change this to the server's IP or hostname
    private List<String> clientSongs = new ArrayList<>();
    private int p2pPort;
    private String musicFolderPath;
    private PrintWriter out;
    private BufferedReader buffin;

    // Updated Constructor
    public UnifiedClient(String clientName, String musicFolderPath, List<String> clientSongs, int p2pPort) {
        this.clientName = clientName;
        this.musicFolderPath = musicFolderPath; // Set the music folder path
        this.clientSongs = clientSongs;
        this.p2pPort = 0;
    }

    public void start() {
        System.out.println("Starting the client...");
        try (Socket clientSocket = new Socket(InetAddress.getByName(serverName), SERVER_PORT);
                Scanner scanner = new Scanner(System.in)) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            buffin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Start P2P server in a new thread
            new Thread(this::startP2PServer).start();

            System.out.println("Attempting to connect to the server at " + serverName + ":" + SERVER_PORT);
            System.out.println("Connected to server. Ready for user input.");

            out.println("REGISTER " + clientName + " " + p2pPort + " " + String.join(" ", clientSongs));

            showMenu();

            while (true) {
                String commandInput = scanner.nextLine();
                processCommand(commandInput);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println("Connection lost or unable to connect. Exiting.");
        }
    }

    public String waitForResponse(String responseType) {
        try {
            String response;
            while ((response = buffin.readLine()) != null) {
                if (response.startsWith(responseType)) {
                    return response;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading server response: " + e.getMessage());
        }
        return null;
    }

    private void startP2PServer() {
        try {
            p2pServerSocket = new ServerSocket(0); // Lets the system pick an available port
            p2pPort = p2pServerSocket.getLocalPort();
            System.out.println("P2P Server started on port: " + p2pPort);

            if (out != null) {
                out.println("UPDATE_PORT " + clientName + " " + p2pPort);
            }

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
            System.out.println("handleP2PClient: New P2P connection from " + clientSocket.getInetAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            OutputStream outStream = clientSocket.getOutputStream();

            String requestedSong = reader.readLine();
            System.out.println("handleP2PClient: Requested song: " + requestedSong);

            Path songPath = Paths.get(musicFolderPath, requestedSong);
            if (Files.exists(songPath)) {
                System.out.println("handleP2PClient: Streaming song: " + songPath);
                Files.copy(songPath, outStream);
                System.out.println("handleP2PClient: Finished streaming. Awaiting acknowledgment...");
                writer.println("END_OF_SONG"); // Send end-of-song signal
                String ack = reader.readLine(); // Wait for acknowledgment
                if ("ACK".equals(ack)) {
                    System.out.println("handleP2PClient: Acknowledgment received.");
                }
            } else {
                System.out.println("handleP2PClient: Song not found.");
            }
        } catch (IOException e) {
            System.err.println("handleP2PClient: Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("handleP2PClient: Closed connection.");
            } catch (IOException e) {
                System.err.println("handleP2PClient: Error closing socket: " + e.getMessage());
            }
        }
    }

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("--------------------------------------------------------");
        System.out.println("'help'          - Show this menu.");
        System.out.println("'list'          - List all available songs.");
        System.out.println("'request'       - Request a song from another client.");
        System.out.println("'info'          - Get info about a specific client.");
        System.out.println("'exit'          - Quit the application.");
        System.out.println("--------------------------------------------------------");
        System.out.print("Enter command: ");
    }

    private void processCommand(String commandInput) {
        Command command = CommandFactory.createCommand(commandInput, out, this);
        if (command != null) {
            command.execute();
        } else {
            System.out.println("Invalid command, please try again.");
        }
    }

    public void playMP3Stream(InputStream stream, Socket peerSocket) {
        try {
            System.out.println("Starting to play stream...");
            
            // Initialize MP3 decoder components
            Bitstream bitstream = new Bitstream(stream);
            Decoder decoder = new Decoder();
            AudioDevice audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
            audioDevice.open(decoder);
    
            boolean done = false;
            while (!done) {
                try {
                    Header frameHeader = bitstream.readFrame();
                    if (frameHeader != null) {
                        // Decode and play the frame
                        SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);
                        audioDevice.write(output.getBuffer(), 0, output.getBufferLength());
                        bitstream.closeFrame();
                    } 
    
                    // Check for end-of-stream indicator
                    if (stream.available() <= 0) {
                        // If no more data is available, assume end of stream
                        done = true;
                    }
                    
                } catch (BitstreamException | DecoderException e) {
                    System.err.println("Error decoding MP3 frame: " + e.getMessage());
                    break; // Exit loop on error
                }
            }
    
            audioDevice.flush();
            System.out.println("Finished playing the file.");
    
        } catch (Exception e) {
            System.err.println("Problem playing stream: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
                if (peerSocket != null && !peerSocket.isClosed()) peerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
