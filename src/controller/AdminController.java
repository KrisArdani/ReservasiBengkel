package controller;

import config.Database;
import model.Montir;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    /**
     * Fetches all reservations in the system with joined customer, vehicle, and mechanic details.
     * Support search keyword (matches customer name or plate number or vehicle brand).
     */
    public List<Object[]> getAllReservasi(String keyword) {
        List<Object[]> data = new ArrayList<>();
        Connection conn = Database.getConnection();
        if (conn == null) return data;

        StringBuilder queryBuilder = new StringBuilder(
            "SELECT r.id_reservasi, r.tanggal, r.jam, r.keluhan, r.status, " +
            "p.nama AS nama_pelanggan, p.no_hp, " +
            "k.merk, k.tipe, k.plat_nomer, " +
            "m.id_montir, m.nama AS nama_montir " +
            "FROM reservasi r " +
            "JOIN pelanggan p ON r.id_pelanggan = p.id_pelanggan " +
            "JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
            "LEFT JOIN montir m ON r.id_montir = m.id_montir "
        );

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            queryBuilder.append("WHERE p.nama LIKE ? OR k.plat_nomer LIKE ? OR k.merk LIKE ? ");
        }
        queryBuilder.append("ORDER BY r.tanggal DESC, r.jam DESC");

        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            if (hasKeyword) {
                String searchPattern = "%" + keyword.trim() + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idReservasi = rs.getInt("id_reservasi");
                    Date tanggal = rs.getDate("tanggal");
                    Time jam = rs.getTime("jam");
                    String keluhan = rs.getString("keluhan");
                    String status = rs.getString("status");
                    String pelangganInfo = rs.getString("nama_pelanggan") + " (" + rs.getString("no_hp") + ")";
                    String kendaraanInfo = rs.getString("merk") + " " + rs.getString("tipe") + " (" + rs.getString("plat_nomer") + ")";
                    
                    int idMontir = rs.getInt("id_montir");
                    String montirName = rs.getString("nama_montir");
                    if (rs.wasNull()) {
                        montirName = "- Belum Ditentukan -";
                    }

                    data.add(new Object[] {
                        idReservasi,
                        pelangganInfo,
                        kendaraanInfo,
                        tanggal.toString(),
                        jam.toString().substring(0, 5),
                        keluhan,
                        montirName,
                        status
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal menarik data reservasi admin: " + e.getMessage());
        }

        return data;
    }

    /**
     * Overload for getting all reservations without filtering.
     */
    public List<Object[]> getAllReservasi() {
        return getAllReservasi(null);
    }

    /**
     * Updates only the status of a specific reservation.
     */
    public boolean updateStatus(int idReservasi, String status) {
        Connection conn = Database.getConnection();
        if (conn == null) return false;

        String query = "UPDATE reservasi SET status = ? WHERE id_reservasi = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, idReservasi);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal update status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the complete list of technicians/montirs from the database.
     */
    public List<Montir> getMontirList() {
        List<Montir> list = new ArrayList<>();
        Connection conn = Database.getConnection();
        if (conn == null) return list;

        String query = "SELECT * FROM montir ORDER BY nama ASC";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Montir(
                    rs.getInt("id_montir"),
                    rs.getString("nama"),
                    rs.getString("keahlian")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data montir: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates/Assigns scheduling details (technician, date, time) to a reservation.
     */
    public String assignMontirAndSchedule(int idReservasi, int idMontir, String tanggalStr, String jamStr) {
        if (tanggalStr.trim().isEmpty() || jamStr.trim().isEmpty()) {
            return "Tanggal dan Jam harus diisi!";
        }

        Connection conn = Database.getConnection();
        if (conn == null) return "Koneksi database gagal!";

        String query = "UPDATE reservasi SET id_montir = ?, tanggal = ?, jam = ? WHERE id_reservasi = ?";
        try {
            Date sqlDate = Date.valueOf(tanggalStr);
            Time sqlTime = Time.valueOf(jamStr + ":00");

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, idMontir);
                stmt.setDate(2, sqlDate);
                stmt.setTime(3, sqlTime);
                stmt.setInt(4, idReservasi);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    return "SUCCESS";
                } else {
                    return "Gagal memperbarui jadwal, Reservasi tidak ditemukan!";
                }
            }
        } catch (IllegalArgumentException e) {
            return "Format Tanggal (YYYY-MM-DD) atau Jam (HH:MM) tidak valid!";
        } catch (SQLException e) {
            return "Gagal update jadwal/montir: " + e.getMessage();
        }
    }
}
