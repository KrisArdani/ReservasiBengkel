package model;

public class Montir {
    private int idMontir;
    private String nama;
    private String keahlian;

    public Montir(int idMontir, String nama, String keahlian) {
        this.idMontir = idMontir;
        this.nama = nama;
        this.keahlian = keahlian;
    }

    public Montir(String nama, String keahlian) {
        this.nama = nama;
        this.keahlian = keahlian;
    }

    // Getters and Setters
    public int getIdMontir() { return idMontir; }
    public void setIdMontir(int idMontir) { this.idMontir = idMontir; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKeahlian() { return keahlian; }
    public void setKeahlian(String keahlian) { this.keahlian = keahlian; }

    @Override
    public String toString() {
        return nama + " (" + keahlian + ")";
    }
}
