public class Doctor {
    String name;
    String specialization;

    public Doctor(String name, String specialization) {
        this.name = name;
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Specialization: " + specialization;
    }
}