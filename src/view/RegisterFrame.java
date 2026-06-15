package view;

import controller.AuthController;

import javax.swing.*;
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

    private static final Color PRIMARY_BLUE = new Color(37, 99, 235); // #2563EB (Royal Blue)
    private static final Color GRAY_BUTTON = new Color(241, 245, 249); // #F1F5F9 (slate-100)
    private static final Color TEXT_DARK = new Color(15, 23, 42); // #0F172A
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // #64748B
    private static final Color BG_LIGHT = new Color(248, 250, 252); // #F8FAFC

    public RegisterFrame() {
        authController = new AuthController();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Registrasi Pelanggan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 680);
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
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.gridx = 0;

        // Title: REGISTRASI
        gbc.gridy = 0;
        JLabel lblTitle = new JLabel("Registrasi Pelanggan", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);
        cardPanel.add(lblTitle, gbc);

        // Subtitle
        gbc.gridy = 1;
        JLabel lblSub = new JLabel("Silakan lengkapi data diri Anda di bawah ini", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);
        gbc.insets = new Insets(0, 0, 12, 0); // extra bottom spacing
        cardPanel.add(lblSub, gbc);

        // Reset inset
        gbc.insets = new Insets(4, 0, 4, 0);

        // Nama Lengkap
        gbc.gridy = 2;
        JLabel lblNama = new JLabel("Nama Lengkap");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNama.setForeground(TEXT_DARK);
        cardPanel.add(lblNama, gbc);

        gbc.gridy = 3;
        txtNama = new JTextField();
        txtNama.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNama.setPreferredSize(new Dimension(340, 32));
        txtNama.putClientProperty("JTextField.placeholderText", "Nama Lengkap Anda");
        cardPanel.add(txtNama, gbc);

        // Alamat
        gbc.gridy = 4;
        JLabel lblAlamat = new JLabel("Alamat");
        lblAlamat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAlamat.setForeground(TEXT_DARK);
        cardPanel.add(lblAlamat, gbc);

        gbc.gridy = 5;
        txtAlamat = new JTextField();
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlamat.setPreferredSize(new Dimension(340, 32));
        txtAlamat.putClientProperty("JTextField.placeholderText", "Alamat Tinggal Anda");
        cardPanel.add(txtAlamat, gbc);

        // No. Telepon
        gbc.gridy = 6;
        JLabel lblNoHp = new JLabel("No. Telepon");
        lblNoHp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNoHp.setForeground(TEXT_DARK);
        cardPanel.add(lblNoHp, gbc);

        gbc.gridy = 7;
        txtNoHp = new JTextField();
        txtNoHp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNoHp.setPreferredSize(new Dimension(340, 32));
        txtNoHp.putClientProperty("JTextField.placeholderText", "Contoh: 081234567890");
        cardPanel.add(txtNoHp, gbc);

        // Username
        gbc.gridy = 8;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(TEXT_DARK);
        cardPanel.add(lblUser, gbc);

        gbc.gridy = 9;
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(340, 32));
        txtUsername.putClientProperty("JTextField.placeholderText", "Pilih Username");
        cardPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 10;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_DARK);
        cardPanel.add(lblPass, gbc);

        gbc.gridy = 11;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(340, 32));
        txtPassword.putClientProperty("JTextField.placeholderText", "Buat Password");
        cardPanel.add(txtPassword, gbc);

        // Konfirmasi Password
        gbc.gridy = 12;
        JLabel lblConfirm = new JLabel("Konfirmasi Password");
        lblConfirm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblConfirm.setForeground(TEXT_DARK);
        cardPanel.add(lblConfirm, gbc);

        gbc.gridy = 13;
        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtConfirmPassword.setPreferredSize(new Dimension(340, 32));
        txtConfirmPassword.putClientProperty("JTextField.placeholderText", "Ulangi Password");
        cardPanel.add(txtConfirmPassword, gbc);

        // Buttons Panel (Daftar & Batal)
        gbc.gridy = 14;
        gbc.insets = new Insets(18, 0, 0, 0);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnRegister = new JButton("Daftar");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(PRIMARY_BLUE);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(130, 38));
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        btnBatal = new JButton("Batal");
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.setBackground(GRAY_BUTTON);
        btnBatal.setForeground(TEXT_DARK);
        btnBatal.setPreferredSize(new Dimension(130, 38));
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
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(cardPanel);
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
