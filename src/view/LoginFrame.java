package view;

import controller.AuthController;
import model.User;
import config.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin;
    private JButton btnBatal;
    private AuthController authController;

    private static final Color PRIMARY_BLUE = new Color(37, 99, 235); // #2563EB (Royal Blue)
    private static final Color GRAY_BUTTON = new Color(241, 245, 249); // #F1F5F9 (slate-100)
    private static final Color TEXT_DARK = new Color(15, 23, 42); // #0F172A
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // #64748B
    private static final Color BG_LIGHT = new Color(248, 250, 252); // #F8FAFC

    public LoginFrame() {
        authController = new AuthController();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Login - Sistem Reservasi Bengkel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Background Panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_LIGHT);
        setContentPane(mainPanel);

        // Card Panel Container
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Title: Welcome Back
        gbc.gridy = 0;
        JLabel lblTitle = new JLabel("Welcome Back", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        cardPanel.add(lblTitle, gbc);

        // Subtitle
        gbc.gridy = 1;
        JLabel lblSub = new JLabel("Login ke Sistem Reservasi Bengkel", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_MUTED);
        gbc.insets = new Insets(0, 0, 20, 0); // extra bottom spacing
        cardPanel.add(lblSub, gbc);

        // Reset inset
        gbc.insets = new Insets(6, 0, 6, 0);

        // Label Username
        gbc.gridy = 2;
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(TEXT_DARK);
        cardPanel.add(lblUsername, gbc);

        // Input Username
        gbc.gridy = 3;
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(300, 36));
        txtUsername.putClientProperty("JTextField.placeholderText", "Masukkan username Anda");
        cardPanel.add(txtUsername, gbc);

        // Label Password
        gbc.gridy = 4;
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(TEXT_DARK);
        cardPanel.add(lblPassword, gbc);

        // Input Password
        gbc.gridy = 5;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(300, 36));
        txtPassword.putClientProperty("JTextField.placeholderText", "Masukkan password Anda");
        cardPanel.add(txtPassword, gbc);

        // Show Password Checkbox
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 0, 15, 0);
        chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setBackground(Color.WHITE);
        chkShowPassword.setForeground(TEXT_MUTED);
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chkShowPassword.isSelected()) {
                    txtPassword.setEchoChar((char) 0);
                } else {
                    txtPassword.setEchoChar('•');
                }
            }
        });
        cardPanel.add(chkShowPassword, gbc);

        // Buttons Panel (Login & Batal)
        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 12, 0);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(PRIMARY_BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        btnBatal = new JButton("Reset");
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.setBackground(GRAY_BUTTON);
        btnBatal.setForeground(TEXT_DARK);
        btnBatal.setPreferredSize(new Dimension(140, 38));
        btnBatal.setFocusPainted(false);
        btnBatal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtUsername.setText("");
                txtPassword.setText("");
                chkShowPassword.setSelected(false);
                txtPassword.setEchoChar('•');
            }
        });

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnBatal);
        cardPanel.add(buttonPanel, gbc);

        // Bottom registration panel
        gbc.gridy = 8;
        gbc.insets = new Insets(8, 0, 0, 0);
        JPanel regPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        regPanel.setBackground(Color.WHITE);

        JLabel lblNoAccount = new JLabel("Belum punya akun?");
        lblNoAccount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoAccount.setForeground(TEXT_MUTED);

        JLabel lblRegLink = new JLabel("Daftar Sekarang");
        lblRegLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegLink.setForeground(PRIMARY_BLUE);
        lblRegLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame().setVisible(true);
                dispose();
            }
        });

        regPanel.add(lblNoAccount);
        regPanel.add(lblRegLink);
        cardPanel.add(regPanel, gbc);

        // Add Card to Main Panel
        mainPanel.add(cardPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = authController.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login Berhasil! Selamat datang, " + Session.getNama(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            String role = Session.getRole();
            if ("Pelanggan".equalsIgnoreCase(role)) {
                new PelangganDashboardFrame().setVisible(true);
            } else if ("Admin".equalsIgnoreCase(role)) {
                new AdminDashboardFrame().setVisible(true);
            } else if ("Kepala Bengkel".equalsIgnoreCase(role)) {
                new KepalaBengkelDashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Role '" + role + "' tidak dikenali!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
