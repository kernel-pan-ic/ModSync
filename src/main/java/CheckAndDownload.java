import com.google.common.net.PercentEscaper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CheckAndDownload extends JPanel implements Runnable {
	private final File modsDir;
	private final File configsDir;
	private final File syncFolder;

	private static Boolean server;
	
	private static File modsList = new File("mods");
	private static File configsList = new File("configs");
	private static File deletionList = new File("delete");

	private final JPanel progressPanel = new JPanel();

	private final JLabel status = new JLabel();
	private final JProgressBar progress = new JProgressBar();

	private static final Gson gson = new GsonBuilder().create();
	
	public CheckAndDownload(File syncFolder, Boolean server) {
		this.syncFolder = syncFolder;
		CheckAndDownload.server = server;

		modsDir = new File(syncFolder.getAbsolutePath() + File.separator + "mods");
		configsDir = new File(syncFolder.getAbsolutePath() + File.separator + "config");

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));

		JFrame frame = new JFrame("Status");
		frame.setMinimumSize(new Dimension(400, 200));
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.NORTH);
		frame.add(progressPanel);

		add(status);
		add(progress);

		frame.setVisible(true);
	}

	@Override
	public void run() {
		try {

			if (Main.debugModsList == null) {
				status.setText("Downloading mods list.");
				download(modsList, "https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/meta/mods");

				status.setText("Downloading configs list.");
				download(configsList, "https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/meta/configs");

				status.setText("Downloading deletion list.");
				download(deletionList, "https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/meta/delete");
			} else {
				modsList = Main.debugModsList;
				configsList = Main.debugConfigList;
				deletionList = Main.debugDeletionList;
			}

			status.setText("Reading in mods and configs list.");
			ModsList mods;
			try (InputStreamReader input = new InputStreamReader(new FileInputStream(modsList))) {
				mods = gson.fromJson(input, ModsList.class);
			}
			List<String> configsLines = Files.readAllLines(configsList.toPath());
			List<String> deletionLines = Files.readAllLines(deletionList.toPath());

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
			status.setText("Syncing modpacks (if any)");

			int modpackCount = mods.getModpackCount();
			if (modpackCount > 0) {
				progress.setMaximum(modpackCount);
			}
			for (Mod mod : mods) {
				if (mod.type.equals("cursemodpack")) {
					System.out.println(mod.getDownload());
					if ((!server && mod.client) || (server && mod.server)) {
						progress.setValue(mods.indexOf(mod));
						File modFile = new File(mod.filename);
						download(modFile, mod.getDownload());

						FileOperations.extractModpackMetadata(modFile.toPath(), new File("extracted").toPath());

						installModpack(new File("extracted"));
					}
				}
			}

			status.setText("Syncing mods.");

			int modCount = mods.getModCount();
			if (modCount > 0) {
				progress.setMaximum(modCount);
			}
			for (Mod mod : mods) {
				if (!mod.type.equals("cursemodpack")) {
					System.out.println(mod.getDownload());
					if ((!server && mod.client) || (server && mod.server)) {
						progress.setValue(mods.indexOf(mod));
						File modFile = new File(modsDir.getAbsolutePath() + File.separator + mod.filename.replaceAll("/", File.separator));

						verifyAndDownload(modFile, mod.getDownload(), mod.hash);
					}
				}
			}

			//deleteNonMods(modsDir, mods);

			status.setText("Verifying configs.");

			for (String configAndHash : configsLines) {
				if (configAndHash != null && !configAndHash.equals("")) {
					String config = configAndHash.split(" : ")[1];
					String hash = configAndHash.split(" : ")[0];
					File configFile = new File(configsDir.getAbsolutePath() + File.separator + config);

					verifyAndDownload(configFile, "https://raw.githubusercontent.com/kernel-pan-ic/modpack/main/configs/" + config, hash);
				}
			}

			status.setText("Deleting mods not in modpack.");

			for (String file : deletionLines) {
				new File(modsDir.getAbsolutePath() + File.separator + file).delete();
			}

			status.setText("Done!");
			SwingUtilities.getWindowAncestor(this).dispose();
		} catch (Exception e) {
			try {
				throw e;
			} catch (IOException | URISyntaxException ioException) {
				ioException.printStackTrace();
			}
			catchExceptions(e);
		}
	}

	private void verifyAndDownload(File file, String downloadURL, String hash) {
		try {

			if (!file.exists()) {
				makeDirs(file);
				download(file, downloadURL);
				verifyAndDownload(file, downloadURL, hash);
			} else {
				if (!FileOperations.checkHash(hash, file)) {
					//System.out.println("The hash for file " + file.getAbsolutePath() + " doesn't match expected hash. Expected hash is: " + hash
					//		+ " when file hash is " + DigestUtils.sha256Hex(new FileInputStream(file)));

					download(file, downloadURL);
					verifyAndDownload(file, downloadURL, hash);
				}
			}

		} catch (Exception e) {
			catchExceptions(e);
		}
	}

	private void download(File file, String downloadURL) {
		try {
			if (!file.exists()) {
				makeDirs(file);
			}
			JLabel progressText = new JLabel();
			JProgressBar progress = new JProgressBar();
			progressPanel.add(progressText);
			progressPanel.add(progress);
			FileOperations.downloadToFile(downloadURL, file, progress, progressText);
			progressPanel.remove(progressText);
			progressPanel.remove(progress);
		} catch (Exception e) {
			catchExceptions(e);
		}
	}

	private void makeDirs(File file) {
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
	}

	private void installModpack(File extractedDir) {
		CurseEntriesList manifest;
		try {
			try (InputStreamReader input = new InputStreamReader(new FileInputStream(extractedDir.getAbsolutePath() + File.separator + "manifest.json"))) {
				manifest = gson.fromJson(input, CurseEntriesList.class);
			}

			for (CurseEntriesList.CurseModpackEntry mod : manifest.files) {
				String URL = FileOperations.resolveModURL(mod.projectID, mod.fileID);
				String filename = URL.substring(URL.lastIndexOf('/') + 1);
				URL = URL.substring(0, URL.lastIndexOf('/') + 1);

				download(new File(modsDir + File.separator + filename), URL + new PercentEscaper("", false).escape(filename));
			}

			Path overridesDir = extractedDir.toPath().resolve(manifest.overrides);

			Files.walk(overridesDir).filter(Files::isRegularFile).forEach(p -> {
				Path copied = syncFolder.toPath().resolve(overridesDir.relativize(p));
				try {
					Files.createDirectories(copied.getParent());
					Files.copy(p, copied, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});

		} catch (IOException e) {
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
