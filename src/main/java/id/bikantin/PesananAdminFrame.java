package id.bikantin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PesananAdminFrame extends JFrame {

    private JTable tabel;
    private DefaultTableModel model;

    private final String[] pilihanStatus = {
        "Menunggu", "Diproses", "Selesai", "Dibatalkan"
    };

    public PesananAdminFrame() {
        setTitle("BI-KANTIN - Status Pesanan");
        setSize(850, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(
            new String[]{
                "ID Pesanan", "Nama Siswa", "Nama Menu",
                "Jumlah", "Total Harga", "Status"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(model);

        JButton refreshButton = new JButton("Refresh");
        JButton statusButton = new JButton("Ubah Status");

        refreshButton.addActionListener(e -> muatPesanan());
        statusButton.addActionListener(e -> ubahStatus());

        JPanel panelTombol = new JPanel();
        panelTombol.add(refreshButton);
        panelTombol.add(statusButton);

        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        muatPesanan();
    }

    private void muatPesanan() {
        model.setRowCount(0);

        String sql =
            "SELECT p.id_pesanan, a.nama, m.nama_menu, " +
            "p.jumlah, p.total_harga, p.status " +
            "FROM pesanan p " +
            "JOIN akun a ON p.id_akun = a.id_akun " +
            "JOIN menu_kantin m ON p.id_menu = m.id_menu " +
            "ORDER BY p.id_pesanan DESC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_pesanan"),
                    rs.getString("nama"),
                    rs.getString("nama_menu"),
                    rs.getInt("jumlah"),
                    rs.getBigDecimal("total_harga"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal memuat pesanan:\n" + e.getMessage(),
                "Kesalahan Database",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void ubahStatus() {
        int baris = tabel.getSelectedRow();

        if (baris == -1) {
            JOptionPane.showMessageDialog(
                this, "Pilih pesanan yang ingin diubah."
            );
            return;
        }

        int idPesanan = Integer.parseInt(
            model.getValueAt(baris, 0).toString()
        );

        String statusLama =
            model.getValueAt(baris, 5).toString();

        JComboBox<String> comboStatus =
            new JComboBox<>(pilihanStatus);

        for (String status : pilihanStatus) {
            if (status.equalsIgnoreCase(statusLama)) {
                comboStatus.setSelectedItem(status);
                break;
            }
        }

        int hasil = JOptionPane.showConfirmDialog(
            this,
            comboStatus,
            "Pilih Status Pesanan",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (hasil != JOptionPane.OK_OPTION) return;

        String statusBaru =
            comboStatus.getSelectedItem().toString();

        String sql =
            "UPDATE pesanan SET status = ? WHERE id_pesanan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statusBaru);
            ps.setInt(2, idPesanan);

            int berubah = ps.executeUpdate();

            if (berubah > 0) {
                muatPesanan();
                JOptionPane.showMessageDialog(
                    this, "Status pesanan berhasil diubah."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Gagal mengubah status.\n" +
                "Periksa pilihan status pada database.\n" +
                e.getMessage(),
                "Kesalahan Database",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
