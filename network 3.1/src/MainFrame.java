import javax.swing.*;
import java.awt.*;
import java.io.*;

public class MainFrame extends JFrame {

    private static final String USERNAME_FILE = "username.txt";
    private String username;
    private JLabel usernameLabel;
    private JLabel messageLabel;
    private JButton setUsernameButton;

    public MainFrame() {
        setTitle("Main Frame");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        username = loadUsername();

        usernameLabel = new JLabel("Username: " + (username != null ? username : "No username set"));
        messageLabel = new JLabel("", SwingConstants.CENTER);
        setUsernameButton = new JButton(username == null ? "Set Username" : "Change Username");
        JButton clientButton = new JButton("Client");
        JButton serverButton = new JButton("Server");

        setUsernameButton.addActionListener(e -> showSetUsernameDialog());

        clientButton.addActionListener(e -> {
            if (username == null || username.isEmpty()) {
                messageLabel.setText("Please set username");
            } else {
                messageLabel.setText("");
                dispose();
                Client client = new Client(username);
                client.startClient();
            }
        });

        serverButton.addActionListener(e -> {
            if (username == null || username.isEmpty()) {
                messageLabel.setText("Please set username");
            } else {
                messageLabel.setText("");
                dispose();
                Server server = new Server(username);
                server.startServer();
            }
        });

        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(usernameLabel, BorderLayout.WEST);
        topPanel.add(setUsernameButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.add(clientButton);
        buttonPanel.add(serverButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (username == null) {
            messageLabel.setText("Please set username");
        }
    }

    private void showSetUsernameDialog() {
        JTextField usernameField = new JTextField(15);
        int option = JOptionPane.showConfirmDialog(this, usernameField, "Enter Username", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newUsername = usernameField.getText().trim();
            if (!newUsername.isEmpty()) {
                username = newUsername;
                saveUsername(username);
                usernameLabel.setText("Username: " + username);
                setUsernameButton.setText("Change Username");
                messageLabel.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Username cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveUsername(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERNAME_FILE))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadUsername() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERNAME_FILE))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    public void showMainFrame() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
