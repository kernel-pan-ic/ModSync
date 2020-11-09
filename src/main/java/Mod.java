import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Mod {
    public String name;
    public String filename;
    public String hash;
    private String download;
    public boolean server;
    public boolean client;
    public String type;

    public Mod(String name, String filename, String hash, String download, boolean server, boolean client, String type) {
        this.name = name;
        this.filename = filename;
        this.hash = hash;
        this.download = download;
        this.server = server;
        this.client = client;
        this.type = type;
    }

    public String getDownload() throws IOException, URISyntaxException {
        if (type.equals("cursemodpack") || type.equals("curseforge")) {
            String version = download;

            String URL = FileOperations.resolveCurseURL(version);
            filename = URLDecoder.decode(URL.substring(URL.lastIndexOf("/") + 1), StandardCharsets.UTF_8.name());
            return URL;
        //} else if (type.equals("curseforge")) {
//            String IDs = download.split("/")[0];
//            String modID = IDs.substring(0, 4);
//            String fileID = IDs.substring(4);
//            String file = download.split("/")[1];
//            if (fileID.startsWith("0")) {
//                fileID = IDs.substring(5);
//                if (fileID.startsWith("0")) {
//                    fileID = IDs.substring(6);
//                }
//            }
//            try {
//
//                return "https://media.forgecdn.net/files/" + modID + "/" + fileID + "/" + URLEncoder.encode(file.replace("+", " "), "UTF-8");//StandardCharsets.UTF_8);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            System.out.println(this.download);
            //int fileID = Integer.parseInt(download.split("/")[0]);
            //int projectID = Integer.parseInt(download.split("/")[1]);
            //return null;//FileOperations.getCurseDownload(new CurseEntriesList.CurseModpackEntry(fileID, projectID, true));
        } else {
            return download;
        }
    }
}
