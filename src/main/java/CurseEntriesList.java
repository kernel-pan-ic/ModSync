import java.util.List;

public class CurseEntriesList {
    public String author;
    public List<CurseModpackEntry> files;
    public String manifestType;
    public String manifestVersion;
    public String name;
    public String version;
    public String overrides;
    public int projectID;
    public MinecraftData minecraftData;


    public static class CurseModpackEntry {
        public int fileID;
        public int projectID;
        public boolean required;

        public CurseModpackEntry(int fileID, int projectID, boolean required) {
            this.fileID = fileID;
            this.projectID = projectID;
            this.required = required;
        }
    }

    public static class MinecraftData {
        public String version;
        public List<Modloader> modloaders;

        public static class Modloader {
            public String id;
            public boolean primary;
        }
    }

    public String getForgeVersion() {
        for (MinecraftData.Modloader modloader : minecraftData.modloaders) {
            if (modloader.id.startsWith("forge") && modloader.primary) {
                return modloader.id;
            }
        }
        for (MinecraftData.Modloader modloader : minecraftData.modloaders) {
            if (modloader.id.startsWith("forge")) {
                return modloader.id;
            }
        }
        return null;
    }
}

