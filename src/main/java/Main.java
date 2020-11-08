import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
	public static File clientMinecraftDir = null;
	static {
		if (System.getProperty("os.name").startsWith("Win")) {
			clientMinecraftDir = new File(System.getenv("APPDATA") + File.separator + ".minecraft");
		} else {
			clientMinecraftDir = new File(System.getProperty("user.home") + "/.minecraft/");
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("ModSync - Version 1.15 - kernelpanic#6669");

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

