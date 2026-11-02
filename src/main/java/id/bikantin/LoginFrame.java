package id.bikantin;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("BI-KANTIN | Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel title = new JLabel("BI-KANTIN");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("LOGIN");

        panel.add(title);
        panel.add(new JLabel("Silakan Masuk"));
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(
            passwordField.getPassword()
        );

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Username dan password wajib diisi!"
            );
            return;
        }

        String sql =
            "SELECT id_akun, nama, role FROM akun " +
            "WHERE username = ? " +
            "AND password_hash = SHA2(?, 256)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_akun");
                    String nama = rs.getString("nama");
                    String role = rs.getString("role");

                    JOptionPane.showMessageDialog(
                        this, "Login berhasil! Halo, " + nama
                    );

                    if (role.equalsIgnoreCase("admin")) {
                        new AdminFrame(id, nama).setVisible(true);
                    } else {
                        new SiswaFrame(id, nama).setVisible(true);
                    }

                    dispose();

                } else {
                    JOptionPane.showMessageDialog(
                        this, "Username atau password salah!"
                    );
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Koneksi database gagal:\n" + ex.getMessage()
            );
        }
    }
}
