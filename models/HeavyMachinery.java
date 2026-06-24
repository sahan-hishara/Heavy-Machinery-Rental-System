package models;

public class HeavyMachinery extends Equipment {
    
    private double currentEngineHours;
    private boolean requiresOperator;
    private double operatorWage;
    private double otRate;      

    public HeavyMachinery(int equipmentId, String assetTag, String brandModel, double baseDailyRate, 
                          String status, double currentEngineHours, boolean requiresOperator, 
                          double operatorWage, double otRate) {
        super(equipmentId, assetTag, brandModel, baseDailyRate, status, "Heavy");
        this.currentEngineHours = currentEngineHours;
        this.requiresOperator = requiresOperator;
        this.operatorWage = operatorWage;
        this.otRate = otRate;
    }

    @Override
    public double calculateDailyCost() {
        return this.requiresOperator ? this.baseDailyRate + this.operatorWage : this.baseDailyRate;
    }

    public double getCurrentEngineHours() { return currentEngineHours; }
    public boolean isRequiresOperator() { return requiresOperator; }
    public double getOperatorWage() { return operatorWage; }
    public double getOtRate() { return otRate; }

    public void setCurrentEngineHours(double currentEngineHours) { this.currentEngineHours = currentEngineHours; }
    public void setRequiresOperator(boolean requiresOperator) { this.requiresOperator = requiresOperator; }
    public void setOperatorWage(double operatorWage) { this.operatorWage = operatorWage; }
    public void setOtRate(double otRate) { this.otRate = otRate; }
}
