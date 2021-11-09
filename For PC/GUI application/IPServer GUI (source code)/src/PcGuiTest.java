import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

class PcGuiTest {

    // X Y location of component on Screen You might need to change it for different displays/resolutions
    int[] portLocation = {240, 110};
    int[] transferLocation = {240, 145};

    // Testing array {Test-Input, Expected-Value}
    String[][] portValues = {{"234xx0", "2340"}, {"x1xc","1"}, {"x234xc","234"}, {"x0001","0001"}};
    String[][] transferValues = {{"234xx0", "2340"}, {"x1xc","1"}, {"x234xc","234"}, {"x0001","0001"}};

    @org.junit.jupiter.api.Test
    void main() throws AWTException, InterruptedException {
        System.out.println("main");
        PcGui.main(null);

        /* Test the PORT Text Box */
        System.out.println("\n\nTESTING PORT TEXT BOX\n");
        for (String[] portValue : portValues) {
            componentTest(portLocation[0], portLocation[1], portValue[0], portValue[1], PcGui.portTextField);
            System.out.println();
        }


        /* Test the TRANSFER Text Box */
        System.out.println("\n\nTESTING TRANSFER TEXT BOX\n");
        for (String[] transferValues : transferValues) {
            componentTest(transferLocation[0], transferLocation[1], transferValues[0], transferValues[1], PcGui.transferTextField);
            System.out.println();
        }

        System.out.println("ALL TESTS HAVE PASSED");
    }


    /* Test Port textBox */
    public static void componentTest(int x, int y, String testValue, String CorrectValue, JTextField component) throws AWTException, InterruptedException {
        // Test the Gui with a robot
        Robot bot = new Robot();

        //Move cursor to port text box and click
        bot.mouseMove(x,y);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        //Remove everything from Port TextBox
        remove(4);

        //Type into port
        type(testValue);

        System.out.println("Typed into COMPONENT: " + testValue);
        System.out.println("COMPONENT returned "+ component.getText());

        if (! (CorrectValue.equals(component.getText())) ) {
            fail("COMPONENT Returned a Invalid response");
        } else {
            System.out.println("PASS");
        }
    }

    /* Classes to manipulate keyboard Actions */
    public static void remove(int times) throws AWTException, InterruptedException {
        Robot r = new Robot();
        for (int i = 0; i < times; i++) {
            r.keyPress(8);
            Thread.sleep(100);
        }
    }

    public static void type(String str) throws InterruptedException, AWTException {
        for (int i = 0; i < str.length(); i++){
            typeChar(str.charAt(i));
        }
    }

    public static void typeChar(char X) throws AWTException, InterruptedException {
        Robot r = new Robot();
        r.keyPress(KeyEvent.getExtendedKeyCodeForChar(X));
        Thread.sleep(100);
    }
}