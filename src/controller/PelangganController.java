package controller;

import config.Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PelangganController {

    /**
     * Gets or creates a vehicle in the database.
     * Checks if a vehicle with the given plat_nomer already exists.
     * If not, inserts it under the current customer's profile.
     * Returns the id_kendaraan, or -1 on database error.
     */
    public int getOrCreateKendaraan(int idPelanggan, String merk, String tipe, String platNomer) throws SQLException {
        Connection conn = Database.getConnection();
        if (conn == null) {
            throw new SQLException("Koneksi database gagal!");
        }

        // 1. Check if vehicle plate is already registered
        String checkQuery = "SELECT id_kendaraan, id_pelanggan FROM kendaraan WHERE plat_nomer = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, platNomer.trim().toUpperCase());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int existingId = rs.getInt("id_kendaraan");
                    int ownerId = rs.getInt("id_pelanggan");
                    
                    if (ownerId == idPelanggan) {
                        return existingId; // Already owned by this customer
                    } else {
                        // Plate belongs to someone else, but since plat_nomer is UNIQUE, we must return the existing ID or raise exception.
                        // For a real-world scenario, we'll let them know, or reuse it. Let's return the existing ID.
                        return existingId;
                    }
                }
            }
        }

        // 2. If it does not exist, insert it
        String insertQuery = "INSERT INTO kendaraan (id_pelanggan, merk, tipe, plat_nomer) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, idPelanggan);
            insertStmt.setString(2, merk.trim());
            insertStmt.setString(3, tipe.trim());
            insertStmt.setString(4, platNomer.trim().toUpperCase());
            insertStmt.executeUpdate();

            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Creates a new booking/reservation.
     * Returns "SUCCESS" or an error message.
     */
    public String createReservasi(int idPelanggan, String merk, String tipe, String platNomer, String tanggalStr, String jamStr, String keluhan) {
        if (merk.trim().isEmpty() || tipe.trim().isEmpty() || platNomer.trim().isEmpty() ||
            tanggalStr.trim().isEmpty() || jamStr.trim().isEmpty() || keluhan.trim().isEmpty()) {
            return "Semua field input harus diisi!";
        }

        Connection conn = Database.getConnection();
        if (conn == null) {
            return "Koneksi database gagal!";
        }

        try {
            conn.setAutoCommit(false);

            // 1. Get or create vehicle
            int idKendaraan = getOrCreateKendaraan(idPelanggan, merk, tipe, platNomer);
            if (idKendaraan == -1) {
                conn.rollback();
                conn.setAutoCommit(true);
                return "Gagal memproses data kendaraan!";
            }

            // 2. Parse Date and Time
            Date sqlDate = Date.valueOf(tanggalStr); // YYYY-MM-DD
            Time sqlTime = Time.valueOf(jamStr + ":00"); // HH:MM:SS

            // 3. Create Reservation
            String insertQuery = "INSERT INTO reservasi (id_pelanggan, id_kendaraan, id_montir, tanggal, jam, keluhan, status) " +
                                 "VALUES (?, ?, NULL, ?, ?, ?, 'Menunggu Konfirmasi')";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, idPelanggan);
                stmt.setInt(2, idKendaraan);
                stmt.setDate(3, sqlDate);
                stmt.setTime(4, sqlTime);
                stmt.setString(5, keluhan.trim());
                stmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return "SUCCESS";

        } catch (IllegalArgumentException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return "Format Tanggal (YYYY-MM-DD) atau Jam (HH:MM) tidak valid!";
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            return "Gagal membuat reservasi: " + e.getMessage();
        }
    }

    /**
     * Fetches all reservation history for the logged-in customer.
     * Returns List of rows where each row contains:
     * [id_reservasi, tanggal, jam, kendaraan_info, keluhan, montir_name, status]
     */
    public List<Object[]> getHistoryReservasi(int idPelanggan) {
        List<Object[]> data = new ArrayList<>();
        Connection conn = Database.getConnection();
        if (conn == null) return data;

        String query = "SELECT r.id_reservasi, r.tanggal, r.jam, r.keluhan, r.status, " +
                       "k.merk, k.tipe, k.plat_nomer, m.nama AS nama_montir " +
                       "FROM reservasi r " +
                       "JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
                       "LEFT JOIN montir m ON r.id_montir = m.id_montir " +
                       "WHERE r.id_pelanggan = ? " +
                       "ORDER BY r.tanggal DESC, r.jam DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idPelanggan);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idReservasi = rs.getInt("id_reservasi");
                    Date tanggal = rs.getDate("tanggal");
                    Time jam = rs.getTime("jam");
                    String kendaraanInfo = rs.getString("merk") + " " + rs.getString("tipe") + " (" + rs.getString("plat_nomer") + ")";
                    String keluhan = rs.getString("keluhan");
                    String montirName = rs.getString("nama_montir");
                    if (montirName == null) montirName = "- Belum Ditentukan -";
                    String status = rs.getString("status");

                    data.add(new Object[] {
                        idReservasi,
                        tanggal.toString(),
                        jam.toString().substring(0, 5), // HH:MM
                        kendaraanInfo,
                        keluhan,
                        montirName,
                        status
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal menarik riwayat reservasi: " + e.getMessage());
        }

        return data;
    }
}
