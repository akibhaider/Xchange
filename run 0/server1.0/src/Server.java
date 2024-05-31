import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    static ArrayList<MyFile> my_files = new ArrayList<>();

    public static void main(String[] args) throws IOException {
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
