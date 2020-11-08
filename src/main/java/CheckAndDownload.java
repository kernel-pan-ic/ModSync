import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.codec.digest.DigestUtils;

public class CheckAndDownload extends JPanel {
	private File modsDir;
	private File configsDir;
	
	private static final File modsList = new File("mods");
	private static final File configsList = new File("configs");

	private final JLabel status = new JLabel();
	private final JProgressBar progress = new JProgressBar();
	
	public CheckAndDownload(File syncFolder) {
		modsDir = new File(syncFolder.getAbsolutePath() + File.separator + "mods");
		configsDir = new File(syncFolder.getAbsolutePath() + File.separator + "config");

		JFrame frame = new JFrame("Status");
		frame.setMinimumSize(new Dimension(400, 200));
		frame.setContentPane(this);

		add(status);
		add(progress);

		frame.setVisible(true);
	}
	
	public void sync(boolean server) {
		try {
			status.setText("Downloading mods list.");
			FileOperations.downloadToFile("https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/meta/mods", modsList);

			status.setText("Downloading configs list.");
			FileOperations.downloadToFile("https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/meta/configs", configsList);

			status.setText("Reading in mods and configs list.");
			ModsList mods;
			try (InputStreamReader input = new InputStreamReader(new FileInputStream(modsList))) {
				Gson gson = new GsonBuilder().create();
				mods = gson.fromJson(input, ModsList.class);
			}
			List<String> configsLines = Files.readAllLines(configsList.toPath());

			status.setText("Checking for mods folder.");

			if (!modsDir.exists() || !modsDir.isDirectory()) {
				if (modsDir.exists()) {
					modsDir.delete();
				}
				modsDir.mkdir();
			}

			status.setText("Checking for configs folder.");

			if (!configsDir.exists() || !configsDir.isDirectory()) {
				if (configsDir.exists()) {
					configsDir.delete();
				}
				configsDir.mkdir();
			}

			status.setText("Syncing mods.");

			for (Mod mod : mods) {
				System.out.println(mod.getDownload());
				if (!server || mod.server) {
					File modFile;
					if (mod.filename.contains("/")) {
						modFile = new File(modsDir.getAbsolutePath() + File.separator + mod.filename.split("/")[0] + File.separator + mod.filename.split("/")[1]);
					} else {
						modFile = new File(modsDir.getAbsolutePath() + File.separator + mod.filename);
					}

					if (!modFile.exists() || !FileOperations.checkHash(mod.hash, modFile)) {
						if (modFile.exists()) {
							System.out.println("The hash for file " + mod + " doesn't match expected hash. Expected hash is: " + mod.hash
									+ " when file hash is " + DigestUtils.sha256Hex(new FileInputStream(modFile)));
						}

						Files.createDirectories(modFile.toPath().getParent());

						status.setText("Downloading mod: " + mod.name);

						FileOperations.downloadToFile(mod.getDownload(), modFile);
					}
				}
			}

			status.setText("Deleting mods not in modpack.");

			//deleteNonMods(modsDir, mods);

			status.setText("Verifying configs.");

			for (String configAndHash : configsLines) {
				if (configAndHash != null && !configAndHash.equals("")) {
					String config = configAndHash.split(" : ")[1];
					String hash = configAndHash.split(" : ")[0];
					File configFile = new File(configsDir.getAbsolutePath() + File.separator + config);

					if (!configFile.exists() || !FileOperations.checkHash(hash, configFile)) {
						if (configFile.exists()) {
							System.out.println("The hash for file " + config + " doesn't match expected hash. Expected hash is: " + hash
									+ " when file hash is " + DigestUtils.sha256Hex(new FileInputStream(configFile)));
						}

						Files.createDirectories(configFile.toPath().getParent());

						status.setText("Downloading config: " + config);

						FileOperations.downloadToFile("https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/configs/" + config, configFile);
					}
				}
			}

			status.setText("Done!");
			SwingUtilities.getWindowAncestor(this).dispose();
		} catch (Exception e) {
			catchExceptions(e);
		}
	}

	private void catchExceptions(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		JOptionPane.showMessageDialog(new JFrame("Error"), "Something bad happened! The stack trace will be on the next message box.");
		JOptionPane.showMessageDialog(new JFrame("Stack trace"), sw.toString());
	}

	private void deleteNonMods(File file, ArrayList<Mod> mods) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File innerFile : files) {
				deleteNonMods(innerFile, mods);
			}
		}
		boolean delete = true;
		for (Mod mod : mods) {
			if (mod.filename.equals(file.getName())) {
				delete = false;
			}
		}
		if (delete && !file.isDirectory()) {
			file.delete();
		}
	}
}
