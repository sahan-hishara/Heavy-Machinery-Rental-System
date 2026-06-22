package models;

import java.time.LocalDate;

public class ClientModel {
    
    // Private attributes matching the database schema
    private int clientId;
    private String companyName;
    private String contactPerson;
    private String nicNumber;
    private String brnNumber;
    private String tinNumber;
    private String phoneNumber;
    private String insurancePolicy;
    private LocalDate insuranceExpiry;
    private String creditStatus;
    private boolean isActive;

    /**
     * Full Constructor: Used by ClientDAO when retrieving a record from the database.
     */
    public ClientModel(int clientId, String companyName, String contactPerson, String nicNumber, 
                       String brnNumber, String tinNumber, String phoneNumber, 
                       String insurancePolicy, LocalDate insuranceExpiry, 
                       String creditStatus, boolean isActive) {
        this.clientId = clientId;
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.nicNumber = nicNumber;
        this.brnNumber = brnNumber;
        this.tinNumber = tinNumber;
        this.phoneNumber = phoneNumber;
        this.insurancePolicy = insurancePolicy;
        this.insuranceExpiry = insuranceExpiry;
        this.creditStatus = creditStatus;
        this.isActive = isActive;
    }

    /**
     * Empty Constructor: Used when creating a brand new client from the Swing UI
     * before it has a database ID.
     */
    public ClientModel() {
        this.isActive = true;
        this.creditStatus = "Good"; // Default status for new clients
    }

    // --- Core Business Logic Methods ---
    
    /**
     * Verifies if the client's liability insurance is currently active.
     * @return true if the policy is valid today, false if it has expired.
     */
    public boolean isInsuranceValid() {
        if (this.insuranceExpiry == null) {
            return false;
        }
        // Checks if the current date is strictly AFTER the expiry date
        return !LocalDate.now().isAfter(this.insuranceExpiry);
    }
    
    /**
     * Checks if the client has a corporate registration (BRN) or is an individual (NIC).
     */
    public boolean isCorporateClient() {
        return this.brnNumber != null && !this.brnNumber.trim().isEmpty();
    }

    // --- Standard Getters ---
    
    public int getClientId() { return clientId; }
    public String getCompanyName() { return companyName; }
    public String getContactPerson() { return contactPerson; }
    public String getNicNumber() { return nicNumber; }
    public String getBrnNumber() { return brnNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getInsurancePolicy() { return insurancePolicy; }
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    public String getCreditStatus() { return creditStatus; }
    public boolean isActive() { return isActive; }

    // --- Sanitizing Setters ---

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    /**
     * Capitalizes the first letter of the company name to maintain a clean database.
     */
    public void setCompanyName(String companyName) {
        if (companyName != null && !companyName.trim().isEmpty()) {
            this.companyName = companyName.substring(0, 1).toUpperCase() + companyName.substring(1).trim();
        } else {
            this.companyName = null;
        }
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    /**
     * Formats the Sri Lankan National Identity Card (NIC).
     * Automatically converts trailing 'v' or 'x' to uppercase.
     */
    public void setNicNumber(String nicNumber) {
        if (nicNumber != null && !nicNumber.trim().isEmpty()) {
            this.nicNumber = nicNumber.trim().toUpperCase();
        } else {
            this.nicNumber = null;
        }
    }

    public void setBrnNumber(String brnNumber) {
        this.brnNumber = (brnNumber != null && !brnNumber.trim().isEmpty()) ? brnNumber.trim().toUpperCase() : null;
    }

    public void setTinNumber(String tinNumber) {
        this.tinNumber = (tinNumber != null && !tinNumber.trim().isEmpty()) ? tinNumber.trim() : null;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setInsurancePolicy(String insurancePolicy) {
        this.insurancePolicy = insurancePolicy != null ? insurancePolicy.trim().toUpperCase() : null;
    }

    public void setInsuranceExpiry(LocalDate insuranceExpiry) {
        this.insuranceExpiry = insuranceExpiry;
    }

    public void setCreditStatus(String creditStatus) {
        this.creditStatus = creditStatus;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}