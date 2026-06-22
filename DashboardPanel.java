package views;

import database.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblWelcome, lblActiveCount, lblOverdueCount;
    private JTable contractsTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20)); // Reduced internal spacing
        setBackground(new Color(248, 249, 250)); 
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30)); // Elegant, tighter margins
        initComponents();
    }

    public void refreshDashboard() {
        if (mainFrame.getCurrentUser() != null) {
            lblWelcome.setText("Welcome, " + mainFrame.getCurrentUser().getFullName());
        }
        loadDashboardData();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);
        lblWelcome = new JLabel("Overview");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26)); // Scaled back from 32px
        lblWelcome.setForeground(new Color(24, 24, 27));
        
        JLabel lblSub = new JLabel("Today's fleet activity and dispatch status.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Scaled back from 16px
        lblSub.setForeground(new Color(113, 113, 122));
        
        titlePanel.add(lblWelcome); titlePanel.add(lblSub);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);
        
        statsPanel.add(createStatCard("Active Dispatches", lblActiveCount = new JLabel("0"), new Color(59, 130, 246))); 
        statsPanel.add(createStatCard("Overdue Returns", lblOverdueCount = new JLabel("0"), new Color(239, 68, 68))); 
        headerPanel.add(statsPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE); 
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20) // Tighter table card padding
        ));

        JLabel lblTableTitle = new JLabel("Active Fleet Activity");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTableTitle.setForeground(new Color(39, 39, 42));
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"Contract ID", "Client", "Machine", "Expected Return", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        contractsTable = new JTable(tableModel);
        contractsTable.setSelectionBackground(new Color(244, 244, 245)); // Subtle gray selection
        contractsTable.setSelectionForeground(new Color(24, 24, 27));
        contractsTable.setShowGrid(false); 
        contractsTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = contractsTable.getTableHeader();
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        
        contractsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 244, 245)));
                return c;
            }
        });

        contractsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2 && contractsTable.getSelectedRow() != -1) {
                    int id = Integer.parseInt(tableModel.getValueAt(contractsTable.getSelectedRow(), 0).toString().replace("CTR-", ""));
                    showContractDetails(id);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(contractsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        add(tableContainer, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true),
            BorderFactory.createEmptyBorder(12, 20, 12, 20) // Elegant, refined padding
        ));
        
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(113, 113, 122));
        
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Scaled back from 36px
        valueLabel.setForeground(valueColor);
        
        card.add(titleLabel); card.add(valueLabel);
        return card;
    }

    private void loadDashboardData() {
        tableModel.setRowCount(0);
        int activeCount = 0; int overdueCount = 0;

        String query = "SELECT c.contract_id, c.expected_return, e.asset_tag, e.brand_model, cl.company_name, cl.contact_person " +
                       "FROM Rental_Contracts c JOIN Equipment e ON c.equipment_id = e.equipment_id " +
                       "JOIN Clients cl ON c.client_id = cl.client_id WHERE c.status = 'Active' ORDER BY c.expected_return ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                activeCount++;
                String contractId = "CTR-" + String.format("%04d", rs.getInt("contract_id"));
                String clientName = rs.getString("company_name") != null ? rs.getString("company_name") : rs.getString("contact_person");
                String machine = rs.getString("asset_tag") + " - " + rs.getString("brand_model");
                java.sql.Timestamp expectedReturn = rs.getTimestamp("expected_return");
                String status = "Active";
                
                if (expectedReturn != null && expectedReturn.toLocalDateTime().isBefore(java.time.LocalDateTime.now())) {
                    status = "OVERDUE"; overdueCount++;
                }
                String expectedReturnStr = expectedReturn != null ? expectedReturn.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A";
                tableModel.addRow(new Object[]{contractId, clientName, machine, expectedReturnStr, status});
            }
            lblActiveCount.setText(String.valueOf(activeCount)); lblOverdueCount.setText(String.valueOf(overdueCount));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showContractDetails(int contractId) {
        JDialog dialog = new JDialog(mainFrame, "Contract Details: CTR-" + String.format("%04d", contractId), true);
        dialog.setSize(400, 380); dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String query = "SELECT c.*, e.asset_tag, e.brand_model, cl.company_name, cl.contact_person, op.full_name as op_name " +
                       "FROM Rental_Contracts c JOIN Equipment e ON c.equipment_id = e.equipment_id JOIN Clients cl ON c.client_id = cl.client_id " +
                       "LEFT JOIN Operators op ON c.operator_id = op.operator_id WHERE c.contract_id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, contractId); ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String cName = rs.getString("company_name") != null ? rs.getString("company_name") : rs.getString("contact_person");
                panel.add(new JLabel("<html><b>Client:</b> " + cName + "</html>"));
                panel.add(new JLabel("<html><b>Equipment:</b> " + rs.getString("asset_tag") + " - " + rs.getString("brand_model") + "</html>"));
                panel.add(new JLabel("<html><b>Hire Type:</b> " + (rs.getBoolean("is_wet_hire") ? "Wet Hire" : "Dry Hire") + "</html>"));
                if (rs.getBoolean("is_wet_hire") && rs.getString("op_name") != null) panel.add(new JLabel("<html><b>Operator:</b> " + rs.getString("op_name") + "</html>"));
                panel.add(new JLabel("<html><b>Issue Date:</b> " + rs.getTimestamp("issue_date") + "</html>"));
                panel.add(new JLabel("<html><b>Expected Return:</b> " + rs.getTimestamp("expected_return") + "</html>"));
                panel.add(new JLabel("<html><b>Starting Meter:</b> " + rs.getDouble("start_meter") + " hrs</html>"));
            }
        } catch (Exception e) { e.printStackTrace(); }

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose); dialog.add(panel); dialog.setVisible(true);
    }
}