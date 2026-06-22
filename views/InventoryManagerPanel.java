package views;

import database.DatabaseConnection;
import database.EquipmentDAO;
import models.Equipment;
import models.HeavyMachinery;
import models.LightTool;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryManagerPanel extends JPanel {
    private JComboBox<String> cmbMachineType;
    private MainFrame mainFrame;
    private EquipmentDAO equipmentDAO;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> cmbCategory;
    private JTextField txtAssetTag, txtBrandModel, txtDailyRate, txtEngineHours;
    private JCheckBox chkRequiresOperator, chkRequiresCleaning;
    private JSpinner spnQuantity;
    private JPanel dynamicFormContainer;
    private CardLayout dynamicCardLayout;
    private JTextField txtOpWage, txtOtRate;

    public InventoryManagerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.equipmentDAO = new EquipmentDAO();

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(248, 249, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        initComponents();
        loadInventoryData();
    }

    private void initComponents() {
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Fleet & Inventory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(24, 24, 27));
        
        JLabel lblSub = new JLabel("Manage heavy machinery and light tools in the fleet.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(113, 113, 122));
        
        headerPanel.add(lblTitle); headerPanel.add(lblSub);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setOpaque(false);
        contentPanel.add(createRegistrationForm(), BorderLayout.WEST);
        
        // FIX: Re-added the missing Table Panel
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createRegistrationForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), BorderFactory.createEmptyBorder(20, 25, 20, 25)));

        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(8, 8, 8, 8);
        JLabel lblFormTitle = new JLabel("Add New Equipment"); lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblFormTitle.setForeground(new Color(24, 24, 27));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(5, 8, 15, 8); panel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridy = 1; gbc.gridx = 0; panel.add(createStyledLabel("Category *"), gbc); cmbCategory = new JComboBox<>(new String[]{"Heavy Machinery", "Light Tool"}); gbc.gridx = 1; panel.add(cmbCategory, gbc);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(createStyledLabel("Asset Tag *"), gbc); txtAssetTag = new JTextField(15); gbc.gridx = 1; panel.add(txtAssetTag, gbc);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(createStyledLabel("Brand / Model *"), gbc); txtBrandModel = new JTextField(15); gbc.gridx = 1; panel.add(txtBrandModel, gbc);
        gbc.gridy = 4; gbc.gridx = 0; panel.add(createStyledLabel("Daily Rate (Rs) *"), gbc); txtDailyRate = new JTextField(15); gbc.gridx = 1; panel.add(txtDailyRate, gbc);

        dynamicCardLayout = new CardLayout(); dynamicFormContainer = new JPanel(dynamicCardLayout); dynamicFormContainer.setBackground(Color.WHITE);

        JPanel pnlHeavy = new JPanel(new GridLayout(5, 2, 10, 10)); pnlHeavy.setBackground(Color.WHITE);
        pnlHeavy.add(createStyledLabel("Machine Type")); cmbMachineType = new JComboBox<>(new String[]{"Excavator", "Backhoe", "Crane", "Roller", "Loader"}); pnlHeavy.add(cmbMachineType);
        pnlHeavy.add(createStyledLabel("Engine Hours")); txtEngineHours = new JTextField("0.0"); pnlHeavy.add(txtEngineHours);
        pnlHeavy.add(createStyledLabel("Operator Daily Wage")); txtOpWage = new JTextField("3500.0"); pnlHeavy.add(txtOpWage);
        pnlHeavy.add(createStyledLabel("Hourly OT Rate")); txtOtRate = new JTextField("750.0"); pnlHeavy.add(txtOtRate);
        pnlHeavy.add(createStyledLabel("Requires Operator")); chkRequiresOperator = new JCheckBox("Yes"); chkRequiresOperator.setBackground(Color.WHITE); chkRequiresOperator.setSelected(true); pnlHeavy.add(chkRequiresOperator);
        dynamicFormContainer.add(pnlHeavy, "Heavy Machinery");

        JPanel pnlLight = new JPanel(new GridLayout(2, 2, 10, 12)); pnlLight.setBackground(Color.WHITE);
        pnlLight.add(createStyledLabel("Quantity")); spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); pnlLight.add(spnQuantity);
        pnlLight.add(createStyledLabel("Requires Cleaning")); chkRequiresCleaning = new JCheckBox("Yes"); chkRequiresCleaning.setBackground(Color.WHITE); pnlLight.add(chkRequiresCleaning);
        dynamicFormContainer.add(pnlLight, "Light Tool");

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; panel.add(dynamicFormContainer, gbc);

        cmbCategory.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) dynamicCardLayout.show(dynamicFormContainer, e.getItem().toString()); });

        JButton btnSave = new JButton("Save to Inventory"); btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnSave.setBackground(new Color(16, 185, 129)); btnSave.setForeground(Color.WHITE); btnSave.setPreferredSize(new Dimension(0, 40));
        btnSave.addActionListener(e -> {
            try {
                boolean isHeavy = cmbCategory.getSelectedItem().equals("Heavy Machinery");
                String finalBrandModel = isHeavy ? txtBrandModel.getText() + " " + cmbMachineType.getSelectedItem().toString() : txtBrandModel.getText();
                
                // FIX: Passes the 9 correct arguments
                Equipment eq = isHeavy 
                    ? new HeavyMachinery(0, txtAssetTag.getText(), finalBrandModel, Double.parseDouble(txtDailyRate.getText()), "Available", Double.parseDouble(txtEngineHours.getText()), chkRequiresOperator.isSelected(), Double.parseDouble(txtOpWage.getText()), Double.parseDouble(txtOtRate.getText()))
                    : new LightTool(0, txtAssetTag.getText(), finalBrandModel, Double.parseDouble(txtDailyRate.getText()), "Available", chkRequiresCleaning.isSelected(), (Integer)spnQuantity.getValue());
                    
                if (equipmentDAO.addEquipment(eq)) { JOptionPane.showMessageDialog(this, "Equipment added."); txtAssetTag.setText(""); txtBrandModel.setText(""); txtDailyRate.setText(""); txtEngineHours.setText("0.0"); loadInventoryData(); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid number format."); }
        });
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 8, 5, 8); panel.add(btnSave, gbc);

        return panel;
    }

    // --- FIX: The Missing createTablePanel() method has been restored ---
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(228, 228, 231), 1, true), BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        String[] cols = {"Asset Tag", "Category", "Brand/Model", "Status"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionBackground(new Color(244, 244, 245)); inventoryTable.setShowGrid(false); inventoryTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = inventoryTable.getTableHeader();
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(228, 228, 231)));
        
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(244, 244, 245)));
                return c;
            }
        });

        inventoryTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2 && inventoryTable.getSelectedRow() != -1) {
                    String tag = (String) tableModel.getValueAt(inventoryTable.getSelectedRow(), 0);
                    showEquipmentDetails(tag);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable); scrollPane.setBorder(BorderFactory.createEmptyBorder()); scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        bottomControls.setBackground(Color.WHITE); bottomControls.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton btnDelete = new JButton("Remove Equipment");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnDelete.setBackground(new Color(239, 68, 68)); btnDelete.setForeground(Color.WHITE); btnDelete.setPreferredSize(new Dimension(180, 40));
        btnDelete.addActionListener(e -> deleteSelectedEquipment());
        
        bottomControls.add(btnDelete);
        panel.add(bottomControls, BorderLayout.SOUTH);

        return panel;
    }

    private void deleteSelectedEquipment() {
        int row = inventoryTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an item to remove."); return; }
        
        String assetTag = (String) tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Remove " + assetTag + " from the active fleet?", "Confirm Removal", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection(); 
                 PreparedStatement stmt = conn.prepareStatement("UPDATE Equipment SET status = 'Retired' WHERE asset_tag = ?")) {
                stmt.setString(1, assetTag); stmt.executeUpdate(); 
                JOptionPane.showMessageDialog(this, "Equipment safely retired from active inventory."); 
                loadInventoryData(); 
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Database Error."); }
        }
    }
    
    private void loadInventoryData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("SELECT asset_tag, category, brand_model, status FROM Equipment WHERE status != 'Retired'"); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) { tableModel.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)}); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showEquipmentDetails(String assetTag) {
        JDialog dialog = new JDialog(mainFrame, "Equipment Details: " + assetTag, true);
        dialog.setSize(400, 380); dialog.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBackground(Color.WHITE); panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String query = "SELECT e.*, hm.current_engine_hours, hm.requires_operator, lt.quantity_available, lt.requires_cleaning FROM Equipment e LEFT JOIN Heavy_Machinery hm ON e.equipment_id = hm.equipment_id LEFT JOIN Light_Tools lt ON e.equipment_id = lt.equipment_id WHERE e.asset_tag = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, assetTag); ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String cat = rs.getString("category");
                panel.add(new JLabel("<html><b>Asset Tag:</b> " + rs.getString("asset_tag") + "</html>"));
                panel.add(new JLabel("<html><b>Brand/Model:</b> " + rs.getString("brand_model") + "</html>"));
                panel.add(new JLabel("<html><b>Category:</b> " + cat + "</html>"));
                panel.add(new JLabel("<html><b>Base Daily Rate:</b> Rs. " + rs.getDouble("base_daily_rate") + "</html>"));
                panel.add(new JLabel("<html><b>Status:</b> <span style='color:#3b82f6;'>" + rs.getString("status") + "</span></html>"));
                panel.add(new JLabel("<html><hr></html>"));
                if (cat.equals("Heavy Machinery")) {
                    panel.add(new JLabel("<html><b>Current Engine Hours:</b> " + rs.getDouble("current_engine_hours") + "</html>"));
                    panel.add(new JLabel("<html><b>Requires Operator:</b> " + (rs.getBoolean("requires_operator") ? "Yes" : "No") + "</html>"));
                } else {
                    panel.add(new JLabel("<html><b>Quantity Available:</b> " + rs.getInt("quantity_available") + "</html>"));
                    panel.add(new JLabel("<html><b>Requires Cleaning:</b> " + (rs.getBoolean("requires_cleaning") ? "Yes" : "No") + "</html>"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        panel.add(btnClose); dialog.add(panel); dialog.setVisible(true);
    }

    private JLabel createStyledLabel(String text) { JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(new Color(113, 113, 122)); return lbl; }
}