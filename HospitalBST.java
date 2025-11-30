import java.util.ArrayList;
import java.util.List;

public class HospitalBST {
    static class Node {
        Hospital hospital;
        Node left, right;

        public Node(Hospital hospital) {
            this.hospital = hospital;
        }
    }

    Node root;

    public void insert(Hospital hospital) {
        root = insertRecursive(root, hospital);
    }

    private Node insertRecursive(Node root, Hospital hospital) {
        if (root == null) {
            return new Node(hospital);
        }

        if (hospital.locationId < root.hospital.locationId) {
            root.left = insertRecursive(root.left, hospital);
        } else if (hospital.locationId > root.hospital.locationId) {
            root.right = insertRecursive(root.right, hospital);
        }
        return root;
    }

    public List<Hospital> getSortedHospitals() {
        List<Hospital> hospitals = new ArrayList<>();
        inorderTraversal(root, hospitals);
        return hospitals;
    }

    private void inorderTraversal(Node root, List<Hospital> hospitals) {
        if (root != null) {
            inorderTraversal(root.left, hospitals);
            hospitals.add(root.hospital);
            inorderTraversal(root.right, hospitals);
        }
    }

    public Hospital search(int id) {
        return searchRecursive(root, id);
    }

    private Hospital searchRecursive(Node root, int id) {
        if (root == null || root.hospital.locationId == id) {
            return root == null ? null : root.hospital;
        }

        if (id < root.hospital.locationId) {
            return searchRecursive(root.left, id);
        }
        return searchRecursive(root.right, id);
    }
}