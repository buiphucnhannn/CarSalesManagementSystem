package vn.edu.ute.carsalesms.model.dto;

public record BranchManagementMetadata(
        String nextBranchCode
) {
    public static BranchManagementMetadata empty() {
        return new BranchManagementMetadata("BR-HCM-01");
    }
}

