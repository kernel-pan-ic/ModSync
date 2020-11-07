import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Mod {
    public String name;
    public String filename;
    public String hash;
    public String download;
    public boolean server;

    public Mod(String name, String filename, String hash, String download, boolean server, boolean curseforge) {
        this.name = name;
        this.filename = filename;
        this.hash = hash;
        this.server = server;

        if (curseforge) {
            String modID = download.substring(0, 4);
            String fileID = download.substring(4);
            if (fileID.startsWith("0")) {
                fileID = download.substring(5);
                if (fileID.startsWith("0")) {
                    fileID = download.substring(6);
                }
            }
            this.download = "https://media.forgecdn.net/files/" + modID + "/" + fileID + "/" + URLEncoder.encode(filename.replace("+", " "), StandardCharsets.UTF_8);
            System.out.println(this.download);
        } else {
            this.download = download;
        }
    }
}
