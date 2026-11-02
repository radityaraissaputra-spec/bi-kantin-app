
package id.bikantin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class SiswaFrame extends JFrame {

    private final int idAkun;
    private JTable tabelMenu;
    private JTable tabelPesanan;
    private DefaultTableModel modelMenu;
    private DefaultTableModel modelPesanan;

    public SiswaFrame(int id, String nama) {
        idAkun = id;

        setTitle("BI-KANTIN - Dashboard Siswa");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel judul = new JLabel(
            "BI-KANTIN - Halo, " + nama,
            SwingConstants.CENTER
        );
        judul.setFont(new Font("Arial", Font.BOLD, 20));

        modelMenu = new DefaultTableModel(
            new String[]{"ID", "Nama Menu", "Kategori", "Harga", "Stok"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelPesanan = new DefaultTableModel(
            new String[]{"ID Pesanan", "Menu", "Jumlah",
                         "Total Harga", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelMenu = new JTable(modelMenu);
        tabelPesanan = new JTable(modelPesanan);

        JButton pesanButton = new JButton("Pesan Menu");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");

        pesanButton.addActionListener(e -> pesanMenu());
        refreshButton.addActionListener(e -> {
            muatMenu();
            muatPesanan();
        });
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.add(judul, BorderLayout.CENTER);

        JPanel panelTombol = new JPanel();
        panelTombol.add(pesanButton);
        panelTombol.add(refreshButton);
        panelTombol.add(logoutButton);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Daftar Menu", new JScrollPane(tabelMenu));
        tab.addTab("Pesanan Saya", new JScrollPane(tabelPesanan));

        add(panelAtas, BorderLayout.NORTH);
        add(tab, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        muatMenu();
        muatPesanan();
    }

    private void muatMenu() {
        modelMenu.setRowCount(0);

        String sql = "SELECT id_menu, nama_menu, kategori, harga, stok "
                   + "FROM menu_kantin ORDER BY id_menu";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelMenu.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getString("kategori"),
                    rs.getBigDecimal("harga"),
                    rs.getInt("stok")
                });
            }

        } catch (SQLException e) {
            tampilError("Gagal memuat menu", e);
        }
    }

    private void pesanMenu() {
        int baris = tabelMenu.getSelectedRow();

        if (baris == -1) {
            JOptionPane.showMessageDialog(
                this, "Pilih menu yang ingin dipesan terlebih dahulu."
            );
            return;
        }

        int idMenu = Integer.parseInt(
            modelMenu.getValueAt(baris, 0).toString()
        );
        String namaMenu = modelMenu.getValueAt(baris, 1).toString();
        int stokTampil = Integer.parseInt(
            modelMenu.getValueAt(baris, 4).toString()
        );

        if (stokTampil <= 0) {
            JOptionPane.showMessageDialog(this, "Stok menu habis.");
            return;
        }

        String input = JOptionPane.showInputDialog(
            this,
            "Menu: " + namaMenu + "\nStok tersedia: " + stokTampil
                + "\nMasukkan jumlah:",
            "Pesan Menu",
            JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            int jumlah = Integer.parseInt(input.trim());

            if (jumlah <= 0 || jumlah > stokTampil) {
                JOptionPane.showMessageDialog(
                    this, "Jumlah harus lebih dari 0 dan tidak melebihi stok."
                );
                return;
            }

            buatPesanan(idMenu, jumlah);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Jumlah harus berupa bilangan bulat."
            );
        }
    }

    private void buatPesanan(int idMenu, int jumlah) {
        String sqlMenu =
            "SELECT harga, stok FROM menu_kantin "
            + "WHERE id_menu = ? FOR UPDATE";

        String sqlPesanan =
            "INSERT INTO pesanan "
            + "(id_akun, id_menu, jumlah, total_harga, status) "
            + "VALUES (?, ?, ?, ?, ?)";

        String sqlStok =
            "UPDATE menu_kantin SET stok = stok - ? "
            + "WHERE id_menu = ? AND stok >= ?";

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            try {
                BigDecimal harga;
                int stok;

                try (PreparedStatement ps = conn.prepareStatement(sqlMenu)) {
                    ps.setInt(1, idMenu);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Menu tidak ditemukan.");
                        }

                        harga = rs.getBigDecimal("harga");
                        stok = rs.getInt("stok");
                    }
                }

                if (stok < jumlah) {
                    throw new SQLException(
                        "Stok tidak cukup. Silakan refresh daftar menu."
                    );
                }

                BigDecimal total = harga.multiply(BigDecimal.valueOf(jumlah));

                try (PreparedStatement ps = conn.prepareStatement(sqlStok)) {
                    ps.setInt(1, jumlah);
                    ps.setInt(2, idMenu);
                    ps.setInt(3, jumlah);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Stok berubah. Coba pesan ulang.");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlPesanan)) {
                    ps.setInt(1, idAkun);
                    ps.setInt(2, idMenu);
                    ps.setInt(3, jumlah);
                    ps.setBigDecimal(4, total);
                    ps.setString(5, "Menunggu");
                    ps.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(
                    this, "Pesanan berhasil dibuat!\n"
                        + "Total: Rp" + total.toPlainString()
                        + "\nStatus: Menunggu"
                );

                muatMenu();
                muatPesanan();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            tampilError("Pesanan gagal dibuat", e);
        }
    }

    private void muatPesanan() {
        modelPesanan.setRowCount(0);

        String sql =
            "SELECT p.id_pesanan, m.nama_menu, p.jumlah, "
            + "p.total_harga, p.status "
            + "FROM pesanan p "
            + "JOIN menu_kantin m ON p.id_menu = m.id_menu "
            + "WHERE p.id_akun = ? "
            + "ORDER BY p.id_pesanan DESC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAkun);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelPesanan.addRow(new Object[]{
                        rs.getInt("id_pesanan"),
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah"),
                        rs.getBigDecimal("total_harga"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            tampilError("Gagal memuat pesanan", e);
        }
    }

    private void tampilError(String pesan, SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            pesan + ":\n" + e.getMessage(),
            "Kesalahan Database",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
