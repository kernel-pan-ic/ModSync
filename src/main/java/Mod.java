import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Mod {
    public String name;
    public String filename;
    public String hash;
    private String download;
    public boolean server;
    public boolean curseforge;

    public Mod(String name, String filename, String hash, String download, boolean server, boolean curseforge) {
        this.name = name;
        this.filename = filename;
        this.hash = hash;
        this.download = download;
        this.server = server;
        this.curseforge = curseforge;
    }

    public String getDownload() {
        if (this.curseforge) {
            String IDs = download.split("/")[0];
            String modID = IDs.substring(0, 4);
            String fileID = IDs.substring(4);
            String file = download.split("/")[1];
            if (fileID.startsWith("0")) {
                fileID = IDs.substring(5);
                if (fileID.startsWith("0")) {
                    fileID = IDs.substring(6);
                }
            }
            try {

                return "https://media.forgecdn.net/files/" + modID + "/" + fileID + "/" + URLEncoder.encode(file.replace("+", " "), "UTF-8");//StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println(this.download);
        } else {
            return download;
        }
        return null;
    }
}
