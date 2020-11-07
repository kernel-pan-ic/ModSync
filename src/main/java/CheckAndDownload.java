import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

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
			List<String> modsLines = Files.readAllLines(modsList.toPath());
			List<String> configsLines = Files.readAllLines(configsList.toPath());

			ArrayList<Mod> mods = new ArrayList<>();

			for (String line : modsLines) {
				mods.add(parseForMod(line));
			}

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
				if (!server || mod.server) {
					File modFile = new File(modsDir.getAbsolutePath() + File.separator + mod.filename);

					if (!modFile.exists() || !FileOperations.checkHash(mod.hash, modFile)) {
						if (modFile.exists()) {
							System.out.println("The hash for file " + mod + " doesn't match expected hash. Expected hash is: " + mod.hash
									+ " when file hash is " + DigestUtils.sha256Hex(new FileInputStream(modFile)));
						}

						Files.createDirectories(modFile.toPath().getParent());

						status.setText("Downloading mod: " + mod.name);

						FileOperations.downloadToFile(mod.download, modFile);
					}
				}
			}

			status.setText("Verifying configs.");

			for (String configAndHash : configsLines) {
				if (configAndHash != null && configAndHash != "") {
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

						FileOperations.downloadToFile("https://raw.githubusercontent.com/kernel-pan-ic/modpack/master/configs/" + config, configFile);
					}
				}
			}

			status.setText("Done!");
			SwingUtilities.getWindowAncestor(this).dispose();
		} catch (Exception e) {
			catchExceptions(e);
		}
	}

	private Mod parseForMod(String line) {
		String[] elements = line.split(" : ");

		String name = elements[0];
		String filename = elements[1];
		String hash = elements[2];
		String download = elements[3];
		boolean server = elements[4].contains("1");
		boolean curseforge = elements[5].contains("1");

		return new Mod(name, filename, hash, download, server, curseforge);
	}

	private void catchExceptions(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		JOptionPane.showMessageDialog(new JFrame("Error"), "Something bad happened! The stack trace will be on the next message box.");
		JOptionPane.showMessageDialog(new JFrame("Stack trace"), sw.toString());
	}
}
