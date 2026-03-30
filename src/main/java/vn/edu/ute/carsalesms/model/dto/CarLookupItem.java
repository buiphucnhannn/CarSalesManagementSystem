package vn.edu.ute.carsalesms.model.dto;

public record CarLookupItem(
        Long id,
        String code,
        String name
) {
    @Override
    public String toString() {
        return code + " - " + name;
    }
}

