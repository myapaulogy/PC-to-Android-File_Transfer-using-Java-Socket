import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class PcGui extends IPServer {
    private static IPServer server;
    public static JTextArea activity;
    public static JTextField portTextField;
    public static JTextField transferTextField;

    public static void main(String[] args) {

        JPanel panel = new JPanel();
        panel.setLayout(null);

        /* Display Local IP Address */
        JTextArea ip = new JTextArea();
        ip.setEditable(false);
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

        /* Display Local Port Address */
        JLabel portDescription = new JLabel("Port Address:");
        portDescription.setBounds(50,65,250,25);
        panel.add(portDescription);

        portTextField = new JTextField();
        portTextField.setText("6969");

        portTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = portTextField.getText();
                // Filter only number and KeyCode 8 is the delete key
                portTextField.setEditable
                        (value.length() < 4 && (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')
                                || ke.getKeyCode() == 8 || ke.getKeyCode() == 37 || ke.getKeyCode() == 39 || ke.getKeyCode() == 40 || ke.getKeyCode() == 38);
            }
        });

        portTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                portTextField.setEditable(true);
            }
        });

        portTextField.setBounds(200,65,70,25);
        panel.add(portTextField);

        /* Display transfer Size */
        JLabel transferDescription = new JLabel("Transfer size (Mb):");
        transferDescription.setBounds(50,100,250,25);
        panel.add(transferDescription);

        transferTextField = new JTextField();
        transferTextField.setText("20");

        transferTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                String value = transferTextField.getText();

                transferTextField.setEditable
                        (ke.getKeyCode() == 8 || ke.getKeyCode() == 37 || ke.getKeyCode() == 39 || ke.getKeyCode() == 40 || ke.getKeyCode() == 38
                                || (value.length() < 4 && (ke.getKeyChar() >= '0' && ke.getKeyChar() <= '9')));
            }
        });

        transferTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                transferTextField.setEditable(true);
            }
        });
        transferTextField.setBounds(200,100,70,25);
        panel.add(transferTextField);

        /* Display App activity */
        activity = new JTextArea();
        JScrollPane activityScrollPane = new JScrollPane(activity);
        activityScrollPane.setBounds(50,140,300,250);
        panel.add(activityScrollPane);

        /* LOUD CheckBox Option */
        Checkbox loudOption = new Checkbox("Verbose");
        loudOption.setState(true);
        loudOption.setBounds(260, 400, 70, 30);
        panel.add(loudOption);

        /* Start Server */
        JButton start = new JButton("Start");
        start.setBounds(50, 400, 70, 30);
        panel.add(start);
        start.addActionListener(v -> {
            if(start.getText().equals("Start")) {
                int port = 420;
                int transferSize = 20;

                if (!(portTextField.getText().isEmpty())) {
                    port = Integer.parseInt(portTextField.getText());
                    if(port == 0){
                        activity.append("Port Cannot Be Zero: Default - 420 \n");
                        port = 420;
                    }
                } else {
                    activity.append("Empty Port: Default - 420 \n");
                }

                if (!(transferTextField.getText().isEmpty())) {
                    transferSize = Integer.parseInt(transferTextField.getText());
                    if(transferSize == 0){
                        activity.append("Transfer Cannot Be Zero: Default - 20 \n");
                        transferSize = 20;
                    }
                } else {
                    activity.append("Empty transfer: Default - 20\n");
                }

                activity.append("----------Connection Starting Up---------");

                activity.append("\nStarting Up\n");
                activity.append("On Port: " + port + "\n");
                activity.append("Transfer Size: " + transferSize + "\n");
                activity.append("Verbose Option: " + loudOption.getState() + "\n\n\n");


                server = new IPServer(port, transferSize, loudOption.getState());
                server.start();

                start.setText("Stop");
            } else {

                server.interrupt();
                activity.append("------------Connection Closed-----------\n\n\n");
                start.setText("Start");
            }
         });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PC to Android");
        frame.setSize(425,500);
        frame.add(panel);
        frame.setVisible(true);
        //frame.setResizable(false);

        frame.addComponentListener(new ComponentAdapter() {

            //When User resizes window manually
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                int y = e.getComponent().getHeight();
                int x = e.getComponent().getWidth();

                activityScrollPane.setBounds(50,140,x - 125,y - 250);
                start.setBounds(50, y - 100, 70, 30);
                loudOption.setBounds(260, y - 100, 70, 30);
            }

            //When User clicked FullScreen
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);

                int y = e.getComponent().getHeight();
                int x = e.getComponent().getWidth();

                activityScrollPane.setBounds(50,140,x - 125,y - 250);
                start.setBounds(50, y - 100, 70, 30);
                loudOption.setBounds(260, y - 100, 70, 30);
            }
        });

    }
}
