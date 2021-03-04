import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
	public static File clientMinecraftDir = null;
	public static File debugModsList = null;
	public static File debugConfigList = null;
	public static File debugDeletionList = null;
	static {
		if (System.getProperty("os.name").startsWith("Win")) {
			clientMinecraftDir = new File(System.getenv("APPDATA") + File.separator + ".minecraft");
		} else {
			clientMinecraftDir = new File(System.getProperty("user.home") + "/.minecraft/");
		}
	}

	public static void main(String[] args) {
		if (args.length != 0) {
			debugModsList = new File(args[0]);
			System.out.println("Debug file set to: " + debugModsList.getAbsolutePath());
			debugConfigList = new File(args[1]);
			System.out.println("Debug file set to: " + debugConfigList.getAbsolutePath());
			debugDeletionList = new File(args[2]);
			System.out.println("Debug file set to: " + debugDeletionList.getAbsolutePath());

		}
		JFrame frame = new JFrame("ModSync - Version 1.22 - kernelpanic#6177");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600,300);
		frame.setResizable(false);

		JPanel upperPanel = new ClientPanel();
		JPanel lowerPanel = new ServerPanel();


		frame.getContentPane().add(BorderLayout.NORTH, upperPanel);
		frame.getContentPane().add(BorderLayout.SOUTH, lowerPanel);
		frame.setVisible(true);
	}
}

