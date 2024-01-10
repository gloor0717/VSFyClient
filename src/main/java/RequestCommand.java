import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Represents a command to request a song from the server.
 */
public class RequestCommand implements Command {
    private PrintWriter out;
    private UnifiedClient client;

    /**
     * Constructs a RequestCommand object.
     * 
     * @param out    the PrintWriter object used to send requests to the server
     * @param client the UnifiedClient object representing the client
     */
    public RequestCommand(PrintWriter out, UnifiedClient client) {
        this.out = out;
        this.client = client;
    }

    /**
     * Executes the request command.
     */
    @Override
    public void execute() {
        System.out.println("RequestCommand: Enter the name of the song you want to request:");
        Scanner scanner = new Scanner(System.in);
        String songName = scanner.nextLine();

        out.println("REQUEST_SONG " + songName);
        System.out.println("RequestCommand: Sent request for song: " + songName);

        String response = client.waitForResponse("PEER_ADDRESS");

        if (response != null) {
            String[] responseParts = response.split(" ");
            String peerIP = responseParts[1];
            int peerPort = Integer.parseInt(responseParts[2]);

            try (Socket peerSocket = new Socket(peerIP, peerPort)) {
                PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                peerOut.println(songName);

                InputStream stream = peerSocket.getInputStream();
                client.playMP3Stream(stream, peerSocket); // Pass both stream and socket
            } catch (IOException e) {
                System.err.println("RequestCommand: Error in peer connection or streaming: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("RequestCommand: The requested song was not found or no response received.");
            System.out.println("Enter command:");
        }
    }
}
