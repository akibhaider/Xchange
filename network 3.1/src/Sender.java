import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class Sender {

    public static String ipAdd;
    public int port;
    public static String username;

    public Sender(String ip) {
        this.ipAdd = ip;
    }
    public static void send() {
        final File[] file_to_send = new File[1];

        JFrame j_frame = new JFrame("Xchange 1.0 Client");
        j_frame.setSize(500, 500);
        j_frame.setLayout(new BoxLayout(j_frame.getContentPane(), BoxLayout.Y_AXIS));
        j_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel j_l_title = new JLabel("XChange File Sender");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel j_l_filename = new JLabel("Choose a file to send");
        j_l_filename.setFont(new Font("Arial", Font.BOLD, 20));
        j_l_filename.setBorder(new EmptyBorder(40, 0, 10, 0));
        j_l_filename.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel j_p_Button = new JPanel();
        j_p_Button.setBorder(new EmptyBorder(60, 0, 10, 0));

        JButton j_b_send_file = new JButton("Send File");
        j_b_send_file.setPreferredSize(new Dimension(150, 75));
        j_b_send_file.setFont(new Font("Arial", Font.BOLD, 20));

        JButton j_b_choose_file = new JButton("Choose File");
        j_b_choose_file.setPreferredSize(new Dimension(150, 75));
        j_b_choose_file.setFont(new Font("Arial", Font.BOLD, 20));

        j_p_Button.add(j_b_send_file);
        j_p_Button.add(j_b_choose_file);

        j_b_choose_file.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser j_file_chooser = new JFileChooser();
                j_file_chooser.setDialogTitle("Choose a File to send");

                if (j_file_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    file_to_send[0] = j_file_chooser.getSelectedFile();
                    j_l_filename.setText("The File you want to send is: " + file_to_send[0].getName());
                }
            }
        });

        j_b_send_file.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file_to_send[0] == null) {
                    j_l_filename.setText("Please, Choose a File First");
                } else {
                    JFrame progress_frame = new JFrame("Sending File");
                    progress_frame.setSize(400, 150);
                    JProgressBar j_progress_bar = new JProgressBar(0, (int) file_to_send[0].length());
                    JLabel j_l_speed = new JLabel("Speed: 0 B/s");
                    progress_frame.setLayout(new BoxLayout(progress_frame.getContentPane(), BoxLayout.Y_AXIS));
                    progress_frame.add(j_progress_bar);
                    progress_frame.add(j_l_speed);
                    progress_frame.setVisible(true);

                    new Thread(() -> {
                        try {
                            FileInputStream file_input_stream = new FileInputStream(file_to_send[0].getAbsolutePath());
                            Socket socket = new Socket(ipAdd, 1234);

                            DataOutputStream data_output_stream = new DataOutputStream(socket.getOutputStream());

                            String file_name = file_to_send[0].getName();
                            byte[] file_name_byte = file_name.getBytes();

                            data_output_stream.writeInt(file_name_byte.length);
                            data_output_stream.write(file_name_byte);
                            data_output_stream.writeInt((int) file_to_send[0].length());

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            long totalBytesRead = 0;
                            long startTime = System.currentTimeMillis();

                            while ((bytesRead = file_input_stream.read(buffer)) != -1) {
                                data_output_stream.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;

                                // Update progress bar and speed label on the EDT
                                long finalTotalBytesRead = totalBytesRead;
                                SwingUtilities.invokeLater(() -> {
                                    j_progress_bar.setValue((int) finalTotalBytesRead);
                                    long currentTime = System.currentTimeMillis();
                                    double elapsedTime = (currentTime - startTime) / 1000.0; // seconds
                                    double speed = (finalTotalBytesRead / 1024.0) / elapsedTime; // KB per second
                                    j_l_speed.setText(String.format("Speed: %.2f KB/s", speed));
                                });
                            }

                            file_input_stream.close();
                            data_output_stream.close();
                            socket.close();
                            progress_frame.dispose();

                            System.out.println("Sent file");
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    }).start();
                }
            }
        });

        j_frame.add(j_l_title);
        j_frame.add(j_l_filename);
        j_frame.add(j_p_Button);
        j_frame.setVisible(true);
    }
}
