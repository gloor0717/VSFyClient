import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;

public class UnifiedClient {

    private static final int SERVER_PORT = 45000;
    private static final int FILE_TRANSFER_PORT = 50000;
    private String clientName;
    private String serverName = "localhost";
    private List<String> clientSongs = new ArrayList<>();
    private volatile boolean awaitingServerResponse = false;

    public UnifiedClient(String clientName, List<String> clientSongs) {
        this.clientName = clientName;
        this.clientSongs = clientSongs;
    }

    public void start() {
        try (Socket clientSocket = new Socket(InetAddress.getByName(serverName), SERVER_PORT);
             BufferedReader buffin = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
    
            registerWithServer(out);
            new Thread(() -> listenForServerResponses(buffin)).start(); // Handle server responses in a separate thread
    
            while (true) {
                showMenu();
                String command = scanner.nextLine();
    
                switch (command.toLowerCase()) {
                    case "request":
                        requestSongFromServer(out, buffin, scanner);
                        break;
                    case "list":
                        requestMusicListFromServer(out);
                        break;
                    case "serve":
                        System.out.println("Already serving song requests in the background.");
                        break;
                    case "exit":
                        System.out.println("Exiting client...");
                        return;
                    default:
                        System.out.println("Invalid command, please try again.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("IOException in UnifiedClient: " + e.getMessage());
        }
    }    

    private void registerWithServer(PrintWriter out) {
        String registrationCommand = "REGISTER " + clientName;
        for (String song : clientSongs) {
            registrationCommand += " " + song;
        }
        out.println(registrationCommand);
        System.out.println("Registration message sent to the server.");
    }

    private void showMenu() {
        System.out.println("\nMenu:");
        System.out.println("Type 'list' to list all available songs.");
        System.out.println("Type 'request' to request a song.");
        System.out.println("Type 'serve' to serve song requests.");
        System.out.println("Type 'exit' to quit the application.");
        System.out.print("Enter command: ");
    }

    private void requestSongFromServer(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        out.println("REQUEST_CLIENT_LIST");
        System.out.println("Requested client list from server.");

        String serverResponse = in.readLine();
        System.out.println("Server response: " + serverResponse);

        System.out.println("Enter the name of the song you want to play: ");
        String songName = scanner.nextLine();
        requestFileFromClient("localhost", FILE_TRANSFER_PORT, songName);
    }

    private void requestFileFromClient(String clientIP, int clientPort, String fileName) {
        try (Socket socket = new Socket(clientIP, clientPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedInputStream bis = new BufferedInputStream(socket.getInputStream())) {
    
            out.println("REQUEST_FILE " + fileName); // Send file request
            System.out.println("Requested file: " + fileName + " from " + clientIP + ":" + clientPort);
    
            // Receiving and saving the file
            File file = new File("Downloaded_" + fileName); // Saving file with a new name
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = bis.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                System.out.println("File received and saved as: " + file.getName());
            }
        } catch (IOException e) {
            System.out.println("Error in requesting file: " + e.getMessage());
        }
    }

    private void requestMusicListFromServer(PrintWriter out) {
        out.println("LIST_MUSIC");
        System.out.println("Requested music list from server.");
        awaitingServerResponse = true; // Indicate that the client is waiting for a response
    }    
    

    private void listenForFileRequests() {
        try (ServerSocket serverSocket = new ServerSocket(FILE_TRANSFER_PORT)) {
            System.out.println("Listening for file requests on port " + FILE_TRANSFER_PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    String fileName = in.readLine();
                    System.out.println("Received request for file: " + fileName);
                    sendFile(clientSocket, "Musics/" + fileName); // Assumes 'Musics' folder
                }
            }
        } catch (IOException e) {
            System.out.println("Error in listening for song requests: " + e.getMessage());
        }
    }
    

    private void sendFile(Socket socket, String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream())) {
    
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, count);
            }
            bos.flush();
            System.out.println("File sent: " + filePath);
        }
    }
    
    private void listenForServerResponses(BufferedReader in) {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.equals("END_OF_LIST") || !awaitingServerResponse) {
                    awaitingServerResponse = false; // Reset the flag
                }
            }
        } catch (IOException e) {
            System.out.println("Error listening for server responses: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to VSFy, a simple audio streaming application.");

        System.out.println("Please enter a name for this client instance:");
        String clientName = scanner.nextLine();

        System.out.println("Please enter the path to your music folder:");
        String musicFolderPath = scanner.nextLine();
        List<String> clientSongs = loadSongsFromFolder(musicFolderPath);

        UnifiedClient client = new UnifiedClient(clientName, clientSongs);
        client.start();
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
    
}
