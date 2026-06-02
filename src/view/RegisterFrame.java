package view;

import controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterFrame extends JFrame {
    private JTextField txtNama;
    private JTextField txtAlamat;
    private JTextField txtNoHp;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegister;
    private JButton btnBatal;
    private AuthController authController;

    private static final Color NAVY_BLUE = new Color(0, 32, 96); // #002060
    private static final Color GREEN_BUTTON = new Color(40, 167, 69); // #28A745
    private static final Color GRAY_BUTTON = new Color(214, 214, 214); // #D6D6D6

    public RegisterFrame() {
        authController = new AuthController();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Registrasi Pelanggan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(244, 244, 244));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Title: REGISTRASI PELANGGAN
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("REGISTRASI PELANGGAN", JLabel.CENTER);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 24));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.insets = new Insets(10, 8, 20, 8);
        mainPanel.add(lblTitle, gbc);

        // Reset settings
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridwidth = 1;

        // Form Fields
        String[] labels = {
            "Nama Lengkap", "Alamat", "No. Telepon", 
            "Username", "Password", "Konfirmasi Password"
        };
        
        // Nama Lengkap
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        JLabel lblNama = new JLabel("Nama Lengkap");
        lblNama.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblNama, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNama = new JTextField();
        txtNama.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtNama.setPreferredSize(new Dimension(200, 28));
        txtNama.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtNama, gbc);

        // Alamat
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel lblAlamat = new JLabel("Alamat");
        lblAlamat.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblAlamat, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtAlamat = new JTextField();
        txtAlamat.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtAlamat.setPreferredSize(new Dimension(200, 28));
        txtAlamat.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtAlamat, gbc);

        // No. Telepon
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        JLabel lblNoHp = new JLabel("No. Telepon");
        lblNoHp.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblNoHp, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtNoHp = new JTextField();
        txtNoHp.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtNoHp.setPreferredSize(new Dimension(200, 28));
        txtNoHp.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtNoHp, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblUser, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(200, 28));
        txtUsername.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblPass, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(200, 28));
        txtPassword.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtPassword, gbc);

        // Konfirmasi Password
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.35;
        JLabel lblConfirm = new JLabel("Konfirmasi Password");
        lblConfirm.setFont(new Font("Georgia", Font.PLAIN, 14));
        mainPanel.add(lblConfirm, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Georgia", Font.PLAIN, 13));
        txtConfirmPassword.setPreferredSize(new Dimension(200, 28));
        txtConfirmPassword.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        mainPanel.add(txtConfirmPassword, gbc);

        // Buttons Panel (Daftar & Batal)
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 8, 10, 8);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 25, 0));
        buttonPanel.setBackground(new Color(244, 244, 244));

        btnRegister = new JButton("Daftar");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setBackground(GREEN_BUTTON);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(110, 36));
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBatal.setBackground(GRAY_BUTTON);
        btnBatal.setForeground(Color.BLACK);
        btnBatal.setPreferredSize(new Dimension(110, 36));
        btnBatal.setFocusPainted(false);
        btnBatal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnBatal);
        mainPanel.add(buttonPanel, gbc);
    }

    private void handleRegister() {
        String nama = txtNama.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String noHp = txtNoHp.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        String result = authController.register(nama, alamat, noHp, username, password, confirmPassword);

        if ("SUCCESS".equals(result)) {
            JOptionPane.showMessageDialog(this, "Registrasi Sukses! Silakan login.", "Daftar Sukses", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Daftar Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}
