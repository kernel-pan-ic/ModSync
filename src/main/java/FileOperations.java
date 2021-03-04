import com.google.common.net.PercentEscaper;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileOperations {
    private static final String baseURL = "https://minecraft.curseforge.com/projects/";
    private static final String META_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/0/file/%s/download-url";
    private static final String MOD_URL = "https://addons-ecs.forgesvc.net/api/v2/addon/%s/file/%s";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36";

    public static void downloadToFile(String URL, File file) throws IOException {
        String filename = URL.substring(URL.lastIndexOf("/") + 1);
        URL = URL.substring(0, URL.lastIndexOf("/"));
        URL += "/" + new PercentEscaper("", false).escape(filename);
        System.out.println("Download URL:           " + new URL(URL).toString());
        InputStream in = new URL(URL).openStream();
        Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static boolean checkHash(String hash, File file) throws Exception {
        try (FileInputStream input = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(input).equalsIgnoreCase(hash);
        }
    }

    public static void extractModpackMetadata(Path zipFile, Path extractDir) throws IOException {

        if (Files.exists(extractDir)) {
            Files.walk(extractDir).sorted(Comparator.reverseOrder()).forEachOrdered(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        try (FileSystem zipFs = FileSystems.newFileSystem(zipFile, (ClassLoader) null)) {
            List<Path> roots = new ArrayList<>();
            zipFs.getRootDirectories().forEach(roots::add);

            if (roots.size() != 1) {
                throw new RuntimeException("Too many roots in Zip Filesystem, expected 1");
            }

            Path root = roots.get(0);
            ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            Files.walk(root).filter(Files::isRegularFile).forEach(p -> es.submit(() -> {
                Path extracted = extractDir.resolve(root.relativize(p).toString());
                try {
                    Files.createDirectories(extracted.getParent());
                    Files.copy(p, extracted, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }));

            es.shutdown();

            try {
                es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ignored) {

            }
        }
    }

    public static String resolveCurseURL(String version) throws IOException {
        URL url = new URL(String.format(META_URL, version));
        List<String> lines = readLinesFromURL(url);
        if (lines.size() != 1) {
            throw new RuntimeException();
        }
        return lines.get(0);
    }

    public static String resolveModURL(int project, int version) throws IOException {
        URL url = new URL(String.format(MOD_URL, project, version));
        List<String> lines = readLinesFromURL(url);
        if (lines.size() != 1) {
            throw new RuntimeException();
        }
        Pattern downloadURL = Pattern.compile("\"downloadUrl\":\".*?\"");
        Matcher matcher = downloadURL.matcher(String.join("\n", lines));

        System.out.println(String.join("\n", lines));
        matcher.find();
        String URL = matcher.group();
        return URL.substring(15, URL.length() - 1);
    }

    private static List<String> readLinesFromURL(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);

        List<String> lines = new ArrayList<>();
        String line;
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))
        ) {
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }
}
