import javax.swing.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

public class PcGui extends IPServer{
    public static void main(String[] args) {

        JPanel panel = new JPanel();
        panel.setLayout(null);

        /** Display Local IP Address */
        // Get Possible IP addresses
        JTextArea ip = new JTextArea();
        try {
            Enumeration<NetworkInterface> Interfaces = NetworkInterface.getNetworkInterfaces();
            while(Interfaces.hasMoreElements())
            {
                NetworkInterface Interface = Interfaces.nextElement();
                Enumeration<InetAddress> Addresses = Interface.getInetAddresses();
                while(Addresses.hasMoreElements())
                {
                    InetAddress Address = Addresses.nextElement();

                    //Get only xxx.xxx.xxx.xxx Local Address
                    if(!Address.toString().contains(":")) {
                        System.out.println(Address.getHostAddress());
                        ip.append(Address.getHostAddress() + "\n");
                    }

                }
            }

        } catch (SocketException e) {

            e.printStackTrace();

        }
        JLabel ipDescription = new JLabel("Local IP Addresses:");
        ipDescription.setBounds(50,20,250,25);
        panel.add(ipDescription);

        JScrollPane ipScrollPane = new JScrollPane(ip);
        ipScrollPane.setBounds(200,20,150,35);
        panel.add(ipScrollPane);

        /** Display Local Port Address */
        JLabel portDescription = new JLabel("Port Address:");
        portDescription.setBounds(50,65,250,25);
        panel.add(portDescription);

        JTextField portTextField = new JTextField();
        portTextField.setText("6969");

        portTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = portTextField.getText();

                // Filter only number and KeyCode 8 is the delete key
                portTextField.setEditable
                        (value.length() < 4 && (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')
                                || ke.getKeyCode() == 8);
            }
        });

        portTextField.setBounds(200,65,70,25);
        panel.add(portTextField);

        /** Display transfer Size */
        JLabel transferDescription = new JLabel("Transfer size (Mb):");
        transferDescription.setBounds(50,100,250,25);
        panel.add(transferDescription);

        JTextField transferTextField = new JTextField();
        transferTextField.setText("20");

        transferTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = transferTextField.getText();

                transferTextField.setEditable
                        (ke.getKeyCode() == 8
                                || (value.length() < 4 && (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')));
            }
        });

        transferTextField.setBounds(200,100,70,25);
        panel.add(transferTextField);

        /** Display App activity */
        JTextArea activity = new JTextArea();
        JScrollPane activityScrollPane = new JScrollPane(activity);
        activityScrollPane.setBounds(50,140,300,250);
        panel.add(activityScrollPane);

        JButton start = new JButton("Start");
        start.setBounds(50, 400, 70, 30);
        panel.add(start);
        start.addActionListener(v -> {

            int port = 420;
            int transferSize = 20;
            if(!(portTextField.getText().isEmpty())){
                port = Integer.parseInt(portTextField.getText());
            } else {
                activity.append("Empty Port: Default - 420 \n");
            }

            if(!(transferTextField.getText().isEmpty())) {
                transferSize = Integer.parseInt(transferTextField.getText());
            } else {
                activity.append("Empty transfer: Default - 20\n");
            }

            activity.append("\nStarting Up\n");
            activity.append("On Port: " + port + "\n");
            activity.append("Transfer Size: " + transferSize + "\n\n\n");
         });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PC to Android");
        frame.setSize(425,500);
        frame.add(panel);
        frame.setVisible(true);
        frame.setResizable(false);

    }

    /*
    String[] list = new String[2];
    list[0] = "420";
    list[1] = "3";
    StartService(list);
     */
}
