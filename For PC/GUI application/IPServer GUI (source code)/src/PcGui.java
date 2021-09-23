import javax.swing.*;
import java.awt.*;

public class PcGui extends IPServer{
    public static void main(String[] args) {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setLayout(new GridLayout());

        JFrame frame = new JFrame();
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("PC to Android");
        frame.pack();
        frame.setVisible(true);

    }
    /*
    String[] list = new String[2];
    list[0] = "420";
    list[1] = "3";
    StartService(list);
     */
}
