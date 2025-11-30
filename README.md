# FinalAmbulanceTracker

Simple terminal-based ambulance tracking demo (Java).

Requirements
- Java JDK 8 or later installed and on PATH

How to compile (PowerShell)

```powershell
Set-Location -Path 'd:\DSA_JAVA_OOSE_Project\FinalAmbulanceTracker'
javac *.java
```

How to run (PowerShell)

Interactive run:

```powershell
Set-Location -Path 'd:\DSA_JAVA_OOSE_Project\FinalAmbulanceTracker'
java AmbulanceTracker
```

Run and auto-exit (send choice 8 to exit immediately):

```powershell
Set-Location -Path 'd:\DSA_JAVA_OOSE_Project\FinalAmbulanceTracker'
echo 8 | java AmbulanceTracker
```

Notes
- The project compiles and runs as-is. The main class is `AmbulanceTracker`.
- The application is interactive; use the menu to request ambulances, view availability, change user role, and simulate transfers.
- I ran a compilation and a quick execution (sent "8" to exit) to verify startup.

Next suggestions (optional)
- Add input validation and clearer prompts for production use.
- Add unit tests or a simple CLI test harness for common scenarios.
- Consider packaging as a JAR for distribution.
