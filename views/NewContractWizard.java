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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class NewContractWizard extends JPanel {

    private MainFrame mainFrame;
    private ContractDAO contractDAO;

    private JComboBox<ClientItem> cmbClients;
    private JComboBox<EquipItem> cmbEquipment;
    private JComboBox<OpItem> cmbOperators;
    
    private JRadioButton radDryHire, radWetHire;
    private JSpinner spnExpectedReturn; 
    private JTextField txtStartMeter, txtAdvancePayment;
    
    private List<OpItem> allOperatorsList = new ArrayList<>(); 

    public NewContractWizard(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.contractDAO = new ContractDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250)); 
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30)); 

        initComponents();
    }

    public void refreshWizardData() {
        loadDropdownData(); 
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        spnExpectedReturn.setValue(java.util.Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant()));
        txtAdvancePayment.setText("0.00");
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2)); 
        headerPanel.setOpaque(false);
        
        JLabel lblTitle = new JLabel("New Dispatch Wizard"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        lblTitle.setForeground(new Color(24, 24, 27));
        
        JLabel lblSub = new JLabel("Create a new contract and assign equipment to a client."); 
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

        addSectionHeader(formPanel, gbc, 0, "1. Client & Equipment Selection");
        
        gbc.gridy = 1; gbc.gridx = 0; formPanel.add(createStyledLabel("Active Client *"), gbc); 
        cmbClients = new JComboBox<>(); gbc.gridx = 1; formPanel.add(cmbClients, gbc);
        
        gbc.gridy = 2; gbc.gridx = 0; formPanel.add(createStyledLabel("Available Machine *"), gbc); 
        cmbEquipment = new JComboBox<>(); gbc.gridx = 1; formPanel.add(cmbEquipment, gbc);

        addSectionHeader(formPanel, gbc, 3, "2. Assignment Details");
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); 
        radioPanel.setOpaque(false);
        radDryHire = new JRadioButton("Dry Hire"); radDryHire.setOpaque(false); 
        radWetHire = new JRadioButton("Wet Hire"); radWetHire.setOpaque(false); 
        ButtonGroup bg = new ButtonGroup(); bg.add(radDryHire); bg.add(radWetHire); 
        radDryHire.setSelected(true); radioPanel.add(radDryHire); radioPanel.add(radWetHire);

        gbc.gridy = 4; gbc.gridx = 0; formPanel.add(createStyledLabel("Hire Type *"), gbc); gbc.gridx = 1; formPanel.add(radioPanel, gbc);
        
        gbc.gridy = 5; gbc.gridx = 0; formPanel.add(createStyledLabel("Assign Operator"), gbc); 
        cmbOperators = new JComboBox<>(); cmbOperators.setEnabled(false); gbc.gridx = 1; formPanel.add(cmbOperators, gbc);

        radWetHire.addActionListener(e -> cmbOperators.setEnabled(true));
        radDryHire.addActionListener(e -> cmbOperators.setEnabled(false));

        cmbEquipment.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                EquipItem eq = (EquipItem) cmbEquipment.getSelectedItem();
                if (eq != null) {
                    if (eq.category.equals("Heavy Machinery")) {
                        radDryHire.setEnabled(true); radWetHire.setEnabled(true);
                        radWetHire.setSelected(true); cmbOperators.setEnabled(true);
                        txtStartMeter.setEnabled(true); txtStartMeter.setText(String.valueOf(eq.currentMeter));
                        filterOperators(eq.display);
                    } else {
                        radDryHire.setEnabled(false); radWetHire.setEnabled(false);
                        radDryHire.setSelected(true); cmbOperators.removeAllItems(); cmbOperators.setEnabled(false);
                        txtStartMeter.setText("0.0"); txtStartMeter.setEnabled(false);
                    }
                }
            }
        });

        addSectionHeader(formPanel, gbc, 6, "3. Terms & Metrics");
        
        gbc.gridy = 7; gbc.gridx = 0; formPanel.add(createStyledLabel("Expected Return *"), gbc);
        spnExpectedReturn = new JSpinner(new SpinnerDateModel()); 
        spnExpectedReturn.setEditor(new JSpinner.DateEditor(spnExpectedReturn, "yyyy-MM-dd HH:mm")); 
        gbc.gridx = 1; formPanel.add(spnExpectedReturn, gbc);

        gbc.gridy = 8; gbc.gridx = 0; formPanel.add(createStyledLabel("Start Meter *"), gbc); 
        txtStartMeter = new JTextField(); gbc.gridx = 1; formPanel.add(txtStartMeter, gbc);
        
        gbc.gridy = 9; gbc.gridx = 0; formPanel.add(createStyledLabel("Advance Payment (Rs)"), gbc); 
        txtAdvancePayment = new JTextField(); gbc.gridx = 1; formPanel.add(txtAdvancePayment, gbc);

        JButton btnDispatch = new JButton("Create Contract & Dispatch Equipment");
        btnDispatch.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btnDispatch.setBackground(new Color(79, 70, 229)); 
        btnDispatch.setForeground(Color.WHITE); 
        btnDispatch.setPreferredSize(new Dimension(0, 40)); 
        btnDispatch.addActionListener(e -> processNewContract());
        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(25, 10, 0, 10); formPanel.add(btnDispatch, gbc);

        JPanel centerWrapper = new JPanel(new BorderLayout()); 
        centerWrapper.setOpaque(false); 
        centerWrapper.add(formPanel, BorderLayout.NORTH);
        
        JScrollPane scroll = new JScrollPane(centerWrapper); 
        scroll.setBorder(BorderFactory.createEmptyBorder()); 
        scroll.getViewport().setBackground(new Color(248, 249, 250)); 
        add(scroll, BorderLayout.CENTER);
    }

    private void filterOperators(String equipmentName) {
        cmbOperators.removeAllItems();
        String eqLower = equipmentName.toLowerCase();
        for (OpItem op : allOperatorsList) {
            boolean isMatch = false;
            if (op.specializations.equalsIgnoreCase("All")) { 
                isMatch = true; 
            } else {
                String[] specs = op.specializations.split(",");
                for (String spec : specs) { 
                    if (eqLower.contains(spec.trim().toLowerCase())) { 
                        isMatch = true; 
                        break; 
                    } 
                }
            }
            if (isMatch) cmbOperators.addItem(op);
        }
    }

    private void processNewContract() {
        if (cmbClients.getSelectedItem() == null || cmbEquipment.getSelectedItem() == null) { 
            JOptionPane.showMessageDialog(this, "Select a Client and Equipment.", "Missing Data", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        ClientItem selectedClient = (ClientItem) cmbClients.getSelectedItem(); 
        EquipItem selectedEq = (EquipItem) cmbEquipment.getSelectedItem();
        boolean isWetHire = radWetHire.isSelected(); 
        OpItem selectedOp = null;

        if (isWetHire) {
            if (cmbOperators.getSelectedItem() == null) { 
                JOptionPane.showMessageDialog(this, "Select an Operator for a Wet Hire.", "Missing Data", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            selectedOp = (OpItem) cmbOperators.getSelectedItem();
        }

        double startMeter, advancePay;
        try { 
            startMeter = Double.parseDouble(txtStartMeter.getText().trim()); 
            advancePay = Double.parseDouble(txtAdvancePayment.getText().trim()); 
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Meter and Advance Payment must be valid numbers."); 
            return; 
        }

        if (selectedEq.category.equals("Heavy Machinery") && startMeter < selectedEq.currentMeter) return;

        LocalDateTime expectedReturn = ((java.util.Date) spnExpectedReturn.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        ClientModel client = new ClientModel(); 
        client.setClientId(selectedClient.id); 
        client.setCompanyName(selectedClient.name);
        
        String[] eqParts = selectedEq.display.split(" - "); 
        
        // Pass all 9 arguments including the Wage and OT
        Equipment equipment = new HeavyMachinery(selectedEq.id, eqParts[0], eqParts.length > 1 ? eqParts[1] : "", selectedEq.dailyRate, "Rented", 0.0, isWetHire, selectedEq.opWage, selectedEq.otRate); 
        
        OperatorModel operator = null; 
        if (isWetHire) { 
            operator = new OperatorModel(); 
            operator.setOperatorId(selectedOp.id); 
            operator.setFullName(selectedOp.name); 
        }

        RentalContractModel newContract = new RentalContractModel(client, equipment, operator, mainFrame.getCurrentUser(), isWetHire, LocalDateTime.now(), expectedReturn, startMeter);

        if (contractDAO.createNewContract(newContract, advancePay)) {
            // Generates the accurate DPV format PDF
            String pdfPath = PDFGenerator.generateDispatchPDF(newContract, advancePay);
            if (pdfPath != null) { 
                try { Desktop.getDesktop().open(new File(pdfPath)); } catch (Exception ex) { } 
            }
            
            JOptionPane.showMessageDialog(this, "Contract dispatched! DPV Generated.");
            mainFrame.showPanel("DASHBOARD_PANEL");
        } else { 
            JOptionPane.showMessageDialog(this, "Database error occurred."); 
        }
    }

    private void loadDropdownData() {
        cmbClients.removeAllItems(); cmbEquipment.removeAllItems(); allOperatorsList.clear();
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            PreparedStatement psClient = conn.prepareStatement("SELECT client_id, company_name, contact_person FROM Clients WHERE is_active = TRUE"); 
            ResultSet rsClient = psClient.executeQuery();
            while (rsClient.next()) { 
                String name = rsClient.getString("company_name"); 
                if (name == null || name.isEmpty()) name = rsClient.getString("contact_person"); 
                cmbClients.addItem(new ClientItem(rsClient.getInt("client_id"), name)); 
            }

            // Correctly fetch the base_daily_rate alongside operator_wage and ot_rate
            PreparedStatement psEq = conn.prepareStatement("SELECT e.equipment_id, e.asset_tag, e.brand_model, e.category, e.base_daily_rate, hm.current_engine_hours, hm.operator_wage, hm.ot_rate FROM Equipment e LEFT JOIN Heavy_Machinery hm ON e.equipment_id = hm.equipment_id WHERE e.status = 'Available'"); 
            ResultSet rsEq = psEq.executeQuery();
            while (rsEq.next()) {
                cmbEquipment.addItem(new EquipItem(rsEq.getInt("equipment_id"), rsEq.getString("asset_tag") + " - " + rsEq.getString("brand_model"), rsEq.getString("category"), rsEq.getDouble("current_engine_hours"), rsEq.getDouble("base_daily_rate"), rsEq.getDouble("operator_wage"), rsEq.getDouble("ot_rate")));
            }

            PreparedStatement psOp = conn.prepareStatement("SELECT operator_id, full_name, specializations, daily_wage FROM Operators WHERE status = 'Available'"); 
            ResultSet rsOp = psOp.executeQuery();
            while (rsOp.next()) {
                allOperatorsList.add(new OpItem(rsOp.getInt("operator_id"), rsOp.getString("full_name"), rsOp.getString("specializations") != null ? rsOp.getString("specializations") : "All", rsOp.getDouble("daily_wage")));
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

    private class ClientItem { 
        int id; 
        String name; 
        
        public ClientItem(int id, String name) { 
            this.id = id; 
            this.name = name; 
        } 
        @Override 
        public String toString() { 
            return name; 
        } 
    }
    
    private class EquipItem { 
        int id; 
        String display; 
        String category; 
        double currentMeter; 
        double dailyRate; 
        double opWage; 
        double otRate; 
        
        public EquipItem(int id, String display, String category, double currentMeter, double dailyRate, double opWage, double otRate) { 
            this.id = id; 
            this.display = display; 
            this.category = category; 
            this.currentMeter = currentMeter; 
            this.dailyRate = dailyRate; 
            this.opWage = opWage; 
            this.otRate = otRate;
        } 
        @Override 
        public String toString() { 
            return display; 
        } 
    }
    
    private class OpItem { 
        int id; 
        String name; 
        String specializations; 
        double wage; 
        
        public OpItem(int id, String name, String specializations, double wage) { 
            this.id = id; 
            this.name = name; 
            this.specializations = specializations; 
            this.wage = wage; 
        } 
        @Override 
        public String toString() { 
            return name; 
        } 
    }
}