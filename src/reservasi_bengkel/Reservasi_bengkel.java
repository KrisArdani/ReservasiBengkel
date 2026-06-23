package reservasi_bengkel;

import com.formdev.flatlaf.FlatDarkLaf;
import view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Reservasi_bengkel {

    public static void main(String[] args) {
        // 1. Initialize modern FlatDarkLaf visual system (Dark Mode)
        try {
            FlatDarkLaf.setup();
            
            // Adjust global Swing UI styling to match the screenshot designs
            UIManager.put("Button.arc", 8);         // Oblong rounded buttons
            UIManager.put("Component.arc", 8);      // Soft rounded inputs
            UIManager.put("TextComponent.arc", 8);
            
            // Custom dark mode color definitions
            UIManager.put("Panel.background", new Color(18, 18, 18)); // Matte black background
            UIManager.put("TableHeader.background", new Color(38, 38, 38));
            UIManager.put("Table.background", new Color(30, 30, 30));
            UIManager.put("Table.alternateRowColor", new Color(26, 26, 26));
            UIManager.put("TableHeader.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            UIManager.put("Table.rowHeight", 32);
            UIManager.put("Table.showVerticalLines", false);
            
            System.out.println("Gaya visual FlatLightLaf berhasil dimuat!");
        } catch (Exception ex) {
            System.err.println("Gagal memuat tema FlatLaf. Menggunakan tema default Java.");
        }

        // 2. Open the fully database-functional Login Frame on the thread-safe GUI dispatcher
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
