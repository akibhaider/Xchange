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

public class Client {
    public static void main(String[] args) {
        final File[] file_to_send = new File[1];
        // A variable accessed within an inner class need to be declared as final
        // or use it as a global variable; Final variables can't be changed later
        // Try to function it as an array

        JFrame j_frame=new JFrame("Xchange 1.0 Client");
        j_frame.setSize(500, 500); // 450 * 450
        j_frame.setLayout(new BoxLayout(j_frame.getContentPane(), BoxLayout.Y_AXIS));
        j_frame.setDefaultCloseOperation(j_frame.EXIT_ON_CLOSE);

        JLabel j_l_title = new JLabel("XChange File Sender");
        j_l_title.setFont(new Font("Arial", Font.BOLD, 25));
        j_l_title.setBorder(new EmptyBorder(20, 0, 10, 0));
        j_l_title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel j_l_filename = new JLabel(("Choose a file to send"));
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

                if(j_file_chooser.showOpenDialog(null) == j_file_chooser.APPROVE_OPTION){
                    file_to_send[0] = j_file_chooser.getSelectedFile();
                    j_l_filename.setText("The File ypu want to send is: " + file_to_send[0].getName());
                }
            }
        });

        j_b_send_file.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(file_to_send[0]==null){
                    j_l_filename.setText("Please, Choose a File First");
                }else{
                    try {
                        FileInputStream file_input_stream = new FileInputStream(file_to_send[0].getAbsolutePath()); // File input stream allows access into the files
                        Socket socket = new Socket("192.168.0.18", 1234); // Socket is the way to connect with the server, 1234 is a common port number

                        DataOutputStream data_output_stream = new DataOutputStream(socket.getOutputStream()); // Data Output stream will allow to write into the server

                        String file_name = file_to_send[0].getName();
                        byte[] file_name_byte = file_name.getBytes(); // Sending the file name in a byte array for transmission

                        byte[] file_content_bytes = new byte[(int) file_to_send[0].length()];
                        file_input_stream.read(file_content_bytes); // read the contents of the byte format of file

                        data_output_stream.writeInt(file_name_byte.length); // Firstly, the length of the actual data file is sent (Specifying the length is important; otherwise stream will wait for content if not specified)
                        data_output_stream.write(file_name_byte); // After that we'll send file_name byte information

                        data_output_stream.writeInt(file_content_bytes.length);
                        data_output_stream.write(file_content_bytes);
                    } catch (IOException err){
                        err.printStackTrace();
                    }
                }
            }
        });

        // Adding all components to the Frame to display the components
        j_frame.add(j_l_title);
        j_frame.add(j_l_filename);
        j_frame.add(j_p_Button);
        j_frame.setVisible(true);
    }
}
