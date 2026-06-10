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

    private static final Color NAVY_BLUE = new Color(0, 32, 96); // #002060
    private static final Color ACTIVE_BLUE = new Color(25, 118, 210); // #1976D2
    private static final Color GREEN_BUTTON = new Color(40, 167, 69); // #28A745
    private static final Color GRAY_BUTTON = new Color(214, 214, 214); // #D6D6D6
    private static final Color SIDEBAR_GRAY = new Color(212, 212, 212); // #D4D4D4

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
        setSize(940, 580);
        setLocationRelativeTo(null);
        setResizable(false);

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

        btnNavDashboard = createNavButton("Dashboard");
        btnNavKelola = createNavButton("Kelola Reservasi");
        btnNavJadwal = createNavButton("Jadwal & Montir");
        btnNavLaporan = createNavButton("Laporan");
        btnNavLogout = createNavButton("Logout");

        gbcNav.gridy = 0; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 1; sidebarPanel.add(btnNavKelola, gbcNav);
        gbcNav.gridy = 2; sidebarPanel.add(btnNavJadwal, gbcNav);
        gbcNav.gridy = 3; sidebarPanel.add(btnNavLaporan, gbcNav);

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
        btn.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        btn.setPreferredSize(new Dimension(180, 36));
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

        JLabel lblTitle = new JLabel("DASHBOARD ADMINISTRATOR");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.gridy = 0;
        cardDashboard.add(lblTitle, gbc);

        JLabel lblWelcome = new JLabel("Selamat Datang, " + Session.getNama() + "!");
        lblWelcome.setFont(new Font("Georgia", Font.PLAIN, 18));
        lblWelcome.setForeground(Color.BLACK);
        gbc.gridy = 1;
        cardDashboard.add(lblWelcome, gbc);

        JLabel lblInfo = new JLabel("Gunakan menu di sebelah kiri untuk mengelola reservasi servis, alokasi montir, atau mencetak laporan.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(Color.GRAY);
        gbc.gridy = 2;
        cardDashboard.add(lblInfo, gbc);
    }

    // --- CARD 2: KELOLA RESERVASI (Screenshot 5) ---
    private void createKelolaReservasiCard() {
        cardKelolaReservasi = new JPanel(new BorderLayout());
        cardKelolaReservasi.setBackground(new Color(244, 244, 244));
        cardKelolaReservasi.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel lblTitle = new JLabel("KELOLA RESERVASI");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 36));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardKelolaReservasi.add(lblTitle, BorderLayout.NORTH);

        // Center Panel for Search and Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(244, 244, 244));

        // Search panel
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBarPanel.setBackground(new Color(244, 244, 244));
        
        JLabel lblSearch = new JLabel("Cari");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchBarPanel.add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 28));
        txtSearch.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        searchBarPanel.add(txtSearch);

        btnSearch = new JButton("Cari");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(GRAY_BUTTON);
        btnSearch.setForeground(Color.BLACK);
        btnSearch.setPreferredSize(new Dimension(80, 28));
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllReservations();
            }
        });
        searchBarPanel.add(btnSearch);

        centerPanel.add(searchBarPanel, BorderLayout.NORTH);

        // JTable Table
        String[] columns = {"No", "Pelanggan...", "Tanggal", "Jam", "Status", "Aksi"};
        tableModelKelola = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKelola = new JTable(tableModelKelola);
        tblKelola.setDefaultRenderer(Object.class, new StatusRenderer());
        tblKelola.setRowHeight(22);
        tblKelola.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblKelola.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        JScrollPane scrollTable = new JScrollPane(tblKelola);
        centerPanel.add(scrollTable, BorderLayout.CENTER);

        cardKelolaReservasi.add(centerPanel, BorderLayout.CENTER);

        // Bottom Controls (Refresh & Detail Actions)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(244, 244, 244));
        bottomBar.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(GRAY_BUTTON);
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearch.setText("");
                loadAllReservations();
            }
        });
        bottomBar.add(btnRefresh, BorderLayout.WEST);

        // Action Panel
        JPanel actionSubPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionSubPanel.setBackground(new Color(244, 244, 244));

        JButton btnStatusProses = new JButton("Proses Servis");
        btnStatusProses.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnStatusProses.setBackground(ACTIVE_BLUE);
        btnStatusProses.setForeground(Color.WHITE);
        btnStatusProses.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuickStatusUpdate("Proses");
            }
        });

        JButton btnStatusSelesai = new JButton("Selesai Servis");
        btnStatusSelesai.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnStatusSelesai.setBackground(GREEN_BUTTON);
        btnStatusSelesai.setForeground(Color.WHITE);
        btnStatusSelesai.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuickStatusUpdate("Selesai");
            }
        });

        JButton btnAssignSchedule = new JButton("Atur Jadwal & Montir");
        btnAssignSchedule.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAssignSchedule.setBackground(GRAY_BUTTON);
        btnAssignSchedule.setForeground(Color.BLACK);
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
        cardJadwalMontir.setBackground(new Color(244, 244, 244));
        cardJadwalMontir.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblTitle = new JLabel("ATUR JADWAL & MONTIR");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 36));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardJadwalMontir.add(lblTitle, BorderLayout.NORTH);

        // Inputs Grid
        JPanel inputsGrid = new JPanel(new GridBagLayout());
        inputsGrid.setBackground(new Color(244, 244, 244));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Fonts
        Font gFont = new Font("Georgia", Font.PLAIN, 15);

        // No. Reservasi
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblNoRes = new JLabel("No Reservasi");
        lblNoRes.setFont(gFont);
        inputsGrid.add(lblNoRes, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtJadwalNoRes = new JTextField();
        txtJadwalNoRes.setEditable(false);
        txtJadwalNoRes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtJadwalNoRes.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(txtJadwalNoRes, gbc);

        // Pelanggan
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPelanggan = new JLabel("Pelanggan");
        lblPelanggan.setFont(gFont);
        inputsGrid.add(lblPelanggan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbJadwalPelanggan = new JComboBox<>();
        cmbJadwalPelanggan.setEnabled(false);
        cmbJadwalPelanggan.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbJadwalPelanggan, gbc);

        // Kendaraan
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblKendaraan = new JLabel("Kendaraan");
        lblKendaraan.setFont(gFont);
        inputsGrid.add(lblKendaraan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbJadwalKendaraan = new JComboBox<>();
        cmbJadwalKendaraan.setEnabled(false);
        cmbJadwalKendaraan.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbJadwalKendaraan, gbc);

        // Tanggal Servis
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblTgl = new JLabel("Tanggal Servis");
        lblTgl.setFont(gFont);
        inputsGrid.add(lblTgl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtJadwalTanggal = new JTextField();
        txtJadwalTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtJadwalTanggal.setPreferredSize(new Dimension(200, 30));
        txtJadwalTanggal.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputsGrid.add(txtJadwalTanggal, gbc);

        // Jam Servis
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblJam = new JLabel("Jam Servis");
        lblJam.setFont(gFont);
        inputsGrid.add(lblJam, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbJadwalJam = new JComboBox<>(new String[]{
            "08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
        });
        cmbJadwalJam.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbJadwalJam, gbc);

        // Montir
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel lblMontir = new JLabel("Montir");
        lblMontir.setFont(gFont);
        inputsGrid.add(lblMontir, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbJadwalMontir = new JComboBox<>();
        cmbJadwalMontir.setPreferredSize(new Dimension(200, 30));
        inputsGrid.add(cmbJadwalMontir, gbc);

        // Catatan (Complaint Area)
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblCatatan = new JLabel("Catatan");
        lblCatatan.setFont(gFont);
        inputsGrid.add(lblCatatan, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.CENTER;
        txtJadwalCatatan = new JTextArea(3, 20);
        txtJadwalCatatan.setEditable(false); // Read-only complaint
        txtJadwalCatatan.setLineWrap(true);
        txtJadwalCatatan.setWrapStyleWord(true);
        txtJadwalCatatan.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JScrollPane scrollCatatan = new JScrollPane(txtJadwalCatatan);
        inputsGrid.add(scrollCatatan, gbc);

        cardJadwalMontir.add(inputsGrid, BorderLayout.CENTER);

        // Bottom action bar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        buttonPanel.setBackground(new Color(244, 244, 244));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnSimpanJadwal = new JButton("Simpan Jadwal");
        btnSimpanJadwal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpanJadwal.setBackground(GRAY_BUTTON);
        btnSimpanJadwal.setForeground(Color.BLACK);
        btnSimpanJadwal.setPreferredSize(new Dimension(140, 36));
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
        btnKembaliJadwal.setForeground(Color.BLACK);
        btnKembaliJadwal.setPreferredSize(new Dimension(100, 36));
        btnKembaliJadwal.setFocusPainted(false);
        btnKembaliJadwal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigate("KELOLA", btnNavKelola);
            }
        });

        buttonPanel.add(btnSimpanJadwal);
        buttonPanel.add(btnKembaliJadwal);
        cardJadwalMontir.add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- CARD 4: LAPORAN RESERVASI (Screenshot 7) ---
    private void createLaporanCard() {
        cardLaporan = new JPanel(new BorderLayout());
        cardLaporan.setBackground(new Color(244, 244, 244));
        cardLaporan.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Heading
        JLabel lblTitle = new JLabel("LAPORAN RESERVASI");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 36));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        cardLaporan.add(lblTitle, BorderLayout.NORTH);

        // Content Area (Grid for date range and JTable)
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(244, 244, 244));

        // Header inputs panel matching Screenshot 7
        JPanel inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setBackground(new Color(244, 244, 244));
        inputsPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Label Dari Tanggal
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblDari = new JLabel("Dari tanggal");
        lblDari.setFont(new Font("Georgia", Font.PLAIN, 14));
        inputsPanel.add(lblDari, gbc);

        // Textfield Dari Tanggal
        gbc.gridy = 1;
        txtDariTanggal = new JTextField(LocalDate.now().minusMonths(1).toString());
        txtDariTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDariTanggal.setPreferredSize(new Dimension(130, 28));
        txtDariTanggal.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputsPanel.add(txtDariTanggal, gbc);

        // Label Sampai
        gbc.gridx = 1; gbc.gridy = 0;
        JLabel lblSampai = new JLabel("Sampai");
        lblSampai.setFont(new Font("Georgia", Font.PLAIN, 14));
        inputsPanel.add(lblSampai, gbc);

        // Textfield Sampai
        gbc.gridy = 1;
        txtSampaiTanggal = new JTextField(LocalDate.now().toString());
        txtSampaiTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSampaiTanggal.setPreferredSize(new Dimension(130, 28));
        txtSampaiTanggal.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        inputsPanel.add(txtSampaiTanggal, gbc);

        // Button Tampilkan (Blue, rounded, italic)
        gbc.gridx = 2; gbc.gridy = 1;
        btnTampilkanLaporan = new JButton("Tampilkan");
        btnTampilkanLaporan.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 13));
        btnTampilkanLaporan.setBackground(ACTIVE_BLUE);
        btnTampilkanLaporan.setForeground(Color.WHITE);
        btnTampilkanLaporan.setPreferredSize(new Dimension(120, 28));
        btnTampilkanLaporan.putClientProperty("JButton.buttonType", "roundRect");
        btnTampilkanLaporan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLaporanData();
            }
        });
        inputsPanel.add(btnTampilkanLaporan, gbc);

        // Peningkatan 8: Gabungkan inputsPanel dan metricsPanel ke panel atas Laporan
        JPanel topLaporanPanel = new JPanel(new BorderLayout());
        topLaporanPanel.setBackground(new Color(244, 244, 244));
        topLaporanPanel.add(inputsPanel, BorderLayout.NORTH);

        // Metrics Widgets Panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        metricsPanel.setBackground(new Color(244, 244, 244));
        metricsPanel.setBorder(new EmptyBorder(0, 8, 15, 8));

        lblWidgetTotal = new JLabel("0");
        lblWidgetSelesai = new JLabel("0");
        lblWidgetMontir = new JLabel("-");

        metricsPanel.add(createWidgetCard("TOTAL RESERVASI", lblWidgetTotal, NAVY_BLUE));
        metricsPanel.add(createWidgetCard("SERVIS SELESAI", lblWidgetSelesai, GREEN_BUTTON));
        metricsPanel.add(createWidgetCard("MONTIR TERAKTIF", lblWidgetMontir, ACTIVE_BLUE));

        topLaporanPanel.add(metricsPanel, BorderLayout.CENTER);
        contentArea.add(topLaporanPanel, BorderLayout.NORTH);

        // Table Laporan
        String[] columns = {"No", "Tanggal", "Pelanggan", "Kendaraan"};
        tableModelLaporan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLaporan = new JTable(tableModelLaporan);
        tblLaporan.setDefaultRenderer(Object.class, new StatusRenderer()); // Mewarnai Laporan juga!
        tblLaporan.setRowHeight(22);
        tblLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLaporan.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        JScrollPane scrollTable = new JScrollPane(tblLaporan);
        contentArea.add(scrollTable, BorderLayout.CENTER);

        cardLaporan.add(contentArea, BorderLayout.CENTER);

        // Bottom Controls Panel (Export Excel & Cetak Laporan)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomPanel.setBackground(new Color(244, 244, 244));
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnExportExcel = new JButton("Export CSV");
        btnExportExcel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        btnExportExcel.setBackground(GREEN_BUTTON);
        btnExportExcel.setForeground(Color.WHITE);
        btnExportExcel.setPreferredSize(new Dimension(140, 36));
        btnExportExcel.putClientProperty("JButton.buttonType", "roundRect");
        btnExportExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportLaporan();
            }
        });

        // Peningkatan 9: Tombol Cetak Laporan langsung ke printer / PDF
        JButton btnPrintLaporan = new JButton("Cetak Laporan");
        btnPrintLaporan.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 14));
        btnPrintLaporan.setBackground(ACTIVE_BLUE);
        btnPrintLaporan.setForeground(Color.WHITE);
        btnPrintLaporan.setPreferredSize(new Dimension(160, 36));
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
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);
        card.add(lblTitle, BorderLayout.NORTH);
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
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
