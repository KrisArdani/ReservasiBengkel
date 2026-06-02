package model;

import java.sql.Date;
import java.sql.Time;

public class Reservasi {
    private int idReservasi;
    private int idPelanggan;
    private int idKendaraan;
    private Integer idMontir; // Integer to allow null
    private Date tanggal;
    private Time jam;
    private String keluhan;
    private String status;

    public Reservasi(int idReservasi, int idPelanggan, int idKendaraan, Integer idMontir, Date tanggal, Time jam, String keluhan, String status) {
        this.idReservasi = idReservasi;
        this.idPelanggan = idPelanggan;
        this.idKendaraan = idKendaraan;
        this.idMontir = idMontir;
        this.tanggal = tanggal;
        this.jam = jam;
        this.keluhan = keluhan;
        this.status = status;
    }

    public Reservasi(int idPelanggan, int idKendaraan, Date tanggal, Time jam, String keluhan) {
        this.idPelanggan = idPelanggan;
        this.idKendaraan = idKendaraan;
        this.tanggal = tanggal;
        this.jam = jam;
        this.keluhan = keluhan;
        this.status = "Menunggu Konfirmasi";
        this.idMontir = null;
    }

    // Getters and Setters
    public int getIdReservasi() { return idReservasi; }
    public void setIdReservasi(int idReservasi) { this.idReservasi = idReservasi; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public int getIdKendaraan() { return idKendaraan; }
    public void setIdKendaraan(int idKendaraan) { this.idKendaraan = idKendaraan; }

    public Integer getIdMontir() { return idMontir; }
    public void setIdMontir(Integer idMontir) { this.idMontir = idMontir; }

    public Date getTanggal() { return tanggal; }
    public void setTanggal(Date tanggal) { this.tanggal = tanggal; }

    public Time getJam() { return jam; }
    public void setJam(Time jam) { this.jam = jam; }

    public String getKeluhan() { return keluhan; }
    public void setKeluhan(String keluhan) { this.keluhan = keluhan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
