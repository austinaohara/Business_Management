# Business Management System

Advanced Programming Capstone

(FSC - CSC311: Capstone Project)

## Table of Contents
- [Summary](#summary)
- [Technologies](#technologies)
- [Setup](#setup)
  - [Prerequisites](#prerequisites)
  - [Instructions](#instructions)
- [Features](#features)
- [Intended Users](#intended-users)
- [How it Works](#how-it-works)
  - [Program UI/UX & Main Dashboard](#program-uiux--main-dashboard)
  - [Inventory Page](#inventory-page)
  - [Supplier Page](#supplier-page)
  - [Customer Page](#customer-page)
  - [Database & Repository Architecture](#database--repository-architecture)
- [Status](#status)
- [Credits](#credits)

## Summary
The Business Management System is a full-stack desktop application built with JavaFX and an embedded Apache Derby SQL database. The main purpose of this application is to streamline and optimize the performance of a small, short-staffed retail business. By centralizing and digitizing operations, the application eliminates the need for generic pen-and-paper tracking or Excel sheets. 

Developed collaboratively as a team project for CSC311, this system encapsulates the core principles of modern software development, utilizing MVC architecture, strong repository data patterns, and an intuitive, dynamic user interface. From product receiving and supplier communication to customer management and order processing, this system serves as a holistic, centralized hub for daily retail operations.

## Technologies 
- **IntelliJ IDEA** - Primary IDE
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

## Features
- **Centralized Dashboard**: Live statistics for total revenue, active orders, and low-stock alerts.
- **Inventory Tracking**: Add products, track storage locations, and monitor minimum stock thresholds.
- **Supplier Coordination**: Manage active vendors, view upcoming deliveries, and submit new purchase order requests.
- **Customer Profiles**: Store client contact details and track order history.
- **Dynamic Theming**: (Upcoming) Support for light and dark mode staff preferences.

## Intended Users
- **Retail Staff**: Can process customer orders, look up product locations, and view contact info.
- **Store Managers**: Can add new inventory, adjust pricing, and request new orders from suppliers.
- **System Admins**: Have overarching control over staff profiles and database management.

## How it Works

### Program UI/UX & Main Dashboard
![Main Dashboard](https://github.com/user-attachments/assets/fa7302c4-66b0-4096-bfd5-65cb020b10e3)

Upon launching the application, users are greeted by the Main Dashboard. The application uses a `BorderPane` layout with a persistent left-hand navigation menu. The menu is controlled by the `MainController.java`, which loads the FXML files to keep memory usage low and dynamically updates the active CSS styling based on the user's current view.

The Dashboard provides immediate business intelligence, including total revenue, total orders, active products, and a table of recent customer orders fetched from the database.

### Inventory Page
![Inventory Page](https://github.com/user-attachments/assets/8e310b2a-3a34-41d8-8e9f-9adf18aaac74)

The Inventory Management page displays the current stock of the business. Managers can view detailed tables containing Product IDs, Categories, Stock levels, and Pricing. 

Through the `InventoryController.java`, users can open a hidden form to add new products to the catalog. The system automatically highlights items that have dropped below their minimum stock threshold, alerting staff that a reorder is necessary.

### Supplier Page
![Supplier Page](https://github.com/user-attachments/assets/b1cabff3-96b8-4620-943a-91bd3d8f6aa1)

Instead of relying on disorganized email threads for vendor communication, the Supplier Page offers a dedicated interface to maintain contact info, lead times, and active purchase orders. 

Users can view "Upcoming Deliveries" to see exactly when items will arrive and use the "New Order Request" form to draft purchase orders for specific suppliers, assigning priority levels and budgets to each request.

### Customer Page
![Customer Page](https://github.com/user-attachments/assets/8bd1bbc8-4670-4a35-8572-1e10590d5d32)

A dedicated dashboard to maintain customer relations. Staff can view detailed profiles containing contact information, making it easier to follow up on orders, respond to reviews, and track purchasing history.

### Sales Page
![Sales Page](https://github.com/user-attachments/assets/924cdf8d-9bc9-4e41-ade8-c35bd7abb19b)

The Sales page is the point of sale and order processing center of the application. Staff can use this dashboard to process new retail orders and link transactions directly to existing customer profiles. 

When a new `SalesOrder` is processed and marked as completed, the system automatically communicates with the `InventoryDataRepository` to deduct the purchased quantities from the active stock, ensuring the dashboard statistics and low-stock alerts are always running on real-time data.

### Database & Repository Architecture

The backend relies on an embedded **Apache Derby** SQL database. To ensure clean, maintainable code, the application uses the **Repository Pattern**. 
- Interfaces (e.g., `ProductRepository`, `SupplierRepository`) define the required database operations.
- Data classes (e.g., `InventoryDataRepository`) handle the actual JDBC SQL queries.
- Model classes (e.g., `Product`, `Supplier`, `PurchaseOrder`) are strictly validated using a custom `ModelValidation` class and Enums (`DeliveryStatus`, `ThemePreference`) to ensure data integrity before it ever reaches the database.

## Status
**Active Development** - The project is currently in the implementation phase. The frontend UI shell, database schema, and core repository models are complete. Current development is focused on wiring the JavaFX controllers to the database repositories using data-binding.

## Credits
- **Jayden Montalvo** - [GitHub Profile](https://github.com/JaydenMontalvo)
- **Austin O'Hara** - [GitHub Profile](https://github.com/austinaohara)
- **Giovanni Pugni** - [GitHub Profile](https://github.com/Giovanni6722)
- **Jomar Lubin** - [GitHub Profile](https://github.com/jomarlub17)
- **Olamide Aroso** - [GitHub Profile](https://github.com/olaaroso)
- **Fabian Vasquez** - [GitHub Profile](https://github.com/uriel128)

*Repository link: [Business Management System](https://github.com/austinaohara/Business_Management)*
