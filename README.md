# 🚜 Fleet Equipment Rentals - Enterprise ERP

A comprehensive, cloud-connected Enterprise Resource Planning (ERP) desktop application built with Java Swing. This system is designed specifically for heavy machinery and light tool rental businesses (e.g., excavators, cranes, concrete mixers). It manages the entire lifecycle of equipment dispatching, operator assignment, client management, penalty calculations, and automated PDF billing.

## ✨ Key Features

* **Smart Fleet Inventory:** Handles both `Heavy Machinery` (engine hours, operator requirements, specific OT rates) and `Light Tools` (quantity-based tracking, cleaning requirements).
* **Operator & Crew Management:** Assign specialized operators to specific machines (Wet Hire vs. Dry Hire). Tracks machine-specific daily wages and time-and-a-half Overtime (OT) rates.
* **Contract & Dispatch Wizard:** A streamlined UI to assign equipment to clients, validate starting engine meters, and automatically deduct from available inventory.
* **Automated PDF Generation (iText):**
    * **DPV (Dispatch Notice):** Generates an estimated quote outlining expected days, machine rates, and operator wages upon checkout.
    * **INV (Final Invoice):** Generates a final settlement bill upon return, automatically calculating engine hours, OT penalties, fuel surcharges (Rs. 300/L), and damage fees.
* **Cloud PDF Reconstruction:** If a local PDF is deleted or accessed from a different computer, the system can flawlessly reconstruct the exact historical DPV or INV document directly from the cloud database.
* **Risk & Insurance Tracking:** Tracks Client NIC/BRN numbers and flags expired corporate liability insurance policies.
* **Role-Based Security:** Administrators have full access, while Desk Clerks are restricted from viewing system user management, inventory pricing, and operator wages. All deletions are "Soft Deletes" to permanently preserve financial history.

## 🛠️ Tech Stack

* **Frontend:** Java Swing with [FlatLaf](https://www.formdev.com/flatlaf/) (FlatMacLightLaf) for a modern, high-DPI UI.
* **Backend:** Java 17+ (Object-Oriented Architecture, DAO Pattern).
* **Database:** MySQL (Hosted on Railway Cloud).
* **Connection Pooling:** [HikariCP](https://github.com/brettwooldridge/HikariCP) for high-performance, lag-free cloud queries.
* **Document Generation:** iText PDF.
* **Build Tool:** Maven.

## 🗄️ Database Architecture Highlights

The system relies on a highly normalized relational database with strict foreign key constraints:
* **Polymorphic Equipment Setup:** A base `Equipment` table branches into `Heavy_Machinery` and `Light_Tools` to store category-specific metrics without null-column bloat.
* **Soft Deletes:** Tables utilize an `is_active` boolean. Deleting a user, client, or machine hides them from the UI but retains them in the database to prevent historical invoice corruption (`ON DELETE RESTRICT`).


Prerequisites
* Java Development Kit (JDK) 17 or higher.
* Apache Maven.
* An active MySQL Database (e.g., Railway, AWS, or local XAMPP/WAMP).
