package vn.edu.ute.carsalesms.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;
import vn.edu.ute.carsalesms.service.PaymentRecordFactory;

public class PaymentRecordFactoryImpl implements PaymentRecordFactory {

	@Override
	public Payment create(SaleOrder order, PaymentRequest request) {
		Payment pm = new Payment();
		pm.setPaymentCode(generateCode());
		pm.setSaleOrder(order);
		pm.setPaymentDate(LocalDateTime.now());
		pm.setAmount(request.amount());
		pm.setPaymentMethod(request.paymentMethod());
		pm.setPaymentStatus(PaymentStatus.COMPLETED);
		pm.setTransactionReference(request.transactionReference());
		pm.setNote(request.note());
		return pm;
	}

	private String generateCode() {
		return "PM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}
}

