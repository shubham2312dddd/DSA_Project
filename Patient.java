public class Patient {
    private String name;
    private String area;
    private int severity;
    private String emergencyDescription;
    private String injuryType;

    public Patient(String name, String area, int severity, String emergencyDescription, String injuryType) {
        this.name = name;
        this.area = area;
        this.severity = severity;
        this.emergencyDescription = emergencyDescription;
        this.injuryType = injuryType;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public int getSeverity() {
        return severity;
    }

    public String getEmergencyDescription() {
        return emergencyDescription;
    }

    public String getInjuryType() {
        return injuryType;
    }
}