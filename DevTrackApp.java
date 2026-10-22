/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package DT;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevTrackApp extends JFrame {

    // MySQL Database Configuration 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/DevTrack";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root";

    // Modern Professional Color Palette (Tailwind Slate & Indigo Inspired)
    private static final Color BG_MAIN = new Color(241, 245, 249);       // Slate 100
    private static final Color BG_CARD = Color.WHITE;                    // Pure White
    private static final Color SIDEBAR_BG = new Color(15, 23, 42);       // Slate 900
    private static final Color SIDEBAR_HOVER = new Color(30, 41, 59);    // Slate 800
    private static final Color SIDEBAR_ACTIVE = new Color(79, 70, 229);  // Indigo 600
    private static final Color PRIMARY = new Color(79, 70, 229);         // Indigo 600
    private static final Color SUCCESS = new Color(16, 185, 129);        // Emerald 500
    private static final Color TEXT_DARK = new Color(15, 23, 42);        // Slate 900
    private static final Color TEXT_MUTED = new Color(100, 116, 139);    // Slate 500
    private static final Color BORDER_COLOR = new Color(226, 232, 240);  // Slate 200

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private List<JButton> navButtons = new ArrayList<>();

    // Ticket Module Components
    private DefaultTableModel ticketTableModel;
    private JTable ticketTable;
    private List<JCheckBox> prCheckboxes = new ArrayList<>();
    private JPanel checkListContainer;

    // Timer & Time-Logger Components
    private JComboBox<String> cmbActiveTicket;
    private JLabel lblTimerDisplay;
    private Timer swingTimer;
    private int elapsedSeconds = 0;
    private int pomodoroTargetSeconds = 25 * 60;
    private boolean isPomodoroMode = true;
    private DefaultTableModel timeLogModel;

    // KT Vault Module Components
    private DefaultTableModel vaultTableModel;
    private JTable vaultTable;
    private JTextArea vaultSolutionArea;

    public DevTrackApp() {
        setTitle("DevTrack Pro — Fresher Work & Onboarding Workbench");
        setSize(1380, 760);
        setMinimumSize(new Dimension(1380, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Left Navigation Sidebar
        add(createSidebar(), BorderLayout.WEST);

        // 2. Center Content Area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_MAIN);

        mainContentPanel.add(createTicketModule(), "TICKETS");
        mainContentPanel.add(createTimeTrackerModule(), "TIMER");
        mainContentPanel.add(createStandupModule(), "STANDUP");
        mainContentPanel.add(createVaultModule(), "VAULT");

        add(mainContentPanel, BorderLayout.CENTER);
    }

    // JDBC Connection Helper
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver (connector) is missing from your project libraries!");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // =========================================================================
    // SIDEBAR NAVIGATION
    // =========================================================================
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 18, 25, 18));

        JLabel brandLabel = new JLabel("DevTrack Pro");
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Junior Developer Workbench");
        subLabel.setForeground(new Color(148, 163, 184));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brandLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(subLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 35)));

        JLabel menuHeader = new JLabel("WORKSPACE");
        menuHeader.setForeground(new Color(100, 116, 139));
        menuHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuHeader);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnTickets = createNavButton("  Tickets & PR Guardrail", "TICKETS", true);
        JButton btnTimer = createNavButton("  Pomodoro & Time Log", "TIMER", false);
        JButton btnStandup = createNavButton("  Daily Standup Builder", "STANDUP", false);
        JButton btnVault = createNavButton("  KT & Error Fix Vault", "VAULT", false);

        sidebar.add(btnTickets);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnTimer);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnStandup);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(btnVault);

        sidebar.add(Box.createVerticalGlue());

        JButton btnExportCSV = createModernButton("Export Timesheet (CSV)", SUCCESS, Color.WHITE);
        btnExportCSV.setMaximumSize(new Dimension(220, 40));
        btnExportCSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExportCSV.addActionListener(e -> exportTicketsToCSV());

        sidebar.add(btnExportCSV);
        return sidebar;
    }

    private JButton createNavButton(String text, String cardName, boolean isDefaultActive) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(isDefaultActive ? SIDEBAR_ACTIVE : SIDEBAR_BG);
        btn.setForeground(isDefaultActive ? Color.WHITE : new Color(203, 213, 225));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        navButtons.add(btn);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_ACTIVE) {
                    btn.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_ACTIVE) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            for (JButton b : navButtons) {
                b.setBackground(SIDEBAR_BG);
                b.setForeground(new Color(203, 213, 225));
            }
            btn.setBackground(SIDEBAR_ACTIVE);
            btn.setForeground(Color.WHITE);
            refreshTicketDropdown();
            cardLayout.show(mainContentPanel, cardName);
        });
        return btn;
    }

    // =========================================================================
    // MODULE 1: MY TICKETS & PRE-PR SELF-REVIEW GUARDRAIL (MySQL CONNECTED)
    // =========================================================================
    private JPanel createTicketModule() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel topCard = createCardPanel("DevTrack Ticket — Assign New Task");
    topCard.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 6, 5, 6);  
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    JTextField txtCode = createStyledTextField("", 7);
    JTextField txtTitle = createStyledTextField("", 14);

    String[] defaultTypes = {"", "BUG", "FEATURE", "SQL_CHANGE", "REFACTOR", "HOTFIX"};
    JComboBox<String> cmbType = new JComboBox<>(defaultTypes);
    cmbType.setEditable(true);
    cmbType.setFont(FONT_REGULAR);
    cmbType.setPreferredSize(new Dimension(120, 32));
    cmbType.setBackground(Color.WHITE);
    cmbType.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(2, 4, 2, 4)
    ));
    Component editorComp = cmbType.getEditor().getEditorComponent();
    if (editorComp instanceof JTextField) {
        ((JTextField) editorComp).setBorder(BorderFactory.createEmptyBorder());
        ((JTextField) editorComp).setBackground(Color.WHITE);
    }

    JTextField txtBranch = createStyledTextField("", 10);

    // Date Picker Component
    JTextField txtDate = createStyledTextField(LocalDate.now().toString(), 10);
    JButton btnPickDate = createModernButton("📅", new Color(226, 232, 240), TEXT_DARK);
    btnPickDate.setPreferredSize(new Dimension(35, 32));
    btnPickDate.setToolTipText("Select date from calendar");

    btnPickDate.addActionListener(e -> {
        String selectedDate = showDatePickerDialog(this);
        if (selectedDate != null) {
            txtDate.setText(selectedDate);
        }
    });

    JPanel datePanel = new JPanel(new BorderLayout(4, 0));
    datePanel.setOpaque(false);
    datePanel.add(txtDate, BorderLayout.CENTER);
    datePanel.add(btnPickDate, BorderLayout.EAST);

    JComboBox<String> cmbPriority = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});
    cmbPriority.setFont(FONT_REGULAR);
    cmbPriority.setPreferredSize(new Dimension(90, 32));

    JButton btnAdd = createModernButton("+ Add Ticket", PRIMARY, Color.WHITE);
    btnAdd.setPreferredSize(new Dimension(130, 34));

    // --- ROW 1 ---
    gbc.gridy = 0;
    gbc.gridx = 0; gbc.weightx = 0.0; topCard.add(createFieldLabel("Ticket ID:"), gbc);
    gbc.gridx = 1; gbc.weightx = 0.2; topCard.add(txtCode, gbc);

    gbc.gridx = 2; gbc.weightx = 0.0; topCard.add(createFieldLabel("Title:"), gbc);
    gbc.gridx = 3; gbc.weightx = 0.6; topCard.add(txtTitle, gbc); // Title takes up maximum stretch space

    gbc.gridx = 4; gbc.weightx = 0.0; topCard.add(createFieldLabel("Type:"), gbc);
    gbc.gridx = 5; gbc.weightx = 0.2; topCard.add(cmbType, gbc);

    // --- ROW 2 ---
    gbc.gridy = 1;
    gbc.gridx = 0; gbc.weightx = 0.0; topCard.add(createFieldLabel("Branch:"), gbc);
    gbc.gridx = 1; gbc.weightx = 0.2; topCard.add(txtBranch, gbc);

    gbc.gridx = 2; gbc.weightx = 0.0; topCard.add(createFieldLabel("Assigned Date:"), gbc);
    gbc.gridx = 3; gbc.weightx = 0.6; topCard.add(datePanel, gbc);

    gbc.gridx = 4; gbc.weightx = 0.0; topCard.add(createFieldLabel("Priority:"), gbc);

    JPanel actionGroup = new JPanel(new BorderLayout(8, 0));
    actionGroup.setOpaque(false);
    actionGroup.add(cmbPriority, BorderLayout.CENTER);
    actionGroup.add(btnAdd, BorderLayout.EAST);

    gbc.gridx = 5; gbc.weightx = 0.2; topCard.add(actionGroup, gbc);

        JPanel tableCard = createCardPanel("Active Sprint Tickets (Loaded from MySQL)");
        tableCard.setLayout(new BorderLayout(0, 8));

        String[] columns = {"Ticket ID", "Title", "Type", "Git Branch", "Priority", "Status", "Time (Mins)", "Assigned Date"};
        ticketTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load Tickets from MySQL Database on startup
        loadTicketsFromDatabase();

        ticketTable = new JTable(ticketTableModel);
        styleModernTable(ticketTable);

        TableRowSorter<DefaultTableModel> ticketSorter = new TableRowSorter<>(ticketTableModel);
        ticketTable.setRowSorter(ticketSorter);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        filterBar.setOpaque(false);

        JComboBox<String> cmbFilterStatus = new JComboBox<>(new String[]{
                "ALL", "TODO", "IN_PROGRESS", "CODE_REVIEW", "QA_TESTING", "DONE", "CANCELLED"
        });
        cmbFilterStatus.setFont(FONT_REGULAR);
        cmbFilterStatus.setPreferredSize(new Dimension(150, 30));

        cmbFilterStatus.addActionListener(e -> {
            String selected = cmbFilterStatus.getSelectedItem().toString();
            if ("ALL".equals(selected)) {
                ticketSorter.setRowFilter(null);
            } else {
                ticketSorter.setRowFilter(RowFilter.regexFilter("^" + selected + "$", 5));
            }
        });

        filterBar.add(createFieldLabel("Filter by Status:"));
        filterBar.add(cmbFilterStatus);
        tableCard.add(filterBar, BorderLayout.NORTH);

        ticketTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(FONT_BOLD);
                String status = String.valueOf(value);
                if (!isSelected) {
                    switch (status) {
                        case "DONE":
                            lbl.setBackground(new Color(209, 250, 229));
                            lbl.setForeground(new Color(6, 95, 70));
                            break;
                        case "CODE_REVIEW":
                        case "QA_TESTING":
                            lbl.setBackground(new Color(224, 231, 255));
                            lbl.setForeground(new Color(55, 48, 163));
                            break;
                        case "IN_PROGRESS":
                        case "TODO":
                            lbl.setBackground(new Color(254, 243, 199));
                            lbl.setForeground(new Color(146, 64, 14));
                            break;
                        case "CANCELLED":
                            lbl.setBackground(new Color(254, 226, 226));
                            lbl.setForeground(new Color(153, 27, 27));
                            break;
                        default:
                            lbl.setBackground(new Color(241, 245, 249));
                            lbl.setForeground(new Color(71, 85, 105));
                            break;
                    }
                }
                return lbl;
            }
        });

        btnAdd.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String title = txtTitle.getText().trim();
            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Ticket ID and Title.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object rawType = cmbType.getEditor().getItem();
            String finalType = (rawType == null || rawType.toString().trim().isEmpty())
                    ? "N/A" : rawType.toString().trim();
            String branch = txtBranch.getText().trim();
            String targetDate = txtDate.getText().trim();
            String priority = cmbPriority.getSelectedItem().toString();

            // Save to MySQL DB
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO work_tickets (ticket_code, title, ticket_type, git_branch, priority, status, time_logged, assigned_date) VALUES (?, ?, ?, ?, ?, 'TODO', 0, ?)")) {
                ps.setString(1, code);
                ps.setString(2, title);
                ps.setString(3, finalType);
                ps.setString(4, branch);
                ps.setString(5, priority);
                ps.setString(6, targetDate);
                ps.executeUpdate();

                loadTicketsFromDatabase();
                txtCode.setText("");
                txtTitle.setText("");
                txtBranch.setText("");
                txtDate.setText(LocalDate.now().toString());
                cmbType.setSelectedItem("");
                refreshTicketDropdown();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error (Ticket ID might already exist):\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane tableScroll = new JScrollPane(ticketTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        JPanel bottomCard = createCardPanel("Pre-PR Self-Review Checklist (Mandatory Before Code Review)");
        bottomCard.setLayout(new BorderLayout(10, 12));

        checkListContainer = new JPanel(new GridLayout(0, 2, 12, 8));
        checkListContainer.setOpaque(false);

        String[] defaultChecks = {
            "Removed all System.out.println() & debug code",
            "Handled NullPointerException & edge cases",
            "Used PreparedStatement / parameterized queries",
            "Pulled latest changes from main/develop branch",
            "Tested locally with valid and invalid inputs"
        };
        for (String item : defaultChecks) {
            addChecklistItem(item);
        }

        JScrollPane checkScroll = new JScrollPane(checkListContainer);
        checkScroll.setPreferredSize(new Dimension(100, 95));
        checkScroll.setBorder(BorderFactory.createEmptyBorder());
        checkScroll.getViewport().setBackground(BG_CARD);

        JPanel bottomFooter = new JPanel(new BorderLayout(15, 0));
        bottomFooter.setOpaque(false);
        bottomFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JPanel customCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        customCheckPanel.setOpaque(false);
        JTextField txtCustomCheck = createStyledTextField("", 22);
        txtCustomCheck.setToolTipText("Type your own custom review rule or note...");
        JButton btnAddCustomCheck = createModernButton("+ Add Custom Check", new Color(51, 65, 85), Color.WHITE);

        btnAddCustomCheck.addActionListener(e -> {
            String customRule = txtCustomCheck.getText().trim();
            if (customRule.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Type a custom checklist item first.");
                return;
            }
            addChecklistItem(customRule);
            txtCustomCheck.setText("");
            checkListContainer.revalidate();
            checkListContainer.repaint();
        });

        customCheckPanel.add(createFieldLabel("Custom Check:"));
        customCheckPanel.add(txtCustomCheck);
        customCheckPanel.add(btnAddCustomCheck);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        actionPanel.setOpaque(false);

        JComboBox<String> cmbNewStatus = new JComboBox<>(new String[]{
                "TODO", "IN_PROGRESS", "CODE_REVIEW", "QA_TESTING", "DONE", "CANCELLED"
        });
        cmbNewStatus.setFont(FONT_REGULAR);
        cmbNewStatus.setPreferredSize(new Dimension(130, 32));

        JButton btnCopyBranch = createModernButton("Copy Git Checkout", new Color(226, 232, 240), TEXT_DARK);
        JButton btnUpdateStatus = createModernButton("Update Status", PRIMARY, Color.WHITE);

        btnCopyBranch.addActionListener(e -> {
            int viewRow = ticketTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a ticket from the table first.");
                return;
            }
            int modelRow = ticketTable.convertRowIndexToModel(viewRow);
            String branch = ticketTableModel.getValueAt(modelRow, 3).toString();
            copyToClipboard("git checkout -b " + branch);
            JOptionPane.showMessageDialog(this, "Copied to clipboard:\ngit checkout -b " + branch);
        });

        btnUpdateStatus.addActionListener(e -> {
            int viewRow = ticketTable.getSelectedRow();
            if (viewRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a ticket from the table first.");
                return;
            }
            int modelRow = ticketTable.convertRowIndexToModel(viewRow);
            String ticketCode = ticketTableModel.getValueAt(modelRow, 0).toString();
            String targetStatus = cmbNewStatus.getSelectedItem().toString();

            if ("CODE_REVIEW".equals(targetStatus)) {
                for (JCheckBox cb : prCheckboxes) {
                    if (!cb.isSelected()) {
                        JOptionPane.showMessageDialog(this,
                                "Pre-PR Guardrail Blocked!\nPlease verify and tick ALL checklist items before moving to CODE_REVIEW.",
                                "Incomplete Self-Review", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE work_tickets SET status = ? WHERE ticket_code = ?")) {
                ps.setString(1, targetStatus);
                ps.setString(2, ticketCode);
                ps.executeUpdate();

                ticketTableModel.setValueAt(targetStatus, modelRow, 5);
                ticketTable.repaint();
                JOptionPane.showMessageDialog(this, "Ticket status updated in MySQL DB.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(btnCopyBranch);
        actionPanel.add(createFieldLabel("Status:"));
        actionPanel.add(cmbNewStatus);
        actionPanel.add(btnUpdateStatus);

        bottomFooter.add(customCheckPanel, BorderLayout.WEST);
        bottomFooter.add(actionPanel, BorderLayout.EAST);

        bottomCard.add(checkScroll, BorderLayout.CENTER);
        bottomCard.add(bottomFooter, BorderLayout.SOUTH);

        panel.add(topCard, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        panel.add(bottomCard, BorderLayout.SOUTH);

        return panel;
    }

    private String showDatePickerDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Select Assigned Date", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(300, 220);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_CARD);

        LocalDate now = LocalDate.now();
        JTextField txtYear = createStyledTextField(String.valueOf(now.getYear()), 6);
        JComboBox<Integer> cmbMonth = new JComboBox<>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
        cmbMonth.setSelectedItem(now.getMonthValue());
        JComboBox<Integer> cmbDay = new JComboBox<>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31});
        cmbDay.setSelectedItem(now.getDayOfMonth());

        panel.add(createFieldLabel("Year:"));
        panel.add(txtYear);
        panel.add(createFieldLabel("Month:"));
        panel.add(cmbMonth);
        panel.add(createFieldLabel("Day:"));
        panel.add(cmbDay);

        final String[] resultDate = {null};
        JButton btnOk = createModernButton("Set Date", PRIMARY, Color.WHITE);
        btnOk.addActionListener(e -> {
            try {
                int y = Integer.parseInt(txtYear.getText().trim());
                int m = (Integer) cmbMonth.getSelectedItem();
                int d = (Integer) cmbDay.getSelectedItem();
                resultDate[0] = String.format("%04d-%02d-%02d", y, m, d);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid Year format.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btnOk);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return resultDate[0];
    }

    private void loadTicketsFromDatabase() {
        if (ticketTableModel == null) return;
        ticketTableModel.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ticket_code, title, ticket_type, git_branch, priority, status, time_logged, assigned_date FROM work_tickets ORDER BY created_at DESC")) {
            while (rs.next()) {
                ticketTableModel.addRow(new Object[]{
                        rs.getString("ticket_code"),
                        rs.getString("title"),
                        rs.getString("ticket_type"),
                        rs.getString("git_branch"),
                        rs.getString("priority"),
                        rs.getString("status"),
                        rs.getInt("time_logged"),
                        rs.getString("assigned_date") != null ? rs.getString("assigned_date") : LocalDate.now().toString()
                });
            }
        } catch (SQLException e) {
            System.err.println("MySQL Connection Note: " + e.getMessage());
        }
    }

    private void addChecklistItem(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(FONT_REGULAR);
        cb.setForeground(TEXT_DARK);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
        prCheckboxes.add(cb);
        checkListContainer.add(cb);
    }

    // =========================================================================
    // MODULE 2: POMODORO & TICKET TIME-SPENT LOGGER (MySQL CONNECTED)
    // =========================================================================
    private JPanel createTimeTrackerModule() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel timerCard = createCardPanel("Active Ticket Focus Timer (Pomodoro & Work Logger)");
        timerCard.setLayout(new BorderLayout(10, 15));

        JPanel topSelectPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        topSelectPanel.setOpaque(false);

        cmbActiveTicket = new JComboBox<>();
        cmbActiveTicket.setFont(FONT_REGULAR);
        cmbActiveTicket.setPreferredSize(new Dimension(320, 32));
        refreshTicketDropdown();

        JComboBox<String> cmbMode = new JComboBox<>(new String[]{"Pomodoro (25 Mins Countdown)", "Stopwatch (Count Up)"});
        cmbMode.setFont(FONT_REGULAR);
        cmbMode.setPreferredSize(new Dimension(220, 32));
        cmbMode.addActionListener(e -> {
            stopTimer();
            isPomodoroMode = cmbMode.getSelectedIndex() == 0;
            elapsedSeconds = 0;
            updateTimerLabel();
        });

        topSelectPanel.add(createFieldLabel("Working On Ticket:"));
        topSelectPanel.add(cmbActiveTicket);
        topSelectPanel.add(createFieldLabel("Mode:"));
        topSelectPanel.add(cmbMode);

        lblTimerDisplay = new JLabel("25:00", SwingConstants.CENTER);
        lblTimerDisplay.setFont(new Font("Segoe UI", Font.BOLD, 68));
        lblTimerDisplay.setForeground(PRIMARY);

        JPanel controlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        controlButtons.setOpaque(false);

        JButton btnStart = createModernButton("Start Session", SUCCESS, Color.WHITE);
        JButton btnPause = createModernButton("Pause", new Color(226, 232, 240), TEXT_DARK);
        JButton btnLogAndReset = createModernButton("Stop & Log Time to Ticket", PRIMARY, Color.WHITE);

        swingTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            updateTimerLabel();
            if (isPomodoroMode && elapsedSeconds >= pomodoroTargetSeconds) {
                stopTimer();
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "Pomodoro Session Complete! Take a 5-minute break.");
            }
        });

        btnStart.addActionListener(e -> swingTimer.start());
        btnPause.addActionListener(e -> stopTimer());
        btnLogAndReset.addActionListener(e -> logElapsedTimeToTicket());

        controlButtons.add(btnStart);
        controlButtons.add(btnPause);
        controlButtons.add(btnLogAndReset);

        timerCard.add(topSelectPanel, BorderLayout.NORTH);
        timerCard.add(lblTimerDisplay, BorderLayout.CENTER);
        timerCard.add(controlButtons, BorderLayout.SOUTH);

        JPanel logCard = createCardPanel("Today's Logged Work Sessions");
        logCard.setLayout(new BorderLayout());

        String[] logCols = {"Date", "Ticket ID", "Minutes Logged", "Session Note"};
        timeLogModel = new DefaultTableModel(logCols, 0);

        JTable logTable = new JTable(timeLogModel);
        styleModernTable(logTable);

        JScrollPane logScroll = new JScrollPane(logTable);
        logScroll.setBorder(BorderFactory.createEmptyBorder());
        logScroll.getViewport().setBackground(Color.WHITE);
        logCard.add(logScroll, BorderLayout.CENTER);

        panel.add(timerCard, BorderLayout.NORTH);
        panel.add(logCard, BorderLayout.CENTER);

        return panel;
    }

    private void updateTimerLabel() {
        int displaySecs = isPomodoroMode ? Math.max(0, pomodoroTargetSeconds - elapsedSeconds) : elapsedSeconds;
        int mins = displaySecs / 60;
        int secs = displaySecs % 60;
        lblTimerDisplay.setText(String.format("%02d:%02d", mins, secs));
    }

    private void stopTimer() {
        if (swingTimer != null && swingTimer.isRunning()) {
            swingTimer.stop();
        }
    }

    private void logElapsedTimeToTicket() {
        stopTimer();
        int minutesWorked = Math.max(1, elapsedSeconds / 60);
        if (elapsedSeconds == 0) {
            String manualInput = JOptionPane.showInputDialog(this, "Timer is at 0. Enter minutes manually to log:", "15");
            if (manualInput == null || manualInput.trim().isEmpty()) {
                return;
            }
            try {
                minutesWorked = Integer.parseInt(manualInput.trim());
            } catch (NumberFormatException ex) {
                return;
            }
        }

        String selectedItem = (String) cmbActiveTicket.getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        String ticketCode = selectedItem.split(" - ")[0];

        String note = JOptionPane.showInputDialog(this, "Brief note on what you completed (" + minutesWorked + " mins):", "Implemented logic & tested");
        if (note == null) {
            note = "General Development";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE work_tickets SET time_logged = time_logged + ? WHERE ticket_code = ?")) {
            ps.setInt(1, minutesWorked);
            ps.setString(2, ticketCode);
            ps.executeUpdate();

            loadTicketsFromDatabase();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        timeLogModel.addRow(new Object[]{LocalDate.now().toString(), ticketCode, minutesWorked, note});
        elapsedSeconds = 0;
        updateTimerLabel();
        JOptionPane.showMessageDialog(this, "Logged " + minutesWorked + " mins to " + ticketCode + " in MySQL!");
    }

    private void refreshTicketDropdown() {
        if (cmbActiveTicket == null || ticketTableModel == null) {
            return;
        }
        cmbActiveTicket.removeAllItems();
        for (int i = 0; i < ticketTableModel.getRowCount(); i++) {
            String code = ticketTableModel.getValueAt(i, 0).toString();
            String title = ticketTableModel.getValueAt(i, 1).toString();
            cmbActiveTicket.addItem(code + " - " + title);
        }
    }

    // =========================================================================
    // MODULE 3: DAILY STANDUP LOGGER
    // =========================================================================
    private JPanel createStandupModule() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel header = new JLabel("Daily Scrum / Standup Generator — " + LocalDate.now());
        header.setFont(FONT_TITLE);
        header.setForeground(TEXT_DARK);

        JPanel formPanel = new JPanel(new GridLayout(3, 1, 0, 14));
        formPanel.setOpaque(false);

        JTextArea txtYesterday = createStyledTextArea("- Completed login validation bug fix (PROJ-101)\n- Fixed SQL query in attendance report");
        JTextArea txtToday = createStyledTextArea("- Working on Excel export controller (PROJ-102)\n- Writing JUnit tests for DAO layer");
        JTextArea txtBlockers = createStyledTextArea("None");

        formPanel.add(wrapInCardScroll(txtYesterday, "1. What did I complete yesterday?"));
        formPanel.add(wrapInCardScroll(txtToday, "2. What am I working on today?"));
        formPanel.add(wrapInCardScroll(txtBlockers, "3. Any Blockers / Questions for Senior?"));

        JButton btnCopyStandup = createModernButton("Generate & Copy Standup Summary", PRIMARY, Color.WHITE);
        btnCopyStandup.setPreferredSize(new Dimension(200, 44));

        btnCopyStandup.addActionListener(e -> {
            String summary = "**Daily Standup Update (" + LocalDate.now() + ")**\n\n"
                    + "**Yesterday:**\n" + txtYesterday.getText().trim() + "\n\n"
                    + "**Today:**\n" + txtToday.getText().trim() + "\n\n"
                    + "**Blockers:**\n" + txtBlockers.getText().trim();
            copyToClipboard(summary);
            JOptionPane.showMessageDialog(this, "Standup update copied to clipboard!");
        });

        panel.add(header, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(btnCopyStandup, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // MODULE 4: KT & ERROR SOLUTION VAULT (MySQL CONNECTED)
    // =========================================================================
    private JPanel createVaultModule() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel searchCard = createCardPanel("Search Knowledge Transfer (KT) Notes, Exceptions & SQL Snippets");
        searchCard.setLayout(new BorderLayout(10, 0));

        JTextField txtSearch = createStyledTextField("", 30);
        JButton btnAddNote = createModernButton("+ Save New Note / Solution", PRIMARY, Color.WHITE);
        btnAddNote.addActionListener(e -> showAddVaultDialog());

        searchCard.add(txtSearch, BorderLayout.CENTER);
        searchCard.add(btnAddNote, BorderLayout.EAST);

        String[] cols = {"Category", "Error / Topic Title", "Solution / Command / Snippet"};
        vaultTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadVaultEntriesFromDatabase();

        vaultTable = new JTable(vaultTableModel);
        styleModernTable(vaultTable);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(vaultTableModel);
        vaultTable.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }

            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        vaultSolutionArea = new JTextArea("Select an entry above to view the full solution...");
        vaultSolutionArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        vaultSolutionArea.setForeground(TEXT_DARK);
        vaultSolutionArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        vaultSolutionArea.setEditable(false);

        vaultTable.getSelectionModel().addListSelectionListener(e -> {
            int viewRow = vaultTable.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = vaultTable.convertRowIndexToModel(viewRow);
                vaultSolutionArea.setText(vaultTableModel.getValueAt(modelRow, 2).toString());
            }
        });

        JScrollPane tableScroll = new JScrollPane(vaultTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);

        JPanel topSplitCard = createCardPanel("Saved Entries");
        topSplitCard.setLayout(new BorderLayout());
        topSplitCard.add(tableScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                topSplitCard,
                wrapInCardScroll(vaultSolutionArea, "Solution"));
        splitPane.setDividerLocation(260);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOpaque(false);

        panel.add(searchCard, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadVaultEntriesFromDatabase() {
        if (vaultTableModel == null) return;
        vaultTableModel.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category, title, solution FROM knowledge_vault ORDER BY entry_id DESC")) {
            while (rs.next()) {
                vaultTableModel.addRow(new Object[]{
                        rs.getString("category"),
                        rs.getString("title"),
                        rs.getString("solution")
                });
            }
        } catch (SQLException e) {
            System.err.println("MySQL Vault Load Note: " + e.getMessage());
        }
    }

    private void showAddVaultDialog() {
        JComboBox<String> cmbCat = new JComboBox<>(new String[]{"ERROR_FIX", "KT_NOTE", "SQL_SNIPPET", "GIT_CMD"});
        cmbCat.setEditable(true);
        JTextField txtTitle = createStyledTextField("", 20);
        JTextArea txtSol = createStyledTextArea("");
        txtSol.setRows(5);

        JPanel myPanel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
        top.add(new JLabel("Category:"));
        top.add(cmbCat);
        top.add(new JLabel("Topic / Exception:"));
        top.add(txtTitle);

        myPanel.add(top, BorderLayout.NORTH);
        myPanel.add(new JScrollPane(txtSol), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, myPanel,
                "Add New Knowledge Vault Entry", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && !txtTitle.getText().trim().isEmpty()) {
            String category = cmbCat.getEditor().getItem().toString();
            String title = txtTitle.getText().trim();
            String solution = txtSol.getText().trim();

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO knowledge_vault (category, title, solution) VALUES (?, ?, ?)")) {
                ps.setString(1, category);
                ps.setString(2, title);
                ps.setString(3, solution);
                ps.executeUpdate();

                loadVaultEntriesFromDatabase();
                JOptionPane.showMessageDialog(this, "KT Note saved to MySQL DB!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // CSV TIMESHEET EXPORT & MODERN UI HELPERS
    // =========================================================================
    private void exportTicketsToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Weekly Ticket & Timesheet Report");
        fileChooser.setSelectedFile(new File("Fresher_Timesheet_" + LocalDate.now() + ".csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                for (int i = 0; i < ticketTableModel.getColumnCount(); i++) {
                    fw.write(ticketTableModel.getColumnName(i) + (i == ticketTableModel.getColumnCount() - 1 ? "" : ","));
                }
                fw.write("\n");

                for (int r = 0; r < ticketTableModel.getRowCount(); r++) {
                    for (int c = 0; c < ticketTableModel.getColumnCount(); c++) {
                        String val = String.valueOf(ticketTableModel.getValueAt(r, c)).replace(",", " ");
                        fw.write(val + (c == ticketTableModel.getColumnCount() - 1 ? "" : ","));
                    }
                    fw.write("\n");
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        Border outerLine = new LineBorder(BORDER_COLOR, 1, true);
        Border innerPad = new EmptyBorder(14, 16, 14, 16);
        card.setBorder(BorderFactory.createCompoundBorder(outerLine, innerPad));

        if (title != null && !title.isEmpty()) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    outerLine,
                    BorderFactory.createTitledBorder(
                            new EmptyBorder(6, 10, 10, 10),
                            title,
                            0, 0, FONT_SUBTITLE, TEXT_DARK
                    )
            ));
        }
        return card;
    }

    private JPanel wrapInCardScroll(JComponent component, String title) {
        JPanel card = createCardPanel(title);
        card.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JTextField createStyledTextField(String text, int cols) {
        JTextField field = new JTextField(text, cols);
        field.setFont(FONT_REGULAR);
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTextArea createStyledTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(FONT_REGULAR);
        area.setForeground(TEXT_DARK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        Color hoverColor = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void styleModernTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public static void main(String[] args) {
        try {
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            new DevTrackApp().setVisible(true);
        });
    }
}