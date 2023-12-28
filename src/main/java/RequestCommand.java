import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RequestCommand implements Command {
    private PrintWriter out;
    private BufferedReader buffin;
    private UnifiedClient client; // Reference to the UnifiedClient

    public RequestCommand(PrintWriter out, BufferedReader buffin, UnifiedClient client) {
        this.out = out;
        this.buffin = buffin;
        this.client = client;
    }

    @Override
    public void execute() {
        System.out.println("RequestCommand: Enter the name of the song you want to request:");
        Scanner scanner = new Scanner(System.in);
        String songName = scanner.nextLine();

        out.println("REQUEST_SONG " + songName);
        System.out.println("RequestCommand: Sent request for song: " + songName);

        try {
            System.out.println("RequestCommand: Waiting for server response...");
            String response = buffin.readLine();
            System.out.println("RequestCommand: Received response: " + response);

            if (response.startsWith("PEER_ADDRESS")) {
                String[] responseParts = response.split(" ");
                String peerIP = responseParts[1];
                int peerPort = Integer.parseInt(responseParts[2]);

                System.out.println(
                        "RequestCommand: Attempting to connect to peer at IP: " + peerIP + " and Port: " + peerPort);
                try {
                    Socket peerSocket = new Socket(peerIP, peerPort);
                    System.out.println("RequestCommand: Connection established with peer.");

                    PrintWriter peerOut = new PrintWriter(peerSocket.getOutputStream(), true);
                    peerOut.println(songName);
                    System.out.println("RequestCommand: Requested song: " + songName);

                    InputStream stream = peerSocket.getInputStream();
                    System.out.println("RequestCommand: Starting to receive the song stream...");

                    client.playMP3Stream(stream);
                    peerSocket.close();

                } catch (IOException e) {
                    System.err.println("RequestCommand: Error in peer connection or streaming: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("RequestCommand: The requested song was not found.");
            }
        } catch (IOException e) {
            System.err.println("RequestCommand: Error in processing song request: " + e.getMessage());
            System.out.println("RequestCommand: Exiting.");
            e.printStackTrace();
        }
    }
}