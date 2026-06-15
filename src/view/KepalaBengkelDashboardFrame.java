package view;

import config.Session;
import controller.LaporanController;

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

public class KepalaBengkelDashboardFrame extends JFrame {
    private LaporanController laporanController;

    // Layout & Navigation
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Sidebar Buttons
    private JButton btnNavDashboard;
    private JButton btnNavLaporan;
    private JButton btnNavLogout;
    private List<JButton> navButtons;

    // View Cards
    private JPanel cardDashboard;
    private JPanel cardLaporan;

    // Laporan Tab Components
    private JTextField txtDariTanggal;
    private JTextField txtSampaiTanggal;
    private JButton btnTampilkanLaporan;
    private JTable tblLaporan;
    private DefaultTableModel tableModelLaporan;
    private List<Object[]> rawLaporanRows;
    private JButton btnExportExcel;

    private static final Color NAVY_BLUE = new Color(15, 23, 42); // #0F172A (Slate 900)
    private static final Color ACTIVE_BLUE = new Color(37, 99, 235); // #2563EB (Royal Blue)
    private static final Color GREEN_BUTTON = new Color(16, 185, 129); // #10B981 (Emerald Green)
    private static final Color SIDEBAR_BG = new Color(15, 23, 42); // #0F172A (slate-900)
    private static final Color BG_LIGHT = new Color(248, 250, 252); // #F8FAFC (slate-50)
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // #64748B (slate-500)

    public KepalaBengkelDashboardFrame() {
        laporanController = new LaporanController();
        initializeUI();
        loadLaporanData();
    }

    private void initializeUI() {
        setTitle("Dasbor Kepala Bengkel - Sistem Reservasi Bengkel (Monitoring)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 580); // slightly larger for modern layout spacing
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
        String initial = nameStr.isEmpty() ? "K" : nameStr.substring(0, 1).toUpperCase();
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
        
        JLabel lblUserRole = new JLabel("Kepala Bengkel", JLabel.CENTER);
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
        btnNavLaporan = createNavButton("Laporan");
        btnNavLogout = createNavButton("Logout");

        gbcNav.gridy = 1; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 2; sidebarPanel.add(btnNavLaporan, gbcNav);

        gbcNav.gridy = 3;
        gbcNav.weighty = 1.0;
        sidebarPanel.add(Box.createGlue(), gbcNav);

        gbcNav.gridy = 4;
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
        createLaporanCard();

        contentPanel.add(cardDashboard, "DASHBOARD");
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
                    int confirm = JOptionPane.showConfirmDialog(KepalaBengkelDashboardFrame.this, 
                        "Apakah Anda yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Session.clearSession();
                        new LoginFrame().setVisible(true);
                        dispose();
                    }
                } else if ("Dashboard".equalsIgnoreCase(text)) {
                    navigate("DASHBOARD", btn);
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
        cardDashboard.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("Monitoring Kepala Bengkel");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(NAVY_BLUE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblWelcome = new JLabel("Selamat Datang kembali, " + Session.getNama() + " (Kepala Bengkel)!");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblWelcome.setForeground(TEXT_MUTED);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblWelcome.setBorder(new EmptyBorder(4, 0, 30, 0));

        headerPanel.add(lblTitle);
        headerPanel.add(lblWelcome);
        cardDashboard.add(headerPanel, BorderLayout.NORTH);

        // Shortcut card
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 24, 0));
        cardsGrid.setOpaque(false);

        JPanel cardLaporanShort = new JPanel(new BorderLayout()) {
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
        cardLaporanShort.setOpaque(false);
        cardLaporanShort.setBorder(new EmptyBorder(24, 24, 24, 24));
        cardLaporanShort.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(ACTIVE_BLUE);
        accentBar.setPreferredSize(new Dimension(6, 40));
        cardLaporanShort.add(accentBar, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(new EmptyBorder(0, 16, 0, 0));

        JLabel lblCardTitle = new JLabel("Laporan Reservasi");
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCardTitle.setForeground(NAVY_BLUE);
        lblCardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea taDesc = new JTextArea("Pantau data historis pengerjaan servis bengkel secara terperinci serta ekspor data ke Excel.");
        taDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taDesc.setForeground(TEXT_MUTED);
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        taDesc.setEditable(false);
        taDesc.setFocusable(false);
        taDesc.setOpaque(false);
        taDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        taDesc.setBorder(new EmptyBorder(8, 0, 0, 0));

        textPanel.add(lblCardTitle);
        textPanel.add(taDesc);
        cardLaporanShort.add(textPanel, BorderLayout.CENTER);

        cardLaporanShort.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                navigate("LAPORAN", btnNavLaporan);
            }
        });

        cardsGrid.add(cardLaporanShort);
        
        // Empty placeholder card to keep alignment neat
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        cardsGrid.add(placeholder);

        cardDashboard.add(cardsGrid, BorderLayout.CENTER);
    }

    // --- CARD 2: LAPORAN CARD (Screenshot 7) ---
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

        // Content panel
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);

        // Date Inputs Grid (Dari Tanggal, Sampai, Tampilkan Button)
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

        contentArea.add(inputsPanel, BorderLayout.NORTH);

        // JTable Laporan
        String[] columns = {"No", "Tanggal", "Pelanggan", "Kendaraan"};
        tableModelLaporan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLaporan = new JTable(tableModelLaporan);
        tblLaporan.setRowHeight(32);
        tblLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLaporan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLaporan.setShowVerticalLines(false);
        tblLaporan.setIntercellSpacing(new Dimension(0, 1));
        
        JScrollPane scrollTable = new JScrollPane(tblLaporan);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        contentArea.add(scrollTable, BorderLayout.CENTER);

        cardLaporan.add(contentArea, BorderLayout.CENTER);

        // Bottom Export Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnExportExcel = new JButton("Export Excel");
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
        bottomPanel.add(btnExportExcel);
        cardLaporan.add(bottomPanel, BorderLayout.SOUTH);
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
                kendaraan
            });
        }

        if (rawLaporanRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data transaksi dalam rentang tanggal tersebut.", "Laporan Kosong", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExportLaporan() {
        if (tableModelLaporan.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Silakan tampilkan laporan transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Transaksi (.CSV)");
        fileChooser.setSelectedFile(new File("Laporan_Monitoring_Bengkel.csv"));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            if (!saveFile.getName().toLowerCase().endsWith(".csv")) {
                saveFile = new File(saveFile.getParentFile(), saveFile.getName() + ".csv");
            }

            String[] headers = {"No Reservasi", "Tanggal Servis", "Nama Pelanggan", "Data Kendaraan"};
            List<Object[]> exportRows = new ArrayList<>();
            for (Object[] row : rawLaporanRows) {
                String customNo = "RSV-2026-" + String.format("%04d", (int)row[0]);
                exportRows.add(new Object[]{
                    customNo,
                    row[1],
                    row[3],
                    row[5]
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
}
