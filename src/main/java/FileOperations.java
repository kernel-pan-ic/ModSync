import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.codec.digest.DigestUtils;

public class FileOperations {
	
	public static void downloadToFile(String URL, File file) throws IOException {
		InputStream in = new URL(URL).openStream();
		Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public static boolean checkHash(String hash, File file) throws Exception{
		return DigestUtils.sha256Hex(new FileInputStream(file)).equalsIgnoreCase(hash);
	}
}
