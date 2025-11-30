import java.util.*;
import java.time.LocalDateTime;
import java.time.Duration;

public class AmbulanceTracker {

    // Enums and Constants
    enum UserRole {
        DISPATCHER,
        ADMIN,
        HOSPITAL_STAFF
    }

    static final String TRANSFER_REQUEST = "TRANSFER_REQUEST";

    // Data Structures
    static List<Map<String, Object>> requestHistory = new ArrayList<>();
    static HospitalBST hospitalTree = new HospitalBST();
    static Scanner sc = new Scanner(System.in);
    static Map<String, String> assignedAmbulances = new HashMap<>(); // Track assigned ambulances
    static UserRole currentUserRole = UserRole.DISPATCHER; // Default role

    // Method to find shortest routes using Dijkstra's Algorithm
    static Map<String, Integer> findShortestRoutes(String source, Map<String, Map<String, Integer>> graph) {
        Map<String, Integer> distances = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<Pair<String, Integer>> pq = new PriorityQueue<>(Comparator.comparingInt(Pair::getValue));

        // Initialize distances
        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(source, 0);
        pq.offer(new Pair<>(source, 0));

        // Dijkstra's algorithm
        while (!pq.isEmpty()) {
            Pair<String, Integer> current = pq.poll();
            String u = current.getKey();
            int distU = current.getValue();

            if (visited.contains(u))
                continue;
            visited.add(u);

            if (distU > distances.get(u))
                continue;

            // Iterate through neighbors
            if (graph.containsKey(u)) { // Check if the node exists in the graph
                for (Map.Entry<String, Integer> neighbor : graph.get(u).entrySet()) {
                    String v = neighbor.getKey();
                    int weightUV = neighbor.getValue();
                    int distV = distU + weightUV;

                    if (distV < distances.getOrDefault(v, Integer.MAX_VALUE)) {
                        distances.put(v, distV);
                        pq.offer(new Pair<>(v, distV));
                    }
                }
            }
        }

        // Debugging output to diagnose distances
        System.out.println("Distances from " + source + ":");
        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + (entry.getValue() == Integer.MAX_VALUE ? "∞" : entry.getValue()));
        }

        return distances;
    }

    // Pair class for storing node and distance
    static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    static void findAmbulance(Patient patient, Map<String, Map<String, Integer>> cityHospitalGraph) {
        List<Hospital> sortedHospitals = hospitalTree.getSortedHospitals();
        String nearestHospitalName = null;
        int minDistance = Integer.MAX_VALUE;

        // Find the nearest hospital using Dijkstra's
        Map<String, Integer> distances = findShortestRoutes(patient.getArea(), cityHospitalGraph);
        for (Hospital h : sortedHospitals) {
            if (distances.containsKey(h.getName()) && distances.get(h.getName()) < minDistance) {
                minDistance = distances.get(h.getName());
                nearestHospitalName = h.getName();
            }
        }

        Hospital nearestHospital = findHospitalByName(nearestHospitalName);

        Ambulance amb = nearestHospital.getAvailableAmbulance(patient.getSeverity());
        Doctor assignedDoctor = nearestHospital.getAssignedDoctor(patient.getInjuryType());

        System.out.println("Nearest Hospital: " + nearestHospital.getName() + ", Distance: " + minDistance + " km");
        if (amb != null) {
            System.out.println("Ambulance Found: " + amb.ambulanceId);
            LocalDateTime dispatchTime = LocalDateTime.now();
            amb.setDispatchTime(dispatchTime);
            amb.setStatus("Dispatched");

            Map<String, Object> request = new HashMap<>();
            request.put("type", "EMERGENCY_REQUEST");
            request.put("time", dispatchTime);
            request.put("patientName", patient.getName());
            request.put("patientArea", patient.getArea());
            request.put("severity", patient.getSeverity());
            request.put("description", patient.getEmergencyDescription());
            request.put("injuryType", patient.getInjuryType());
            request.put("ambulanceId", amb.ambulanceId);
            request.put("hospitalName", nearestHospital.getName());
            request.put("status", amb.getStatus());
            request.put("userRole", currentUserRole.toString());
            if (assignedDoctor != null) {
                request.put("assignedDoctor", assignedDoctor.name);
                System.out.println("Assigned Doctor: " + assignedDoctor.name + " (" + assignedDoctor.specialization + ")");
            } else {
                System.out.println("No specific doctor assigned based on injury.");
            }
            requestHistory.add(request);

            System.out.println("Ambulance dispatched at: " + dispatchTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME));
            simulateAmbulanceMovement(amb);

        } else {
            System.out.println("No suitable ambulances available at the nearest hospital.");
        }

        if (requestHistory.size() > 5)
            requestHistory.remove(0);
    }

    static void simulateAmbulanceMovement(Ambulance ambulance) {
        Random random = new Random();
        int travelSeconds = 5; // Simulate travel time

        System.out.println("Ambulance " + ambulance.ambulanceId + " leaves station.");
        ambulance.setStatus("EnRoute");
        System.out.println("Status: " + ambulance.getStatus());

        try {
            Thread.sleep(travelSeconds * 1000);
            System.out.println("Ambulance " + ambulance.ambulanceId + " arrives at scene.");
            ambulance.setStatus("AtScene");
            System.out.println("Status: " + ambulance.getStatus());

            Thread.sleep(travelSeconds * 1000);
            System.out.println("Ambulance " + ambulance.ambulanceId + " patient onboard.");
            ambulance.setStatus("TransportingPatient");
            System.out.println("Status: " + ambulance.getStatus());

            Thread.sleep(travelSeconds * 1000);
            LocalDateTime arrivalTime = LocalDateTime.now();
            ambulance.setArrivalTime(arrivalTime);
            ambulance.setStatus("AtHospital");
            System.out.println("Ambulance " + ambulance.ambulanceId + " arrives at hospital at: " + arrivalTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME));
            System.out.println("Status: " + ambulance.getStatus());
            Duration timeTaken = Duration.between(ambulance.getDispatchTime(), arrivalTime);
            System.out.println("Time taken: " + timeTaken.toMinutes() + " minutes and " + (timeTaken.getSeconds() % 60) + " seconds.");

            ambulance.setAvailable(true);
            ambulance.setDispatchTime(null);
            ambulance.setArrivalTime(null);
            ambulance.setStatus("Available");
            System.out.println("Ambulance " + ambulance.ambulanceId + " returns to base (simulated). Status: " + ambulance.getStatus());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static void manualAssignAmbulance(Map<String, Map<String, Integer>> cityHospitalGraph) {
        if (currentUserRole != UserRole.DISPATCHER && currentUserRole != UserRole.ADMIN) {
            System.out.println("Insufficient permissions for this action.");
            return;
        }
        System.out.println("\n=== Manual Ambulance Assignment ===");
        System.out.print("Enter patient name for whom to assign an ambulance: ");
        String patientName = sc.nextLine();
        System.out.print("Enter ambulance ID to assign: ");
        String ambulanceId = sc.nextLine();

        Ambulance ambulance = findAmbulanceById(ambulanceId);
        if (ambulance != null && ambulance.isAvailable()) {
            System.out.print("Enter patient location (e.g., Sector 17, Mohali Phase 8): ");
            String patientLocation = sc.nextLine();

            //Find Shortest Hospital
            String nearestHospitalName = null;
            int minDistance = Integer.MAX_VALUE;

            Map<String, Integer> distances = findShortestRoutes(patientLocation, cityHospitalGraph);
            for (Hospital h : hospitalTree.getSortedHospitals()) {
                if (distances.containsKey(h.getName()) && distances.get(h.getName()) < minDistance) {
                    minDistance = distances.get(h.getName());
                    nearestHospitalName = h.getName();
                }
            }
            Hospital nearestHospital = findHospitalByName(nearestHospitalName);

            System.out.print("Enter reason for manual assignment: ");
            String reason = sc.nextLine();
            ambulance.setAvailable(false);
            ambulance.setDispatchTime(LocalDateTime.now());
            ambulance.setStatus("Dispatched");
            System.out.println("Ambulance " + ambulanceId + " manually assigned to patient " + patientName + " at " + patientLocation + ".  Nearest Hospital: " + nearestHospitalName + " Distance: " + minDistance +" Reason: " + reason);

        } else {
            System.out.println("Ambulance not found or not available.");
        }
    }

    static Ambulance findAmbulanceById(String id) {
        for (Hospital h : hospitalTree.getSortedHospitals()) {
            for (Ambulance amb : h.getAmbulances()) {
                if (amb.ambulanceId.equals(id)) {
                    return amb;
                }
            }
        }
        return null;
    }

    static void updateAmbulanceStatus() {
        if (currentUserRole != UserRole.DISPATCHER && currentUserRole != UserRole.ADMIN && currentUserRole != UserRole.HOSPITAL_STAFF) {
            System.out.println("Insufficient permissions for this action.");
            return;
        }
        System.out.println("\n=== Update Ambulance Status ===");
        System.out.print("Enter ambulance ID to update: ");
        String ambulanceId = sc.nextLine();
        Ambulance ambulance = findAmbulanceById(ambulanceId);
        if (ambulance != null) {
            System.out.print("Enter new status (Available, EnRoute, AtScene, TransportingPatient, AtHospital, Returning): ");
            String newStatus = sc.nextLine();
            ambulance.setStatus(newStatus);
            ambulance.setAvailable(newStatus.equalsIgnoreCase("Available")); // Update availability based on status
            if (newStatus.equalsIgnoreCase("Dispatched") && ambulance.getDispatchTime() == null) {
                ambulance.setDispatchTime(LocalDateTime.now());
            } else if (newStatus.equalsIgnoreCase("AtHospital") && ambulance.getArrivalTime() == null) {
                ambulance.setArrivalTime(LocalDateTime.now());
            }
            System.out.println("Ambulance " + ambulanceId + " status updated to: " + newStatus);
        } else {
            System.out.println("Ambulance not found.");
        }
    }

    static void changeUserRole() {
        System.out.println("\n=== Change User Role ===");
        System.out.println("Available Roles:");
        for (UserRole role : UserRole.values()) {
            System.out.println(role.ordinal() + 1 + ". " + role);
        }
        System.out.print("Enter role number: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Consume newline

        if (choice > 0 && choice <= UserRole.values().length) {
            currentUserRole = UserRole.values()[choice - 1];
            System.out.println("User role changed to: " + currentUserRole);
        } else {
            System.out.println("Invalid role choice.");
        }
    }

    static void requestInterHospitalTransfer() {
        if (currentUserRole != UserRole.DISPATCHER && currentUserRole != UserRole.ADMIN && currentUserRole != UserRole.HOSPITAL_STAFF) {
            System.out.println("Insufficient permissions for this action.");
            return;
        }

        System.out.println("\n=== Request Inter-Hospital Transfer ===");
        System.out.print("Enter patient name: ");
        String patientName = sc.nextLine();
        System.out.print("Enter current hospital name: ");
        String currentHospitalName = sc.nextLine();
        System.out.print("Enter destination hospital name: ");
        String destinationHospitalName = sc.nextLine();
        System.out.print("Enter reason for transfer: ");
        String reason = sc.nextLine();

        Hospital currentHospital = findHospitalByName(currentHospitalName);
        Hospital destinationHospital = findHospitalByName(destinationHospitalName);

        if (currentHospital == null || destinationHospital == null) {
            System.out.println("Invalid hospital name(s).");
            return;
        }

        Ambulance transferAmbulance = currentHospital.getAvailableAmbulance();

        if (transferAmbulance == null) {
            System.out.println("No available ambulances at " + currentHospitalName + " for transfer.");
            return;
        }

        transferAmbulance.setAvailable(false);
        transferAmbulance.setStatus("Transferring"); // New status

        Map<String, Object> transferRequest = new HashMap<>();
        transferRequest.put("type", TRANSFER_REQUEST);
        transferRequest.put("time", LocalDateTime.now());
        transferRequest.put("patientName", patientName);
        transferRequest.put("fromHospital", currentHospitalName);
        transferRequest.put("toHospital", destinationHospitalName);
        transferRequest.put("reason", reason);
        transferRequest.put("ambulanceId", transferAmbulance.ambulanceId);
        transferRequest.put("status", transferAmbulance.getStatus());
        transferRequest.put("userRole", currentUserRole.toString());

        requestHistory.add(transferRequest);

        System.out.println("Ambulance " + transferAmbulance.ambulanceId + " dispatched for transfer from " +
                currentHospitalName + " to " + destinationHospitalName + ". Reason: " + reason);

        simulateTransfer(transferAmbulance, destinationHospital);
    }

    static void simulateTransfer(Ambulance ambulance, Hospital destination) {
        System.out.println("Simulating transfer to " + destination.getName() + "...");
        ambulance.setStatus("EnRoute");
        System.out.println("Status: " + ambulance.getStatus());

        try {
            Thread.sleep(5000); // Simulate travel time
            ambulance.setStatus("AtHospital"); // Assuming arrival at destination hospital
            System.out.println("Ambulance arrived at " + destination.getName() + ". Status: " + ambulance.getStatus());
            ambulance.setAvailable(true);
            ambulance.setStatus("Available"); // Back to available
            System.out.println("Ambulance " + ambulance.ambulanceId + " available again. Status: " + ambulance.getStatus());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Add this method to find a hospital by name
    static Hospital findHospitalByName(String name) {
        for (Hospital h : hospitalTree.getSortedHospitals()) {
            if (h.getName().equalsIgnoreCase(name)) {
                return h;
            }
        }
        return null;
    }

    static int getNearestIndex(List<Integer> distances) {
        int min = distances.get(0), index = 0;
        for (int i = 1; i < distances.size(); i++) {
            if (distances.get(i) < min) {
                min = distances.get(i);
                index = i;
            }
        }
        return index;
    }

    static int areaToId(String area) {
        Map<String, Integer> areaMap = new HashMap<>();
        areaMap.put("Chandigarh", 10);
        areaMap.put("Zirakpur", 20);
        areaMap.put("Panchkula", 30);
        areaMap.put("Mohali", 40);
        areaMap.put("Rajpura", 50);
        return areaMap.getOrDefault(area, 0);
    }














    // -------------------
    public static void main(String[] args) {
        Hospital h1 = new Hospital(10, "PGI Chandigarh");
        h1.addAmbulance("AMB001");
        h1.addAmbulance("AMB002");
        h1.addDoctor("Satyam Singh", "Neurosurgeon");
        h1.addDoctor("Priya Sharma", "Cardiologist");

        Hospital h2 = new Hospital(20, "Zirakpur Medicity");
        h2.addAmbulance("AMB201");
        h2.addDoctor("Rajesh Kumar", "General Physician");
        h2.addDoctor("Anjali Verma", "Pediatrician");

        Hospital h3 = new Hospital(30, "Panchkula General");
        h3.addAmbulance("AMB301");
        h3.addDoctor("Suresh Gupta", "Orthopedic Surgeon");

        Hospital h4 = new Hospital(40, "Max Mohali");
        h4.addAmbulance("AMB401");
        h4.addDoctor("Meera Patel", "Gynecologist");
        h4.addDoctor("Amit Verma", "General Surgeon");

        Hospital h5 = new Hospital(50, "Rajpura Care Hospital");
        h5.addAmbulance("AMB501");
        h5.addDoctor("Kiran Devi", "ENT Specialist");

        hospitalTree.insert(h1);
        hospitalTree.insert(h2);
        hospitalTree.insert(h3);
        hospitalTree.insert(h4);
        hospitalTree.insert(h5);

        Map<String, Map<String, Integer>> cityHospitalGraph = new HashMap<>();
        cityHospitalGraph.put("Sector 17", new HashMap<>());
        cityHospitalGraph.put("Sector 22", new HashMap<>());
        cityHospitalGraph.put("Sector 35", new HashMap<>());
        cityHospitalGraph.put("Sector 43", new HashMap<>());
        cityHospitalGraph.put("Mohali Phase 8", new HashMap<>());
        cityHospitalGraph.put("PGI Chandigarh", new HashMap<>());
        cityHospitalGraph.put("Max Mohali", new HashMap<>());
        cityHospitalGraph.put("Panchkula General", new HashMap<>());

        // Distances (in km)
        cityHospitalGraph.get("Sector 17").put("PGI Chandigarh", 5);
        cityHospitalGraph.get("Sector 17").put("Max Mohali", 12);
        cityHospitalGraph.get("Sector 22").put("PGI Chandigarh", 4);
        cityHospitalGraph.get("Sector 22").put("Sector 35", 2);
        cityHospitalGraph.get("Sector 35").put("Sector 22", 2);
        cityHospitalGraph.get("Sector 35").put("Sector 43", 4);
        cityHospitalGraph.get("Sector 35").put("Max Mohali", 10);
        cityHospitalGraph.get("Sector 43").put("Panchkula General", 8);
        cityHospitalGraph.get("Sector 43").put("Mohali Phase 8", 6);
        cityHospitalGraph.get("Mohali Phase 8").put("Max Mohali", 3);
        cityHospitalGraph.get("Mohali Phase 8").put("Panchkula General", 10);
        cityHospitalGraph.get("PGI Chandigarh").put("Sector 17", 5);
        cityHospitalGraph.get("PGI Chandigarh").put("Sector 22", 4);
        cityHospitalGraph.get("Max Mohali").put("Sector 17", 12);
        cityHospitalGraph.get("Max Mohali").put("Sector 35", 10);

        cityHospitalGraph.get("Panchkula General").put("Sector 43", 8);
        cityHospitalGraph.get("Panchkula General").put("Mohali Phase 8", 10);

        while (true) {
            System.out.println("\n=== Ambulance Tracker ===");
            System.out.println("Current User Role: " + currentUserRole);
            System.out.println("1. Request Ambulance");
            System.out.println("2. Show Recent Requests");
            System.out.println("3. Ambulance Availability");
            System.out.println("4. Manual Ambulance Assignment");
            System.out.println("5. Update Ambulance Status");
            System.out.println("6. Change User Role");
            System.out.println("7. Request Inter-Hospital Transfer");
            System.out.println("8. Exit");
            System.out.println("9. Show Hospital Distances from City Locations");
            System.out.print("Choice: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter patient name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter your area (Sector 17, Sector 22, Sector 35, Sector 43, Mohali Phase 8): ");
                    String area = sc.nextLine();
                    System.out.print("Enter the severity (1: Critical, 2: Urgent, 3: Non-Urgent): ");
                    int severity = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Briefly describe the emergency: ");
                    String description = sc.nextLine();
                    System.out.print("Enter the type of injury (e.g., Head, Cardiac, Orthopedic): ");
                    String injuryType = sc.nextLine();
                    Patient patient = new Patient(name, area, severity, description, injuryType);
                    findAmbulance(patient, cityHospitalGraph);
                    break;
                case 2:
                    System.out.println("Recent Requests:");
                    for (Map<String, Object> req : requestHistory) {
                        System.out.println("--------------------");
                        for (Map.Entry<String, Object> entry : req.entrySet()) {
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }
                    }
                    break;
                case 3:
                    System.out.println("\n=== Ambulance Availability ===");
                    for (Hospital h : hospitalTree.getSortedHospitals()) {
                        System.out.println(h.getName() + ":");
                        for (Ambulance amb : h.getAmbulances()) {
                            System.out.println("  " + amb.ambulanceId + " - Status: " + amb.getStatus() +
                                    (amb.getDispatchTime() != null ? " (Dispatched at: " + amb.getDispatchTime().format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME) + ")" : "") +
                                    (amb.getArrivalTime() != null ? " (Arrived at: " + amb.getArrivalTime().format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME) + ")" : ""));
                        }
                    }
                    break;
                case 4:
                    manualAssignAmbulance(cityHospitalGraph);
                    break;
                case 5:
                    updateAmbulanceStatus();
                    break;
                case 6:
                    changeUserRole();
                    break;
                case 7:
                    requestInterHospitalTransfer();
                    break;
                case 8:
                    System.out.println("Exiting...");
                    return;
                case 9:
                    System.out.println("\n=== Hospital Distances from City Locations ===");
                    String[] sourceLocations = { "Sector 17", "Sector 22", "Sector 35", "Sector 43", "Mohali Phase 8" };
                    for (String source : sourceLocations) {
                        System.out.println("\nShortest distances from " + source + ":");
                        Map<String, Integer> distances = findShortestRoutes(source, cityHospitalGraph);
                        // Print distances only for hospitals
                        for (Map.Entry<String, Integer> entry : distances.entrySet()) {
                            // Match hospital names exactly to those in hospitalTree
                            // Assuming hospital names end with "Hospital" or include "PGI Chandigarh" etc.
                            // So we'll print distances for hospitals present in hospitalTree
                            boolean isHospital = false;
                            for (Hospital h : hospitalTree.getSortedHospitals()) {
                                if (h.getName().equalsIgnoreCase(entry.getKey())) {
                                    isHospital = true;
                                    break;
                                }
                            }
                            if (isHospital && entry.getValue() != Integer.MAX_VALUE) {
                                System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " km");
                            }
                        }
                    }
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }
}

