import javax.swing.*;
import java.io.File;

public class ClientPanel extends JPanel {
    File currentSelection = Main.clientMinecraftDir;
    JTextField syncFolderField;
    JButton syncButton;
    public ClientPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder("Sync for the clients:"));

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JLabel folderLabel = new JLabel("Folder to sync to:");
        add(folderLabel);

        syncFolderField = new JTextField(Main.clientMinecraftDir.getAbsolutePath());
        syncFolderField.setEditable(false);
        add(syncFolderField);

        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> {
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                this.currentSelection = fileChooser.getSelectedFile();
                syncFolderField.setText(this.currentSelection.getAbsolutePath());
            }
        });
        add(browseButton);

        syncButton = new JButton("Sync");
        syncButton.addActionListener(e -> {
            this.syncButton.setEnabled(false);
            new CheckAndDownload(currentSelection).sync(false);
            this.syncButton.setEnabled(true);
        });
        add(syncButton);
    }
}
