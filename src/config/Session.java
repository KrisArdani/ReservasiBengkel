package config;

public class Session {
    private static int idUser;
    private static String username;
    private static String role;
    private static int idPelanggan; // Only for Pelanggan role
    private static String nama;

    public static void startSession(int userId, String user, String userRole, int pelangganId, String fullName) {
        idUser = userId;
        username = user;
        role = userRole;
        idPelanggan = pelangganId;
        nama = fullName;
    }

    public static void clearSession() {
        idUser = 0;
        username = null;
        role = null;
        idPelanggan = 0;
        nama = null;
    }

    // Getters
    public static int getIdUser() { return idUser; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static int getIdPelanggan() { return idPelanggan; }
    public static String getNama() { return nama; }
}
