package views;

import database.DatabaseConnection;
import models.*;
import utils.PDFGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class BillingHistoryPanel extends JPanel {

    private MainFrame mainFrame;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public BillingHistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        initComponents();
    }

    public void refreshHistoryData() { loadHistoryData(); }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2)); headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Billing & Contract History"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26)); lblTitle.setForeground(new Color(24, 24, 27));
        JLabel lblSub = new JLabel("Double-click any record to view details and open DPV or INV files."); lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblSub.setForeground(new Color(113, 113, 122));
        headerPanel.add(lblTitle); headerPanel.add(lblSub); headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setBackground(Color.WHITE); tableContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        String[] cols = {"Contract ID", "Client Name", "Machine", "Status", "Advance Paid", "Final Total"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        historyTable = new JTable(tableModel); historyTable.setSelectionBackground(new Color(244, 244, 245)); historyTable.setShowGrid(false); historyTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = historyTable.getTableHeader(); header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 244, 245)));
                if (column == 3) {
                    if (value.toString().contains("Active")) setForeground(new Color(37, 99, 235)); 
                    else setForeground(new Color(22, 163, 74)); 
                } else { setForeground(new Color(24, 24, 27)); }
                return c;
            }
        });

        historyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2 && historyTable.getSelectedRow() != -1) {
                    int contractId = Integer.parseInt(tableModel.getValueAt(historyTable.getSelectedRow(), 0).toString().replaceAll("[^0-9]", ""));
                    showFinancialDetails(contractId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable); scrollPane.setBorder(BorderFactory.createEmptyBorder()); scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER); add(tableContainer, BorderLayout.CENTER);
    }

    private void loadHistoryData() {
        tableModel.setRowCount(0);
        String query = "SELECT c.contract_id, cl.company_name, cl.contact_person, e.asset_tag, i.status, i.advance_paid, i.final_total_due " +
                       "FROM Invoices i JOIN Rental_Contracts c ON i.contract_id = c.contract_id JOIN Equipment e ON c.equipment_id = e.equipment_id " +
                       "JOIN Clients cl ON c.client_id = cl.client_id ORDER BY c.contract_id DESC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String cName = rs.getString("company_name") != null ? rs.getString("company_name") : rs.getString("contact_person");
                boolean isFinal = rs.getString("status").equals("Final");
                
                tableModel.addRow(new Object[]{ "CTR-" + String.format("%04d", rs.getInt("contract_id")), cName, rs.getString("asset_tag"), isFinal ? "Finalized Invoice" : "Active Dispatch", "Rs. " + String.format("%.2f", rs.getDouble("advance_paid")), isFinal ? "Rs. " + String.format("%.2f", rs.getDouble("final_total_due")) : "--" });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showFinancialDetails(int contractId) {
        JDialog dialog = new JDialog(mainFrame, "Financial Details: CTR-" + String.format("%04d", contractId), true);
        dialog.setSize(450, 480); dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8)); panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String invoiceStatus = "";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement("SELECT status FROM Invoices WHERE contract_id = ?")) {
            stmt.setInt(1, contractId); ResultSet rs = stmt.executeQuery(); if (rs.next()) invoiceStatus = rs.getString("status");
        } catch (Exception e) {}

        final String finalStatus = invoiceStatus;

        panel.add(new JLabel("<html><h2 style='color:#18181b; margin:0;'>Document Retrieval</h2></html>"));
        panel.add(new JLabel("<html>Select the document you wish to view. If it is missing from your local computer, the system will reconstruct it from cloud data.</html>"));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15)); btnPanel.setBackground(Color.WHITE);
        
        JButton btnOpenDPV = new JButton("Open Dispatch Notice (DPV)");
        btnOpenDPV.setBackground(new Color(59, 130, 246)); btnOpenDPV.setForeground(Color.WHITE); btnOpenDPV.setPreferredSize(new Dimension(240, 45));
        
        btnOpenDPV.addActionListener(e -> attemptToOpenPDF(dialog, contractId, "DPV", finalStatus));
        btnPanel.add(btnOpenDPV);

        if (finalStatus.equals("Final")) {
            JButton btnOpenINV = new JButton("Open Final Bill (INV)");
            btnOpenINV.setBackground(new Color(22, 163, 74)); btnOpenINV.setForeground(Color.WHITE); btnOpenINV.setPreferredSize(new Dimension(240, 45));
            
            btnOpenINV.addActionListener(e -> attemptToOpenPDF(dialog, contractId, "INV", finalStatus));
            btnPanel.add(btnOpenINV);
        }

        JButton btnClose = new JButton("Close"); btnClose.setPreferredSize(new Dimension(100, 40)); btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose); panel.add(btnPanel); dialog.add(panel); dialog.setVisible(true);
    }

    private void attemptToOpenPDF(JDialog dialog, int contractId, String type, String status) {
        String docNumber = String.format("%04d", contractId);
        String fullPath = System.getProperty("user.dir") + File.separator + "Documents" + File.separator + type + "-" + docNumber + ".pdf";
        File pdfFile = new File(fullPath);
        
        if (pdfFile.exists()) {
            try { Desktop.getDesktop().open(pdfFile); } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Could not open file."); }
        } else {
            if (JOptionPane.showConfirmDialog(dialog, type + " not found locally. Reconstruct from cloud?", "Missing File", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                regenerateMissingPDF(contractId, type);
            }
        }
    }

    private void regenerateMissingPDF(int contractId, String type) {
        String query = "SELECT c.*, i.*, cl.company_name, cl.contact_person, e.asset_tag, e.brand_model, e.category, e.base_daily_rate, hm.operator_wage, hm.ot_rate, op.full_name as op_name " +
                       "FROM Rental_Contracts c JOIN Invoices i ON c.contract_id = i.contract_id JOIN Clients cl ON c.client_id = cl.client_id " +
                       "JOIN Equipment e ON c.equipment_id = e.equipment_id LEFT JOIN Heavy_Machinery hm ON e.equipment_id = hm.equipment_id LEFT JOIN Operators op ON c.operator_id = op.operator_id WHERE c.contract_id = ?";
                       
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, contractId); ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ClientModel client = new ClientModel(); client.setCompanyName(rs.getString("company_name") != null ? rs.getString("company_name") : rs.getString("contact_person"));
                Equipment equipment = new HeavyMachinery(rs.getInt("equipment_id"), rs.getString("asset_tag"), rs.getString("brand_model"), rs.getDouble("base_daily_rate"), "Rented", rs.getDouble("start_meter"), rs.getBoolean("is_wet_hire"), rs.getDouble("operator_wage"), rs.getDouble("ot_rate"));
                OperatorModel operator = rs.getBoolean("is_wet_hire") ? new OperatorModel(rs.getInt("operator_id"), rs.getString("op_name"), "All", 0.0, "Available") : null;
                
                // --- THE FIX: SAFELY HANDLE SQL NULL DATES ---
                java.sql.Timestamp issueTs = rs.getTimestamp("issue_date");
                LocalDateTime issueDate = (issueTs != null) ? issueTs.toLocalDateTime() : LocalDateTime.now();

                java.sql.Timestamp expectedTs = rs.getTimestamp("expected_return");
                LocalDateTime expectedReturn = (expectedTs != null) ? expectedTs.toLocalDateTime() : LocalDateTime.now().plusDays(1);
                
                RentalContractModel contract = new RentalContractModel(client, equipment, operator, mainFrame.getCurrentUser(), rs.getBoolean("is_wet_hire"), issueDate, expectedReturn, rs.getDouble("start_meter"));
                contract.setContractId(contractId);

                String path = null;
                if (type.equals("DPV")) {
                    path = PDFGenerator.generateDispatchPDF(contract, rs.getDouble("advance_paid"));
                } else {
                    InvoiceModel inv = new InvoiceModel(contract, rs.getDouble("advance_paid"), rs.getDouble("fuel_surcharge"), rs.getDouble("damage_penalty"), rs.getDouble("overtime_charge"));
                    inv.setInvoiceId(contractId);
                    double endMeter = rs.getDouble("end_meter");
                    double fuelLiters = rs.getDouble("fuel_surcharge") / 300.0;
                    
                    // --- THE FIX: SAFELY HANDLE ACTUAL RETURN DATES ---
                    java.sql.Timestamp actualTs = rs.getTimestamp("actual_return");
                    LocalDateTime actualReturn = (actualTs != null) ? actualTs.toLocalDateTime() : LocalDateTime.now();
                    
                    path = PDFGenerator.generateFinalBillPDF(inv, endMeter, fuelLiters, "As per physical return log.", actualReturn);
                }
                
                if (path != null) { Desktop.getDesktop().open(new File(path)); }
            }
        } catch (Exception e) { e.printStackTrace(); JOptionPane.showMessageDialog(this, "Critical error rebuilding document."); }
    }
}