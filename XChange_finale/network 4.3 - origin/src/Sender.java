import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Sender {

    public static ArrayList<String> ipAdd;

    public static int count;
    public static String username;
    private static Socket socket;

    public Sender(String name) {
        this.ipAdd = new ArrayList<>();
        this.username = name;
        count = 0;
    }

    public void addDevice(String ip){
        ipAdd.add(ip);
        count++;

        for(int i = 0;i<count;i++){
            System.out.println(ipAdd.get(i));
        }
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
                    // UIManager.put("FileChooserUI", "javax.swing.plaf.metal.MetalFileChooserUI");
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                         | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a File to Send");

                try {
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        fileToSend[0] = fileChooser.getSelectedFile();
                        filenameLabel.setText("The file you want to send is: " + fileToSend[0].getName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error selecting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileToSend[0] == null) {
                    filenameLabel.setText("Please, choose a file first");
                } else {
                    for(int i = 0;i<count;i++){
                        JFrame progress_frame = new JFrame("Sending File");
                        progress_frame.setSize(400, 150);
                        progress_frame.setLocationRelativeTo(frame);
                        progress_frame.getContentPane().setBackground(new Color(45, 45, 45));

                        JProgressBar j_progress_bar = new JProgressBar(0, (int) fileToSend[0].length());
                        j_progress_bar.setStringPainted(true);
                        j_progress_bar.setForeground(new Color(41, 128, 185));
                        j_progress_bar.setBackground(new Color(60, 63, 65));

                        JLabel j_l_speed = new JLabel("Speed: 0 B/s", SwingConstants.CENTER);
                        j_l_speed.setFont(new Font("Roboto", Font.PLAIN, 16));
                        j_l_speed.setForeground(new Color(210, 210, 210));

                        progress_frame.setLayout(new BoxLayout(progress_frame.getContentPane(), BoxLayout.Y_AXIS));
                        progress_frame.add(j_progress_bar);
                        progress_frame.add(j_l_speed);
                        progress_frame.setVisible(true);

                        int finalI = i;
                        new Thread(() -> {
                            try {
                                FileInputStream file_input_stream = new FileInputStream(fileToSend[0].getAbsolutePath());
                                System.out.println(ipAdd.get(finalI) + "Sender");
                                Socket socket = new Socket(ipAdd.get(finalI), 1234);

                                DataOutputStream data_output_stream = new DataOutputStream(socket.getOutputStream());

                                String file_name = fileToSend[0].getName();
                                byte[] file_name_byte = file_name.getBytes();

                                data_output_stream.writeInt(file_name_byte.length);
                                data_output_stream.write(file_name_byte);
                                data_output_stream.writeInt((int) fileToSend[0].length());

                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                long totalBytesRead = 0;
                                long startTime = System.currentTimeMillis();

                                while ((bytesRead = file_input_stream.read(buffer)) != -1) {
                                    data_output_stream.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;

                                    long finalTotalBytesRead = totalBytesRead;
                                    SwingUtilities.invokeLater(() -> {
                                        j_progress_bar.setValue((int) finalTotalBytesRead);
                                        long currentTime = System.currentTimeMillis();
                                        double elapsedTime = (currentTime - startTime) / 1000.0; // seconds
                                        double speed = (finalTotalBytesRead / 1024.0) / elapsedTime; // KB per second
                                        j_l_speed.setText(String.format("Speed: %.2f KB/s", speed));
                                    });
                                }

                                progress_frame.dispose();
                                file_input_stream.close();
                                data_output_stream.close();
                                socket.close();

                                JOptionPane.showMessageDialog(frame, "File sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException err) {
                                JOptionPane.showMessageDialog(frame, "Error sending file: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }).start();
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

