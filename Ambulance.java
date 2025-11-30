import java.time.LocalDateTime;

public class Ambulance {
    String ambulanceId;
    private boolean available;
    private LocalDateTime dispatchTime;
    private LocalDateTime arrivalTime;
    private String status; // Added status field based on the state diagram

    public Ambulance(String ambulanceId) {
        this.ambulanceId = ambulanceId;
        this.available = true;
        this.status = "Available"; // Initial status
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
        if (available) {
            this.status = "Available";
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(LocalDateTime dispatchTime) {
        this.dispatchTime = dispatchTime;
        if (dispatchTime != null) {
            this.status = "Dispatched";
        }
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        if (arrivalTime != null) {
            this.status = "AtHospital"; // Assuming arrival at hospital signifies this for now
        }
    }

    @Override
    public String toString() {
        return "ID: " + ambulanceId + ", Status: " + status +
                (dispatchTime != null ? ", Dispatched at: " + dispatchTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME) : "") +
                (arrivalTime != null ? ", Arrived at: " + arrivalTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME) : "");
    }
}