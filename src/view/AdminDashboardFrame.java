package view;

import config.Session;
import controller.AdminController;
import controller.LaporanController;
import model.Montir;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private AdminController adminController;
    private LaporanController laporanController;

    // Layout & Navigation
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Sidebar Buttons
    private JButton btnNavDashboard;
    private JButton btnNavKelola;
    private JButton btnNavJadwal;
    private JButton btnNavLaporan;
    private JButton btnNavLogout;
    private List<JButton> navButtons;

    // View Cards
    private JPanel cardDashboard;
    private JPanel cardKelolaReservasi;
    private JPanel cardJadwalMontir;
    private JPanel cardLaporan;

    // Card 1: Kelola Reservasi Components
    private JTextField txtSearch;
    private JButton btnSearch;
    private JTable tblKelola;
    private DefaultTableModel tableModelKelola;
    private List<Object[]> rawReservasiRows;


    // Card 2: Atur Jadwal & Montir Components
    private JTextField txtJadwalNoRes;
    private JComboBox<String> cmbJadwalPelanggan;
    private JComboBox<String> cmbJadwalKendaraan;
    private JTextField txtJadwalTanggal;
    private JComboBox<String> cmbJadwalJam;
    private JComboBox<Montir> cmbJadwalMontir;
    private JTextArea txtJadwalCatatan;
    private JButton btnSimpanJadwal;
    private JButton btnKembaliJadwal;
    private int currentEditingReservasiId = -1;

    // Card 3: Laporan Components
    private JTextField txtDariTanggal;
    private JTextField txtSampaiTanggal;
    private JButton btnTampilkanLaporan;
    private JTable tblLaporan;
    private DefaultTableModel tableModelLaporan;
    private List<Object[]> rawLaporanRows;
    private JButton btnExportExcel;

    // Peningkatan 8: Label untuk Widget Metrik Ringkasan Laporan
    private JLabel lblWidgetTotal;
    private JLabel lblWidgetSelesai;
    private JLabel lblWidgetMontir;

    private static final Color NAVY_BLUE = new Color(15, 23, 42); // #0F172A (Slate 900)
    private static final Color ACTIVE_BLUE = new Color(37, 99, 235); // #2563EB (Royal Blue)
    private static final Color GREEN_BUTTON = new Color(16, 185, 129); // #10B981 (Emerald Green)
    private static final Color GRAY_BUTTON = new Color(241, 245, 249); // #F1F5F9 (slate-100)
    private static final Color SIDEBAR_BG = new Color(15, 23, 42); // #0F172A (slate-900)
    private static final Color BG_LIGHT = new Color(248, 250, 252); // #F8FAFC (slate-50)
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // #64748B (slate-500)

    public AdminDashboardFrame() {
        adminController = new AdminController();
        laporanController = new LaporanController();
        initializeUI();
        loadAllReservations();
        loadMontirComboBox();
    }

    private void initializeUI() {
        setTitle("Dasbor Admin - Sistem Reservasi Bengkel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620); // slightly larger for modern layout spacing
        setLocationRelativeTo(null);
        setResizable(false);

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
        String initial = nameStr.isEmpty() ? "A" : nameStr.substring(0, 1).toUpperCase();
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
        
        JLabel lblUserRole = new JLabel("Administrator", JLabel.CENTER);
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblUserRole.setForeground(TEXT_MUTED);
        gbcProfile.gridy = 2;
        gbcProfile.insets = new Insets(0, 0, 25, 0);
        profilePanel.add(lblUserRole, gbcProfile);

        // Add profile panel at the top of the sidebar
        gbcNav.gridy = 0;
        sidebarPanel.add(profilePanel, gbcNav);

        navButtons = new ArrayList<>();

        btnNavDashboard = createNavButton("Dashboard");
        btnNavKelola = createNavButton("Kelola Reservasi");
        btnNavJadwal = createNavButton("Jadwal & Montir");
        btnNavLaporan = createNavButton("Laporan");
        btnNavLogout = createNavButton("Logout");

        gbcNav.gridy = 1; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 2; sidebarPanel.add(btnNavKelola, gbcNav);
        gbcNav.gridy = 3; sidebarPanel.add(btnNavJadwal, gbcNav);
        gbcNav.gridy = 4; sidebarPanel.add(btnNavLaporan, gbcNav);

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

        // Initialize Cards
        createDashboardCard();
        createKelolaReservasiCard();
        createJadwalMontirCard();
        createLaporanCard();

        contentPanel.add(cardDashboard, "DASHBOARD");
        contentPanel.add(cardKelolaReservasi, "KELOLA");
        contentPanel.add(cardJadwalMontir, "JADWAL");
        contentPanel.add(cardLaporan, "LAPORAN");

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
                    int confirm = JOptionPane.showConfirmDialog(AdminDashboardFrame.this, 
                        "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Session.clearSession();
                        new LoginFrame().setVisible(true);
                        dispose();
                    }
                } else if ("Dashboard".equalsIgnoreCase(text)) {
                    navigate("DASHBOARD", btn);
                } else if ("Kelola Reservasi".equalsIgnoreCase(text)) {
                    loadAllReservations();
                    navigate("KELOLA", btn);
                } else if ("Jadwal & Montir".equalsIgnoreCase(text)) {
                    navigate("JADWAL", btn);
                } else if ("Laporan".equalsIgnoreCase(text)) {
                    navigate("LAPORAN", btn);
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
        cardDashboard.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Dashboard Admin");
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
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsGrid.setOpaque(false);

        // Shortcut 1: Kelola Reservasi
        JPanel cardKelola = createShortcutCard(
            "Kelola Reservasi", 
            "Verifikasi data reservasi masuk, alokasi status pengerjaan servis bengkel.", 
            "KELOLA", 
            btnNavKelola, 
            ACTIVE_BLUE
        );
        
        // Shortcut 2: Jadwal & Montir
        JPanel cardJadwal = createShortcutCard(
            "Jadwal & Montir", 
            "Alokasikan tugas montir ke kendaraan servis dan tentukan jam kerja.", 
            "JADWAL", 
            btnNavJadwal, 
            new Color(139, 92, 246) // Purple accent
        );

        // Shortcut 3: Laporan
        JPanel cardLaporan = createShortcutCard(
            "Laporan Servis", 
            "Cetak laporan rekap reservasi bengkel dan ekspor data ke file CSV.", 
            "LAPORAN", 
            btnNavLaporan, 
            GREEN_BUTTON
        );

        cardsGrid.add(cardKelola);
        cardsGrid.add(cardJadwal);
        cardsGrid.add(cardLaporan);
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
        card.setBorder(new EmptyBorder(24, 20, 24, 20));
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
        textPanel.setBorder(new EmptyBorder(0, 14, 0, 0));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea taDesc = new JTextArea(desc);
        taDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                if ("KELOLA".equals(cardTarget)) {
                    loadAllReservations();
                }
                navigate(cardTarget, navBtnTarget);
            }
        });

        return card;
    }

    // --- CARD 2: KELOLA RESERVASI (Screenshot 5) ---
    private void createKelolaReservasiCard() {
        cardKelolaReservasi = new JPanel(new BorderLayout());
        cardKelolaReservasi.setBackground(BG_LIGHT);
        cardKelolaReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel lblTitle = new JLabel("Kelola Reservasi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardKelolaReservasi.add(lblTitle, BorderLayout.NORTH);

        // Center Panel for Search and Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Search panel
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBarPanel.setOpaque(false);
        searchBarPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel lblSearch = new JLabel("Cari Reservasi");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(NAVY_BLUE);
        searchBarPanel.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(240, 32));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Masukkan kata kunci...");
        searchBarPanel.add(txtSearch);

        btnSearch = new JButton("Cari");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(GRAY_BUTTON);
        btnSearch.setForeground(NAVY_BLUE);
        btnSearch.setPreferredSize(new Dimension(80, 32));
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllReservations();
            }
        });
        searchBarPanel.add(btnSearch);

        centerPanel.add(searchBarPanel, BorderLayout.NORTH);

        // JTable Table
        String[] columns = {"No", "Pelanggan", "Tanggal", "Jam", "Status", "Aksi"};
        tableModelKelola = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKelola = new JTable(tableModelKelola);
        tblKelola.setDefaultRenderer(Object.class, new StatusRenderer());
        tblKelola.setRowHeight(32);
        tblKelola.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKelola.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblKelola.setShowVerticalLines(false);
        tblKelola.setIntercellSpacing(new Dimension(0, 1));
        
        JScrollPane scrollTable = new JScrollPane(tblKelola);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        cardKelolaReservasi.add(centerPanel, BorderLayout.CENTER);

        // Bottom Controls (Refresh & Detail Actions)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(GRAY_BUTTON);
        btnRefresh.setForeground(NAVY_BLUE);
        btnRefresh.setPreferredSize(new Dimension(100, 38));
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.setText("");
                loadAllReservations();
            }
        });
        bottomBar.add(btnRefresh, BorderLayout.WEST);

        // Action Panel
        JPanel actionSubPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionSubPanel.setOpaque(false);

        JButton btnStatusProses = new JButton("Proses Servis");
        btnStatusProses.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStatusProses.setBackground(ACTIVE_BLUE);
        btnStatusProses.setForeground(Color.WHITE);
        btnStatusProses.setPreferredSize(new Dimension(120, 38));
        btnStatusProses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuickStatusUpdate("Proses");
            }
        });

        JButton btnStatusSelesai = new JButton("Selesai Servis");
        btnStatusSelesai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnStatusSelesai.setBackground(GREEN_BUTTON);
        btnStatusSelesai.setForeground(Color.WHITE);
        btnStatusSelesai.setPreferredSize(new Dimension(120, 38));
        btnStatusSelesai.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuickStatusUpdate("Selesai");
            }
        });

        JButton btnAssignSchedule = new JButton("Atur Jadwal & Montir");
        btnAssignSchedule.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAssignSchedule.setBackground(GRAY_BUTTON);
        btnAssignSchedule.setForeground(NAVY_BLUE);
        btnAssignSchedule.setPreferredSize(new Dimension(165, 38));
        btnAssignSchedule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tblKelola.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(AdminDashboardFrame.this, 
                        "Pilih salah satu baris reservasi terlebih dahulu!", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                Object[] rawRow = rawReservasiRows.get(row);
                String currentStatus = rawRow[7].toString();
                if ("Selesai".equalsIgnoreCase(currentStatus) || "Dibatalkan".equalsIgnoreCase(currentStatus) || "Ditolak".equalsIgnoreCase(currentStatus)) {
                    JOptionPane.showMessageDialog(AdminDashboardFrame.this, 
                        "Jadwal & Montir tidak dapat diatur untuk reservasi yang sudah " + currentStatus + "!", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                loadSelectedRowToSchedule(row);
            }
        });

        actionSubPanel.add(btnStatusProses);
        actionSubPanel.add(btnStatusSelesai);
        actionSubPanel.add(btnAssignSchedule);
        bottomBar.add(actionSubPanel, BorderLayout.EAST);

        cardKelolaReservasi.add(bottomBar, BorderLayout.SOUTH);
    }

    // --- CARD 3: ATUR JADWAL & MONTIR (Screenshot 6) ---
    private void createJadwalMontirCard() {
        cardJadwalMontir = new JPanel(new BorderLayout());
        cardJadwalMontir.setBackground(BG_LIGHT);
        cardJadwalMontir.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblTitle = new JLabel("Atur Jadwal & Montir");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardJadwalMontir.add(lblTitle, BorderLayout.NORTH);

        // Inputs Card Container
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 12, 6, 12);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

        // Row 0: No Reservasi (Col 0) & Pelanggan (Col 1)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        JLabel lblNoRes = new JLabel("No Reservasi");
        lblNoRes.setFont(labelFont);
        lblNoRes.setForeground(NAVY_BLUE);
        formCard.add(lblNoRes, gbc);

        gbc.gridx = 1;
        JLabel lblPelanggan = new JLabel("Nama Pelanggan");
        lblPelanggan.setFont(labelFont);
        lblPelanggan.setForeground(NAVY_BLUE);
        formCard.add(lblPelanggan, gbc);

        // Row 1: No Reservasi Input & Pelanggan Input
        gbc.gridx = 0; gbc.gridy = 1;
        txtJadwalNoRes = new JTextField();
        txtJadwalNoRes.setEditable(false);
        txtJadwalNoRes.setFont(valueFont);
        txtJadwalNoRes.setPreferredSize(new Dimension(180, 36));
        formCard.add(txtJadwalNoRes, gbc);

        gbc.gridx = 1;
        cmbJadwalPelanggan = new JComboBox<>();
        cmbJadwalPelanggan.setEnabled(false);
        cmbJadwalPelanggan.setFont(valueFont);
        cmbJadwalPelanggan.setPreferredSize(new Dimension(180, 36));
        formCard.add(cmbJadwalPelanggan, gbc);

        // Row 2: Kendaraan (Col 0) & Tanggal Servis (Col 1)
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblKendaraan = new JLabel("Kendaraan");
        lblKendaraan.setFont(labelFont);
        lblKendaraan.setForeground(NAVY_BLUE);
        formCard.add(lblKendaraan, gbc);

        gbc.gridx = 1;
        JLabel lblTgl = new JLabel("Tanggal Servis (YYYY-MM-DD)");
        lblTgl.setFont(labelFont);
        lblTgl.setForeground(NAVY_BLUE);
        formCard.add(lblTgl, gbc);

        // Row 3: Kendaraan Input & Tanggal Input
        gbc.gridx = 0; gbc.gridy = 3;
        cmbJadwalKendaraan = new JComboBox<>();
        cmbJadwalKendaraan.setEnabled(false);
        cmbJadwalKendaraan.setFont(valueFont);
        cmbJadwalKendaraan.setPreferredSize(new Dimension(180, 36));
        formCard.add(cmbJadwalKendaraan, gbc);

        gbc.gridx = 1;
        txtJadwalTanggal = new JTextField();
        txtJadwalTanggal.setFont(valueFont);
        txtJadwalTanggal.setPreferredSize(new Dimension(180, 36));
        formCard.add(txtJadwalTanggal, gbc);

        // Row 4: Jam Servis (Col 0) & Montir (Col 1)
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblJam = new JLabel("Jam Servis");
        lblJam.setFont(labelFont);
        lblJam.setForeground(NAVY_BLUE);
        formCard.add(lblJam, gbc);

        gbc.gridx = 1;
        JLabel lblMontir = new JLabel("Montir Yang Dialokasikan");
        lblMontir.setFont(labelFont);
        lblMontir.setForeground(NAVY_BLUE);
        formCard.add(lblMontir, gbc);

        // Row 5: Jam Input & Montir Input
        gbc.gridx = 0; gbc.gridy = 5;
        cmbJadwalJam = new JComboBox<>(new String[]{
            "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        });
        cmbJadwalJam.setFont(valueFont);
        cmbJadwalJam.setPreferredSize(new Dimension(180, 36));
        formCard.add(cmbJadwalJam, gbc);

        gbc.gridx = 1;
        cmbJadwalMontir = new JComboBox<>();
        cmbJadwalMontir.setFont(valueFont);
        cmbJadwalMontir.setPreferredSize(new Dimension(180, 36));
        formCard.add(cmbJadwalMontir, gbc);

        // Row 6: Catatan (Span 2)
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JLabel lblCatatan = new JLabel("Catatan Keluhan");
        lblCatatan.setFont(labelFont);
        lblCatatan.setForeground(NAVY_BLUE);
        formCard.add(lblCatatan, gbc);

        // Row 7: Catatan Input (Span 2)
        gbc.gridx = 0; gbc.gridy = 7;
        txtJadwalCatatan = new JTextArea(2, 20);
        txtJadwalCatatan.setEditable(false);
        txtJadwalCatatan.setLineWrap(true);
        txtJadwalCatatan.setWrapStyleWord(true);
        txtJadwalCatatan.setFont(valueFont);
        JScrollPane scrollCatatan = new JScrollPane(txtJadwalCatatan);
        scrollCatatan.setPreferredSize(new Dimension(380, 56));
        formCard.add(scrollCatatan, gbc);

        cardJadwalMontir.add(formCard, BorderLayout.CENTER);

        // Bottom action bar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnSimpanJadwal = new JButton("Simpan Jadwal");
        btnSimpanJadwal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpanJadwal.setBackground(ACTIVE_BLUE);
        btnSimpanJadwal.setForeground(Color.WHITE);
        btnSimpanJadwal.setPreferredSize(new Dimension(140, 38));
        btnSimpanJadwal.setFocusPainted(false);
        btnSimpanJadwal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveSchedule();
            }
        });

        btnKembaliJadwal = new JButton("Kembali");
        btnKembaliJadwal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnKembaliJadwal.setBackground(GRAY_BUTTON);
        btnKembaliJadwal.setForeground(NAVY_BLUE);
        btnKembaliJadwal.setPreferredSize(new Dimension(100, 38));
        btnKembaliJadwal.setFocusPainted(false);
        btnKembaliJadwal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigate("KELOLA", btnNavKelola);
            }
        });

        buttonPanel.add(btnKembaliJadwal);
        buttonPanel.add(btnSimpanJadwal);
        cardJadwalMontir.add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- CARD 4: LAPORAN RESERVASI (Screenshot 7) ---
    private void createLaporanCard() {
        cardLaporan = new JPanel(new BorderLayout());
        cardLaporan.setBackground(BG_LIGHT);
        cardLaporan.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblTitle = new JLabel("Laporan Reservasi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardLaporan.add(lblTitle, BorderLayout.NORTH);

        // Content Area (Grid for date range and JTable)
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);

        // Header inputs panel matching Screenshot 7
        JPanel inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setOpaque(false);
        inputsPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Label Dari Tanggal
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblDari = new JLabel("Dari tanggal");
        lblDari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDari.setForeground(NAVY_BLUE);
        inputsPanel.add(lblDari, gbc);

        // Textfield Dari Tanggal
        gbc.gridy = 1;
        txtDariTanggal = new JTextField(LocalDate.now().minusMonths(1).toString());
        txtDariTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDariTanggal.setPreferredSize(new Dimension(140, 32));
        inputsPanel.add(txtDariTanggal, gbc);

        // Label Sampai
        gbc.gridx = 1; gbc.gridy = 0;
        JLabel lblSampai = new JLabel("Sampai");
        lblSampai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSampai.setForeground(NAVY_BLUE);
        inputsPanel.add(lblSampai, gbc);

        // Textfield Sampai
        gbc.gridy = 1;
        txtSampaiTanggal = new JTextField(LocalDate.now().toString());
        txtSampaiTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSampaiTanggal.setPreferredSize(new Dimension(140, 32));
        inputsPanel.add(txtSampaiTanggal, gbc);

        // Button Tampilkan
        gbc.gridx = 2; gbc.gridy = 1;
        btnTampilkanLaporan = new JButton("Tampilkan");
        btnTampilkanLaporan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTampilkanLaporan.setBackground(ACTIVE_BLUE);
        btnTampilkanLaporan.setForeground(Color.WHITE);
        btnTampilkanLaporan.setPreferredSize(new Dimension(120, 32));
        btnTampilkanLaporan.putClientProperty("JButton.buttonType", "roundRect");
        btnTampilkanLaporan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLaporanData();
            }
        });
        inputsPanel.add(btnTampilkanLaporan, gbc);

        // topLaporanPanel combines inputsPanel and metricsPanel
        JPanel topLaporanPanel = new JPanel(new BorderLayout());
        topLaporanPanel.setOpaque(false);
        topLaporanPanel.add(inputsPanel, BorderLayout.NORTH);

        // Metrics Widgets Panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.setBorder(new EmptyBorder(0, 8, 15, 8));

        lblWidgetTotal = new JLabel("0");
        lblWidgetSelesai = new JLabel("0");
        lblWidgetMontir = new JLabel("-");

        metricsPanel.add(createWidgetCard("TOTAL RESERVASI", lblWidgetTotal, ACTIVE_BLUE));
        metricsPanel.add(createWidgetCard("SERVIS SELESAI", lblWidgetSelesai, GREEN_BUTTON));
        metricsPanel.add(createWidgetCard("MONTIR TERAKTIF", lblWidgetMontir, new Color(139, 92, 246)));

        topLaporanPanel.add(metricsPanel, BorderLayout.CENTER);
        contentArea.add(topLaporanPanel, BorderLayout.NORTH);

        // Table Laporan
        String[] columns = {"No", "Tanggal", "Pelanggan", "Kendaraan"};
        tableModelLaporan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLaporan = new JTable(tableModelLaporan);
        tblLaporan.setDefaultRenderer(Object.class, new StatusRenderer());
        tblLaporan.setRowHeight(32);
        tblLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLaporan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLaporan.setShowVerticalLines(false);
        tblLaporan.setIntercellSpacing(new Dimension(0, 1));
        
        JScrollPane scrollTable = new JScrollPane(tblLaporan);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        contentArea.add(scrollTable, BorderLayout.CENTER);

        cardLaporan.add(contentArea, BorderLayout.CENTER);

        // Bottom Controls Panel (Export Excel & Cetak Laporan)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnExportExcel = new JButton("Export CSV");
        btnExportExcel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExportExcel.setBackground(GREEN_BUTTON);
        btnExportExcel.setForeground(Color.WHITE);
        btnExportExcel.setPreferredSize(new Dimension(140, 38));
        btnExportExcel.putClientProperty("JButton.buttonType", "roundRect");
        btnExportExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportLaporan();
            }
        });

        JButton btnPrintLaporan = new JButton("Cetak Laporan");
        btnPrintLaporan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrintLaporan.setBackground(ACTIVE_BLUE);
        btnPrintLaporan.setForeground(Color.WHITE);
        btnPrintLaporan.setPreferredSize(new Dimension(150, 38));
        btnPrintLaporan.putClientProperty("JButton.buttonType", "roundRect");
        btnPrintLaporan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePrintLaporan();
            }
        });

        bottomPanel.add(btnExportExcel);
        bottomPanel.add(btnPrintLaporan);
        cardLaporan.add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- DATA HANDLING AND EVENTS ---

    private void loadAllReservations() {
        tableModelKelola.setRowCount(0);
        String keyword = txtSearch.getText().trim();
        rawReservasiRows = adminController.getAllReservasi(keyword.isEmpty() ? null : keyword);
        

        for (Object[] row : rawReservasiRows) {
            String customNo = "RSV-2026-" + String.format("%04d", (int)row[0]);
            
            // Format customer & vehicle names
            String pelanggan = row[1].toString().split(" \\(")[0];


            tableModelKelola.addRow(new Object[]{
                customNo,
                pelanggan,
                row[3], // Tanggal
                row[4], // Jam
                row[7], // Status
                "Atur Jadwal"
            });
        }
    }

    private void loadMontirComboBox() {
        cmbJadwalMontir.removeAllItems();
        List<Montir> list = adminController.getMontirList();
        for (Montir m : list) {
            cmbJadwalMontir.addItem(m);
        }
    }

    private void loadSelectedRowToSchedule(int selectedRow) {
        Object[] rawRow = rawReservasiRows.get(selectedRow);
        currentEditingReservasiId = (int) rawRow[0];

        txtJadwalNoRes.setText("RSV-2026-" + String.format("%04d", currentEditingReservasiId));
        
        cmbJadwalPelanggan.removeAllItems();
        cmbJadwalPelanggan.addItem(rawRow[1].toString().split(" \\(")[0]);
        cmbJadwalPelanggan.setSelectedIndex(0);

        cmbJadwalKendaraan.removeAllItems();
        cmbJadwalKendaraan.addItem(rawRow[2].toString().split(" \\(")[0]);
        cmbJadwalKendaraan.setSelectedIndex(0);

        txtJadwalTanggal.setText(rawRow[3].toString());
        cmbJadwalJam.setSelectedItem(rawRow[4].toString());
        txtJadwalCatatan.setText(rawRow[5].toString());

        // Set matching montir if already assigned
        String currentMontirDesc = rawRow[6].toString();
        for (int i = 0; i < cmbJadwalMontir.getItemCount(); i++) {
            Montir m = cmbJadwalMontir.getItemAt(i);
            if (currentMontirDesc.contains(m.getNama())) {
                cmbJadwalMontir.setSelectedIndex(i);
                break;
            }
        }

        // Navigate to scheduling form card and select sidebar Nav Button for UX
        navigate("JADWAL", btnNavJadwal);
    }

    private void handleQuickStatusUpdate(String status) {
        int row = tblKelola.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu baris reservasi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object[] rawRow = rawReservasiRows.get(row);
        int idRes = (int) rawRow[0];
        String currentStatus = rawRow[7].toString();
        String montirName = rawRow[6].toString();

        // Peningkatan 7: Workflow Enforcer
        // 1. Prevent illogical updates on terminal statuses
        if ("Selesai".equalsIgnoreCase(currentStatus) || "Dibatalkan".equalsIgnoreCase(currentStatus) || "Ditolak".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Reservasi yang sudah " + currentStatus + " tidak dapat diubah lagi statusnya!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Prohibit processing or completing a service if no mechanic is assigned
        if (montirName == null || montirName.trim().isEmpty() || montirName.contains("- Belum Ditentukan -") || montirName.contains("-")) {
            JOptionPane.showMessageDialog(this, "Montir belum dialokasikan untuk reservasi ini! Harap atur jadwal & montir terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = adminController.updateStatus(idRes, status);
        if (success) {
            JOptionPane.showMessageDialog(this, "Status Reservasi ID RSV-2026-" + String.format("%04d", idRes) + " diubah menjadi '" + status + "'!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadAllReservations();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui status di database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveSchedule() {
        if (currentEditingReservasiId == -1) {
            JOptionPane.showMessageDialog(this, "Tidak ada data reservasi aktif yang sedang diedit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Montir m = (Montir) cmbJadwalMontir.getSelectedItem();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Harap daftarkan montir terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tanggal = txtJadwalTanggal.getText().trim();
        String jam = cmbJadwalJam.getSelectedItem().toString();

        // Validasi Hari dan Tanggal Operasional (Cegah tanggal lampau & Hari Minggu)
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

        // Peningkatan 6: Overlap Schedule Prevention (Deteksi Tabrakan Jadwal Montir)
        boolean available = adminController.isMontirAvailable(m.getIdMontir(), tanggal, jam, currentEditingReservasiId);
        if (!available) {
            JOptionPane.showMessageDialog(this, 
                "Montir " + m.getNama() + " sudah memiliki jadwal tugas aktif lain pada tanggal " + tanggal + " jam " + jam + "!\n" +
                "Harap tentukan montir lain atau ganti waktu reservasi.", 
                "Bentrok Jadwal Montir", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String result = adminController.assignMontirAndSchedule(currentEditingReservasiId, m.getIdMontir(), tanggal, jam);
        if ("SUCCESS".equals(result)) {
            // Auto update status to Dikonfirmasi on schedule assign
            adminController.updateStatus(currentEditingReservasiId, "Dikonfirmasi");

            JOptionPane.showMessageDialog(this, "Alokasi jadwal dan teknisi sukses!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadAllReservations();
            
            // Go back to main Kelola list
            navigate("KELOLA", btnNavKelola);
        } else {
            JOptionPane.showMessageDialog(this, result, "Gagal Menyimpan Jadwal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLaporanData() {
        tableModelLaporan.setRowCount(0);
        String dari = txtDariTanggal.getText().trim();
        String sampai = txtSampaiTanggal.getText().trim();

        rawLaporanRows = laporanController.getLaporanData(dari, sampai);

        for (Object[] row : rawLaporanRows) {
            String customNo = "RSV-2026-" + String.format("%04d", (int)row[0]);
            String pelanggan = row[3].toString();
            String kendaraan = row[5].toString();

            tableModelLaporan.addRow(new Object[]{
                customNo,
                row[1], // Tanggal
                pelanggan,
                kendaraan,
                row[8]  // Status untuk rendering warna status laporan
            });
        }

        // Peningkatan 8: Hitung Metrik Ringkasan secara dinamis dari data Laporan
        int totalReservasi = rawLaporanRows.size();
        int totalSelesai = 0;
        java.util.Map<String, Integer> montirCount = new java.util.HashMap<>();

        for (Object[] row : rawLaporanRows) {
            String status = row[8].toString();
            if ("Selesai".equalsIgnoreCase(status)) {
                totalSelesai++;
            }
            
            String montir = row[7] != null ? row[7].toString() : "-";
            if (!"-".equals(montir) && !montir.trim().isEmpty()) {
                montirCount.put(montir, montirCount.getOrDefault(montir, 0) + 1);
            }
        }

        String montirTeraktif = "-";
        int maxCount = 0;
        for (java.util.Map.Entry<String, Integer> entry : montirCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                montirTeraktif = entry.getKey();
            }
        }

        lblWidgetTotal.setText(String.valueOf(totalReservasi));
        lblWidgetSelesai.setText(String.valueOf(totalSelesai));
        if (!"-".equals(montirTeraktif)) {
            lblWidgetMontir.setText(montirTeraktif + " (" + maxCount + "x)");
        } else {
            lblWidgetMontir.setText("-");
        }

        if (rawLaporanRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada transaksi dalam rentang tanggal tersebut.", "Laporan Kosong", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExportLaporan() {
        if (tableModelLaporan.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Silakan tampilkan laporan transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Transaksi (.CSV)");
        fileChooser.setSelectedFile(new File("Laporan_Reservasi_Bengkel.csv"));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            if (!saveFile.getName().toLowerCase().endsWith(".csv")) {
                saveFile = new File(saveFile.getParentFile(), saveFile.getName() + ".csv");
            }

            String[] headers = {"No Reservasi", "Tanggal Servis", "Nama Pelanggan", "Data Kendaraan"};
            
            // Extract current preview rows
            List<Object[]> exportRows = new ArrayList<>();
            for (Object[] row : rawLaporanRows) {
                String customNo = "RSV-2026-" + String.format("%04d", (int)row[0]);
                exportRows.add(new Object[]{
                    customNo,
                    row[1], // Tanggal
                    row[3], // Pelanggan
                    row[5]  // Kendaraan
                });
            }

            boolean success = laporanController.exportToCSV(headers, exportRows, saveFile);
            if (success) {
                JOptionPane.showMessageDialog(this, "Ekspor Laporan Sukses ke:\n" + saveFile.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan file laporan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Peningkatan 8: Card Widget Builder untuk Metrik Ringkasan
    private JPanel createWidgetCard(String title, JLabel valueLabel, Color valColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_MUTED);
        card.add(lblTitle, BorderLayout.NORTH);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(valColor);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    // Peningkatan 9: Fungsi Cetak Laporan Ke Printer / PDF
    private void handlePrintLaporan() {
        if (tableModelLaporan.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Silakan tampilkan laporan transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            java.text.MessageFormat header = new java.text.MessageFormat("Laporan Reservasi Bengkel (" + txtDariTanggal.getText() + " s/d " + txtSampaiTanggal.getText() + ")");
            java.text.MessageFormat footer = new java.text.MessageFormat("Halaman {0}");
            boolean complete = tblLaporan.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Pencetakan berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Peningkatan 4 & 8: Custom Renderer untuk Pewarnaan Baris Tabel Berdasarkan Status
    private class StatusRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = "";
            if (table.getColumnCount() > 4) {
                // Untuk tabel kelola yang memiliki kolom status di index 4
                Object val = table.getValueAt(row, 4);
                status = val != null ? val.toString() : "";
            } else {
                // Untuk tabel laporan (hanya 4 kolom), ambil status secara aman dari data rawLaporanRows
                if (rawLaporanRows != null && row < rawLaporanRows.size()) {
                    Object[] rawRow = rawLaporanRows.get(row);
                    if (rawRow != null && rawRow.length > 8) {
                        status = rawRow[8] != null ? rawRow[8].toString() : "";
                    }
                }
            }
            
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
