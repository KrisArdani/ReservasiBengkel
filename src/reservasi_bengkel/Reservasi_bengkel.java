package reservasi_bengkel;

import com.formdev.flatlaf.FlatLightLaf;
import view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Reservasi_bengkel {

    public static void main(String[] args) {
        // 1. Initialize modern FlatLightLaf visual system (Light Mode)
        try {
            FlatLightLaf.setup();
            
            // Adjust global Swing UI styling to match the screenshot designs
            UIManager.put("Button.arc", 8);         // Oblong rounded buttons
            UIManager.put("Component.arc", 8);      // Soft rounded inputs
            UIManager.put("TextComponent.arc", 8);
            
            // Custom light mode color definitions
            UIManager.put("Panel.background", new Color(248, 250, 252)); // Slate-50 background (cleaner than pure light gray)
            UIManager.put("TableHeader.background", Color.WHITE);
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
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
