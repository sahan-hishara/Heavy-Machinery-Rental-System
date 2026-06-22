package utils;

import models.HeavyMachinery;
import models.InvoiceModel;
import models.RentalContractModel;
import models.OperatorModel;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PDFGenerator {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(17, 24, 39));
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new BaseColor(37, 99, 235));
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(55, 65, 81));
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(17, 24, 39));
    
    // ADD THIS MISSING LINE RIGHT HERE:
    private static final Font OBLIQUE_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, new BaseColor(107, 114, 128));

    // ========================================================================
    // 1. GENERATE DISPATCH NOTICE (DPV) - Shows Estimates & Rates before the job
    // ========================================================================
    public static String generateDispatchPDF(RentalContractModel contract, double advancePaid) {
        String dirPath = System.getProperty("user.dir") + File.separator + "Documents";
        new File(dirPath).mkdirs();

        String docNumber = String.format("%04d", contract.getContractId());
        String fullPath = dirPath + File.separator + "DPV-" + docNumber + ".pdf";
        Document doc = new Document();

        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fullPath)); doc.open();
            
            doc.add(new Paragraph("FLEET EQUIPMENT RENTALS", TITLE_FONT));
            doc.add(new Paragraph("DISPATCH NOTICE & RATE AGREEMENT #DPV-" + docNumber + "\n\n", HEADER_FONT));

            PdfPTable infoTable = new PdfPTable(2); infoTable.setWidthPercentage(100); infoTable.setSpacingAfter(15f);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            infoTable.addCell(createCell("Contract Ref: CTR-" + docNumber + "\nDispatched On: " + contract.getIssueDate().format(fmt) + "\nExpected Return: " + contract.getExpectedReturn().format(fmt), NORMAL_FONT, Element.ALIGN_LEFT));
            String clientName = contract.getClient().getCompanyName() != null ? contract.getClient().getCompanyName() : contract.getClient().getContactPerson();
            infoTable.addCell(createCell("Billed To:\n" + clientName, BOLD_FONT, Element.ALIGN_RIGHT));
            doc.add(infoTable);

            // Calculate Estimates
            long estDays = ChronoUnit.DAYS.between(contract.getIssueDate().toLocalDate(), contract.getExpectedReturn().toLocalDate());
            if (estDays < 1) estDays = 1;
            boolean isHeavy = contract.getEquipment() instanceof HeavyMachinery;

            PdfPTable detailsTable = new PdfPTable(2); detailsTable.setWidthPercentage(100); detailsTable.setWidths(new float[]{3f, 1f});
            addTableHeader(detailsTable, "Dispatch Details & Agreed Rates"); addTableHeader(detailsTable, "Rate");

            addRow(detailsTable, "Equipment: " + contract.getEquipment().getAssetTag() + " (" + contract.getEquipment().getBrandModel() + ")\nBase Daily Machine Rental Fee", contract.getEquipment().getBaseDailyRate());
            
            double estMachineTotal = contract.getEquipment().getBaseDailyRate() * estDays;
            double estOpTotal = 0;

            if (isHeavy) {
                HeavyMachinery heavy = (HeavyMachinery) contract.getEquipment();
                addRow(detailsTable, "Machine Overtime Rate (Applied after 8 hrs/day)", heavy.getOtRate());
                addRow(detailsTable, "Starting Engine Meter", contract.getStartMeter());
                
                if (contract.isWetHire() && contract.getOperator() != null) {
                    double opOtRate = (heavy.getOperatorWage() / 8.0) * 1.5; // Standard Time-and-a-half
                    addRow(detailsTable, "Assigned Operator: " + contract.getOperator().getFullName() + "\nOperator Daily Wage", heavy.getOperatorWage());
                    addRow(detailsTable, "Operator Overtime Rate", opOtRate);
                    estOpTotal = heavy.getOperatorWage() * estDays;
                }
            }

            PdfPCell spacer = new PdfPCell(new Phrase(" ")); spacer.setBorder(PdfPCell.NO_BORDER); detailsTable.addCell(spacer); detailsTable.addCell(spacer);

            addRow(detailsTable, "ESTIMATED Machine Cost (" + estDays + " days)", estMachineTotal);
            if (estOpTotal > 0) addRow(detailsTable, "ESTIMATED Operator Cost (" + estDays + " days)", estOpTotal);
            
            double estGross = estMachineTotal + estOpTotal;
            addRow(detailsTable, "ESTIMATED GROSS TOTAL (Excluding Fuel/OT)", estGross);
            addRow(detailsTable, "LESS: Advance Payment Made", -advancePaid);
            
            PdfPCell finalLbl = new PdfPCell(new Phrase("ESTIMATED BALANCE DUE", BOLD_FONT)); finalLbl.setPadding(8f);
            PdfPCell finalVal = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", (estGross - advancePaid)), BOLD_FONT)); finalVal.setPadding(8f); finalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            detailsTable.addCell(finalLbl); detailsTable.addCell(finalVal);

            doc.add(detailsTable); doc.close(); return fullPath;
        } catch (Exception e) { return null; }
    }

    // ========================================================================
    // 2. GENERATE FINAL BILL (INV) - Shows Exact Hours, Fuel, and Penalties
    // ========================================================================
    public static String generateFinalBillPDF(InvoiceModel invoice, double endMeter, double fuelLiters, String damageNotes, LocalDateTime actualReturn) {
        RentalContractModel contract = invoice.getContract();
        String dirPath = System.getProperty("user.dir") + File.separator + "Documents";
        new File(dirPath).mkdirs();

        String docNumber = String.format("%04d", contract.getContractId());
        String fullPath = dirPath + File.separator + "INV-" + docNumber + ".pdf";
        Document doc = new Document();

        try {
            PdfWriter.getInstance(doc, new FileOutputStream(fullPath)); doc.open();
            
            doc.add(new Paragraph("FLEET EQUIPMENT RENTALS", TITLE_FONT));
            doc.add(new Paragraph("FINAL SETTLEMENT INVOICE #INV-" + docNumber + "\n\n", HEADER_FONT));

            PdfPTable infoTable = new PdfPTable(2); infoTable.setWidthPercentage(100); infoTable.setSpacingAfter(15f);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            infoTable.addCell(createCell("Contract Ref: CTR-" + docNumber + "\nDispatched On: " + contract.getIssueDate().format(fmt) + "\nReturned On: " + actualReturn.format(fmt), NORMAL_FONT, Element.ALIGN_LEFT));
            String clientName = contract.getClient().getCompanyName() != null ? contract.getClient().getCompanyName() : contract.getClient().getContactPerson();
            infoTable.addCell(createCell("Billed To:\n" + clientName, BOLD_FONT, Element.ALIGN_RIGHT));
            doc.add(infoTable);

            long actualDays = ChronoUnit.DAYS.between(contract.getIssueDate().toLocalDate(), actualReturn.toLocalDate());
            if (actualDays < 1) actualDays = 1;
            boolean isHeavy = contract.getEquipment() instanceof HeavyMachinery;

            PdfPTable detailsTable = new PdfPTable(2); detailsTable.setWidthPercentage(100); detailsTable.setWidths(new float[]{3f, 1f});
            addTableHeader(detailsTable, "Billing Description"); addTableHeader(detailsTable, "Amount (Rs.)");

            double totalMachine = contract.getEquipment().getBaseDailyRate() * actualDays;
            addRow(detailsTable, "Equipment Rental: " + contract.getEquipment().getAssetTag() + "\n(" + actualDays + " Days @ Rs." + contract.getEquipment().getBaseDailyRate() + "/day)", totalMachine);

            double totalOp = 0, totalOpOt = 0, totalMachineOt = 0;

            if (isHeavy) {
                HeavyMachinery heavy = (HeavyMachinery) contract.getEquipment();
                double totalHours = endMeter - contract.getStartMeter();
                double allowedHours = actualDays * 8.0;
                double otHours = totalHours > allowedHours ? totalHours - allowedHours : 0;

                PdfPCell meterCell = new PdfPCell(new Phrase("Meter Metrics: Start [" + contract.getStartMeter() + "] - End [" + endMeter + "]\nTotal Hours Worked: " + totalHours + " hrs (Allowed: " + allowedHours + " hrs)", OBLIQUE_FONT));
                meterCell.setColspan(2); meterCell.setBorderColor(new BaseColor(228, 228, 231)); meterCell.setPadding(6f); detailsTable.addCell(meterCell);

                if (contract.isWetHire()) {
                    totalOp = heavy.getOperatorWage() * actualDays;
                    addRow(detailsTable, "Operator Wage: " + contract.getOperator().getFullName() + "\n(" + actualDays + " Days @ Rs." + heavy.getOperatorWage() + "/day)", totalOp);
                }

                if (otHours > 0) {
                    totalMachineOt = otHours * heavy.getOtRate();
                    addRow(detailsTable, "Machine Overtime Charge\n(" + String.format("%.1f", otHours) + " Hrs @ Rs." + heavy.getOtRate() + "/hr)", totalMachineOt);
                    
                    if (contract.isWetHire()) {
                        double opOtRate = (heavy.getOperatorWage() / 8.0) * 1.5;
                        totalOpOt = otHours * opOtRate;
                        addRow(detailsTable, "Operator Overtime Charge\n(" + String.format("%.1f", otHours) + " Hrs @ Rs." + String.format("%.2f", opOtRate) + "/hr)", totalOpOt);
                    }
                }
            }

            if (fuelLiters > 0) addRow(detailsTable, "Fuel Surcharge (" + fuelLiters + " Liters @ Rs. 300.00/L)", fuelLiters * 300.00);
            if (invoice.getDamagePenalty() > 0) {
                String note = (damageNotes != null && !damageNotes.isEmpty()) ? damageNotes : "Damage / Cleaning penalty applied upon inspection.";
                addRow(detailsTable, "Damage Penalty\nNotes: " + note, invoice.getDamagePenalty());
            }

            PdfPCell spacer = new PdfPCell(new Phrase(" ")); spacer.setBorder(PdfPCell.NO_BORDER); detailsTable.addCell(spacer); detailsTable.addCell(spacer);

            double grossTotal = totalMachine + totalOp + totalMachineOt + totalOpOt + (fuelLiters * 300.00) + invoice.getDamagePenalty();
            addRow(detailsTable, "GROSS TOTAL INVOICED", grossTotal);
            addRow(detailsTable, "LESS: Advance Deposit", -invoice.getAdvancePaid());
            
            PdfPCell finalLbl = new PdfPCell(new Phrase("FINAL BALANCE DUE", BOLD_FONT)); finalLbl.setPadding(8f);
            PdfPCell finalVal = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", (grossTotal - invoice.getAdvancePaid())), BOLD_FONT)); finalVal.setPadding(8f); finalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            detailsTable.addCell(finalLbl); detailsTable.addCell(finalVal);

            doc.add(detailsTable); doc.close(); return fullPath;
        } catch (Exception e) { return null; }
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font)); cell.setBorder(PdfPCell.NO_BORDER); cell.setHorizontalAlignment(alignment); return cell;
    }
    private static void addTableHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell(new Phrase(title, BOLD_FONT)); header.setBackgroundColor(new BaseColor(243, 244, 246)); header.setBorderWidth(1); header.setBorderColor(new BaseColor(228, 228, 231)); header.setPadding(8f); table.addCell(header);
    }
    private static void addRow(PdfPTable table, String desc, double amount) {
        PdfPCell descCell = new PdfPCell(new Phrase(desc, NORMAL_FONT)); descCell.setPadding(6f); descCell.setBorderColor(new BaseColor(228, 228, 231));
        PdfPCell amountCell = new PdfPCell(new Phrase(String.format("%.2f", amount), NORMAL_FONT)); amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT); amountCell.setPadding(6f); amountCell.setBorderColor(new BaseColor(228, 228, 231));
        table.addCell(descCell); table.addCell(amountCell);
    }
}