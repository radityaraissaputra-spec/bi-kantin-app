
package id.bikantin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class MenuAdminFrame extends JFrame {
    private JTable tabel;
    private DefaultTableModel model;

    public MenuAdminFrame() {
        setTitle("BI-KANTIN - Kelola Menu");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel(
            new String[]{"ID", "Nama Menu", "Kategori", "Harga", "Stok"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(model);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton tambah = new JButton("Tambah Menu");
        JButton edit = new JButton("Edit Menu");
        JButton hapus = new JButton("Hapus Menu");
        JButton refresh = new JButton("Refresh");

        tambah.addActionListener(e -> tambahMenu());
        edit.addActionListener(e -> editMenu());
        hapus.addActionListener(e -> hapusMenu());
        refresh.addActionListener(e -> muatMenu());

        JPanel tombol = new JPanel(new FlowLayout());
        tombol.add(tambah);
        tombol.add(edit);
        tombol.add(hapus);
        tombol.add(refresh);

        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(tombol, BorderLayout.SOUTH);

        muatMenu();
    }

    private void muatMenu() {
        model.setRowCount(0);

        String sql = "SELECT id_menu, nama_menu, kategori, harga, stok "
                   + "FROM menu_kantin ORDER BY id_menu";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
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

    private void tambahMenu() {
        JTextField nama = new JTextField();
        JTextField kategori = new JTextField();
        JTextField harga = new JTextField();
        JTextField stok = new JTextField();

        Object[] form = {
            "Nama menu:", nama,
            "Kategori:", kategori,
            "Harga:", harga,
            "Stok:", stok
        };

        int hasil = JOptionPane.showConfirmDialog(
            this, form, "Tambah Menu",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (hasil != JOptionPane.OK_OPTION) return;

        try {
            String n = nama.getText().trim();
            String k = kategori.getText().trim();
            BigDecimal h = new BigDecimal(harga.getText().trim());
            int s = Integer.parseInt(stok.getText().trim());

            if (n.isEmpty() || k.isEmpty() ||
                h.compareTo(BigDecimal.ZERO) < 0 || s < 0) {
                throw new IllegalArgumentException(
                    "Isi semua kolom dengan benar. Harga dan stok tidak boleh negatif."
                );
            }

            String sql = "INSERT INTO menu_kantin "
                       + "(nama_menu, kategori, harga, stok) VALUES (?, ?, ?, ?)";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, n);
                ps.setString(2, k);
                ps.setBigDecimal(3, h);
                ps.setInt(4, s);
                ps.executeUpdate();
            }

            muatMenu();
            JOptionPane.showMessageDialog(this, "Menu berhasil ditambahkan.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Harga harus berupa angka dan stok harus bilangan bulat."
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (SQLException e) {
            tampilError("Gagal menambahkan menu", e);
        }
    }

    private void editMenu() {
        int baris = tabel.getSelectedRow();

        if (baris == -1) {
            JOptionPane.showMessageDialog(this, "Pilih menu yang ingin diedit.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(baris, 0).toString());
        JTextField nama = new JTextField(model.getValueAt(baris, 1).toString());
        JTextField kategori = new JTextField(model.getValueAt(baris, 2).toString());
        JTextField harga = new JTextField(model.getValueAt(baris, 3).toString());
        JTextField stok = new JTextField(model.getValueAt(baris, 4).toString());

        Object[] form = {
            "Nama menu:", nama,
            "Kategori:", kategori,
            "Harga:", harga,
            "Stok:", stok
        };

        int hasil = JOptionPane.showConfirmDialog(
            this, form, "Edit Menu",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (hasil != JOptionPane.OK_OPTION) return;

        try {
            String n = nama.getText().trim();
            String k = kategori.getText().trim();
            BigDecimal h = new BigDecimal(harga.getText().trim());
            int s = Integer.parseInt(stok.getText().trim());

            if (n.isEmpty() || k.isEmpty() ||
                h.compareTo(BigDecimal.ZERO) < 0 || s < 0) {
                throw new IllegalArgumentException(
                    "Isi semua kolom dengan benar. Harga dan stok tidak boleh negatif."
                );
            }

            String sql = "UPDATE menu_kantin SET nama_menu=?, kategori=?, "
                       + "harga=?, stok=? WHERE id_menu=?";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, n);
                ps.setString(2, k);
                ps.setBigDecimal(3, h);
                ps.setInt(4, s);
                ps.setInt(5, id);
                ps.executeUpdate();
            }

            muatMenu();
            JOptionPane.showMessageDialog(this, "Menu berhasil diperbarui.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Harga harus berupa angka dan stok harus bilangan bulat."
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (SQLException e) {
            tampilError("Gagal mengedit menu", e);
        }
    }

    private void hapusMenu() {
        int baris = tabel.getSelectedRow();

        if (baris == -1) {
            JOptionPane.showMessageDialog(this, "Pilih menu yang ingin dihapus.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(baris, 0).toString());
        String nama = model.getValueAt(baris, 1).toString();

        int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin menghapus menu " + nama + "?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM menu_kantin WHERE id_menu=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            muatMenu();
            JOptionPane.showMessageDialog(this, "Menu berhasil dihapus.");

        } catch (SQLException e) {
            tampilError(
                "Gagal menghapus menu. Jika menu sudah pernah dipesan, "
                + "data tersebut mungkin masih terhubung dengan pesanan.",
                e
            );
        }
    }

    private void tampilError(String pesan, SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            pesan + ":\n" + e.getMessage(),
            "Kesalahan",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
