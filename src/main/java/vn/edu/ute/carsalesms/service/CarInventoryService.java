package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.CarCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CarManagementItem;
import vn.edu.ute.carsalesms.model.dto.CarManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Nhom use-case lien quan truc tiep den xe ton kho.
 */
public interface CarInventoryService {

    List<CarManagementItem> getCars(String keyword, Status statusFilter);

    CarManagementMetadata getMetadata();

    CarManagementItem createCar(CarCommandRequest request);

    CarManagementItem updateCar(CarCommandRequest request);

    void deactivateCar(Long carId);
}

