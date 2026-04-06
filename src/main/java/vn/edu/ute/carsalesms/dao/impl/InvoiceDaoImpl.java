package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.InvoiceDao;
import vn.edu.ute.carsalesms.model.entity.Invoice;

import java.util.Optional;
import java.util.List;

/**
 * Lớp triển khai cho InvoiceDao, sử dụng JPA/Hibernate để thao tác với dữ liệu hóa đơn.
 */
public class InvoiceDaoImpl implements InvoiceDao {

    /**
     * Tìm hóa đơn dựa trên ID của đơn hàng.
     */
    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Invoice inv = em.createQuery(
                            "select i from Invoice i " +
                            "join fetch i.saleOrder o " + // Tải sẵn thông tin đơn hàng
                            "where o.id = :orderId", Invoice.class)
                    .setParameter("orderId", orderId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(inv);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu một hóa đơn mới vào cơ sở dữ liệu, được bao bọc trong một transaction.
     */
    @Override
    public Invoice save(Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Invoice merged = em.merge(invoice);
            em.flush();
            // Chủ động khởi tạo proxy để tránh lỗi sau khi EntityManager đóng
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

    /**
     * Tìm kiếm tất cả các hóa đơn, hỗ trợ tìm kiếm theo từ khóa trên nhiều trường.
     */
    @Override
    public List<Invoice> findAll(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Câu truy vấn join nhiều bảng để có thể tìm kiếm và hiển thị thông tin đầy đủ.
            String qlString = "SELECT i FROM Invoice i JOIN FETCH i.saleOrder o JOIN FETCH o.customer c " +
                              "JOIN FETCH o.staff st JOIN FETCH st.branch b " +
                              "WHERE :kw IS NULL OR LOWER(i.invoiceCode) LIKE :kw " + // Tìm theo mã hóa đơn
                              "OR LOWER(o.orderCode) LIKE :kw OR LOWER(c.fullName) LIKE :kw " + // Tìm theo mã đơn hàng hoặc tên khách
                              "ORDER BY i.issuedDate DESC"; // Sắp xếp theo ngày phát hành gần nhất
            
            // Chuẩn bị từ khóa tìm kiếm
            String searchKw = (keyword != null && !keyword.trim().isEmpty()) ? "%" + keyword.trim().toLowerCase() + "%" : null;
            
            return em.createQuery(qlString, Invoice.class)
                    .setParameter("kw", searchKw)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm một hóa đơn theo ID và tải tất cả các chi tiết liên quan một cách đầy đủ.
     * Phương thức này rất quan trọng cho các chức năng cần hiển thị toàn bộ thông tin của hóa đơn, ví dụ như xuất PDF.
     */
    @Override
    public Optional<Invoice> findByIdWithOrderDetails(Long invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Sử dụng 'distinct' và nhiều 'join fetch'/'left join fetch' để tải một đồ thị đối tượng phức tạp.
            Invoice invoice = em.createQuery(
                            "select distinct i from Invoice i " +
                            "join fetch i.saleOrder so " +
                            "join fetch so.customer c " +
                            "join fetch so.staff st " +
                            "join fetch st.branch b " +
                            "left join fetch so.saleOrderDetails sod " + // Left join vì một đơn hàng có thể không có chi tiết nào
                            "left join fetch sod.car car " +
                            "left join fetch car.brand br " +
                            "left join fetch car.category cg " +
                            "where i.id = :id", Invoice.class)
                    .setParameter("id", invoiceId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(invoice);
        } finally {
            em.close();
        }
    }
}
