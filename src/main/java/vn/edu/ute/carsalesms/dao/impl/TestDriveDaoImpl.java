package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.carsalesms.dao.TestDriveDao;
import vn.edu.ute.carsalesms.model.entity.TestDrive;
import vn.edu.ute.carsalesms.config.JpaUtil;

import java.util.List;
import java.util.Optional;

/**
 * Lớp triển khai cho TestDriveDao, sử dụng JPA để quản lý dữ liệu các lịch hẹn lái thử.
 */
public class TestDriveDaoImpl implements TestDriveDao {

    /**
     * Lấy tất cả các lịch hẹn lái thử.
     */
    @Override
    public List<TestDrive> findAll() {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            // Sử dụng JOIN FETCH để tải trước các thông tin liên quan (Khách hàng, Xe, Nhân viên, Chi nhánh)
            // nhằm tránh vấn đề N+1 query, giúp tối ưu hiệu năng.
            return em.createQuery(
                "SELECT td FROM TestDrive td JOIN FETCH td.customer JOIN FETCH td.car JOIN FETCH td.staff s JOIN FETCH s.branch ORDER BY td.scheduledTime DESC", 
                TestDrive.class).getResultList();
        }
    }

    /**
     * Tìm kiếm lịch hẹn lái thử theo từ khóa.
     */
    @Override
    public List<TestDrive> findByKeyword(String keyword) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return findAll(); // Nếu không có từ khóa, trả về tất cả.
            }
            // Tìm kiếm theo mã lái thử, tên khách hàng, hoặc tên xe.
            String jpql = "SELECT td FROM TestDrive td JOIN FETCH td.customer c JOIN FETCH td.car car JOIN FETCH td.staff s JOIN FETCH s.branch " +
                          "WHERE LOWER(td.testDriveCode) LIKE LOWER(:kw) OR LOWER(c.fullName) LIKE LOWER(:kw) " +
                          "OR LOWER(car.carName) LIKE LOWER(:kw) ORDER BY td.scheduledTime DESC";
            TypedQuery<TestDrive> query = em.createQuery(jpql, TestDrive.class);
            query.setParameter("kw", "%" + keyword.trim() + "%");
            return query.getResultList();
        }
    }

    /**
     * Tìm một lịch hẹn lái thử theo ID.
     */
    @Override
    public Optional<TestDrive> findById(Long id) {
        try (EntityManager em = JpaUtil.getEntityManager()) {
            TestDrive td = em.createQuery(
                            "SELECT td FROM TestDrive td " +
                                    "JOIN FETCH td.customer " +
                                    "JOIN FETCH td.car " +
                                    "JOIN FETCH td.staff s " +
                                    "JOIN FETCH s.branch " +
                                    "WHERE td.id = :id", TestDrive.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(td);
        }
    }

    /**
     * Lưu một lịch hẹn lái thử mới.
     */
    @Override
    public TestDrive save(TestDrive testDrive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(testDrive); // Dùng persist cho việc tạo mới.
            em.getTransaction().commit();
            return testDrive;
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
     * Cập nhật một lịch hẹn lái thử đã có.
     */
    @Override
    public TestDrive update(TestDrive testDrive) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TestDrive updated = em.merge(testDrive); // Dùng merge cho việc cập nhật.
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
