package vn.edu.ute.carsalesms.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Lớp tiện ích để khởi tạo và quản lý EntityManagerFactory.
 * Hỗ trợ tạo EntityManager để thao tác với cơ sở dữ liệu thông qua JPA.
 * Tuân theo mẫu singleton để đảm bảo rằng chỉ có một EntityManagerFactory được tạo cho toàn bộ ứng dụng.
 */
public class JpaUtil {

    /**
     * Tên persistence-unit phải trùng với tên khai báo trong persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "car-sales-ms-pu";

    /**
     * EntityManagerFactory được tạo một lần và tái sử dụng trong toàn bộ ứng dụng.
     */
    private static final EntityManagerFactory entityManagerFactory = buildEntityManagerFactory();

    /**
     * Khởi tạo EntityManagerFactory từ file persistence.xml.
     *
     * @return EntityManagerFactory đã được khởi tạo
     */
    private static EntityManagerFactory buildEntityManagerFactory() {
        try {
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo EntityManagerFactory: " + e.getMessage());
            throw new RuntimeException("Không thể khởi tạo EntityManagerFactory.", e);
        }
    }

    /**
     * Tạo mới một EntityManager để thao tác với cơ sở dữ liệu.
     *
     * @return EntityManager mới
     */
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    /**
     * Đóng EntityManagerFactory khi ứng dụng kết thúc.
     */
    public static void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    /**
     * Constructor private để ngăn khởi tạo đối tượng từ bên ngoài.
     */
    private JpaUtil() {
    }
}
