package views;

import database.DatabaseConnection;
import models.UserModel;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sideBarPanel;

    // --- ADDED btnOperators ---
    private JButton btnDashboard, btnClients, btnContracts, btnReturns, btnBilling, btnInventory, btnOperators, btnUsers, btnLogout;

    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private ClientManagerPanel clientPanel;
    private InventoryManagerPanel inventoryPanel;
    private NewContractWizard contractWizard;
    private ReturnInspectionPanel returnPanel;
    private BillingHistoryPanel billingPanel;
    private UserManagerPanel userPanel;
    private OperatorManagerPanel operatorPanel; // --- ADDED PANEL ---

    private UserModel currentUser;

    public MainFrame() {
        setTitle("Fleet Equipment Rentals - Enterprise");
        setSize(1280, 800); 
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 249, 250)); 

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        initSideBar();
        
        add(sideBarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { shutdownApplication(); }
        });

        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        clientPanel = new ClientManagerPanel(this);
        inventoryPanel = new InventoryManagerPanel(this);
        contractWizard = new NewContractWizard(this);
        returnPanel = new ReturnInspectionPanel(this);
        billingPanel = new BillingHistoryPanel(this);
        userPanel = new UserManagerPanel(this);
        operatorPanel = new OperatorManagerPanel(this); // --- INITIALIZED ---

        mainContentPanel.add(loginPanel, "LOGIN_PANEL");
        mainContentPanel.add(dashboardPanel, "DASHBOARD_PANEL");
        mainContentPanel.add(clientPanel, "CLIENT_PANEL");
        mainContentPanel.add(inventoryPanel, "INVENTORY_PANEL");
        mainContentPanel.add(contractWizard, "CONTRACT_PANEL");
        mainContentPanel.add(returnPanel, "RETURN_PANEL");
        mainContentPanel.add(billingPanel, "BILLING_PANEL");
        mainContentPanel.add(userPanel, "USER_PANEL");
        mainContentPanel.add(operatorPanel, "OPERATOR_PANEL"); // --- ADDED TO CARD LAYOUT ---

        showPanel("LOGIN_PANEL");
    }

    private void initSideBar() {
        sideBarPanel = new JPanel();
        // Increased grid layout to 11 rows to fit the new button
        sideBarPanel.setLayout(new GridLayout(11, 1, 0, 5)); 
        sideBarPanel.setPreferredSize(new Dimension(240, 800)); 
        sideBarPanel.setBackground(new Color(24, 24, 27)); 
        sideBarPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        JLabel logoLabel = new JLabel("<html><div style='text-align: center;'>FLEET<br><span style='color:#3b82f6;'>RENTALS</span></div></html>");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sideBarPanel.add(logoLabel);

        btnDashboard = createNavButton("Dashboard");
        btnClients = createNavButton("Manage Clients");
        btnContracts = createNavButton("New Dispatch");
        btnReturns = createNavButton("Check-In / Return");
        btnBilling = createNavButton("Billing & History");
        btnInventory = createNavButton("Fleet Inventory");
        btnOperators = createNavButton("Manage Operators"); // --- CREATED BUTTON ---
        btnUsers = createNavButton("System Users");
        btnLogout = createNavButton("Secure Logout");

        btnDashboard.addActionListener(e -> { dashboardPanel.refreshDashboard(); showPanel("DASHBOARD_PANEL"); });
        btnClients.addActionListener(e -> showPanel("CLIENT_PANEL"));
        btnContracts.addActionListener(e -> { contractWizard.refreshWizardData(); showPanel("CONTRACT_PANEL"); });
        btnReturns.addActionListener(e -> { returnPanel.refreshPanelData(); showPanel("RETURN_PANEL"); });
        btnInventory.addActionListener(e -> showPanel("INVENTORY_PANEL"));
        btnBilling.addActionListener(e -> { billingPanel.refreshHistoryData(); showPanel("BILLING_PANEL"); });
        
        // --- ADDED ACTION LISTENER ---
        btnOperators.addActionListener(e -> { operatorPanel.loadOperatorData(); showPanel("OPERATOR_PANEL"); }); 
        
        btnUsers.addActionListener(e -> showPanel("USER_PANEL"));
        btnLogout.addActionListener(e -> performLogout());

        sideBarPanel.add(btnDashboard);
        sideBarPanel.add(btnClients);
        sideBarPanel.add(btnContracts);
        sideBarPanel.add(btnReturns);
        sideBarPanel.add(btnBilling);
        sideBarPanel.add(btnInventory); 
        sideBarPanel.add(btnOperators); // --- ADDED TO SIDEBAR ---
        sideBarPanel.add(btnUsers);
        sideBarPanel.add(Box.createVerticalGlue());
        sideBarPanel.add(btnLogout);

        sideBarPanel.setVisible(false);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        btn.setBackground(new Color(39, 39, 42)); 
        btn.setForeground(new Color(161, 161, 170)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(63, 63, 70)); btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(39, 39, 42)); btn.setForeground(new Color(161, 161, 170));
            }
        });
        return btn;
    }

    public void showPanel(String panelName) { cardLayout.show(mainContentPanel, panelName); }

    public void loginSuccess(UserModel user) {
        this.currentUser = user;
        sideBarPanel.setVisible(true);
        
        // --- DESK CLERKS CANNOT SEE INVENTORY, OPERATORS, OR USERS ---
        btnInventory.setVisible(!currentUser.isDeskClerk());
        btnOperators.setVisible(!currentUser.isDeskClerk()); 
        btnUsers.setVisible(currentUser.isAdmin());
        
        dashboardPanel.refreshDashboard();
        showPanel("DASHBOARD_PANEL");
    }

    private void performLogout() {
        if (JOptionPane.showConfirmDialog(this, "Log out?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.currentUser = null; sideBarPanel.setVisible(false); showPanel("LOGIN_PANEL");
        }
    }

    private void shutdownApplication() {
        if (JOptionPane.showConfirmDialog(this, "Exit system?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection(); System.exit(0);
        }
    }

    public UserModel getCurrentUser() { return currentUser; }

    public static void applyModernTheme() {
        try {
            FlatMacLightLaf.setup(); 

            FontUIResource baseFont = new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)); 
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) { UIManager.put(key, baseFont); }
            }

            UIManager.put("Button.arc", 8);         
            UIManager.put("Component.arc", 8);      
            UIManager.put("ProgressBar.arc", 8);    
            UIManager.put("TextComponent.arc", 8);
            
            UIManager.put("Component.focusWidth", 1); 
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.innerFocusWidth", 0);
            
            UIManager.put("Table.rowHeight", 35); 
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.height", 35);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));

        } catch (Exception ex) { System.err.println("Failed to initialize FlatLaf"); }
    }

    public static void main(String[] args) {
        applyModernTheme(); 
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}