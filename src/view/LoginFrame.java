package view;

import controller.AuthController;
import model.User;
import config.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    private static final Color NAVY_BLUE = new Color(0, 32, 96); // #002060
    private static final Color GREEN_BUTTON = new Color(40, 167, 69); // #28A745
    private static final Color GRAY_BUTTON = new Color(214, 214, 214); // #D6D6D6

    public LoginFrame() {
        authController = new AuthController();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Login - Sistem Reservasi Bengkel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Container Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(244, 244, 244)); // Off-white/light gray
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Title: LOGIN
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("LOGIN", JLabel.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 42));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.insets = new Insets(10, 8, 30, 8);
        mainPanel.add(lblTitle, gbc);

        // Reset insets
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.gridwidth = 1;

        // Label Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Georgia", Font.PLAIN, 15));
        lblUsername.setForeground(Color.BLACK);
        mainPanel.add(lblUsername, gbc);

        // Input Username
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Georgia", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(200, 32));
        txtUsername.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtUsername, gbc);

        // Label Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Georgia", Font.PLAIN, 15));
        lblPassword.setForeground(Color.BLACK);
        mainPanel.add(lblPassword, gbc);

        // Input Password
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Georgia", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(200, 32));
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtPassword, gbc);

        // Show Password Checkbox
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        chkShowPassword = new JCheckBox("Tampilkan Password");
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.setBackground(new Color(244, 244, 244));
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
        mainPanel.add(chkShowPassword, gbc);

        // Buttons Panel (Login & Batal)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 15, 8);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBackground(new Color(244, 244, 244));

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(GREEN_BUTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(120, 36));
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBatal.setBackground(GRAY_BUTTON);
        btnBatal.setForeground(Color.BLACK);
        btnBatal.setPreferredSize(new Dimension(120, 36));
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
        mainPanel.add(buttonPanel, gbc);

        // Bottom registration panel
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 8, 5, 8);
        JPanel regPanel = new JPanel();
        regPanel.setLayout(new BoxLayout(regPanel, BoxLayout.Y_AXIS));
        regPanel.setBackground(new Color(244, 244, 244));

        JLabel lblNoAccount = new JLabel("Belum punya akun?", JLabel.CENTER);
        lblNoAccount.setFont(new Font("Georgia", Font.PLAIN, 14));
        lblNoAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNoAccount.setForeground(Color.BLACK);

        JLabel lblRegLink = new JLabel("Silakan Registrasi", JLabel.CENTER);
        lblRegLink.setFont(new Font("Georgia", Font.ITALIC | Font.BOLD, 14));
        lblRegLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRegLink.setForeground(NAVY_BLUE);
        lblRegLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new RegisterFrame().setVisible(true);
                dispose();
            }
        });

        regPanel.add(lblNoAccount);
        regPanel.add(Box.createVerticalStrut(5));
        regPanel.add(lblRegLink);
        mainPanel.add(regPanel, gbc);
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
