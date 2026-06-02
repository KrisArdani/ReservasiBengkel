package model;

public class Pelanggan {
    private int idPelanggan;
    private int idUser;
    private String nama;
    private String alamat;
    private String noHp;

    public Pelanggan(int idPelanggan, int idUser, String nama, String alamat, String noHp) {
        this.idPelanggan = idPelanggan;
        this.idUser = idUser;
        this.nama = nama;
        this.alamat = alamat;
        this.noHp = noHp;
    }

    public Pelanggan(int idUser, String nama, String alamat, String noHp) {
        this.idUser = idUser;
        this.nama = nama;
        this.alamat = alamat;
        this.noHp = noHp;
    }

    // Getters and Setters
    public int getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(int idPelanggan) { this.idPelanggan = idPelanggan; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
}
