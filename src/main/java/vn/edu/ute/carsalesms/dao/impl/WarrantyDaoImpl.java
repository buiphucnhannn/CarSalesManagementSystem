package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.dao.WarrantyDao;
import vn.edu.ute.carsalesms.model.entity.Warranty;
import vn.edu.ute.carsalesms.config.JpaUtil;

import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho WarrantyDao, sử dụng JPA để quản lý dữ liệu bảo hành.
 */
public class WarrantyDaoImpl implements WarrantyDao {

    /**
     * Lấy tất cả các phiếu bảo hành, tải kèm các thông tin liên quan để hiển thị.
     */
    @Override
    public List<Warranty> findAll() {
        // Sử dụng try-with-resources để đảm bảo EntityManager được đóng tự động.
        try (EntityManager em = JpaUtil.getEntityManager()) {
            // Câu truy vấn phức tạp với nhiều JOIN FETCH để tải một đồ thị đối tượng lớn.
            return em.createQuery(
                "SELECT w FROM Warranty w JOIN FETCH w.saleOrderDetail sod JOIN FETCH sod.saleOrder o JOIN FETCH o.customer c JOIN FETCH o.staff st JOIN FETCH st.branch b JOIN FETCH sod.car car ORDER BY w.endDate DESC", 
                Warranty.class).getResultList();
        }
    }

    /**
     * Tìm kiếm phiếu bảo hành theo từ khóa.
     */
    @Override
    public List<Warranty> findByKeyword(String keyword) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            // Nếu không có từ khóa, trả về tất cả.
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll();
            }
            // Tìm kiếm trên nhiều trường liên quan như mã bảo hành, tên khách, tên xe, mã đơn hàng.
            String jpql = "SELECT w FROM Warranty w JOIN FETCH w.saleOrderDetail sod JOIN FETCH sod.saleOrder o JOIN FETCH o.customer c JOIN FETCH o.staff st JOIN FETCH st.branch b JOIN FETCH sod.car car " +
                          "WHERE LOWER(w.warrantyCode) LIKE LOWER(:kw) " +
                          "OR LOWER(c.fullName) LIKE LOWER(:kw) " +
                          "OR LOWER(car.carName) LIKE LOWER(:kw) " +
                          "OR LOWER(o.orderCode) LIKE LOWER(:kw) " +
                          "ORDER BY w.endDate DESC";
            TypedQuery<Warranty> query = em.createQuery(jpql, Warranty.class);
            query.setParameter("kw", "%" + keyword.trim() + "%");
            return query.getResultList();
        }
    }

    /**
     * Tìm một phiếu bảo hành theo ID, tải kèm đầy đủ thông tin chi tiết.
     */
    @Override
    public Optional<Warranty> findById(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            Warranty w = em.createQuery(
                            "SELECT w FROM Warranty w " +
                                    "JOIN FETCH w.saleOrderDetail sod " +
                                    "JOIN FETCH sod.saleOrder o " +
                                    "JOIN FETCH o.staff st " +
                                    "JOIN FETCH st.branch b " +
                                    "JOIN FETCH sod.car car " +
                                    "WHERE w.id = :id", Warranty.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(w);
        }
    }

    /**
     * Tìm phiếu bảo hành dựa trên ID của chi tiết đơn hàng.
     */
    @Override
    public Optional<Warranty> findBySaleOrderDetailId(Long detailId) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TypedQuery<Warranty> query = em.createQuery(
                "SELECT w FROM Warranty w WHERE w.saleOrderDetail.id = :sodId", Warranty.class);
            query.setParameter("sodId", detailId);
            // getSingleResult() sẽ ném NoResultException nếu không tìm thấy, cần bắt lại.
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty(); // Trả về Optional rỗng nếu không tìm thấy.
        }
    }

    /**
     * Lưu một phiếu bảo hành mới.
     */
    @Override
    public Warranty save(Warranty warranty) {
         EntityManager em = JpaUtil.getEntityManager();
         try {
             em.getTransaction().begin();
             // persist() dùng cho việc tạo mới một entity.
             em.persist(warranty);
             em.getTransaction().commit();
             return warranty;
         } catch (Exception e) {
             if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
             }
             throw e;
         } finally {
             em.close();
         }
    }

    /**
     * Cập nhật một phiếu bảo hành đã có.
     */
    @Override
    public Warranty update(Warranty warranty) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // merge() dùng để cập nhật một entity đã tồn tại.
            Warranty updated = em.merge(warranty);
            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
