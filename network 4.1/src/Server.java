import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Server {

    private String username;
    private static final int BROADCAST_PORT = 8888;
    private static final int SERVER_PORT = 9999;
    private static final String MULTICAST_ADDRESS = "230.0.0.0";

    private JFrame frame;
    private JTextArea textArea;
    private JButton disconnectButton;
    private MulticastSocket multicastSocket;
    private boolean running;

    static ArrayList<MyFile> my_files = new ArrayList<>();

    public Color darkGray = new Color(45, 45, 45);
    public Color lightGray = new Color(88, 88, 88);

    public Server(String username) {
        this.username = username;
    }

    public void startServer() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("info", new Color(45, 45, 45));
            UIManager.put("nimbusBase", new Color(18, 30, 49));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(45, 45, 45));
            UIManager.put("nimbusOrange", new Color(23, 165, 185));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
            UIManager.put("text", new Color(230, 230, 230));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame("XChange");
        frame.setUndecorated(true);
        frame.setSize(600,400);
        //frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(darkGray);

        JPanel titleBar = new JPanel();
        titleBar.setBackground(new Color(31,31,31));
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), 40));
        titleBar.setMaximumSize(new Dimension(frame.getWidth(), 40));

        JLabel title = new JLabel("XChange");
        title.setFont(new Font("Roboto",Font.BOLD,18));
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

        textArea = new JTextArea();
        textArea.setEditable(false);
        JLabel usernameLabel = new JLabel("Username : " + username);
        usernameLabel.setFont(new Font("Roboto", Font.BOLD, 15));
        usernameLabel.setBorder(new EmptyBorder(10, 20, 10, 0));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBackground(Color.darkGray);
        JButton backButton = new JButton("Back");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);

        styleButton(backButton,80,30);
        styleButton(disconnectButton,150,30);
        disconnectButton.setBackground(new Color(169,46,34));

        backButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame,
                    "Connection will be terminated. Are you sure you want to go to the mainframe?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                running = false;
                disconnect();
                frame.dispose();
                new MainFrame().showMainFrame();
            }
        });

        disconnectButton.addActionListener(e -> disconnect());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(usernameLabel, BorderLayout.CENTER);
        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(disconnectButton, BorderLayout.EAST);
        topPanel.setPreferredSize(new Dimension(frame.getWidth(), 50));
        topPanel.setMaximumSize(new Dimension(frame.getWidth(), 50));

        JScrollPane scroll_pane = new JScrollPane(textArea);
        scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_pane.getVerticalScrollBar().setBackground(lightGray);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(titleBar, BorderLayout.NORTH);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scroll_pane, BorderLayout.CENTER);
        frame.setVisible(true);

        running = true;
        startBroadcasting();
        runServer();
    }

    private void startBroadcasting() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                multicastSocket = new MulticastSocket(BROADCAST_PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                multicastSocket.joinGroup(group);

                byte[] msg = username.getBytes();
                while (running) {
                    DatagramPacket packet = new DatagramPacket(msg, msg.length, group, BROADCAST_PORT);
                    multicastSocket.send(packet);
                    Thread.sleep(5000); // Broadcast every 5 seconds
                }
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void runServer() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    handleClientConnection(clientSocket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleClientConnection(Socket clientSocket) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String clientName = in.readLine();
                int response = JOptionPane.showConfirmDialog(frame,
                        clientName + " wants to connect with you. Accept?",
                        "Connection Request",
                        JOptionPane.YES_NO_OPTION);

                if (response == JOptionPane.YES_OPTION) {
                    out.println("Connection Successful");
                    textArea.append("Connected to " + clientName + "\n");
                    disconnectButton.setEnabled(true);
                    Receive();
                } else {
                    out.println("Connection Denied");
                    textArea.append("Connection denied for " + clientName + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void disconnect() {
        running = false;
        try {
            if (multicastSocket != null) {
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
            }
            textArea.append("Disconnected from clients\n");
            disconnectButton.setEnabled(false); // Disable disconnect button after disconnection
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Receive() throws IOException {
        // Set dark theme using Nimbus Look and Feel
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.put("control", new Color(45, 45, 45));
            UIManager.put("info", new Color(45, 45, 45));
            UIManager.put("nimbusBase", new Color(18, 30, 49));
            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
            UIManager.put("nimbusFocus", new Color(115, 164, 209));
            UIManager.put("nimbusGreen", new Color(176, 179, 50));
            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
            UIManager.put("nimbusLightBackground", new Color(45, 45, 45));
            UIManager.put("nimbusOrange", new Color(23, 165, 185));
            UIManager.put("nimbusRed", new Color(169, 46, 34));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
            UIManager.put("text", new Color(230, 230, 230));
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        int file_id = 0;
        Color darkGray = new Color(45, 45, 45);
        Color lightGray = new Color(100, 100, 100);
        Color skyBlue = new Color(135, 206, 235); // Sky blue color

        JFrame j_frame = new JFrame();
        j_frame.setUndecorated(true); // Remove default title bar
        j_frame.setSize(800, 500);
        j_frame.setLayout(new BorderLayout());
        j_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j_frame.setLocationRelativeTo(null);
        j_frame.setResizable(false);
        j_frame.getContentPane().setBackground(darkGray);

        // Custom title bar
        JPanel titleBar = new JPanel();
        titleBar.setBackground(new Color(31, 31, 31));
        titleBar.setLayout(new BorderLayout());
        titleBar.setPreferredSize(new Dimension(j_frame.getWidth(), 40));

        JLabel title = new JLabel("XChange");
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

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Roboto", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(41, 128, 185));
        backButton.setForeground(Color.WHITE);
        //backButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 13, 20)); // Adjust padding
        backButton.setPreferredSize(new Dimension(80,30));

        backButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(j_frame,
                    "Are you sure you want to go back? This will terminate the connection.",
                    "Warning",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                running = false;
                disconnect(); // Terminate connection
                j_frame.dispose(); // Close the current receiver frame
                startServer(); // Restart the server functionality
            }
        });

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(darkGray);

        JScrollPane j_scroll_pane = new JScrollPane(contentPanel);
        j_scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        j_scroll_pane.getVerticalScrollBar().setBackground(lightGray);

        JLabel j_l_title = new JLabel("XChange File Receiver");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);
        j_l_title.setForeground(Color.WHITE);

        contentPanel.add(j_l_title);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setBackground(new Color(45, 45, 45));
        backPanel.add(backButton);

        j_frame.add(titleBar, BorderLayout.NORTH);
        j_frame.add(j_scroll_pane, BorderLayout.CENTER);
        j_frame.add(backPanel,BorderLayout.SOUTH);
        j_frame.setVisible(true);

        ServerSocket server_socket = new ServerSocket(1234);

        while (true) {
            try {
                Socket socket = server_socket.accept();

                DataInputStream data_input_stream = new DataInputStream(socket.getInputStream());

                int file_name_length = data_input_stream.readInt();
                if (file_name_length > 0) {
                    byte[] file_name_bytes = new byte[file_name_length];
                    data_input_stream.readFully(file_name_bytes, 0, file_name_bytes.length);
                    String file_name = new String(file_name_bytes);

                    int file_content_length = data_input_stream.readInt();
                    if (file_content_length > 0) {
                        byte[] file_content_bytes = new byte[file_content_length];

                        // Progress bar
                        JProgressBar j_progress_bar = new JProgressBar(0, file_content_length);
                        j_progress_bar.setBackground(darkGray);
                        j_progress_bar.setForeground(skyBlue);

                        JLabel j_l_speed = new JLabel("Speed: 0 KB/s Time: 0.00 s");
                        j_l_speed.setFont(new Font("Arial", Font.BOLD, 14)); // Set custom font
                        j_l_speed.setForeground(Color.WHITE);
                        j_l_speed.setAlignmentX(Component.CENTER_ALIGNMENT);

                        JPanel progressPanel = new JPanel();
                        progressPanel.setLayout(new BorderLayout());
                        progressPanel.add(j_progress_bar, BorderLayout.CENTER);
                        progressPanel.add(j_l_speed, BorderLayout.SOUTH);
                        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

                        JPanel j_p_file_row = new JPanel();
                        j_p_file_row.setBackground(darkGray);
                        j_p_file_row.setLayout(new BoxLayout(j_p_file_row, BoxLayout.Y_AXIS));

                        JLabel j_l_file_name = new JLabel(file_name);
                        j_l_file_name.setFont(new Font("Arial", Font.BOLD, 20));
                        j_l_file_name.setBorder(new EmptyBorder(10, 0, 10, 0));
                        j_l_file_name.setAlignmentX(Component.CENTER_ALIGNMENT);
                        j_l_file_name.setForeground(Color.WHITE);

                        j_p_file_row.setName(String.valueOf(file_id));
                        j_p_file_row.addMouseListener(getMyMouseListener());

                        j_p_file_row.add(j_l_file_name);
                        j_p_file_row.add(progressPanel);
                        j_p_file_row.add(j_l_speed);
                        contentPanel.add(j_p_file_row);
                        j_frame.validate();

                        my_files.add(new MyFile(file_id, file_name, file_content_bytes, getFileExtension(file_name)));
                        file_id += 1;

                        long startTime = System.currentTimeMillis();
                        int bytesRead = 0;
                        while (bytesRead < file_content_length) {
                            int bytesRemaining = file_content_length - bytesRead;
                            int chunkSize = Math.min(bytesRemaining, 4096);
                            byte[] buffer = new byte[chunkSize];
                            int read = data_input_stream.read(buffer, 0, chunkSize);
                            if (read > 0) {
                                System.arraycopy(buffer, 0, file_content_bytes, bytesRead, read);
                                bytesRead += read;
                                j_progress_bar.setValue(bytesRead);

                                long currentTime = System.currentTimeMillis();
                                double elapsedTime = (currentTime - startTime) / 1000.0; // seconds
                                double speed = (bytesRead / 1024.0) / elapsedTime; // KB per second
                                j_l_speed.setText(String.format("Speed: %.2f KB/s        Time: %.2f s", speed, elapsedTime));

                            }
                        }

                        File file_to_save = new File(file_name);
                        try (FileOutputStream file_output_stream = new FileOutputStream(file_to_save)) {
                            file_output_stream.write(file_content_bytes);
                        }
                    }
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }

    public static MouseListener getMyMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel j_panel = (JPanel) e.getSource();
                int file_id = Integer.parseInt(j_panel.getName());
                for (MyFile my_file : my_files) {
                    if (my_file.getId() == file_id) {
                        JFrame j_f_preview = createFrame(my_file.getName(), my_file.getData(), my_file.getFile_extension());
                        j_f_preview.setVisible(true);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        };
    }

    public static JFrame createFrame(String file_name, byte[] file_data, String file_extension) {
        JFrame j_frame = new JFrame("XChange: Download Panel");
        j_frame.setSize(400, 400);
        j_frame.getContentPane().setBackground(new Color(45, 45, 45));

        JPanel j_panel = new JPanel();
        j_panel.setBackground(new Color(45, 45, 45));
        j_panel.setLayout(new BoxLayout(j_panel, BoxLayout.Y_AXIS));

        JLabel j_l_title = new JLabel("File Downloader");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);
        j_l_title.setForeground(Color.WHITE);

        JLabel j_l_prompt = new JLabel("Do you want to download " + file_name);
        j_l_prompt.setFont(new Font("Arial", Font.BOLD, 20));
        j_l_prompt.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        j_l_prompt.setForeground(Color.WHITE);

        JButton j_b_yes = new JButton("Yes");
        j_b_yes.setPreferredSize(new Dimension(150, 75));
        j_b_yes.setFont(new Font("Arial", Font.BOLD, 20));
        j_b_yes.setBackground(new Color(45, 45, 45));
        j_b_yes.setForeground(Color.WHITE);
        j_b_yes.setFocusPainted(false);
        j_b_yes.setBorderPainted(false);
        j_b_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File file_to_save = new File(file_name);
                    try (FileOutputStream file_output_stream = new FileOutputStream(file_to_save)) {
                        file_output_stream.write(file_data);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                j_frame.dispose();
            }
        });

        JButton j_b_no = new JButton("No");
        j_b_no.setPreferredSize(new Dimension(150, 75));
        j_b_no.setFont(new Font("Arial", Font.BOLD, 20));
        j_b_no.setBackground(new Color(45, 45, 45));
        j_b_no.setForeground(Color.WHITE);
        j_b_no.setFocusPainted(false);
        j_b_no.setBorderPainted(false);
        j_b_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j_frame.dispose();
            }
        });

        JPanel j_p_buttons = new JPanel();
        j_p_buttons.setBackground(new Color(45, 45, 45));
        j_p_buttons.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_p_buttons.add(j_b_yes);
        j_p_buttons.add(j_b_no);

        j_panel.add(j_l_title);
        j_panel.add(j_l_prompt);
        j_panel.add(j_p_buttons);

        j_frame.add(j_panel);
        j_frame.setLocationRelativeTo(null);
        j_frame.setResizable(false);
        return j_frame;
    }

    public static String getFileExtension(String file_name) {
        int i = file_name.lastIndexOf('.');
        if (i > 0) {
            return file_name.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }

    public void styleButton(JButton button, int width, int height) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 13, 20)); // Adjust padding
        button.setPreferredSize(new Dimension(width, height)); // Set preferred size
    }
}
