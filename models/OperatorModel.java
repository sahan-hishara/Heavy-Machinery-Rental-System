package models;

public class OperatorModel {
    private int operatorId;
    private String fullName;
    private String status;

    private String specializations;
    private double dailyWage;

    public OperatorModel() {}

    public OperatorModel(int operatorId, String fullName, String specializations, double dailyWage, String status) {
        this.operatorId = operatorId;
        this.fullName = fullName;
        this.specializations = specializations;
        this.dailyWage = dailyWage;
        this.status = status;
    }

    public int getOperatorId() { return operatorId; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public String getSpecializations() { return specializations; }
    public double getDailyWage() { return dailyWage; }

    public void setOperatorId(int operatorId) { this.operatorId = operatorId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setStatus(String status) { this.status = status; }
    public void setSpecializations(String specializations) { this.specializations = specializations; }
    public void setDailyWage(double dailyWage) { this.dailyWage = dailyWage; }
}
