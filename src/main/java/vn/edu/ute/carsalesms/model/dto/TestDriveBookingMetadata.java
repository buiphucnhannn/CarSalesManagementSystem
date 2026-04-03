package vn.edu.ute.carsalesms.model.dto;

import java.util.List;

/**
 * Du lieu lookup dung cho dialog dat lich lai thu.
 */
public record TestDriveBookingMetadata(
        List<TestDriveLookupOption> customers,
        List<TestDriveLookupOption> cars,
        List<TestDriveLookupOption> staffs
) {
}

