package id.bikantin;

import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {

    public AdminFrame(int id, String nama) {
        setTitle("BI-KANTIN - Dashboard Admin");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel judul = new JLabel(
            "Dashboard Admin - Halo, " + nama,
            SwingConstants.CENTER
        );

        judul.setFont(new Font("Arial", Font.BOLD, 20));

        JButton menuButton = new JButton("Kelola Menu");
        JButton logoutButton = new JButton("Logout");
        JButton pesananButton = new JButton("Status Pesanan");

pesananButton.addActionListener(e ->
    new PesananAdminFrame().setVisible(true)
);

       menuButton.addActionListener(e ->
    new MenuAdminFrame().setVisible(true)
);

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel tombol = new JPanel();
       tombol.add(menuButton);
tombol.add(pesananButton);
tombol.add(logoutButton);

        add(judul, BorderLayout.CENTER);
        add(tombol, BorderLayout.SOUTH);
    }
}
