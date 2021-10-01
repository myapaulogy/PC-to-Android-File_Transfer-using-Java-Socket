import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class PcGui extends IPServer{
    public static void main(String[] args) {

        // Get Possible IP addresses
        ArrayList<String> ip = new ArrayList<>();
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
                        ip.add(Address.getHostAddress());
                    }

                }
            }

        } catch (SocketException e) {

            e.printStackTrace();

        }
        JList<String> ipList = new JList<>(ip.toArray(new String[0]));

        JPanel panel = new JPanel();
        panel.setLayout(null);

        //Display Local IP Address
        JLabel ipDescription = new JLabel("Local IP Addresses:");
        ipDescription.setBounds(50,20,250,25);
        panel.add(ipDescription);

        JScrollPane ipScrollPane = new JScrollPane(ipList);
        ipScrollPane.setBounds(200,20,150,40);
        panel.add(ipScrollPane);

        //Display Local Port Address
        JLabel portDescription = new JLabel("Port Address:");
        portDescription.setBounds(50,65,250,25);
        panel.add(portDescription);

        JTextField portTextField = new JTextField();
        portTextField.setText("6969");

        portTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = portTextField.getText();

                //Port should not start with 0
                if(value.length() <= 1 && value.startsWith("0")) {
                    portTextField.setText(null);
                    return;
                }

                // Filter only number and KeyCode 8 is the delete key
                portTextField.setEditable
                        (value.length() < 4 && (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')
                                || ke.getKeyCode() == 8);
            }
        });

        portTextField.setBounds(200,65,70,25);
        panel.add(portTextField);

        JScrollPane activityScrollPane = new JScrollPane();
        activityScrollPane.setBounds(50,100,300,40);
        panel.add(activityScrollPane);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PC to Android");
        frame.setSize(550,500);
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
