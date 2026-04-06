package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.model.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho CustomerDao, sử dụng JPA/Hibernate để thao tác với dữ liệu khách hàng.
 * Mỗi phương thức trong lớp này tự quản lý vòng đời của EntityManager (mở, sử dụng, và đóng),
 * tuân thủ theo mẫu Transaction Script, phù hợp với kiến trúc chung của dự án.
 */
public class CustomerDaoImpl implements CustomerDao {

    /**
     * Tìm kiếm khách hàng dựa trên từ khóa. Từ khóa sẽ được so khớp với mã, tên, số điện thoại, và email.
     * Kết quả được sắp xếp theo ngày cập nhật gần nhất để đưa những khách hàng mới tương tác lên đầu.
     */
    @Override
    public List<Customer> findCustomers(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // Xây dựng câu truy vấn JPQL một cách linh hoạt.
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
                // Gán giá trị cho tham số :kw, sử dụng ký tự đại diện '%' cho truy vấn LIKE.
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm khách hàng theo ID.
     */
    @Override
    public Optional<Customer> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Customer.class, id));
        } finally {
            em.close();
        }
    }

    /**
     * Tìm khách hàng theo mã khách hàng.
     */
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
     * Lưu (thêm mới hoặc cập nhật) một khách hàng. Thao tác được thực hiện trong một transaction.
     * Sử dụng `merge()` để xử lý cả trường hợp entity đã tồn tại (detached) hoặc chưa.
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
     * Xóa vĩnh viễn một khách hàng khỏi cơ sở dữ liệu.
     * Lưu ý: Nếu khách hàng này có các ràng buộc khóa ngoại (ví dụ: đã có đơn hàng),
     * cơ sở dữ liệu sẽ ném ra một ConstraintViolationException.
     * Lớp Service ở tầng trên có trách nhiệm bắt lỗi này và hiển thị thông báo thân thiện cho người dùng.
     */
    @Override
    public void deleteById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // Sử dụng getReference() để lấy một tham chiếu đến đối tượng mà không cần tải nó từ DB,
            // giúp tối ưu hóa hiệu năng cho thao tác xóa.
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
