package models;

public class InvoiceModel {
    private int invoiceId;
    private RentalContractModel contract;
    private double advancePaid;
    private double baseRentalFee;
    private double operatorFee;
    private double overtimeCharge; 
    private double cleaningFee;
    private double fuelSurcharge; 
    private double damagePenalty; 
    private double finalTotalDue;
    private String status; 

    public InvoiceModel(RentalContractModel contract, double advancePaid) {
        this.contract = contract;
        this.advancePaid = advancePaid;
        this.status = "Partial";
        calculateInitialFees();
    }

    public InvoiceModel(RentalContractModel contract, double advancePaid, double fuelSurcharge, double damagePenalty, double overtimeCharge) {
        this.contract = contract;
        this.advancePaid = advancePaid;
        this.fuelSurcharge = fuelSurcharge;
        this.damagePenalty = damagePenalty;
        this.overtimeCharge = overtimeCharge;
        this.status = "Final";
        calculateFinalFees();
    }

    private void calculateInitialFees() {
        this.baseRentalFee = contract.getEquipment().getBaseDailyRate();
        if (contract.isWetHire() && contract.getEquipment() instanceof HeavyMachinery) {
            this.operatorFee = ((HeavyMachinery) contract.getEquipment()).getOperatorWage();
        } else { this.operatorFee = 0.0; }
        this.finalTotalDue = (baseRentalFee + operatorFee) - advancePaid;
    }

    private void calculateFinalFees() {
        this.baseRentalFee = contract.getEquipment().getBaseDailyRate();
        if (contract.isWetHire() && contract.getEquipment() instanceof HeavyMachinery) {
            this.operatorFee = ((HeavyMachinery) contract.getEquipment()).getOperatorWage();
        } else { this.operatorFee = 0.0; }
        double subtotal = baseRentalFee + operatorFee + overtimeCharge + cleaningFee + fuelSurcharge + damagePenalty;
        this.finalTotalDue = subtotal - advancePaid;
    }

    public int getInvoiceId() { return invoiceId; }
    public RentalContractModel getContract() { return contract; }
    public double getAdvancePaid() { return advancePaid; }
    public double getBaseRentalFee() { return baseRentalFee; }
    public double getOperatorFee() { return operatorFee; }
    public double getOvertimeCharge() { return overtimeCharge; }
    public double getCleaningFee() { return cleaningFee; }
    public double getFuelSurcharge() { return fuelSurcharge; }
    public double getDamagePenalty() { return damagePenalty; }
    public double getFinalTotalDue() { return finalTotalDue; }
    public String getStatus() { return status; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
}
