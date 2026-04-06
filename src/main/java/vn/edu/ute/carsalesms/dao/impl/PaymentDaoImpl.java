package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.PaymentDao;
import vn.edu.ute.carsalesms.model.entity.Payment;
import vn.edu.ute.carsalesms.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho PaymentDao, sử dụng JPA/Hibernate để thao tác với dữ liệu thanh toán.
 */
public class PaymentDaoImpl implements PaymentDao {

    /**
     * Tìm tất cả các thanh toán của một đơn hàng, sắp xếp theo ngày thanh toán gần nhất.
     */
    @Override
    public List<Payment> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select p from Payment p " +
                            "join fetch p.saleOrder o " + // Tải sẵn thông tin đơn hàng
                            "where o.id = :orderId " +
                            "order by p.paymentDate desc", Payment.class)
                    .setParameter("orderId", orderId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tính tổng số tiền đã thanh toán thành công (COMPLETED) cho một đơn hàng.
     * Sử dụng hàm tổng hợp `sum()` của JPQL và `coalesce()` để trả về 0 nếu không có kết quả.
     * @return Trả về tổng số tiền dưới dạng BigDecimal. Trả về BigDecimal.ZERO nếu chưa có thanh toán nào.
     */
    @Override
    public BigDecimal sumCompletedByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            BigDecimal result = em.createQuery(
                            "select coalesce(sum(p.amount), 0) " +
                            "from Payment p " +
                            "where p.saleOrder.id = :orderId " +
                            "and p.paymentStatus = :status", BigDecimal.class)
                    .setParameter("orderId", orderId)
                    .setParameter("status", PaymentStatus.COMPLETED)
                    .getSingleResult();
            // Đảm bảo không bao giờ trả về null.
            return result == null ? BigDecimal.ZERO : result;
        } finally {
            em.close();
        }
    }

    /**
     * Tìm một thanh toán theo ID của nó.
     */
    @Override
    public Optional<Payment> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Payment p = em.createQuery(
                            "select p from Payment p " +
                            "join fetch p.saleOrder o " + // Tải sẵn thông tin đơn hàng
                            "where p.id = :id", Payment.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(p);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu một bản ghi thanh toán vào cơ sở dữ liệu trong một transaction.
     */
    @Override
    public Payment save(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Payment merged = em.merge(payment);
            em.flush();
            // Chủ động khởi tạo proxy SaleOrder để tránh lỗi sau khi EntityManager đóng.
            merged.getSaleOrder().getOrderCode();
            em.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
