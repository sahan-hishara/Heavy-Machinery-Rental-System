package models;

import java.time.LocalDateTime;

public class InspectionModel {

    private int inspectionId;
    private RentalContractModel contract;
    private UserModel inspectedBy;
    private String inspectionType; 
    private int fuelLevel; 
    private String tireTrackCondition; 
    private String structuralNotes;
    private String photoPath; 
    private LocalDateTime createdAt;
    private boolean requiresAdminReview;

    public InspectionModel(int inspectionId, RentalContractModel contract, String inspectionType, 
                           int fuelLevel, String tireTrackCondition, String structuralNotes, 
                           String photoPath, UserModel inspectedBy, LocalDateTime createdAt) {
        this.inspectionId = inspectionId;
        this.contract = contract;
        this.inspectionType = inspectionType;
        this.fuelLevel = fuelLevel;
        this.tireTrackCondition = tireTrackCondition;
        this.structuralNotes = structuralNotes;
        this.photoPath = photoPath;
        this.inspectedBy = inspectedBy;
        this.createdAt = createdAt;
        this.requiresAdminReview = false;
    }

    public InspectionModel(RentalContractModel contract, String inspectionType, int fuelLevel, 
                           String tireTrackCondition, String structuralNotes, String photoPath, 
                           UserModel inspectedBy) {
        this.contract = contract;
        this.inspectionType = inspectionType;
        this.fuelLevel = fuelLevel;
        this.tireTrackCondition = tireTrackCondition;
        this.structuralNotes = structuralNotes;
        this.photoPath = photoPath;
        this.inspectedBy = inspectedBy;
        this.createdAt = LocalDateTime.now();
        this.requiresAdminReview = false;
    }

    public double calculateDamagePenalty(InspectionModel dispatchLog) {
        if (!"Return".equalsIgnoreCase(this.inspectionType) || !"Dispatch".equalsIgnoreCase(dispatchLog.getInspectionType())) {
            System.err.println("Error: Invalid comparison. Must compare Return to Dispatch.");
            return 0.0;
        }

        double totalPenalty = 0.0;
        if (this.fuelLevel < dispatchLog.getFuelLevel()) {
            int missingFuelPercentage = dispatchLog.getFuelLevel() - this.fuelLevel;
            totalPenalty += (missingFuelPercentage * 50.00); 
        }
        if ("Good".equalsIgnoreCase(dispatchLog.getTireTrackCondition()) && 
            "Poor".equalsIgnoreCase(this.tireTrackCondition)) {
            totalPenalty += 15000.00; 
        }
        boolean dispatchWasClean = (dispatchLog.getStructuralNotes() == null || dispatchLog.getStructuralNotes().trim().isEmpty());
        boolean returnHasDamage = (this.structuralNotes != null && !this.structuralNotes.trim().isEmpty());

        if (dispatchWasClean && returnHasDamage) {
            this.requiresAdminReview = true;
        }

        return totalPenalty;
    }

    public int getInspectionId() { return inspectionId; }
    public RentalContractModel getContract() { return contract; }
    public String getInspectionType() { return inspectionType; }
    public int getFuelLevel() { return fuelLevel; }
    public String getTireTrackCondition() { return tireTrackCondition; }
    public String getStructuralNotes() { return structuralNotes; }
    public String getPhotoPath() { return photoPath; }
    public UserModel getInspectedBy() { return inspectedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRequiresAdminReview() { return requiresAdminReview; }

    public void setInspectionId(int inspectionId) {
        this.inspectionId = inspectionId;
    }

    public void setContract(RentalContractModel contract) {
        this.contract = contract;
    }

    public void setInspectionType(String inspectionType) {
        this.inspectionType = inspectionType;
    }

    public void setFuelLevel(int fuelLevel) {
        if (fuelLevel >= 0 && fuelLevel <= 100) {
            this.fuelLevel = fuelLevel;
        } else {
            System.err.println("Error: Fuel level must be between 0 and 100.");
        }
    }

    public void setTireTrackCondition(String tireTrackCondition) {
        this.tireTrackCondition = tireTrackCondition;
    }

    public void setStructuralNotes(String structuralNotes) {
        this.structuralNotes = structuralNotes;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setInspectedBy(UserModel inspectedBy) {
        this.inspectedBy = inspectedBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
