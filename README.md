# Shticell - Collaborative Spreadsheet Application

A multi-user client-server spreadsheet system with real-time collaboration, formula evaluation engine, and comprehensive permission management.

## System Description

This system is a client-server application for collaborative spreadsheet management. The JavaFX client provides a user-friendly interface for editing and viewing spreadsheets, while the server handles data persistence, version control, and user permissions. It ensures real-time updates and consistent collaboration across multiple users.

## Architecture

```
┌─────────────────┐     HTTP/JSON      ┌─────────────────┐
│ ShticellClient  │ ◄─────────────────► │ ShticellServer  │
│   (JavaFX UI)   │                     │   (Servlets)    │
└─────────────────┘                     └─────────────────┘
         │                                       │
         │                                       │
         └───────────────┬───────────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   Logics (Engine)    │
              │  ┌────────────────┐  │
              │  │ DTO │ XML/JAXB │  │
              │  └────────────────┘  │
              └──────────────────────┘
```

### Module Structure

| Module | Purpose | Key Components |
|--------|---------|----------------|
| **Logics** | Core business logic | ShticellEngine, SheetManager, Expression parser, Permissions |
| **DTO** | Data transfer layer | SheetDTO, CellDTO, JSON deserializers |
| **XML** | XML persistence | JAXB parser, generated classes from XSD schema |
| **ShticellServer** | REST API backend | 24 servlet endpoints (login, sheets, permissions) |
| **ShticellClient** | JavaFX frontend | MVC controllers, FXML views, HTTP client |

## Key Features

- **Multi-user Collaboration** - Owner/Reader/Writer permission system with approval workflow
- **Formula Engine** - 20+ expressions (SUM, AVERAGE, REF, IF, AND, OR, PLUS, MINUS, etc.)
- **Version History** - Complete snapshot-based versioning with change tracking
- **Named Ranges** - Define cell groups (row, column, rectangular) for operations
- **Dynamic Analysis** - What-if scenarios without modifying original data
- **Data Operations** - Sorting, filtering by column values
- **Cell Styling** - Background/text colors, custom formatting
- **Error Handling** - Circular dependency detection, type validation, detailed error messages

## Technology Stack

- **Java 21** - Core language
- **JavaFX** - UI framework
- **Apache Tomcat 10.1.26** - Web server
- **Jakarta Servlets** - REST API
- **JAXB 4.0** - XML parsing
- **Gson 2.11.0** - JSON serialization
- **OkHttp** - HTTP client

## Installation & Running

### Prerequisites
- JDK 21 or higher
- Apache Tomcat 10.1.26
- All JAR dependencies (bundled in `Logics/XML/JAXB/mod/`)

### Server Setup
1. Build `ShticellServer.war` from the server module
2. Deploy to Tomcat's `webapps/` directory
3. Start Tomcat server

### Client Execution
```bash
java -jar ShticellClient.jar
```

**Important Notes:**
- Ensure all necessary JAR files are in the same directory as `ShticellClient.jar`, or that their paths are correctly specified in the classpath
- Ensure that a Tomcat server is up and running, having the `ShticellServer.war` file in the webapps path
- If there are cells in the spreadsheet containing invalid formulas or circular dependencies, the system will notify the user accordingly

## Key Design Choices

### 1. System Structure
The system is divided into several modules:
- **Logics module** handles the core logic of the spreadsheet
- **ShticellClient module** provides a graphical user interface with controls for sorting, filtering, dynamic analysis, and cell selection
- **ShticellServer module** contains servlets which interact with the engine through HTTP calls
- **DTO module** contains interfaces representing a "read-only" state of Sheet and Cell objects

### 2. Error Handling
Error handling was enhanced to improve the system's robustness. This includes stricter checks during formula parsing, file loading, and ensuring only valid spreadsheets are loaded. Errors are communicated to the user with detailed error messages.

### 3. Function and Range Support
The system supports a variety of functions, including addition, subtraction, multiplication, division, and cell references (REF). Support for range operations was added, allowing operations on multiple cells at once. A notable design decision was allowing operations involving REF to return special markers like "NaN" or "!UNDEFINED!" if references are empty or cause an error due to type mismatch.

### 4. Graphical Design and Range Features
The interface provides functionalities similar to other popular spreadsheet tools, including the ability to adjust column and row sizes, apply different alignments, and use a color picker for cell formatting. Users can also define ranges of cells, which can be referred to by name, supporting both row-wise, column-wise, and rectangular selections.

## Dominant Classes

| Class | Responsibility |
|-------|---------------|
| `ShticellEngine` | The core class of the system. Contains a Map of SheetManagers |
| `SheetManager` | The core class of spreadsheet, managing the logic and versions of the spreadsheet, including permissions |
| `Sheet` | Represents the data of a single sheet, including cells, dependencies, and other properties |
| `Cell` | Represents a single cell in the spreadsheet. Contains coordinates, original value, effective value (after evaluation), dependencies, and influenced cells |
| `FunctionParser` | Parses and evaluates various functions that appear in the spreadsheet cells |
| `ShticellClient` | Manages user interaction through a GUI made by JavaFX |
| `DashboardController` | Contains the "menu" of the application, navigation through sheets, view permissions, and request/handle permissions |
| `SheetMainController` | Main controller holding top pane, left pane and center pane which holds the sheet. Uses SharedModel for information sharing between controllers |

## Project Structure

```
source_files/
├── DTO/                    # Data Transfer Objects + deserializers
├── Logics/                 # Core engine + expression parser
│   └── XML/                # JAXB XML handling + test sheets
├── ShticellClient/         # JavaFX UI with MVC pattern
└── ShticellServer/         # Servlet REST API
    ├── src/shticell/servlets/
    │   ├── login/          # Authentication
    │   ├── sheet/          # Sheet operations
    │   │   ├── permission/ # Permission management
    │   │   ├── range/      # Range operations
    │   │   ├── readonly/   # Sort, filter, analysis
    │   │   └── cell/        # Cell styling
    │   └── utils/          # Utilities
    └── web/WEB-INF/        # Servlet configuration
```

## Design Patterns

- **DTO Pattern** - Immutable read-only data transfer objects
- **MVC Pattern** - JavaFX controllers with FXML views
- **Strategy Pattern** - Expression evaluation with 20+ implementations
- **Snapshot Pattern** - Version history with deep copying
- **Singleton Pattern** - Engine and session management

---

**Academic Project** - Demonstrating enterprise Java development with client-server architecture, REST APIs, and collaborative features.

