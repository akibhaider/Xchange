import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Client {

    private String username;
    private static final int BROADCAST_PORT = 8888;
    private static final int SERVER_PORT = 9999;
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private String serverAddress;
    public String address;

    private JFrame frame;
    private DefaultListModel<String> serverListModel;
    private List<String> serverAddresses;
    private JButton disconnectButton;
    private JButton sendButton;
    private MulticastSocket multicastSocket;

    public Color darkGray = new Color(45, 45, 45);
    public Color lightGray = new Color(100, 100, 100);

    JPanel contentPanel = new JPanel();

    public Client(String username) {
        this.username = username;
    }

    public void startClient() {
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

        frame = new JFrame("Client - " + username);
        frame.setUndecorated(true);
        frame.setSize(600,400);
        frame.setLayout(new BorderLayout());
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

        serverListModel = new DefaultListModel<>();
        serverAddresses = new ArrayList<>();
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);

        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new BorderLayout());
        usernamePanel.setBackground(lightGray);

        JLabel usernameLabel = new JLabel("Username : " + username);
        usernameLabel.setFont(new Font("Roboto", Font.BOLD, 15));
        usernameLabel.setBorder(new EmptyBorder(10, 20, 10, 0));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setBackground(Color.darkGray);

        JList<String> serverList = new JList<>(serverListModel);
        JButton connectButton = new JButton("Connect");
        styleButton(connectButton,180,50);
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Roboto", Font.BOLD, 12));
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(41, 128, 185));
        backButton.setForeground(Color.WHITE);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adjust padding
        backButton.setPreferredSize(new Dimension(60,30));

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(lightGray);

        JScrollPane scroll_pane = new JScrollPane(serverList);
        serverList.setCellRenderer(new CustomCellRenderer());
        scroll_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll_pane.getVerticalScrollBar().setBackground(lightGray);

        scroll_pane.setPreferredSize(new Dimension(frame.getWidth(), 300));
        scroll_pane.setMaximumSize(new Dimension(frame.getWidth(), 300));

        connectButton.addActionListener(this::connectToSelectedServer);

        backButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame,
                    "Connection will be terminated. Are you sure you want to go to the mainframe?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                frame.dispose();
                disconnect();
                new MainFrame().showMainFrame();
            }
        });

        disconnectButton.addActionListener(e -> disconnect());
        styleButton(disconnectButton, 180,50);
        disconnectButton.setBackground(new Color(169,46,34));

        sendButton.addActionListener(e -> sends());
        styleButton(sendButton,100,50);

        usernamePanel.add(usernameLabel, BorderLayout.CENTER);
        usernamePanel.add(backButton, BorderLayout.EAST);
        usernamePanel.setPreferredSize(new Dimension(frame.getWidth(),40));
        usernamePanel.setMaximumSize(new Dimension(frame.getWidth(),40));
        //usernamePanel.setMinimumSize(new Dimension(frame.getWidth(),50));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel2 = new JPanel(new BorderLayout());
        buttonPanel.setPreferredSize(new Dimension(frame.getWidth(),40));
        buttonPanel.setMaximumSize(new Dimension(frame.getWidth(),40));
        buttonPanel2.setPreferredSize(new Dimension(frame.getWidth(),40));
        buttonPanel2.setMaximumSize(new Dimension(frame.getWidth(),40));
        buttonPanel.setBackground(darkGray);
        buttonPanel2.setBackground(darkGray);

        buttonPanel.add(connectButton, BorderLayout.WEST);
        buttonPanel.add(disconnectButton, BorderLayout.EAST);
        buttonPanel2.add(sendButton, BorderLayout.CENTER);
        //buttonPanel.add(backButton, BorderLayout.EAST);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(titleBar, BorderLayout.NORTH);
        frame.add(usernamePanel, BorderLayout.NORTH);
        frame.add(scroll_pane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(buttonPanel2, BorderLayout.SOUTH);
        //frame.setSize(400, 300);
       // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Executors.newSingleThreadExecutor().execute(this::discoverServers);
    }

    private void discoverServers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                multicastSocket = new MulticastSocket(BROADCAST_PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                multicastSocket.joinGroup(group);

                int i=1;
                byte[] buf = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        multicastSocket.receive(packet);
                    } catch (SocketException e) {
                        // Socket closed, exit loop
                        break;
                    }
                    String serverName = new String(packet.getData(), 0, packet.getLength());
                    serverAddress = packet.getAddress().getHostAddress();

                    // Skip own server broadcasts and already discovered servers
                    if (!serverAddresses.contains(serverAddress) && !serverAddress.equals(InetAddress.getLocalHost().getHostAddress())) {

                        serverAddresses.add(serverAddress);
                        serverListModel.addElement(i + ". " + serverName + " (" + serverAddress + ")");
                        i++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectToSelectedServer(ActionEvent event) {
        int selectedIndex = ((JList<String>) ((JScrollPane) frame.getContentPane().getComponent(2)).getViewport().getView()).getSelectedIndex();
        if (selectedIndex >= 0) {
            String selectedServer = serverAddresses.get(selectedIndex);
            Executors.newSingleThreadExecutor().execute(() -> sendConnectionRequest(selectedServer));
        }
    }

    private void sendConnectionRequest(String serverAddress) {
        try (Socket socket = new Socket(serverAddress, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(username);
            String response = in.readLine();
            JOptionPane.showMessageDialog(frame, response);
            if(response.equals("Connection Successful")){
                disconnectButton.setEnabled(true);
                sendButton.setEnabled(true);
            }
            else{
                disconnectButton.setEnabled(false);
                sendButton.setEnabled(false);
            }

            this.address = serverAddress;

            System.out.println(serverAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (multicastSocket != null) {
            try {
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverListModel.clear();
        serverAddresses.clear();
        disconnectButton.setEnabled(false);
        discoverServers(); // Restart discovery after disconnect
    }

    public void sends() {
        Sender sender = new Sender(address);
        frame.dispose(); // Dispose of the current frame
        sender.send(); // Start the sender
    }

    private void styleButton(JButton button, int width, int height) {
        button.setFont(new Font("Roboto", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 13, 20)); // Adjust padding
        button.setPreferredSize(new Dimension(width, height)); // Set preferred size
    }

    class CustomCellRenderer extends DefaultListCellRenderer {
        private static final Font CUSTOM_FONT = new Font("Roboto", Font.BOLD, 14);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(CUSTOM_FONT);
            label.setBorder(new EmptyBorder(5,5,5,0));

            if (isSelected) {
                label.setBackground(new Color(104, 93, 156));
                label.setForeground(new Color(255, 255, 255));
            } else {
                label.setBackground(darkGray);
                label.setForeground(Color.WHITE);
            }

            return label;
        }
    }
}
