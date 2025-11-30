import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class Hospital {
    int locationId;
    String name;
    LinkedList<Ambulance> ambulances;
    List<Doctor> doctors; // Added list of doctors

    public Hospital(int locationId, String name) {
        this.locationId = locationId;
        this.name = name;
        this.ambulances = new LinkedList<>();
        this.doctors = new ArrayList<>(); // Initialize the list of doctors
    }

    public String getName() {
        return name;
    }

    public List<Ambulance> getAmbulances() {
        return ambulances;
    }

    public void addAmbulance(String id) {
        ambulances.add(new Ambulance(id));
    }

    public void addDoctor(String name, String specialization) {
        doctors.add(new Doctor(name, specialization));
    }

    public Doctor getAssignedDoctor(String injuryType) {
        for (Doctor doctor : doctors) {
            if (injuryType != null && doctor.specialization.toLowerCase().contains(injuryType.toLowerCase())) {
                return doctor;
            }
        }
        return null; // No matching doctor found
    }

    public Ambulance getAvailableAmbulance(int severity) {
        for (Ambulance amb : ambulances) {
            if (amb.isAvailable() /* && ambulance has required capabilities based on severity (if implemented) */) {
                amb.setAvailable(false);
                return amb;
            }
        }
        return null; // Return null if no available ambulance is found
    }

    public Ambulance getAvailableAmbulance() {
        return getAvailableAmbulance(3); // Default to non-urgent
    }

    @Override
    public String toString() {
        return "ID: " + locationId + ", Name: " + name + ", Ambulances: " + ambulances.size() + ", Doctors: " + doctors.size();
    }
}