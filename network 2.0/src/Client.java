import javax.swing.*;
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

    public Client(String username) {
        this.username = username;
    }

    public void startClient() {
        frame = new JFrame("Client - " + username);
        serverListModel = new DefaultListModel<>();
        serverAddresses = new ArrayList<>();
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);

        JLabel usernameLabel = new JLabel("Username: " + username);
        JList<String> serverList = new JList<>(serverListModel);
        JButton connectButton = new JButton("Connect");
        JButton backButton = new JButton("Back");

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

        sendButton.addActionListener(e -> sends());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel2 = new JPanel(new BorderLayout());
        buttonPanel.add(connectButton, BorderLayout.WEST);
        buttonPanel.add(disconnectButton, BorderLayout.CENTER);
        buttonPanel2.add(sendButton, BorderLayout.CENTER);
        buttonPanel.add(backButton, BorderLayout.EAST);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(usernameLabel, BorderLayout.NORTH);
        frame.add(new JScrollPane(serverList), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(buttonPanel2, BorderLayout.SOUTH);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Executors.newSingleThreadExecutor().execute(this::discoverServers);
    }

    private void discoverServers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                multicastSocket = new MulticastSocket(BROADCAST_PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                multicastSocket.joinGroup(group);

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
                        serverListModel.addElement(serverName + " (" + serverAddress + ")");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void connectToSelectedServer(ActionEvent event) {
        int selectedIndex = ((JList<String>) ((JScrollPane) frame.getContentPane().getComponent(1)).getViewport().getView()).getSelectedIndex();
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
            disconnectButton.setEnabled(true);
            sendButton.setEnabled(true);

            address = serverAddress;

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
        discoverServers();
    }

    public void sends(){
        Sender sender = new Sender(address);
        frame.dispose();
        sender.send();
    }
}
