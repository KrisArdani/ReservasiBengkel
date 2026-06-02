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

    private static final Color NAVY_BLUE = new Color(0, 32, 96); // #002060
    private static final Color ACTIVE_BLUE = new Color(25, 118, 210); // #1976D2
    private static final Color GREEN_BUTTON = new Color(40, 167, 69); // #28A745
    private static final Color GRAY_BUTTON = new Color(214, 214, 214); // #D6D6D6
    private static final Color SIDEBAR_GRAY = new Color(212, 212, 212); // #D4D4D4

    public KepalaBengkelDashboardFrame() {
        laporanController = new LaporanController();
        initializeUI();
        loadLaporanData();
    }

    private void initializeUI() {
        setTitle("Dasbor Kepala Bengkel - Sistem Reservasi Bengkel (Monitoring)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 540);
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
        btnNavLaporan = createNavButton("Laporan");
        btnNavLogout = createNavButton("Logout");

        gbcNav.gridy = 0; sidebarPanel.add(btnNavDashboard, gbcNav);
        gbcNav.gridy = 1; sidebarPanel.add(btnNavLaporan, gbcNav);

        gbcNav.gridy = 2;
        gbcNav.weighty = 1.0;
        sidebarPanel.add(Box.createGlue(), gbcNav);

        gbcNav.gridy = 3;
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
        createLaporanCard();

        contentPanel.add(cardDashboard, "DASHBOARD");
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

        JLabel lblTitle = new JLabel("MONITORING KEPALA BENGKEL");
        lblTitle.setFont(new Font("Georgia", Font.BOLD, 28));
        lblTitle.setForeground(NAVY_BLUE);
        gbc.gridy = 0;
        cardDashboard.add(lblTitle, gbc);

        JLabel lblWelcome = new JLabel("Selamat Datang, " + Session.getNama() + " (Kepala Bengkel)!");
        lblWelcome.setFont(new Font("Georgia", Font.PLAIN, 18));
        lblWelcome.setForeground(Color.BLACK);
        gbc.gridy = 1;
        cardDashboard.add(lblWelcome, gbc);

        JLabel lblInfo = new JLabel("Gunakan menu 'Laporan' di sebelah kiri untuk melihat rekapitulasi data transaksi atau mengekspornya ke Excel.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(Color.GRAY);
        gbc.gridy = 2;
        cardDashboard.add(lblInfo, gbc);
    }

    // --- CARD 2: LAPORAN CARD (Screenshot 7) ---
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

        // Content panel
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(244, 244, 244));

        // Date Inputs Grid (Dari Tanggal, Sampai, Tampilkan Button)
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

        contentArea.add(inputsPanel, BorderLayout.NORTH);

        // JTable Laporan
        String[] columns = {"No", "Tanggal", "Pelanggan", "Kendaraan"};
        tableModelLaporan = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblLaporan = new JTable(tableModelLaporan);
        tblLaporan.setRowHeight(22);
        tblLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLaporan.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        JScrollPane scrollTable = new JScrollPane(tblLaporan);
        contentArea.add(scrollTable, BorderLayout.CENTER);

        cardLaporan.add(contentArea, BorderLayout.CENTER);

        // Bottom Export Button (Green, rounded, italic)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(new Color(244, 244, 244));
        bottomPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnExportExcel = new JButton("Export Excel");
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
        bottomPanel.add(btnExportExcel);
        cardLaporan.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadLaporanData() {
        tableModelLaporan.setRowCount(0);
        String dari = txtDariTanggal.getText().trim();
        String sampai = txtSampaiTanggal.getText().trim();

        rawLaporanRows = laporanController.getLaporanData(dari, sampai);
        int no = 1;
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
            no++;
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
