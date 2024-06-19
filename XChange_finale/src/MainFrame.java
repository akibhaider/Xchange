/*import javax.swing.*;
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
}*/

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;

public class MainFrame extends JFrame {

    private static final String USERNAME_FILE = "username.txt";
    private String username;
    private JLabel usernameLabel;
    private JLabel messageLabel;
    private JButton setUsernameButton;

    public MainFrame() {
        setTitle("XChange 1.0");
        setUndecorated(true); // Remove default title bar
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(45, 45, 45));

        username = loadUsername();

        usernameLabel = new JLabel("Username : " + (username != null ? username : "No username set"));
        usernameLabel.setFont(new Font("Roboto", Font.BOLD, 16));
        usernameLabel.setForeground(new Color(210, 210, 210));
        usernameLabel.setBorder(new EmptyBorder(30, 10, 35, 10));

        setUsernameButton = new JButton(username == null ? "Set Username" : "Change Username");
        styleButton(setUsernameButton, 180, 50); // Set a smaller preferred size for the username button

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        messageLabel.setForeground(new Color(200, 50, 50));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        setUsernameButton.addActionListener(e -> showSetUsernameDialog());

        JLabel descLabel = new JLabel("Welcome to Xchange, your go to file transfer app.", SwingConstants.CENTER);
        descLabel.setFont(new Font("Roboto", Font.PLAIN, 16));
        descLabel.setForeground(new Color(210, 210, 210));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton clientButton = new JButton("Send");
        styleButton(clientButton, 150, 50); // Keep preferred size for the client button
        JButton serverButton = new JButton("Receive");
        styleButton(serverButton, 150, 50); // Keep preferred size for the server button

        clientButton.addActionListener(e -> {
            if (username == null || username.isEmpty()) {
                messageLabel.setText("Please set username to proceed");
            } else {
                messageLabel.setText("");
                dispose();
                Client client = new Client(username);
                client.startClient();
            }
        });

        serverButton.addActionListener(e -> {
            if (username == null || username.isEmpty()) {
                messageLabel.setText("Please set username to proceed");
            } else {
                messageLabel.setText("");
                dispose();
                Server server = new Server(username);
                server.startServer();
            }
        });

        // Custom title bar
        JPanel titleBar = new JPanel();
        titleBar.setBackground(new Color(31, 31, 31));
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(getWidth(), 40));

        JLabel title = new JLabel("XChange 1.0 Client");
        title.setFont(new Font("Roboto", Font.BOLD, 18));
        title.setForeground(new Color(210, 210, 210));
        title.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Roboto", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(200, 50, 50));
        closeButton.setBorderPainted(false);
        closeButton.setPreferredSize(new Dimension(50, 40));
        closeButton.addActionListener(e -> System.exit(0));

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);

        // Username and button panel
        JPanel usernamePanel = new JPanel();
        usernamePanel.setBackground(new Color(65, 65, 65));
        usernamePanel.setLayout(new BorderLayout());
        usernamePanel.add(usernameLabel, BorderLayout.WEST);
        usernamePanel.add(setUsernameButton, BorderLayout.EAST);

        usernamePanel.setPreferredSize(new Dimension(600,70));
        usernamePanel.setMaximumSize(new Dimension(600,70));
        usernamePanel.setMinimumSize(new Dimension(600,70));

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(45, 45, 45));
        contentPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(new Color(45, 45, 45));
        buttonPanel.add(clientButton);
        buttonPanel.add(serverButton);

        JPanel extra1 = new JPanel();
        //extra1.setMinimumSize(new Dimension(100,500));
        extra1.setPreferredSize(new Dimension(100,700));
        extra1.setBackground(new Color(45, 45, 45));
        extra1.add(descLabel);

        JPanel extra2 = new JPanel();
        extra2.setPreferredSize(new Dimension(100,30));
        extra2.setBackground(new Color(45, 45, 45));


        // usernamePanel.setPreferredSize(new Dimension(50,50));
        // usernamePanel.setBorder(BorderFactory.createEmptyBorder(30,10,30,10));

        contentPanel.add(usernamePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(extra1);
        //contentPanel.add(extra2);
        usernamePanel.add(extra2, BorderLayout.SOUTH);
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(buttonPanel);

        add(titleBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        if (username == null) {
            messageLabel.setText("Please set username to proceed");
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

    private void styleButton(JButton button, int width, int height) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Adjust padding
        button.setPreferredSize(new Dimension(width, height)); // Set preferred size
    }

    public void showMainFrame() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

