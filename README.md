# RPG Project

A procedural RPG world generator and viewer built with Java Swing.

## Project Overview

*   **Language:** Java 16
*   **Build System:** Maven
*   **GUI:** Java Swing

## Getting Started

### Prerequisites

*   Java Development Kit (JDK) 16 or newer.
*   Maven.

### Build and Run

1.  **Compile the project:**
    ```bash
    mvn clean compile
    ```

2.  **Run the application:**
    ```bash
    java -cp target/classes game.Main
    ```
    *Optional arguments: `java -cp target/classes game.Main [partySize] [avgLevel]`*

---

## Installing OpenCode on Windows (WSL)

This project uses the `opencode` CLI agent for development assistance. To install `opencode` on Windows, we recommend using the Windows Subsystem for Linux (WSL).

### Step 1: Install WSL (PowerShell)

1.  Open **PowerShell** as Administrator.
2.  Run the following command to install WSL and the default Ubuntu distribution:
    ```powershell
    wsl --install
    ```
3.  Restart your computer if prompted.

### Step 2: Initialize Linux

1.  Open the **Ubuntu** application from your Windows Start menu.
2.  Wait for the installation to initialize.
3.  Create a UNIX username and password when prompted.

### Step 3: Install OpenCode

Inside your new Ubuntu terminal, run the following command to install `opencode`:

```bash
curl -fsSL https://opencode.dev/install.sh | sh
```

*(Note: Replace the URL above with the specific distribution link provided by your organization if different.)*

### Step 4: Configuration

After installation, configure your API keys and authenticate:

```bash
opencode login
```

### Step 5: Start Using OpenCode

Navigate to your Windows project files using the `/mnt/c/` path in your Ubuntu terminal:

```bash
# Example path to your project
cd /mnt/c/Users/YourUsername/IdeaProjects/Rpg

# Start the agent
opencode
```
