
package id.bikantin;

import javax.swing.SwingUtilities;

public class BIKANTINAPP {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
