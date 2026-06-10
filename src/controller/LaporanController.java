package controller;

import config.Database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LaporanController {

    /**
     * Fetches all completed or active reservations within a date range.
     */
    public List<Object[]> getLaporanData(String dateFromStr, String dateToStr) {
        List<Object[]> data = new ArrayList<>();
        Connection conn = Database.getConnection();
        if (conn == null) return data;

        String query = "SELECT r.id_reservasi, r.tanggal, r.jam, r.keluhan, r.status, " +
                       "p.nama AS nama_pelanggan, p.no_hp, " +
                       "k.merk, k.tipe, k.plat_nomer, " +
                       "m.nama AS nama_montir " +
                       "FROM reservasi r " +
                       "JOIN pelanggan p ON r.id_pelanggan = p.id_pelanggan " +
                       "JOIN kendaraan k ON r.id_kendaraan = k.id_kendaraan " +
                       "LEFT JOIN montir m ON r.id_montir = m.id_montir " +
                       "WHERE r.tanggal BETWEEN ? AND ? " +
                       "ORDER BY r.tanggal ASC, r.jam ASC";

        try {
            Date dateFrom = Date.valueOf(dateFromStr);
            Date dateTo = Date.valueOf(dateToStr);

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDate(1, dateFrom);
                stmt.setDate(2, dateTo);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int idReservasi = rs.getInt("id_reservasi");
                        Date tanggal = rs.getDate("tanggal");
                        Time jam = rs.getTime("jam");
                        String pelanggan = rs.getString("nama_pelanggan");
                        String noHp = rs.getString("no_hp");
                        String kendaraan = rs.getString("merk") + " " + rs.getString("tipe") + " (" + rs.getString("plat_nomer") + ")";
                        String keluhan = rs.getString("keluhan");
                        String montirName = rs.getString("nama_montir");
                        if (montirName == null) montirName = "-";
                        String status = rs.getString("status");

                        data.add(new Object[] {
                            idReservasi,
                            tanggal.toString(),
                            jam.toString().substring(0, 5),
                            pelanggan,
                            noHp,
                            kendaraan,
                            keluhan,
                            montirName,
                            status
                        });
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Format tanggal tidak valid: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Gagal menarik data laporan: " + e.getMessage());
        }

        return data;
    }

    /**
     * Exports a list of rows to a CSV file.
     * Encapsulates values in double quotes and escapes existing double quotes to conform with standard RFC 4180.
     */
    public boolean exportToCSV(String[] headers, List<Object[]> data, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write Headers
            for (int i = 0; i < headers.length; i++) {
                writer.write(escapeCSVField(headers[i]));
                if (i < headers.length - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            // Write Data Row by Row
            for (Object[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    String value = row[i] != null ? row[i].toString() : "";
                    writer.write(escapeCSVField(value));
                    if (i < row.length - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            System.err.println("Gagal mengekspor CSV: " + e.getMessage());
            return false;
        }
    }

    private String escapeCSVField(String field) {
        if (field == null) return "\"\"";
        // Double quotes are escaped as two double quotes
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
