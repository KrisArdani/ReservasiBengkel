package view;

import config.Session;
import controller.PelangganController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PelangganDashboardFrame extends JFrame {
    private PelangganController pelangganController;
    
    // Layout & Navigation
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Sidebar Buttons
    private JButton btnNavDashboard;
    private JButton btnNavBuat;
    private JButton btnNavStatus;
    private JButton btnNavProfil;
    private JButton btnNavLogout;
    private List<JButton> navButtons;

    // View Cards
    private JPanel cardDashboard;
    private JPanel cardBuatReservasi;
    private JPanel cardStatusReservasi;
    private JPanel cardStatusDetail;
    private JPanel cardProfil;

    // Form inputs (Buat Reservasi)
    private JComboBox<String> cmbPelanggan;
    private JComboBox<String> cmbKendaraan;
    private JTextField txtTanggalServis;
    private JComboBox<String> cmbJamServis;
    private JTextArea txtKeluhan;
    private List<Integer> kendaraanIds; // Maps combobox index to database vehicle ID

    // History List Components
    private JTable tblHistory;
    private DefaultTableModel tableModel;
    private List<Object[]> rawHistoryRows;

    // Detail Panel Components (Screenshot 4)
    private JLabel lblDetailNoRes;
    private JLabel lblDetailTglRes;
    private JLabel lblDetailKendaraan;
    private JLabel lblDetailTglServis;
    private JLabel lblDetailJamServis;
    private JLabel lblDetailKeluhan;
    private JLabel lblDetailStatus;

    private static final Color NAVY_BLUE = new Color(0, 32, 96); // #002060
    private static final Color ACTIVE_BLUE = new Color(25, 118, 210); // #1976D2
    private static final Color GREEN_BUTTON = new Color(40, 167, 69); // #28A745
    private static final Color GRAY_BUTTON = new Color(214, 214, 214); // #D6D6D6
    private static final Color SIDEBAR_GRAY = new Color(212, 212, 212); // #D4D4D4

    public PelangganDashboardFrame() {
        pelangganController = new PelangganController();
        initializeUI();
        loadHistoryData();
        loadCustomerVehicles();
    }

    private void initializeUI() {
        setTitle("Dasbor Pelanggan - Sistem Reservasi Bengkel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel (BorderLayout)
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // --- 1. SIDEBAR PANEL (LEFT) ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_GRAY);
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBorder(new EmptyBorder(40, 15, 40, 15));
        sidebarPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcNav = new GridBagConstraints();
        gbcNav.fill = GridBagConstraints.HORIZONTAL;
        gbcNav.insets = new Insets(10, 0, 10, 0);
        gbcNav.weightx = 1.0;
        gbcNav.gridx = 0;

        navButtons = new ArrayList<>();

        // Create Navigation Buttons with Georgia Bold Italic style
        btnNavDashboard = createNavButton("Dashboard");
        btnNavBuat = createNavButton("Buat Reservasi");
        btnNavStatus = createNavButton("Status Reservasi");
        btnNavProfil = createNavButton("Profil");
        btnNavLogout = createNavButton("Logout");

        // Add to sidebar panel
        gbcNav.gridy = 0; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 1; sidebarPanel.add(btnNavBuat, gbcNav);
        gbcNav.gridy = 2; sidebarPanel.add(btnNavStatus, gbcNav);
        gbcNav.gridy = 3; sidebarPanel.add(btnNavProfil, gbcNav);
        
        // Push logout to the bottom
        gbcNav.gridy = 4;
        gbcNav.weighty = 1.0;
        sidebarPanel.add(Box.createGlue(), gbcNav);
        
        gbcNav.gridy = 5;
        gbcNav.weighty = 0.0;
        sidebarPanel.add(btnNavLogout, gbcNav);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL (RIGHT - CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(244, 244, 244));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Initialize Individual Cards
        createDashboardCard();
        createBuatReservasiCard();
        createStatusReservasiCard();
        createStatusDetailCard();
        createProfilCard();

        // Add Cards to Layout
        contentPanel.add(cardDashboard, "DASHBOARD");
        contentPanel.add(cardBuatReservasi, "BUAT");
        contentPanel.add(cardStatusReservasi, "STATUS_LIST");
        contentPanel.add(cardStatusDetail, "STATUS_DETAIL");
        contentPanel.add(cardProfil, "PROFIL");

        // Set default card and navigate
        navigate("DASHBOARD", btnNavDashboard);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        btn.setPreferredSize(new Dimension(180, 36));
        btn.setFocusPainted(false);
        // Using FlatLaf's oblong round rectangle properties
        btn.putClientProperty("JButton.buttonType", "roundRect");
        
        navButtons.add(btn);

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Logout".equalsIgnoreCase(text)) {
                    int confirm = JOptionPane.showConfirmDialog(PelangganDashboardFrame.this, 
                        "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Session.clearSession();
                        new LoginFrame().setVisible(true);
                        dispose();
                    }
                } else if ("Dashboard".equalsIgnoreCase(text)) {
                    navigate("DASHBOARD", btn);
                } else if ("Buat Reservasi".equalsIgnoreCase(text)) {
                    navigate("BUAT", btn);
                } else if ("Status Reservasi".equalsIgnoreCase(text)) {
                    loadHistoryData();
                    navigate("STATUS_LIST", btn);
                } else if ("Profil".equalsIgnoreCase(text)) {
                    navigate("PROFIL", btn);
                }
            }
        });

        return btn;
    }

    private void navigate(String cardName, JButton activeButton) {
        // Toggle Sidebar colors
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setBackground(ACTIVE_BLUE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(NAVY_BLUE);
            }
        }
        cardLayout.show(contentPanel, cardName);
    }

    // --- CARD 1: DEFAULT DASHBOARD ---
    private void createDashboardCard() {
        cardDashboard = new JPanel(new GridBagLayout());
        cardDashboard.setBackground(new Color(244, 244, 244));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("DASHBOARD PELANGGAN");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.gridy = 0;
        cardDashboard.add(lblTitle, gbc);

        JLabel lblWelcome = new JLabel("Selamat Datang, " + Session.getNama() + "!");
        lblWelcome.setFont(new Font("Georgia", Font.PLAIN, 18));
        lblWelcome.setForeground(Color.BLACK);
        gbc.gridy = 1;
        cardDashboard.add(lblWelcome, gbc);

        JLabel lblInfo = new JLabel("Gunakan menu di sebelah kiri untuk melakukan reservasi servis baru atau melihat riwayat pemesanan Anda.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(Color.GRAY);
        gbc.gridy = 2;
        cardDashboard.add(lblInfo, gbc);
    }

    // --- CARD 2: BUAT RESERVASI (Screenshot 3) ---
    private void createBuatReservasiCard() {
        cardBuatReservasi = new JPanel(new BorderLayout());
        cardBuatReservasi.setBackground(new Color(244, 244, 244));
        cardBuatReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblHeading = new JLabel("BUAT RESERVASI");
        lblHeading.setFont(new Font("Georgia", Font.BOLD, 36));
        lblHeading.setForeground(NAVY_BLUE);
        lblHeading.setBorder(new EmptyBorder(0, 0, 20, 0));
        cardBuatReservasi.add(lblHeading, BorderLayout.NORTH);

        // Inputs Panel
        JPanel inputsGrid = new JPanel(new GridBagLayout());
        inputsGrid.setBackground(new Color(244, 244, 244));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Pelanggan Dropdown
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblPelanggan = new JLabel("Pelanggan");
        lblPelanggan.setFont(new Font("Georgia", Font.PLAIN, 15));
        inputsGrid.add(lblPelanggan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbPelanggan = new JComboBox<>(new String[]{ Session.getNama() });
        cmbPelanggan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPelanggan.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbPelanggan, gbc);

        // Kendaraan Dropdown
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblKendaraan = new JLabel("Kendaraan");
        lblKendaraan.setFont(new Font("Georgia", Font.PLAIN, 15));
        inputsGrid.add(lblKendaraan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbKendaraan = new JComboBox<>();
        cmbKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbKendaraan.setPreferredSize(new Dimension(200, 30));
        cmbKendaraan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbKendaraan.getSelectedItem() != null && 
                    cmbKendaraan.getSelectedItem().toString().contains("Tambah Kendaraan Baru")) {
                    handleAddNewVehiclePrompt();
                }
            }
        });
        inputsGrid.add(cmbKendaraan, gbc);

        // Tanggal Servis
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblTglServis = new JLabel("Tanggal Servis");
        lblTglServis.setFont(new Font("Georgia", Font.PLAIN, 15));
        inputsGrid.add(lblTglServis, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtTanggalServis = new JTextField(LocalDate.now().toString());
        txtTanggalServis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTanggalServis.setPreferredSize(new Dimension(200, 30));
        txtTanggalServis.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputsGrid.add(txtTanggalServis, gbc);

        // Jam Servis Dropdown
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblJamServis = new JLabel("Jam Servis");
        lblJamServis.setFont(new Font("Georgia", Font.PLAIN, 15));
        inputsGrid.add(lblJamServis, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbJamServis = new JComboBox<>(new String[]{
            "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        });
        cmbJamServis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbJamServis.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbJamServis, gbc);

        // Keluhan
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblKeluhan = new JLabel("Keluhan");
        lblKeluhan.setFont(new Font("Georgia", Font.PLAIN, 15));
        inputsGrid.add(lblKeluhan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.CENTER;
        txtKeluhan = new JTextArea(4, 20);
        txtKeluhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKeluhan.setLineWrap(true);
        txtKeluhan.setWrapStyleWord(true);
        txtKeluhan.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JScrollPane scrollKeluhan = new JScrollPane(txtKeluhan);
        inputsGrid.add(scrollKeluhan, gbc);

        cardBuatReservasi.add(inputsGrid, BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonsBar.setBackground(new Color(244, 244, 244));
        buttonsBar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnSimpan = new JButton("Simpan Reservasi");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setBackground(GREEN_BUTTON);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setPreferredSize(new Dimension(160, 36));
        btnSimpan.setFocusPainted(false);
        btnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCreateReservasi();
            }
        });

        JButton btnBatalForm = new JButton("Batal");
        btnBatalForm.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatalForm.setBackground(GRAY_BUTTON);
        btnBatalForm.setForeground(Color.BLACK);
        btnBatalForm.setPreferredSize(new Dimension(100, 36));
        btnBatalForm.setFocusPainted(false);
        btnBatalForm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtKeluhan.setText("");
                txtTanggalServis.setText(LocalDate.now().toString());
                cmbJamServis.setSelectedIndex(0);
                if (cmbKendaraan.getItemCount() > 0) cmbKendaraan.setSelectedIndex(0);
                navigate("DASHBOARD", btnNavDashboard);
            }
        });

        buttonsBar.add(btnSimpan);
        buttonsBar.add(btnBatalForm);
        cardBuatReservasi.add(buttonsBar, BorderLayout.SOUTH);
    }

    // --- CARD 3: STATUS RESERVASI LIST ---
    private void createStatusReservasiCard() {
        cardStatusReservasi = new JPanel(new BorderLayout());
        cardStatusReservasi.setBackground(new Color(244, 244, 244));
        cardStatusReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblHeading = new JLabel("STATUS RESERVASI");
        lblHeading.setFont(new Font("Georgia", Font.BOLD, 36));
        lblHeading.setForeground(NAVY_BLUE);
        lblHeading.setBorder(new EmptyBorder(0, 0, 20, 0));
        cardStatusReservasi.add(lblHeading, BorderLayout.NORTH);

        // JTable listing
        String[] columns = {"No. Reservasi", "Tanggal Servis", "Kendaraan", "Jam", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblHistory = new JTable(tableModel);
        tblHistory.setRowHeight(22);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblHistory.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        JScrollPane scrollTable = new JScrollPane(tblHistory);
        cardStatusReservasi.add(scrollTable, BorderLayout.CENTER);

        // Bottom Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controlPanel.setBackground(new Color(244, 244, 244));
        controlPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnDetail = new JButton("Lihat Detail Status");
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDetail.setBackground(ACTIVE_BLUE);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setPreferredSize(new Dimension(160, 32));
        btnDetail.setFocusPainted(false);
        btnDetail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tblHistory.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(PelangganDashboardFrame.this, 
                        "Silakan pilih salah satu baris reservasi dari tabel terlebih dahulu!", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showStatusDetail(row);
            }
        });

        JButton btnRefreshList = new JButton("Refresh");
        btnRefreshList.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefreshList.setBackground(GRAY_BUTTON);
        btnRefreshList.setForeground(Color.BLACK);
        btnRefreshList.setPreferredSize(new Dimension(100, 32));
        btnRefreshList.setFocusPainted(false);
        btnRefreshList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadHistoryData();
            }
        });

        controlPanel.add(btnRefreshList);
        controlPanel.add(btnDetail);
        cardStatusReservasi.add(controlPanel, BorderLayout.SOUTH);
    }

    // --- CARD 4: RESERVASI DETAILS CARD (Screenshot 4) ---
    private void createStatusDetailCard() {
        cardStatusDetail = new JPanel(new BorderLayout());
        cardStatusDetail.setBackground(new Color(244, 244, 244));
        cardStatusDetail.setBorder(new EmptyBorder(35, 50, 35, 50));

        JLabel lblHeading = new JLabel("STATUS RESERVASI");
        lblHeading.setFont(new Font("Georgia", Font.BOLD, 36));
        lblHeading.setForeground(NAVY_BLUE);
        lblHeading.setBorder(new EmptyBorder(0, 0, 25, 0));
        cardStatusDetail.add(lblHeading, BorderLayout.NORTH);

        // Details grid
        JPanel detailGrid = new JPanel(new GridBagLayout());
        detailGrid.setBackground(new Color(244, 244, 244));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Fonts
        Font fieldLabelFont = new Font("Georgia", Font.PLAIN, 16);
        Font fieldValueFont = new Font("Segoe UI", Font.PLAIN, 15);

        // Helper method to add details
        addDetailRow(detailGrid, "No. Reservasi :", lblDetailNoRes = new JLabel(), 0, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Tanggal Reservasi :", lblDetailTglRes = new JLabel(), 1, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Kendaraan :", lblDetailKendaraan = new JLabel(), 2, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Tanggal Servis :", lblDetailTglServis = new JLabel(), 3, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Jam Servis :", lblDetailJamServis = new JLabel(), 4, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Keluhan :", lblDetailKeluhan = new JLabel(), 5, fieldLabelFont, fieldValueFont);
        addDetailRow(detailGrid, "Status :", lblDetailStatus = new JLabel(), 6, fieldLabelFont, fieldValueFont);

        cardStatusDetail.add(detailGrid, BorderLayout.CENTER);

        // KEMBALI button (italic, bold)
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setBackground(new Color(244, 244, 244));
        
        JButton btnKembali = new JButton("KEMBALI");
        btnKembali.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        btnKembali.setBackground(GRAY_BUTTON);
        btnKembali.setForeground(Color.BLACK);
        btnKembali.setPreferredSize(new Dimension(130, 36));
        btnKembali.setFocusPainted(false);
        btnKembali.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(contentPanel, "STATUS_LIST");
            }
        });
        backPanel.add(btnKembali);
        cardStatusDetail.add(backPanel, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, String labelText, JLabel valueLabel, int row, Font labelFont, Font valueFont) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        lbl.setForeground(Color.BLACK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        valueLabel.setFont(valueFont);
        valueLabel.setForeground(Color.BLACK);
        panel.add(valueLabel, gbc);
    }

    // --- CARD 5: CUSTOMER PROFILE VIEW ---
    private void createProfilCard() {
        cardProfil = new JPanel(new GridBagLayout());
        cardProfil.setBackground(new Color(244, 244, 244));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("PROFIL PELANGGAN");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 30, 10);
        cardProfil.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font gFont = new Font("Georgia", Font.PLAIN, 15);

        // Fetch session data
        addProfileRow(cardProfil, "ID Pelanggan :", String.valueOf(Session.getIdPelanggan()), 1, gFont);
        addProfileRow(cardProfil, "Nama Lengkap :", Session.getNama(), 2, gFont);
        // We can look up other details or display available ones
        addProfileRow(cardProfil, "Username :", Session.getUsername(), 3, gFont);
    }

    private void addProfileRow(JPanel panel, String labelText, String valueText, int row, Font labelFont) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.gridy = row;

        gbc.gridx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        lbl.setForeground(Color.BLACK);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Segoe UI", Font.BOLD, 15));
        val.setForeground(NAVY_BLUE);
        panel.add(val, gbc);
    }

    // --- DATA LOADING & INTERACTION ---

    private void loadCustomerVehicles() {
        cmbKendaraan.removeAllItems();
        kendaraanIds = new ArrayList<>();

        Connection conn = config.Database.getConnection();
        if (conn == null) return;

        String query = "SELECT id_kendaraan, merk, tipe, plat_nomer FROM kendaraan WHERE id_pelanggan = ? ORDER BY merk ASC";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, Session.getIdPelanggan());
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_kendaraan");
                    String desc = rs.getString("merk") + " " + rs.getString("tipe") + " - " + rs.getString("plat_nomer");
                    cmbKendaraan.addItem(desc);
                    kendaraanIds.add(id);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat kendaraan pelanggan: " + e.getMessage());
        }

        // Add special prompt option
        cmbKendaraan.addItem("[ + Tambah Kendaraan Baru ]");
        kendaraanIds.add(-1);
    }

    private void handleAddNewVehiclePrompt() {
        JTextField txtM = new JTextField();
        JTextField txtT = new JTextField();
        JTextField txtP = new JTextField();
        Object[] fields = {
            "Merk Kendaraan (contoh: Honda):", txtM,
            "Tipe Kendaraan (contoh: Beat):", txtT,
            "Plat Nomor (contoh: B 1234 ABC):", txtP
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Tambah Kendaraan Baru", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String merk = txtM.getText().trim();
            String tipe = txtT.getText().trim();
            String plat = txtP.getText().trim();

            if (merk.isEmpty() || tipe.isEmpty() || plat.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua data kendaraan harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                cmbKendaraan.setSelectedIndex(0);
                return;
            }

            try {
                int newId = pelangganController.getOrCreateKendaraan(Session.getIdPelanggan(), merk, tipe, plat);
                if (newId != -1) {
                    JOptionPane.showMessageDialog(this, "Kendaraan " + merk + " " + tipe + " (" + plat + ") berhasil didaftarkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomerVehicles();
                    // Auto-select the newly added vehicle (it will be near the end, index count - 2 because of the prompt option)
                    cmbKendaraan.setSelectedIndex(cmbKendaraan.getItemCount() - 2);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal mendaftarkan kendaraan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                cmbKendaraan.setSelectedIndex(0);
            }
        } else {
            // Cancelled
            cmbKendaraan.setSelectedIndex(0);
        }
    }

    private void loadHistoryData() {
        tableModel.setRowCount(0);
        rawHistoryRows = pelangganController.getHistoryReservasi(Session.getIdPelanggan());
        int no = 1;
        for (Object[] row : rawHistoryRows) {
            // Mapping details list format
            String customNoRes = "RSV-2026-" + String.format("%04d", (int)row[0]);
            tableModel.addRow(new Object[]{
                customNoRes,
                row[1], // Tanggal Servis
                row[3], // Kendaraan Info
                row[2], // Jam Servis
                row[6]  // Status
            });
            no++;
        }
    }

    private void showStatusDetail(int selectedRowIndex) {
        Object[] rawRow = rawHistoryRows.get(selectedRowIndex);
        
        lblDetailNoRes.setText("RSV-2026-" + String.format("%04d", (int)rawRow[0]));
        // Since we don't store booking date/time, we'll use appointment date as reference or generic placeholder as in design
        lblDetailTglRes.setText(rawRow[1].toString() + " " + rawRow[2].toString());
        lblDetailKendaraan.setText(rawRow[3].toString());
        lblDetailTglServis.setText(rawRow[1].toString());
        lblDetailJamServis.setText(rawRow[2].toString());
        lblDetailKeluhan.setText(rawRow[4].toString());
        lblDetailStatus.setText(rawRow[6].toString());
        
        cardLayout.show(contentPanel, "STATUS_DETAIL");
    }

    private void handleCreateReservasi() {
        if (cmbKendaraan.getSelectedItem() == null || cmbKendaraan.getSelectedItem().toString().contains("Tambah Kendaraan Baru")) {
            JOptionPane.showMessageDialog(this, "Pilih kendaraan yang valid!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = cmbKendaraan.getSelectedIndex();
        int idKendaraan = kendaraanIds.get(selectedIndex);

        String tanggal = txtTanggalServis.getText().trim();
        String jam = cmbJamServis.getSelectedItem().toString();
        String keluhan = txtKeluhan.getText().trim();

        if (tanggal.isEmpty() || keluhan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap lengkapi semua data input!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // We can execute reservation using the retrieved id_kendaraan directly to avoid re-inserting
        Connection conn = config.Database.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String insertQuery = "INSERT INTO reservasi (id_pelanggan, id_kendaraan, id_montir, tanggal, jam, keluhan, status) " +
                             "VALUES (?, ?, NULL, ?, ?, ?, 'Menunggu Konfirmasi')";
        try {
            java.sql.Date sqlDate = java.sql.Date.valueOf(tanggal);
            java.sql.Time sqlTime = java.sql.Time.valueOf(jam + ":00");

            try (java.sql.PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, Session.getIdPelanggan());
                stmt.setInt(2, idKendaraan);
                stmt.setDate(3, sqlDate);
                stmt.setTime(4, sqlTime);
                stmt.setString(5, keluhan);

                int success = stmt.executeUpdate();
                if (success > 0) {
                    JOptionPane.showMessageDialog(this, "Reservasi berhasil dikirim!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    txtKeluhan.setText("");
                    txtTanggalServis.setText(LocalDate.now().toString());
                    cmbJamServis.setSelectedIndex(0);

                    // Reload & Navigate to status list
                    loadHistoryData();
                    navigate("STATUS_LIST", btnNavStatus);
                }
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Format Tanggal (YYYY-MM-DD) tidak valid!", "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat reservasi: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
