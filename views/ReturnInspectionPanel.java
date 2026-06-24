package views;

import database.DatabaseConnection;
import database.ContractDAO;
import models.*;
import utils.PDFGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ReturnInspectionPanel extends JPanel {

    private MainFrame mainFrame;
    private ContractDAO contractDAO;

    private JComboBox<ContractItem> cmbActiveContracts;
    private JLabel lblClientName, lblEquipmentDetails, lblStartMeter, lblIssueDate;
    private JTextField txtEndMeter, txtFuelLiters, txtDamagePenalty;
    private JComboBox<String> cmbCondition;
    private JTextArea txtNotes;

    public ReturnInspectionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.contractDAO = new ContractDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        initComponents();
    }

    public void refreshPanelData() {
        loadActiveContracts();
        txtEndMeter.setText(""); 
        txtFuelLiters.setText("0.0"); 
        txtDamagePenalty.setText("0.00");
        txtDamagePenalty.setEnabled(false); 
        cmbCondition.setSelectedIndex(0); 
        txtNotes.setText("");
    } 

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2)); 
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Return & Final Billing"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        lblTitle.setForeground(new Color(24, 24, 27));
        JLabel lblSub = new JLabel("Check-in equipment, calculate OT metrics, and generate the settlement invoice."); 
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        lblSub.setForeground(new Color(113, 113, 122));
        
        headerPanel.add(lblTitle); 
        headerPanel.add(lblSub); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); 
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout()); 
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), 
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = new Insets(8, 10, 8, 10); 
        gbc.weightx = 1.0;

        addSectionHeader(formPanel, gbc, 0, "1. Active Contract");
        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(createStyledLabel("Select Contract *"), gbc); 
        cmbActiveContracts = new JComboBox<>(); gbc.gridx = 1; formPanel.add(cmbActiveContracts, gbc);

        JPanel contextPanel = new JPanel(new GridLayout(4, 1, 5, 5)); 
        contextPanel.setBackground(new Color(250, 250, 250));
        contextPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(228, 228, 231)), 
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        lblClientName = new JLabel("Client: --"); 
        lblEquipmentDetails = new JLabel("Equipment: --"); 
        lblIssueDate = new JLabel("Dispatched On: --");
        lblStartMeter = new JLabel("Start Meter: --"); 
        lblStartMeter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        contextPanel.add(lblClientName); 
        contextPanel.add(lblEquipmentDetails); 
        contextPanel.add(lblIssueDate); 
        contextPanel.add(lblStartMeter);
        
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; formPanel.add(contextPanel, gbc); gbc.gridwidth = 1;

        cmbActiveContracts.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ContractItem item = (ContractItem) cmbActiveContracts.getSelectedItem();
                if (item != null) {
                    lblClientName.setText("Client: " + item.clientName);
                    lblEquipmentDetails.setText("Equipment: " + item.equipmentTag + " (Rate: Rs. " + item.dailyRate + "/day)");
                    lblIssueDate.setText("Dispatched On: " + item.issueDate.toString().replace("T", " "));
                    lblStartMeter.setText("Start Meter: " + item.startMeter + " hrs");
                    
                    if (item.category.equals("Heavy Machinery")) {
                        txtEndMeter.setEnabled(true); 
                        txtEndMeter.setText(String.valueOf(item.startMeter)); 
                        txtFuelLiters.setEnabled(true); 
                        txtFuelLiters.setText("0.0");
                    } else {
                        txtEndMeter.setEnabled(false); 
                        txtEndMeter.setText("N/A (Light Tool)"); 
                        txtFuelLiters.setEnabled(false); 
                        txtFuelLiters.setText("N/A (No Tank)");
                    }
                }
            }
        });

        addSectionHeader(formPanel, gbc, 3, "2. Inspection & Penalties");
        
        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(createStyledLabel("End Engine Meter *"), gbc); 
        txtEndMeter = new JTextField(); gbc.gridx = 1; formPanel.add(txtEndMeter, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(createStyledLabel("Liters to Refill Tank *"), gbc); 
        txtFuelLiters = new JTextField(); txtFuelLiters.setText("0.0"); gbc.gridx = 1; formPanel.add(txtFuelLiters, gbc);

        gbc.gridy = 6; gbc.gridx = 0; formPanel.add(createStyledLabel("Machine Condition *"), gbc); 
        cmbCondition = new JComboBox<>(new String[]{"Good", "Fair", "Poor (Apply Penalty)"}); gbc.gridx = 1; formPanel.add(cmbCondition, gbc);
        
        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(createStyledLabel("Damage Penalty (Rs)"), gbc); 
        txtDamagePenalty = new JTextField(); txtDamagePenalty.setText("0.00"); txtDamagePenalty.setEnabled(false); gbc.gridx = 1; formPanel.add(txtDamagePenalty, gbc);

        cmbCondition.addItemListener(e -> {
            if (cmbCondition.getSelectedIndex() == 2) { 
                txtDamagePenalty.setEnabled(true); 
                txtDamagePenalty.requestFocus(); 
            } else { 
                txtDamagePenalty.setEnabled(false); 
                txtDamagePenalty.setText("0.00"); 
            }
        });

        gbc.gridy = 8; gbc.gridx = 0; formPanel.add(createStyledLabel("Damage Notes"), gbc); 
        txtNotes = new JTextArea(3, 20); txtNotes.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219))); gbc.gridx = 1; formPanel.add(new JScrollPane(txtNotes), gbc);

        JButton btnProcess = new JButton("Finalize Return & Generate Invoice");
        btnProcess.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btnProcess.setBackground(new Color(220, 38, 38)); 
        btnProcess.setForeground(Color.WHITE); 
        btnProcess.setPreferredSize(new Dimension(0, 40));
        btnProcess.addActionListener(e -> processReturn());
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(25, 10, 0, 10); formPanel.add(btnProcess, gbc);

        JPanel centerWrapper = new JPanel(new BorderLayout()); 
        centerWrapper.setOpaque(false); 
        centerWrapper.add(formPanel, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(centerWrapper); 
        scroll.setBorder(BorderFactory.createEmptyBorder()); 
        scroll.getViewport().setBackground(new Color(248, 249, 250)); 
        add(scroll, BorderLayout.CENTER);
    }

    private void processReturn() {
        if (cmbActiveContracts.getSelectedItem() == null) return;
        ContractItem item = (ContractItem) cmbActiveContracts.getSelectedItem();
        
        try {
            double endMeter = item.category.equals("Heavy Machinery") ? Double.parseDouble(txtEndMeter.getText().trim()) : item.startMeter;
            double fuelLiters = txtFuelLiters.isEnabled() ? Double.parseDouble(txtFuelLiters.getText().trim()) : 0.0;
            double damagePenalty = txtDamagePenalty.isEnabled() ? Double.parseDouble(txtDamagePenalty.getText().trim()) : 0.0;
            String damageNotes = txtNotes.getText().trim();
            
            // Re-established 300L fuel cost
            double fuelSurcharge = fuelLiters * 300.00; 

            if (item.category.equals("Heavy Machinery") && endMeter < item.startMeter) { 
                JOptionPane.showMessageDialog(this, "SECURITY ALERT: End meter cannot be less than Start meter.", "Invalid Data", JOptionPane.ERROR_MESSAGE); 
                return; 
            }

            double overtimeCharge = 0.0;
            if (item.category.equals("Heavy Machinery")) {
                long daysRented = ChronoUnit.DAYS.between(item.issueDate.toLocalDate(), LocalDateTime.now().toLocalDate());
                if (daysRented < 1) daysRented = 1; 
                double allowedHours = daysRented * 8.0;
                double actualHoursUsed = endMeter - item.startMeter;

                if (actualHoursUsed > allowedHours) {
                    double otHours = actualHoursUsed - allowedHours;
                    double machineOt = otHours * item.otRate;
                    double operatorOt = item.isWetHire ? otHours * ((item.opWage / 8.0) * 1.5) : 0.0;
                    overtimeCharge = machineOt + operatorOt; 
                }
            }
            
            ClientModel client = new ClientModel(); 
            client.setCompanyName(item.clientName);
            
            HeavyMachinery equipment = new HeavyMachinery(item.equipmentId, item.equipmentTag, "", item.dailyRate, "Rented", endMeter, item.isWetHire, item.opWage, item.otRate);
            
            OperatorModel operator = null;
            if(item.isWetHire) { 
                operator = new OperatorModel(); 
                operator.setOperatorId(item.operatorId); 
                operator.setFullName("Assigned Operator"); 
            }

            // Accurately maps the expectedReturn to fix the NullPointerException
            RentalContractModel contract = new RentalContractModel(client, equipment, operator, mainFrame.getCurrentUser(), item.isWetHire, item.issueDate, item.expectedReturn, item.startMeter);
            contract.setContractId(item.contractId);

            LocalDateTime actualReturn = LocalDateTime.now();
            
            if (contractDAO.completeContract(item.contractId, item.equipmentId, item.operatorId, item.isWetHire, endMeter, java.sql.Timestamp.valueOf(actualReturn), fuelSurcharge, damagePenalty, overtimeCharge)) {
                
                double advancePaidMock = fetchAdvancePaid(item.contractId); 
                InvoiceModel finalInvoice = new InvoiceModel(contract, advancePaidMock, fuelSurcharge, damagePenalty, overtimeCharge);
                finalInvoice.setInvoiceId(item.contractId); 
                
                String path = PDFGenerator.generateFinalBillPDF(finalInvoice, endMeter, fuelLiters, damageNotes, actualReturn);
                if (path != null) {
                    java.awt.Desktop.getDesktop().open(new File(path));
                }
                
                JOptionPane.showMessageDialog(this, "Return Processed & Final Bill Generated!"); 
                refreshPanelData(); 
                mainFrame.showPanel("DASHBOARD_PANEL");
            }
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Invalid number format in form."); 
        }
    }

    private double fetchAdvancePaid(int contractId) {
        double advance = 0.0;
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT advance_paid FROM Invoices WHERE contract_id = ?")) {
            stmt.setInt(1, contractId); 
            ResultSet rs = stmt.executeQuery(); 
            if (rs.next()) {
                advance = rs.getDouble("advance_paid");
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return advance;
    }

    private void loadActiveContracts() {
        cmbActiveContracts.removeAllItems(); 
        lblClientName.setText("Client: --"); 
        lblEquipmentDetails.setText("Equipment: --"); 
        lblStartMeter.setText("Start Meter: --"); 
        lblIssueDate.setText("Dispatched On: --");
        
        // Retained c.expected_return in the query
        String query = "SELECT c.contract_id, c.start_meter, c.issue_date, c.expected_return, c.equipment_id, c.operator_id, c.is_wet_hire, e.asset_tag, e.base_daily_rate, e.category, hm.operator_wage, hm.ot_rate, cl.company_name, cl.contact_person " +
                       "FROM Rental_Contracts c JOIN Equipment e ON c.equipment_id = e.equipment_id JOIN Clients cl ON c.client_id = cl.client_id LEFT JOIN Heavy_Machinery hm ON e.equipment_id = hm.equipment_id WHERE c.status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String cName = rs.getString("company_name"); 
                if (cName == null || cName.isEmpty()) cName = rs.getString("contact_person");
                
                cmbActiveContracts.addItem(new ContractItem(
                    rs.getInt("contract_id"), 
                    rs.getInt("equipment_id"), 
                    rs.getInt("operator_id"), 
                    rs.getBoolean("is_wet_hire"), 
                    rs.getString("asset_tag"), 
                    cName, 
                    rs.getString("category"), 
                    rs.getDouble("start_meter"), 
                    rs.getDouble("base_daily_rate"), 
                    rs.getTimestamp("issue_date").toLocalDateTime(), 
                    rs.getTimestamp("expected_return").toLocalDateTime(), 
                    rs.getDouble("operator_wage"), 
                    rs.getDouble("ot_rate")
                ));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, int yPos, String text) { 
        gbc.gridy = yPos; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 10, 5, 10); 
        JLabel header = new JLabel(text); 
        header.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        header.setForeground(new Color(24, 24, 27)); 
        panel.add(header, gbc); 
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 10, 8, 10); 
    }

    private JLabel createStyledLabel(String text) { 
        JLabel lbl = new JLabel(text); 
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        lbl.setForeground(new Color(113, 113, 122)); 
        return lbl; 
    }

    private class ContractItem {
        int contractId, equipmentId, operatorId; 
        boolean isWetHire; 
        String equipmentTag, clientName, category; 
        double startMeter, dailyRate, opWage, otRate; 
        LocalDateTime issueDate, expectedReturn;
        
        public ContractItem(int id, int eqId, int opId, boolean wet, String tag, String client, String cat, double meter, double rate, LocalDateTime issue, LocalDateTime expectedReturn, double opWage, double otRate) { 
            this.contractId = id; 
            this.equipmentId = eqId; 
            this.operatorId = opId; 
            this.isWetHire = wet; 
            this.equipmentTag = tag; 
            this.clientName = client; 
            this.category = cat; 
            this.startMeter = meter; 
            this.dailyRate = rate; 
            this.issueDate = issue; 
            this.expectedReturn = expectedReturn; 
            this.opWage = opWage; 
            this.otRate = otRate; 
        }
        
        @Override 
        public String toString() { 
            return "CTR-" + String.format("%04d", contractId) + " | " + equipmentTag; 
        }
    }
}