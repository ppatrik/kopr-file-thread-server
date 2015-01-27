package sk.upjs.kopr2014.ppatrik.common;

import javax.swing.*;
import java.awt.*;


public abstract class DialogUtils {
    public static void errorDialog(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
