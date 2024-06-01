import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    public Server(String username) {
        this.username = username;
    }

    public void startServer() {
        frame = new JFrame("Server - " + username);
        textArea = new JTextArea();
        textArea.setEditable(false);
        JLabel usernameLabel = new JLabel("Username: " + username);
        JButton backButton = new JButton("Back");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false); // Disabled initially

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

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    /*public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Server("ServerUsername").startServer());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Receive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }*/
    static ArrayList<MyFile> my_files = new ArrayList<>();

    public static void Receive() throws IOException {
        int file_id = 0;
        JFrame j_frame = new JFrame("XChange 1.0 Server");
        j_frame.setSize(400, 400);
        j_frame.setLayout(new BoxLayout(j_frame.getContentPane(), BoxLayout.Y_AXIS));
        j_frame.setDefaultCloseOperation(j_frame.EXIT_ON_CLOSE);

        JPanel j_panel = new JPanel();
        j_panel.setLayout(new BoxLayout(j_panel, BoxLayout.Y_AXIS));

        JScrollPane j_scroll_pane = new JScrollPane(j_panel);
        j_scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel j_l_title = new JLabel("XChange File Receiver");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);

        j_frame.add(j_l_title);
        j_frame.add(j_scroll_pane);
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
                        JLabel j_l_speed = new JLabel("Speed: 0 KB/s");

                        JPanel j_p_file_row = new JPanel();
                        j_p_file_row.setLayout(new BoxLayout(j_p_file_row, BoxLayout.Y_AXIS));

                        JLabel j_l_file_name = new JLabel(file_name);
                        j_l_file_name.setFont(new Font("Arial", Font.BOLD, 20));
                        j_l_file_name.setBorder(new EmptyBorder(10, 0, 10, 0));
                        j_l_file_name.setAlignmentX(Component.CENTER_ALIGNMENT);

                        j_p_file_row.setName(String.valueOf(file_id));
                        j_p_file_row.addMouseListener(getMyMouseListener());

                        j_p_file_row.add(j_l_file_name);
                        j_p_file_row.add(j_progress_bar);
                        j_p_file_row.add(j_l_speed);
                        j_panel.add(j_p_file_row);
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
                                double speed = (bytesRead/1024.0) / elapsedTime; // bytes per second
                                j_l_speed.setText(String.format("Speed: %.2f KB/s", speed));
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

        JPanel j_panel = new JPanel();
        j_panel.setLayout(new BoxLayout(j_panel, BoxLayout.Y_AXIS));

        JLabel j_l_title = new JLabel("File Downloader");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel j_l_prompt = new JLabel("Do you want to download " + file_name);
        j_l_prompt.setFont(new Font("Arial", Font.BOLD, 20));
        j_l_prompt.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_prompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton j_b_yes = new JButton("Yes");
        j_b_yes.setPreferredSize(new Dimension(150, 75));
        j_b_yes.setFont(new Font("Arial", Font.BOLD, 20));

        JButton j_b_no = new JButton("No");
        j_b_no.setPreferredSize(new Dimension(150, 75));
        j_b_no.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel j_l_file_content = new JLabel();
        j_l_file_content.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel j_p_buttons = new JPanel();
        j_p_buttons.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_p_buttons.add(j_b_yes);
        j_p_buttons.add(j_b_no);

        if (file_extension.equalsIgnoreCase("txt")) {
            j_l_file_content.setText("<html>" + new String(file_data) + "/html");
        } else {
            j_l_file_content.setIcon(new ImageIcon());
        }

        j_b_yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file_to_download = new File(file_name);
                try {
                    FileOutputStream file_output_stream = new FileOutputStream(file_to_download);
                    file_output_stream.write(file_data);
                    file_output_stream.close();
                    j_frame.dispose();
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        });

        j_b_no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j_frame.dispose();
            }
        });

        j_panel.add(j_l_title);
        j_panel.add(j_l_prompt);
        j_panel.add(j_l_file_content);
        j_panel.add(j_p_buttons);

        j_frame.add(j_panel);
        return j_frame;
    }

    public static String getFileExtension(String file_name) {
        int i = file_name.lastIndexOf(".");
        if (i > 0) {
            return file_name.substring(i + 1);
        } else {
            return "No Extension Found";
        }
    }
}
