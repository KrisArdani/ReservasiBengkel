package model;

public class Kendaraan {
    private int idKendaraan;
    private int idPelanggan;
    private String merk;
    private String tipe;
    private String platNomer;

    public Kendaraan(int idKendaraan, int idPelanggan, String merk, String tipe, String platNomer) {
        this.idKendaraan = idKendaraan;
        this.idPelanggan = idPelanggan;
        this.merk = merk;
        this.tipe = tipe;
        this.platNomer = platNomer;
    }

    public Kendaraan(int idPelanggan, String merk, String tipe, String platNomer) {
        this.idPelanggan = idPelanggan;
        this.merk = merk;
        this.tipe = tipe;
        this.platNomer = platNomer;
    }

    // Getters and Setters
    public int getIdKendaraan() { return idKendaraan; }
    public void setIdKendaraan(int idKendaraan) { this.idKendaraan = idKendaraan; }

    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public String getMerk() { return merk; }
    public void setMerk(String merk) { this.merk = merk; }

    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }

    public String getPlatNomer() { return platNomer; }
    public void setPlatNomer(String platNomer) { this.platNomer = platNomer; }
}
