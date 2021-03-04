import java.util.ArrayList;

public class ModsList extends ArrayList<Mod> {
//Class just used for JSON reading using GSON

    public int getModpackCount() {
        int count = 0;
        for (Mod mod : this) {
            if (mod.type.equals("cursemodpack")) {
                count++;
            }
        }
        return count;
    }

    public int getModCount() {
        int count = 0;
        for (Mod mod : this) {
            if (mod.type.equals("curseforge")) {
                count++;
            }
        }
        return count;
    }
}
