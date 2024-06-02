import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Sender {

    public static String ipAdd;
    public static String username;
    private static Socket socket;

    public Sender(String ip) {
        this.ipAdd = ip;
    }

    public static void send() {
        final File[] fileToSend = new File[1];

        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(new Color(45, 45, 45));

        // Custom title bar
        JPanel titleBar = new JPanel();
        titleBar.setBackground(new Color(31, 31, 31));
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 40));

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

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(45, 45, 45));

        JLabel filenameLabel = new JLabel("Choose a file to send");
        filenameLabel.setFont(new Font("Roboto", Font.PLAIN, 20));
        filenameLabel.setBorder(new EmptyBorder(30, 0, 10, 0));
        filenameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        filenameLabel.setForeground(new Color(210, 210, 210));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(40, 0, 10, 0));
        buttonPanel.setBackground(new Color(45, 45, 45));

        JButton sendButton = new JButton("Send File");
        styleButton(sendButton);

        JButton chooseButton = new JButton("Choose File");
        styleButton(chooseButton);

        JButton backButton = new JButton("Back");
        styleButton(backButton);

        buttonPanel.add(sendButton);
        buttonPanel.add(chooseButton);
        buttonPanel.add(backButton);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                frame.dispose();
                Client client = new Client(username);
                client.startClient();
            }
        });

        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                         | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a File to Send");

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileToSend[0] = fileChooser.getSelectedFile();
                    filenameLabel.setText("The file you want to send is: " + fileToSend[0].getName());
                }
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend[0] == null) {
                    filenameLabel.setText("Please, choose a file first");
                } else {
                    try {
                        FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
                        socket = new Socket(ipAdd, 1234);

                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        String fileName = fileToSend[0].getName();
                        byte[] fileNameBytes = fileName.getBytes();

                        dataOutputStream.writeInt(fileNameBytes.length);
                        dataOutputStream.write(fileNameBytes);
                        dataOutputStream.writeInt((int) fileToSend[0].length());

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            dataOutputStream.write(buffer, 0, bytesRead);
                        }

                        fileInputStream.close();
                        dataOutputStream.close();
                        socket.close();

                        JOptionPane.showMessageDialog(frame, "File sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error sending file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        contentPanel.add(filenameLabel);
        contentPanel.add(buttonPanel);

        frame.add(titleBar, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Roboto", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
    }
}

