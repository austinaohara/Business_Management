# Business Management System

Advanced Programming Capstone

(FSC - CSC311: Capstone Project)

## Table of Contents
- [Summary](#summary)
- [Technologies](#technologies)
- [Setup](#setup)
  - [Prerequisites](#prerequisites)
  - [Instructions](#instructions)
  - [Testing Credentials & Guide](#testing-credentials--guide)
  - [Prerequisites](#prerequisites)
  - [Instructions](#instructions)
- [Features](#features)
- [Intended Users](#intended-users)
- [How it Works](#how-it-works)
  - [Authentication & Registration](#authentication--registration)
    - [Login](#login)
    - [Registration](#registration)
  - [Main Dashboard](#main-dashboard)
  - [Inventory Page](#inventory-page)
  - [Supplier Page](#supplier-page)
  - [Customer Page](#customer-page)
  - [Sales Page](#sales-page)
  - [Database & Repository Architecture](#database--repository-architecture)
  - [Testing & Quality Assurance](#testing--quality-assurance)
- [AI Usage](#ai-usage)
- [Status](#status)
- [Credits](#credits)

## Summary
The Business Management System is a full-stack desktop application built with JavaFX and an embedded Apache Derby SQL database. The main purpose of this application is to streamline and optimize the performance of a small, short-staffed retail business. By centralizing and digitizing operations, the application eliminates the need for generic pen-and-paper tracking or Excel sheets. 

Developed collaboratively as a team project for CSC311, this system encapsulates the core principles of modern software development, utilizing MVC architecture, strong repository data patterns, and an intuitive, dynamic user interface. From product receiving and supplier communication to customer management and order processing, this system serves as a holistic, centralized hub for daily retail operations.

## Technologies 
- **IntelliJ IDEA** - Primary IDE
- **Figma Make** - [Prototyping](https://www.figma.com/make/kLrltERLUJL0mtTIlUgtaw/Business-Management-Application?p=f&t=QkqfHU0iVCGg5Xav-0&fullscreen=1)
- **Java** (JDK 25) - Backend programming language
- **JavaFX** (Version 23) - Frontend markup language, UI controls, and graphics
- **Apache Derby** (Version 10.16) - Embedded relational SQL database
- **Maven** - Dependency management and build tool
- **CSS** - Custom styling for a modern, responsive user interface

## Setup

### Prerequisites:
1. **Java Development Kit (JDK)**
   - Version: 25 or higher
2. **Apache Maven**
   - Required for fetching JavaFX and Derby dependencies.

### Instructions:
1. **Clone the Repository:** Pull the latest code from the master branch.
2. **Build the Project:** Allow Maven to resolve and download the required dependencies (JavaFX and Apache Derby) defined in the `pom.xml`.
3. **Run the Application:** Execute the `Main.java` file. 
4. **Database Configuration (Automatic):** You do not need to install an external database server. On the first launch, the `DatabaseManager.java` class will automatically execute and generate a local `BusinessManagementDB` folder in your project directory containing the complete SQL schema.

### Testing Credentials & Guide
Because this repository safely ignores local `.db` files, the database will be generated from scratch upon your first launch. 

**Recommended Testing Path (Registration):**
We highly recommend creating a **new account** via the Registration portal. This will allow you to experience the Multi-Tenant architecture in action as the system provisions a dedicated Apache Derby database for your profile and automatically seeds the default supplier data in the background.

**Quick Access:**
If you prefer to bypass registration, the `DatabaseManager` automatically creates a default administrator profile upon initial compilation.
- **Username:** Admin
- **Password:** password

## Features
- **Centralized Dashboard**: Live statistics for total revenue, active orders, and low-stock alerts.
- **Inventory Tracking**: Add products, track storage locations, and monitor minimum stock thresholds.
- **Supplier Coordination**: Manage active vendors, view upcoming deliveries, and submit new purchase order requests.
- **Customer Profiles**: Store client contact details, track order history, and perform full CRUD operations (Create, Read, Update, Delete) with built-in safety confirmation prompts to prevent accidental data loss.
- **Auto-Backups**: Utilizes JavaFX application lifecycle hooks (`stop()`) to trigger automatic database backup routines during secure user logouts and emergency window closures.
- **System Audit Logging**: A secure background utility automatically tracks employee authentication events and sales transactions into a centralized, timestamped log file.
- **CSV Data Export**: Generate formatted `.csv` reports for inventory stock, sales ledgers, and customer contact lists directly from the UI.
- **Secure Authentication & Registration**: A dedicated login portal to verify staff credentials, allowing new employees to securely register their own accounts.
- **Dynamic Theming**: Support for light and dark mode staff preferences, automatically saved to the database and applied upon login.
- **Global Session Management**: Utilizes a `UserSession` singleton to securely track the active employee navigating the application.
- **Dynamic UI Updates**: Implements a custom `Refreshable` interface across controllers to ensure table views and dashboard statistics instantly reflect database changes without requiring a hard reload.
- **Input Sanitization**: Utilizes a custom `TextFieldFormatter` to enforce strict formatting rules on the frontend.

## Intended Users
- **Retail Staff**: Can process customer orders, look up product locations, and view contact info.
- **Store Managers**: Can add new inventory, adjust pricing, and request new orders from suppliers.
- **System Admins**: Have overarching control over staff profiles and database management.

## How it Works

### Authentication & Registration
![Welcome Window](https://github.com/user-attachments/assets/e088bc34-3a83-4326-a823-d2be4c43b75d)

Upon launching the application, users are presented with a secure authentication portal. This layer ensures that only authorized retail staff and system admins can access the business's sensitive data. 

Existing staff members can securely enter their credentials, which the `LoginController.java` validates directly against the embedded Apache Derby database. New employees can seamlessly toggle to the Registration view to create their own accounts, assign a secure password, and define their preferred application theme (Light or Dark mode). Once registered, their profile is instantly validated, granting them immediate access to the Main Dashboard with their custom theme automatically applied.

### Login 
![Login Window](https://github.com/user-attachments/assets/13b5a173-4b47-47d3-9168-ed80ef7e7788)

The primary entry point for existing users. Staff members securely enter their credentials, which the `LoginController.java` validates directly against the `StaffProfiles` table within the embedded Apache Derby database. Upon successful authentication, the user is transitioned to the Main Dashboard, and their saved theme preference is automatically applied.

#### Registration
![Registration Window](https://github.com/user-attachments/assets/becc2823-7e43-4e0d-a0f5-de3703c3db35)

New employees can access the Registration view to create their own accounts. The `RegistrationController.java` handles capturing their details, assigning a secure password, and allowing the user to define their preferred application theme (Light or Dark mode). Once registered, their profile is instantly validated and saved to the database, granting them immediate login access.

### Main Dashboard
![Main Dashboard](https://github.com/user-attachments/assets/db310169-dd98-497f-ab43-d620f59194b3)

Once logged in, users are greeted by the Main Dashboard. The application uses a `BorderPane` layout with a persistent left-hand navigation menu. The menu is controlled by the `MainController.java`, which loads the FXML files to keep memory usage low and dynamically updates the active CSS styling based on the user's current view.

The Dashboard provides immediate business intelligence, including total revenue, total orders, active products, and a table of recent customer orders fetched from the database.

### Inventory Page
![Inventory Page](https://github.com/user-attachments/assets/d7e8fc13-5ecb-4b05-ac63-adac6c313749)

The Inventory Management page displays the current stock of the business. Managers can view detailed tables containing Product IDs, Categories, Stock levels, and Pricing. 

Through the `InventoryController.java`, users can open a hidden form to add new products to the catalog. The system automatically highlights items that have dropped below their minimum stock threshold, alerting staff that a reorder is necessary.

### Supplier Page
![Supplier Page](https://github.com/user-attachments/assets/c88b8a7f-8303-4314-b4bc-e5c71223efbe)

Instead of relying on disorganized email threads for vendor communication, the Supplier Page offers a dedicated interface to maintain contact info, lead times, and active purchase orders. 

Users can view "Upcoming Deliveries" to see exactly when items will arrive and use the "New Order Request" form to draft purchase orders for specific suppliers, assigning priority levels and budgets to each request.

### Customer Page
![Customer Page](https://github.com/user-attachments/assets/c453944a-3c0f-4d0d-b3f5-b8480833dcdb)

A dedicated dashboard to maintain customer relations. Staff can view detailed profiles containing contact information, making it easier to follow up on orders, respond to reviews, and track purchasing history. The interface supports full data manipulation, allowing administrators to dynamically edit existing customer details or permanently delete profiles through secure, alert-protected UI actions.

### Sales Page
![Sales Page](https://github.com/user-attachments/assets/ced434c4-b8a5-49e2-96bf-750b04cb6c11)

The Sales page is the point of sale and order processing center of the application. Staff can use this dashboard to process new retail orders and link transactions directly to existing customer profiles. 

When a new `SalesOrder` is processed and marked as completed, the system automatically communicates with the `InventoryDataRepository` to deduct the purchased quantities from the active stock, ensuring the dashboard statistics and low-stock alerts are always running on real-time data.

### Database & Repository Architecture

The backend relies on an embedded **Apache Derby** SQL database, utilizing a multi-tenant architecture to ensure data isolation and security.
- **Master & Per-User Databases:** The `DatabaseManager` maintains a central Master DB exclusively for secure `StaffProfiles` authentication. Upon login, the system dynamically routes the user to their own dedicated, isolated database for all operational tables (Inventory, Customers, Sales).
- **Interfaces & Implementations:** The application uses the **Repository Pattern**. Interfaces define the required operations, while data classes handle the JDBC SQL queries using the active `UserSession` connection.
- **Data Integrity:** Model classes are strictly validated using a custom `ModelValidation` utility, Enums (`DeliveryStatus`, `ThemePreference`), and a `TextFieldFormatter` to ensure data integrity before executing SQL inserts.
- **Decoupled Architecture:** The system fully separates the UI layer (JavaFX Controllers) from the Database layer, allowing for highly modular, testable, and scalable code.
- **Auditing:** The system actively monitors data integrity by writing to a local `system_audit.log` file for critical transactions. Furthermore, it overrides native JavaFX lifecycle methods to execute Apache Derby's internal `SYSCS_BACKUP_DATABASE` procedure, guaranteeing a failsafe data export if the application is unexpectedly terminated.

### Testing & Quality Assurance

To maintain a high standard of code reliability, the system is backed by a robust suite of JUnit tests. 
- **Model Validation:** Every entity (Product, Customer, PurchaseOrder, SalesOrder) is strictly tested to ensure constraints (like negative stock values, invalid emails, or missing names) are caught immediately.
- **State Verification:** Tests ensure that state changes, such as moving a `PurchaseOrderStatus` from `PENDING` to `DELIVERED`, behave exactly as expected before interacting with the database.

## AI Usage
Our team leveraged generative AI tools (including Claude, Codex, and Gemini) to accelerate the development of this capstone project. All AI-generated code was strictly reviewed, modified, and integrated by the team to meet our architectural standards. Specific use cases included:

- **UI/UX Design & Frontend:** Utilized Figma's AI generation tools to design high-fidelity mockups and prototypes. Codex and Claude were used to assist in translating these designs into functional JavaFX `.fxml` markup, specifically aiding in the layout of login page components.
- **Boilerplate & Core Logic:** AI was used to generate repetitive foundational model boilerplate, draft pseudocode for initial logic mapping, and assist in rewiring system functionality.
- **Optimization & Debugging:** Used continuously as a pair-programming partner to troubleshoot stack traces, optimize JDBC database queries, and refine JavaFX event handling across the controllers.

## Status
**Completed** - The project has reached its final production-ready state. All frontend UI components are wired to the embedded Apache Derby database. The underlying multi-tenant data architecture is fully implemented and successfully tested.

## Credits
- **Jayden Montalvo** - [GitHub Profile](https://github.com/JaydenMontalvo)
- **Austin O'Hara** - [GitHub Profile](https://github.com/austinaohara)
- **Giovanni Pugni** - [GitHub Profile](https://github.com/Giovanni6722)
- **Jomar Lubin** - [GitHub Profile](https://github.com/jomarlub17)
- **Olamide Aroso** - [GitHub Profile](https://github.com/olaaroso)
- **Fabian Vasquez** - [GitHub Profile](https://github.com/uriel128)

*Repository link: [Business Management System](https://github.com/austinaohara/Business_Management)*
