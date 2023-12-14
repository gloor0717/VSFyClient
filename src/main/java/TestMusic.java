import javazoom.jl.player.Player;
import java.io.FileInputStream;

public class TestMusic {
    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("C:\\Users\\gian-\\Desktop\\Musics\\song1.mp3");
            Player player = new Player(fis);
            System.out.println("Playing the file...");
            player.play();
            System.out.println("Finished playing the file.");
        } catch (Exception e) {
            System.err.println("Problem playing file");
            e.printStackTrace();
        }
    }
}
