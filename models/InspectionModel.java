package models;

import java.time.LocalDateTime;

public class InspectionModel {

    // Private attributes matching the database schema
    private int inspectionId;
    
    // Object Composition
    private RentalContractModel contract;
    private UserModel inspectedBy;
    
    private String inspectionType; // 'Dispatch' or 'Return'
    private int fuelLevel; // Stored as a percentage (0 to 100)
    private String tireTrackCondition; // 'Good', 'Fair', 'Poor'
    private String structuralNotes;
    private String photoPath; // Local file path to the damage photo
    private LocalDateTime createdAt;
    
    // Internal flag for unquantifiable damages that require human intervention
    private boolean requiresAdminReview;

    /**
     * Full Constructor: Used by the DAO when pulling logs from the database.
     */
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

    /**
     * UI Constructor: Used when the Desk Clerk submits a new inspection form.
     */
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

    // --- Core Risk Management Logic ---

    /**
     * Compares this 'Return' inspection against the original 'Dispatch' inspection.
     * Automatically tallies up standard penalties (like fuel) and flags severe damage.
     * * @param dispatchLog The original inspection conducted when the keys were handed over.
     * @return The calculable penalty in rupees to be added to the Invoice.
     */
    public double calculateDamagePenalty(InspectionModel dispatchLog) {
        // Security check: Ensure we are actually comparing a Return to a Dispatch
        if (!"Return".equalsIgnoreCase(this.inspectionType) || !"Dispatch".equalsIgnoreCase(dispatchLog.getInspectionType())) {
            System.err.println("Error: Invalid comparison. Must compare Return to Dispatch.");
            return 0.0;
        }

        double totalPenalty = 0.0;

        // 1. Calculate the Refueling Premium (e.g., 50.00 rupees per 1% of fuel missing)
        if (this.fuelLevel < dispatchLog.getFuelLevel()) {
            int missingFuelPercentage = dispatchLog.getFuelLevel() - this.fuelLevel;
            totalPenalty += (missingFuelPercentage * 50.00); 
        }

        // 2. Track/Tire Degradation Check
        // If it left 'Good' and came back 'Poor', apply a flat hardware wear penalty
        if ("Good".equalsIgnoreCase(dispatchLog.getTireTrackCondition()) && 
            "Poor".equalsIgnoreCase(this.tireTrackCondition)) {
            totalPenalty += 15000.00; 
        }

        // 3. Unquantifiable Structural Damage
        // If the dispatch notes were empty/clean, but the return notes have text (like "Dent on right door")
        boolean dispatchWasClean = (dispatchLog.getStructuralNotes() == null || dispatchLog.getStructuralNotes().trim().isEmpty());
        boolean returnHasDamage = (this.structuralNotes != null && !this.structuralNotes.trim().isEmpty());

        if (dispatchWasClean && returnHasDamage) {
            // We cannot automatically calculate the price of a dent, so we lock the invoice 
            // and flag it for an Admin to review and manually type in a repair quote.
            this.requiresAdminReview = true;
        }

        return totalPenalty;
    }

    // --- Standard Getters ---

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

    // --- Sanitizing Setters ---

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
        // Enforce the 0-100 percentage constraint
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