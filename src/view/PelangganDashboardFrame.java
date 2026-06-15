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

    private static final Color NAVY_BLUE = new Color(15, 23, 42); // #0F172A (Slate 900)
    private static final Color ACTIVE_BLUE = new Color(37, 99, 235); // #2563EB (Royal Blue)
    private static final Color GREEN_BUTTON = new Color(16, 185, 129); // #10B981 (Emerald Green)
    private static final Color GRAY_BUTTON = new Color(241, 245, 249); // #F1F5F9 (slate-100)
    private static final Color SIDEBAR_BG = new Color(15, 23, 42); // #0F172A (slate-900)
    private static final Color BG_LIGHT = new Color(248, 250, 252); // #F8FAFC (slate-50)
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // #64748B (slate-500)

    public PelangganDashboardFrame() {
        pelangganController = new PelangganController();
        initializeUI();
        loadHistoryData();
        loadCustomerVehicles();
    }

    private void initializeUI() {
        setTitle("Dasbor Pelanggan - Sistem Reservasi Bengkel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(940, 600); // slightly larger for modern layout spacing
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel (BorderLayout)
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // --- 1. SIDEBAR PANEL (LEFT) ---
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(230, 0));
        sidebarPanel.setBorder(new EmptyBorder(30, 15, 30, 15));
        sidebarPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcNav = new GridBagConstraints();
        gbcNav.fill = GridBagConstraints.HORIZONTAL;
        gbcNav.insets = new Insets(8, 0, 8, 0);
        gbcNav.weightx = 1.0;
        gbcNav.gridx = 0;

        // PROFILE WIDGET
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcProfile = new GridBagConstraints();
        gbcProfile.gridx = 0;
        
        // Circular initials avatar
        String nameStr = Session.getNama();
        String initial = nameStr.isEmpty() ? "P" : nameStr.substring(0, 1).toUpperCase();
        JLabel avatarLabel = new JLabel(initial, JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setPreferredSize(new Dimension(54, 54));
        avatarLabel.setMinimumSize(new Dimension(54, 54));
        
        gbcProfile.gridy = 0;
        gbcProfile.insets = new Insets(0, 0, 8, 0);
        profilePanel.add(avatarLabel, gbcProfile);
        
        JLabel lblUserName = new JLabel(Session.getNama(), JLabel.CENTER);
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserName.setForeground(Color.WHITE);
        gbcProfile.gridy = 1;
        gbcProfile.insets = new Insets(0, 0, 2, 0);
        profilePanel.add(lblUserName, gbcProfile);
        
        JLabel lblUserRole = new JLabel("Pelanggan", JLabel.CENTER);
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUserRole.setForeground(TEXT_MUTED);
        gbcProfile.gridy = 2;
        gbcProfile.insets = new Insets(0, 0, 25, 0);
        profilePanel.add(lblUserRole, gbcProfile);

        // Add profile panel at the top of the sidebar
        gbcNav.gridy = 0;
        sidebarPanel.add(profilePanel, gbcNav);

        navButtons = new ArrayList<>();

        // Create Navigation Buttons
        btnNavDashboard = createNavButton("Dashboard");
        btnNavBuat = createNavButton("Buat Reservasi");
        btnNavStatus = createNavButton("Status Reservasi");
        btnNavProfil = createNavButton("Profil");
        btnNavLogout = createNavButton("Logout");

        // Add to sidebar panel (shift indices for profile panel)
        gbcNav.gridy = 1; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 2; sidebarPanel.add(btnNavBuat, gbcNav);
        gbcNav.gridy = 3; sidebarPanel.add(btnNavStatus, gbcNav);
        gbcNav.gridy = 4; sidebarPanel.add(btnNavProfil, gbcNav);
        
        // Push logout to the bottom
        gbcNav.gridy = 5;
        gbcNav.weighty = 1.0;
        sidebarPanel.add(Box.createGlue(), gbcNav);
        
        gbcNav.gridy = 6;
        gbcNav.weighty = 0.0;
        sidebarPanel.add(btnNavLogout, gbcNav);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // --- 2. CONTENT PANEL (RIGHT - CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_LIGHT);
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(190, 38));
        btn.setFocusPainted(false);
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
        for (JButton btn : navButtons) {
            if (btn == activeButton) {
                btn.setBackground(ACTIVE_BLUE);
                btn.setForeground(Color.WHITE);
                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
            } else {
                btn.setBackground(new Color(0, 0, 0, 0));
                btn.setForeground(new Color(203, 213, 225)); // slate-300 light text for dark sidebar
                btn.setContentAreaFilled(false);
                btn.setOpaque(false);
            }
        }
        cardLayout.show(contentPanel, cardName);
    }

    // --- CARD 1: DEFAULT DASHBOARD ---
    private void createDashboardCard() {
        cardDashboard = new JPanel(new BorderLayout());
        cardDashboard.setBackground(BG_LIGHT);
        cardDashboard.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Dashboard Pelanggan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblWelcome = new JLabel("Selamat Datang kembali, " + Session.getNama() + "!");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblWelcome.setForeground(TEXT_MUTED);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblWelcome.setBorder(new EmptyBorder(4, 0, 30, 0));

        headerPanel.add(lblTitle);
        headerPanel.add(lblWelcome);
        cardDashboard.add(headerPanel, BorderLayout.NORTH);

        // Grid for Shortcut Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        cardsGrid.setOpaque(false);

        // Shortcut 1: Buat Reservasi
        JPanel cardBuat = createShortcutCard(
            "Buat Reservasi Baru", 
            "Daftarkan kendaraan Anda untuk jadwal servis di bengkel kami secara cepat.", 
            "BUAT", 
            btnNavBuat, 
            ACTIVE_BLUE
        );
        
        // Shortcut 2: Status Reservasi
        JPanel cardStatus = createShortcutCard(
            "Status & Riwayat Reservasi", 
            "Pantau status pengerjaan servis kendaraan Anda dan lihat riwayat transaksi.", 
            "STATUS_LIST", 
            btnNavStatus, 
            GREEN_BUTTON
        );

        cardsGrid.add(cardBuat);
        cardsGrid.add(cardStatus);
        cardDashboard.add(cardsGrid, BorderLayout.CENTER);
    }

    private JPanel createShortcutCard(String title, String desc, final String cardTarget, final JButton navBtnTarget, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top Accent Bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(6, 40));
        card.add(accentBar, BorderLayout.WEST);

        // Text Content
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(new EmptyBorder(0, 16, 0, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea taDesc = new JTextArea(desc);
        taDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taDesc.setForeground(TEXT_MUTED);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        taDesc.setEditable(false);
        taDesc.setFocusable(false);
        taDesc.setOpaque(false);
        taDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        taDesc.setBorder(new EmptyBorder(8, 0, 0, 0));

        textPanel.add(lblTitle);
        textPanel.add(taDesc);
        card.add(textPanel, BorderLayout.CENTER);

        // Click interaction
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if ("STATUS_LIST".equals(cardTarget)) {
                    loadHistoryData();
                }
                navigate(cardTarget, navBtnTarget);
            }
        });

        return card;
    }

    // --- CARD 2: BUAT RESERVASI (Screenshot 3) ---
    private void createBuatReservasiCard() {
        cardBuatReservasi = new JPanel(new BorderLayout());
        cardBuatReservasi.setBackground(BG_LIGHT);
        cardBuatReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblHeading = new JLabel("Buat Reservasi Baru");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeading.setForeground(NAVY_BLUE);
        lblHeading.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardBuatReservasi.add(lblHeading, BorderLayout.NORTH);

        // Form Card Container
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);

        // Pelanggan
        gbc.gridy = 0;
        JLabel lblPelanggan = new JLabel("Nama Pelanggan");
        lblPelanggan.setFont(labelFont);
        lblPelanggan.setForeground(NAVY_BLUE);
        formCard.add(lblPelanggan, gbc);

        gbc.gridy = 1;
        cmbPelanggan = new JComboBox<>(new String[]{ Session.getNama() });
        cmbPelanggan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPelanggan.setPreferredSize(new Dimension(300, 36));
        formCard.add(cmbPelanggan, gbc);

        // Kendaraan
        gbc.gridy = 2;
        JLabel lblKendaraan = new JLabel("Kendaraan");
        lblKendaraan.setFont(labelFont);
        lblKendaraan.setForeground(NAVY_BLUE);
        formCard.add(lblKendaraan, gbc);

        gbc.gridy = 3;
        cmbKendaraan = new JComboBox<>();
        cmbKendaraan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbKendaraan.setPreferredSize(new Dimension(300, 36));
        cmbKendaraan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmbKendaraan.getSelectedItem() != null && 
                    cmbKendaraan.getSelectedItem().toString().contains("Tambah Kendaraan Baru")) {
                    handleAddNewVehiclePrompt();
                }
            }
        });
        formCard.add(cmbKendaraan, gbc);

        // Tanggal Servis
        gbc.gridy = 4;
        JLabel lblTglServis = new JLabel("Tanggal Servis (YYYY-MM-DD)");
        lblTglServis.setFont(labelFont);
        lblTglServis.setForeground(NAVY_BLUE);
        formCard.add(lblTglServis, gbc);

        gbc.gridy = 5;
        txtTanggalServis = new JTextField(LocalDate.now().toString());
        txtTanggalServis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTanggalServis.setPreferredSize(new Dimension(300, 36));
        formCard.add(txtTanggalServis, gbc);

        // Jam Servis
        gbc.gridy = 6;
        JLabel lblJamServis = new JLabel("Jam Servis");
        lblJamServis.setFont(labelFont);
        lblJamServis.setForeground(NAVY_BLUE);
        formCard.add(lblJamServis, gbc);

        gbc.gridy = 7;
        cmbJamServis = new JComboBox<>(new String[]{
            "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        });
        cmbJamServis.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbJamServis.setPreferredSize(new Dimension(300, 36));
        formCard.add(cmbJamServis, gbc);

        // Keluhan
        gbc.gridy = 8;
        JLabel lblKeluhan = new JLabel("Deskripsi Keluhan / Catatan Servis");
        lblKeluhan.setFont(labelFont);
        lblKeluhan.setForeground(NAVY_BLUE);
        formCard.add(lblKeluhan, gbc);

        gbc.gridy = 9;
        txtKeluhan = new JTextArea(3, 20);
        txtKeluhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtKeluhan.setLineWrap(true);
        txtKeluhan.setWrapStyleWord(true);
        JScrollPane scrollKeluhan = new JScrollPane(txtKeluhan);
        scrollKeluhan.setPreferredSize(new Dimension(300, 70));
        formCard.add(scrollKeluhan, gbc);

        cardBuatReservasi.add(formCard, BorderLayout.CENTER);

        // Bottom buttons
        JPanel buttonsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonsBar.setOpaque(false);
        buttonsBar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnSimpan = new JButton("Kirim Reservasi");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setBackground(ACTIVE_BLUE);
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setPreferredSize(new Dimension(150, 38));
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
        btnBatalForm.setForeground(NAVY_BLUE);
        btnBatalForm.setPreferredSize(new Dimension(100, 38));
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

        buttonsBar.add(btnBatalForm);
        buttonsBar.add(btnSimpan);
        cardBuatReservasi.add(buttonsBar, BorderLayout.SOUTH);
    }

    // --- CARD 3: STATUS RESERVASI LIST ---
    private void createStatusReservasiCard() {
        cardStatusReservasi = new JPanel(new BorderLayout());
        cardStatusReservasi.setBackground(BG_LIGHT);
        cardStatusReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblHeading = new JLabel("Status & Riwayat Reservasi");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 28));
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
        tblHistory.setDefaultRenderer(Object.class, new StatusRenderer());
        tblHistory.setRowHeight(32);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblHistory.setShowVerticalLines(false);
        tblHistory.setIntercellSpacing(new Dimension(0, 1));
        
        JScrollPane scrollTable = new JScrollPane(tblHistory);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        cardStatusReservasi.add(scrollTable, BorderLayout.CENTER);

        // Bottom Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnDetail = new JButton("Lihat Detail Status");
        btnDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDetail.setBackground(ACTIVE_BLUE);
        btnDetail.setForeground(Color.WHITE);
        btnDetail.setPreferredSize(new Dimension(160, 38));
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

        JButton btnBatal = new JButton("Batalkan Reservasi");
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnBatal.setBackground(new Color(239, 68, 68)); // Slate Red-500
        btnBatal.setForeground(Color.WHITE);
        btnBatal.setPreferredSize(new Dimension(160, 38));
        btnBatal.setFocusPainted(false);
        btnBatal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tblHistory.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(PelangganDashboardFrame.this, 
                        "Silakan pilih salah satu baris reservasi dari tabel terlebih dahulu!", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                Object[] rawRow = rawHistoryRows.get(row);
                int idReservasi = (int) rawRow[0];
                String status = rawRow[6].toString();
                
                if (!"Menunggu Konfirmasi".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(PelangganDashboardFrame.this, 
                        "Hanya reservasi dengan status 'Menunggu Konfirmasi' yang dapat dibatalkan secara mandiri!", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(PelangganDashboardFrame.this, 
                    "Apakah Anda yakin ingin membatalkan reservasi ini?", 
                    "Konfirmasi Pembatalan", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = pelangganController.cancelReservasi(idReservasi);
                    if (success) {
                        JOptionPane.showMessageDialog(PelangganDashboardFrame.this, 
                            "Reservasi berhasil dibatalkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadHistoryData();
                    } else {
                        JOptionPane.showMessageDialog(PelangganDashboardFrame.this, 
                            "Gagal membatalkan reservasi.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton btnRefreshList = new JButton("Refresh");
        btnRefreshList.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefreshList.setBackground(GRAY_BUTTON);
        btnRefreshList.setForeground(NAVY_BLUE);
        btnRefreshList.setPreferredSize(new Dimension(100, 38));
        btnRefreshList.setFocusPainted(false);
        btnRefreshList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadHistoryData();
            }
        });

        controlPanel.add(btnRefreshList);
        controlPanel.add(btnBatal);
        controlPanel.add(btnDetail);
        cardStatusReservasi.add(controlPanel, BorderLayout.SOUTH);
    }

    // --- CARD 4: RESERVASI DETAILS CARD (Screenshot 4) ---
    private void createStatusDetailCard() {
        cardStatusDetail = new JPanel(new BorderLayout());
        cardStatusDetail.setBackground(BG_LIGHT);
        cardStatusDetail.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblHeading = new JLabel("Detail Status Reservasi");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHeading.setForeground(NAVY_BLUE);
        lblHeading.setBorder(new EmptyBorder(0, 0, 20, 0));
        cardStatusDetail.add(lblHeading, BorderLayout.NORTH);

        // Details card container
        JPanel detailCard = new JPanel(new GridBagLayout());
        detailCard.setBackground(Color.WHITE);
        detailCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        // Fonts
        Font fieldLabelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldValueFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Helper method to add details
        addDetailRow(detailCard, "No. Reservasi", lblDetailNoRes = new JLabel(), 0, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Tanggal Reservasi", lblDetailTglRes = new JLabel(), 1, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Kendaraan", lblDetailKendaraan = new JLabel(), 2, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Tanggal Servis", lblDetailTglServis = new JLabel(), 3, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Jam Servis", lblDetailJamServis = new JLabel(), 4, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Keluhan", lblDetailKeluhan = new JLabel(), 5, fieldLabelFont, fieldValueFont);
        addDetailRow(detailCard, "Status", lblDetailStatus = new JLabel(), 6, fieldLabelFont, fieldValueFont);

        cardStatusDetail.add(detailCard, BorderLayout.CENTER);

        // KEMBALI button
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backPanel.setOpaque(false);
        backPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton btnKembali = new JButton("Kembali");
        btnKembali.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnKembali.setBackground(GRAY_BUTTON);
        btnKembali.setForeground(NAVY_BLUE);
        btnKembali.setPreferredSize(new Dimension(110, 38));
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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        valueLabel.setFont(valueFont);
        valueLabel.setForeground(NAVY_BLUE);
        panel.add(valueLabel, gbc);
    }

    // --- CARD 5: CUSTOMER PROFILE VIEW ---
    private void createProfilCard() {
        cardProfil = new JPanel(new BorderLayout());
        cardProfil.setBackground(BG_LIGHT);
        cardProfil.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("Profil Pelanggan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 20, 0));
        cardProfil.add(lblTitle, BorderLayout.NORTH);

        JPanel profileCard = new JPanel(new GridBagLayout());
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Fetch session data
        addProfileRow(profileCard, "ID Pelanggan", String.valueOf(Session.getIdPelanggan()), 0, labelFont, valFont);
        addProfileRow(profileCard, "Nama Lengkap", Session.getNama(), 1, labelFont, valFont);
        addProfileRow(profileCard, "Username", Session.getUsername(), 2, labelFont, valFont);

        cardProfil.add(profileCard, BorderLayout.CENTER);
        
        // Add glue at south to keep it compact
        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        cardProfil.add(southPanel, BorderLayout.SOUTH);
    }

    private void addProfileRow(JPanel panel, String labelText, String valueText, int row, Font labelFont, Font valueFont) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(labelFont);
        lbl.setForeground(TEXT_MUTED);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(valueText);
        val.setFont(valueFont);
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
            String platRaw = txtP.getText().trim();

            if (merk.isEmpty() || tipe.isEmpty() || platRaw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua data kendaraan harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                cmbKendaraan.setSelectedIndex(0);
                return;
            }

            // Standardisasi Plat Nomor (Kapitalisasi dan normalisasi spasi)
            String plat = platRaw.replaceAll("\\s+", " ").toUpperCase();
            if (!plat.matches("^[A-Z]{1,2}\\s?\\d{1,4}\\s?[A-Z]{1,3}$")) {
                JOptionPane.showMessageDialog(this, "Format Plat Nomor tidak valid (contoh: B 1234 ABC)!", "Format Error", JOptionPane.ERROR_MESSAGE);
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

        // Peningkatan 1: Validasi Jam & Hari Operasional Bengkel (Cegah tanggal lampau & Hari Minggu)
        try {
            LocalDate chosenDate = LocalDate.parse(tanggal);
            if (chosenDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Tanggal servis tidak boleh di masa lampau!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (chosenDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                JOptionPane.showMessageDialog(this, "Bengkel tutup pada hari Minggu! Harap pilih hari lain.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format Tanggal (YYYY-MM-DD) tidak valid!", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = config.Database.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Koneksi database gagal!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Peningkatan 3: Pengecekan Reservasi Ganda Aktif (Double Booking Check)
        String checkActiveQuery = "SELECT COUNT(*) FROM reservasi WHERE id_pelanggan = ? AND id_kendaraan = ? " +
                                  "AND status IN ('Menunggu Konfirmasi', 'Dalam Proses', 'Proses', 'Dikonfirmasi')";
        try (java.sql.PreparedStatement checkStmt = conn.prepareStatement(checkActiveQuery)) {
            checkStmt.setInt(1, Session.getIdPelanggan());
            checkStmt.setInt(2, idKendaraan);
            try (java.sql.ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Kendaraan ini sudah memiliki reservasi aktif yang sedang berjalan!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mengecek reservasi aktif: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
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

    // Peningkatan 4: Custom Renderer untuk Pewarnaan Baris Tabel Berdasarkan Status
    private static class StatusRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            Object val = table.getValueAt(row, 4);
            String status = val != null ? val.toString() : "";
            
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                if ("Menunggu Konfirmasi".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(255, 243, 205)); // Light yellow
                    c.setForeground(new Color(133, 100, 4));
                } else if ("Dalam Proses".equalsIgnoreCase(status) || "Proses".equalsIgnoreCase(status) || "Dikonfirmasi".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(209, 236, 241)); // Light blue
                    c.setForeground(new Color(12, 84, 96));
                } else if ("Selesai".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(212, 239, 223)); // Light green
                    c.setForeground(new Color(21, 67, 34));
                } else if ("Dibatalkan".equalsIgnoreCase(status) || "Ditolak".equalsIgnoreCase(status)) {
                    c.setBackground(new Color(248, 215, 218)); // Light red
                    c.setForeground(new Color(114, 28, 36));
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
            }
            return c;
        }
    }
}
