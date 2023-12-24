import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class RequestCommand implements Command {
    private PrintWriter out;
    private BufferedReader buffin;
    private UnifiedClient client; // Reference to the UnifiedClient

    // Constructor should now take the UnifiedClient as a parameter
    public RequestCommand(PrintWriter out, BufferedReader buffin, UnifiedClient client) {
        this.out = out;
        this.buffin = buffin;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.println("Enter the name of the song you want to request:");
        Scanner scanner = new Scanner(System.in);
        String songName = scanner.nextLine();

        // Ask the server for the address of the peer that has the song
        out.println("REQUEST_SONG " + songName);

        try {
            String response = buffin.readLine();
            if (response.startsWith("PEER_ADDRESS")) {
                String[] responseParts = response.split(" ");
                String peerIP = responseParts[1];
                int peerPort = Integer.parseInt(responseParts[2]);

                // Connect to the peer and request the song
                try (Socket peerSocket = new Socket(peerIP, peerPort)) {
                    PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                    peerOut.println(songName); // Send the song name to the peer

                    // Stream and play the song
                    InputStream stream = peerSocket.getInputStream();
                    client.playMP3Stream(stream);
                }
            } else {
                System.out.println("Could not find the requested song.");
            }
        } catch (IOException e) {
            System.err.println("Error handling song request: " + e.getMessage());
        }
    }
}
