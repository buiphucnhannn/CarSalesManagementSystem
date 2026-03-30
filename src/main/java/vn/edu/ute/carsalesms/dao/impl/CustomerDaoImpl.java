package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.model.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Triển khai CustomerDao dùng JPA/Hibernate.
 * Mỗi phương thức tự quản lý vòng đời EntityManager (open → thao tác → close).
 * Tuân thủ pattern Transaction Script nhất quán với toàn bộ dự án.
 */
public class CustomerDaoImpl implements CustomerDao {

    /**
     * Tìm khách hàng theo từ khóa (mã, tên, số điện thoại, email).
     * Kết quả sắp xếp theo updated_at DESC.
     */
    @Override
    public List<Customer> findCustomers(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Xây JPQL động dựa trên keyword
            StringBuilder jpql = new StringBuilder(
                    "select c from Customer c where 1=1");

            if (keyword != null && !keyword.isBlank()) {
                jpql.append(
                        " and (lower(c.customerCode) like :kw" +
                        " or lower(c.fullName) like :kw" +
                        " or lower(c.phone) like :kw" +
                        " or lower(c.email) like :kw)");
            }
            jpql.append(" order by c.updatedAt desc, c.id desc");

            TypedQuery<Customer> query = em.createQuery(jpql.toString(), Customer.class);
            if (keyword != null && !keyword.isBlank()) {
                // Tham số LIKE: %keyword%
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Customer.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Customer> findByCode(String customerCode) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Customer result = em.createQuery(
                            "select c from Customer c where lower(c.customerCode) = :code",
                            Customer.class)
                    .setParameter("code", customerCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(result);
        } finally {
            em.close();
        }
    }

    /**
     * Lưu hoặc cập nhật khách hàng trong một transaction.
     * Dùng merge() để xử lý cả trường hợp entity detached.
     */
    @Override
    public Customer save(Customer customer) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Customer merged = em.merge(customer);
            em.flush();
            em.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa cứng khách hàng.
     * Nếu khách hàng đang có đơn bán, CSDL sẽ ném ConstraintViolationException.
     * Service sẽ bắt lỗi này và hiển thị thông báo thân thiện cho người dùng.
     */
    @Override
    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // Dùng getReference để tránh query không cần thiết
            Customer ref = em.getReference(Customer.class, id);
            em.remove(ref);
            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
