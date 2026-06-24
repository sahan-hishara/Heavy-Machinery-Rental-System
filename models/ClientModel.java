package models;

import java.time.LocalDate;

public class ClientModel {
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

    public ClientModel() {
        this.isActive = true;
        this.creditStatus = "Good"; 
    }


    public boolean isInsuranceValid() {
        if (this.insuranceExpiry == null) {
            return false;
        }
        return !LocalDate.now().isAfter(this.insuranceExpiry);
    }
    
    public boolean isCorporateClient() {
        return this.brnNumber != null && !this.brnNumber.trim().isEmpty();
    }
    
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

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

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
