import javax.swing.*;
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
                    System.out.println(Address.getHostAddress());

                    ip.add(Address.getHostAddress());
                }
            }

        } catch (SocketException e) {

            e.printStackTrace();

        }
        JList<String> ipList = new JList<>(ip.toArray(new String[0]));

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JLabel ipAddress = new JLabel("Possible IP Address:");
        ipAddress.setBounds(50,20,250,25);
        panel.add(ipAddress);

        JScrollPane scrollPane = new JScrollPane(ipList);
        scrollPane.setBounds(200,23,300,20);
        panel.add(scrollPane);

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
