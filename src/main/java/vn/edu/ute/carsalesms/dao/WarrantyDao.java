package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Warranty;

import java.util.List;
import java.util.Optional;

public interface WarrantyDao {
    List<Warranty> findAll();
    /**
     * Tìm Thẻ bảo hành qua Mã phiếu, Biển kiểm soát/Số khung xe hoặc tên khách
     */
    List<Warranty> findByKeyword(String keyword);
    Optional<Warranty> findById(Long id);
    Optional<Warranty> findBySaleOrderDetailId(Long detailId);
    Warranty save(Warranty warranty);
    Warranty update(Warranty warranty);
}
