package models;

import java.time.LocalDateTime;

public class RentalContractModel {
    private int contractId;
    private ClientModel client;
    private Equipment equipment;
    private OperatorModel operator; 
    private UserModel issuedBy;    
    private boolean isWetHire;
    private LocalDateTime issueDate;
    private LocalDateTime expectedReturn;
    private double startMeter;

    public RentalContractModel(ClientModel client, Equipment equipment, OperatorModel operator, 
                               UserModel issuedBy, boolean isWetHire, LocalDateTime issueDate, 
                               LocalDateTime expectedReturn, double startMeter) {
        this.client = client;
        this.equipment = equipment;
        this.operator = operator;
        this.issuedBy = issuedBy;
        this.isWetHire = isWetHire;
        this.issueDate = issueDate;
        this.expectedReturn = expectedReturn;
        this.startMeter = startMeter;
    }

    public int getContractId() { return contractId; }
    public ClientModel getClient() { return client; }
    public Equipment getEquipment() { return equipment; }
    public OperatorModel getOperator() { return operator; }
    public UserModel getIssuedBy() { return issuedBy; }
    public boolean isWetHire() { return isWetHire; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public LocalDateTime getExpectedReturn() { return expectedReturn; }
    public double getStartMeter() { return startMeter; }
    public void setContractId(int contractId) { this.contractId = contractId; }
    public void setClient(ClientModel client) { this.client = client; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }
    public void setOperator(OperatorModel operator) { this.operator = operator; }
    public void setIssuedBy(UserModel issuedBy) { this.issuedBy = issuedBy; }
    public void setWetHire(boolean wetHire) { isWetHire = wetHire; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public void setExpectedReturn(LocalDateTime expectedReturn) { this.expectedReturn = expectedReturn; }
    public void setStartMeter(double startMeter) { this.startMeter = startMeter; }
}
