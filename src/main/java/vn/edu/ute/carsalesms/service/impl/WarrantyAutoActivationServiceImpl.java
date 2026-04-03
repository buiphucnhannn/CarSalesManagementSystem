package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.WarrantyDao;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.entity.Warranty;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.WarrantyAutoActivationService;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class WarrantyAutoActivationServiceImpl implements WarrantyAutoActivationService {

	private final WarrantyDao warrantyDao;

	public WarrantyAutoActivationServiceImpl(WarrantyDao warrantyDao) {
		this.warrantyDao = Objects.requireNonNull(warrantyDao, "warrantyDao is required");
	}

	@Override
	public void activateForPaidOrder(SaleOrder order) {
		try {
			for (SaleOrderDetail sod : order.getSaleOrderDetails()) {
				if (warrantyDao.findBySaleOrderDetailId(sod.getId()).isEmpty()) {
					Warranty w = new Warranty();
					w.setWarrantyCode("WR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
					w.setSaleOrderDetail(sod);
					w.setStartDate(LocalDate.now());
					w.setEndDate(LocalDate.now().plusYears(3));
					w.setWarrantyStatus(WarrantyStatus.ACTIVE);
					w.setNote("Kích hoạt Bảo Hành Tự Động do Đơn thanh toán Xong (PAID).");
					warrantyDao.save(w);
				}
			}
		} catch (Exception ex) {
			System.err.println("Lỗi Auto-Generate Warranty: " + ex.getMessage());
		}
	}
}

