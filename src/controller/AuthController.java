package controller;

import config.Database;
import config.Session;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthController {

    /**
     * Registers a new customer in both user and pelanggan tables.
     * Returns "SUCCESS" on success, or an error message on validation failure.
     */
    public String register(String nama, String alamat, String noHp, String username, String password, String confirmPassword) {
        // 1. Check for empty fields
        if (nama.trim().isEmpty() || alamat.trim().isEmpty() || noHp.trim().isEmpty() ||
            username.trim().isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            return "Semua field harus diisi!";
        }

        // 2. Validate passwords match
        if (!password.equals(confirmPassword)) {
            return "Password dan Konfirmasi Password tidak cocok!";
        }

        Connection conn = Database.getConnection();
        if (conn == null) {
            return "Koneksi database gagal!";
        }

        try {
            // Disable auto-commit to run both inserts in a single transaction
            conn.setAutoCommit(false);

            // 3. Check if username already exists
            String checkQuery = "SELECT id_user FROM user WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        return "Username '" + username + "' sudah terdaftar!";
                    }
                }
            }

            // 4. Insert into 'user' table
            String insertUserQuery = "INSERT INTO user (username, password, role) VALUES (?, ?, 'Pelanggan')";
            int generatedUserId = -1;
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password); // Stored in plain text or hash as desired. Using plain text as per PRD specs.
                userStmt.executeUpdate();

                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedUserId = generatedKeys.getInt(1);
                    }
                }
            }

            if (generatedUserId == -1) {
                conn.rollback();
                return "Gagal mendapatkan ID User baru!";
            }

            // 5. Insert into 'pelanggan' table
            String insertPelangganQuery = "INSERT INTO pelanggan (id_user, nama, alamat, no_hp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pelangganStmt = conn.prepareStatement(insertPelangganQuery)) {
                pelangganStmt.setInt(1, generatedUserId);
                pelangganStmt.setString(2, nama);
                pelangganStmt.setString(3, alamat);
                pelangganStmt.setString(4, noHp);
                pelangganStmt.executeUpdate();
            }

            // Commit the transaction
            conn.commit();
            conn.setAutoCommit(true);
            return "SUCCESS";

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            return "Database Error: " + e.getMessage();
        }
    }

    /**
     * Autentikasi / Login.
     * Returns User object on success, or null on failure.
     */
    public User login(String username, String password) {
        if (username.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        Connection conn = Database.getConnection();
        if (conn == null) {
            return null;
        }

        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idUser = rs.getInt("id_user");
                    String role = rs.getString("role");
                    
                    int idPelanggan = 0;
                    String nama = "";

                    // If user is a customer, load customer-specific info (id_pelanggan, nama)
                    if ("Pelanggan".equalsIgnoreCase(role)) {
                        String pelangganQuery = "SELECT id_pelanggan, nama FROM pelanggan WHERE id_user = ?";
                        try (PreparedStatement pelangganStmt = conn.prepareStatement(pelangganQuery)) {
                            pelangganStmt.setInt(1, idUser);
                            try (ResultSet rsPelanggan = pelangganStmt.executeQuery()) {
                                if (rsPelanggan.next()) {
                                    idPelanggan = rsPelanggan.getInt("id_pelanggan");
                                    nama = rsPelanggan.getString("nama");
                                }
                            }
                        }
                    } else {
                        // For Admin or Kepala Bengkel, full name can just be the role or username
                        nama = role;
                    }

                    // Start global session
                    Session.startSession(idUser, username, role, idPelanggan, nama);

                    return new User(idUser, username, password, role);
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return null;
    }
}
