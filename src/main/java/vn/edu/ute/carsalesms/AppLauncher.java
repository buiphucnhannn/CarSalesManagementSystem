package vn.edu.ute.carsalesms;

import jakarta.persistence.EntityManager;
import vn.edu.ute.carsalesms.config.JpaUtil;

/**
 * Lớp khởi chạy ứng dụng.
 */
public class AppLauncher {

    public static void main(String[] args) {
        EntityManager entityManager = null;

        try {
            entityManager = JpaUtil.getEntityManager();
            System.out.println("Kết nối JPA thành công.");
        } catch (Exception e) {
            System.out.println("Kết nối JPA thất bại: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
            JpaUtil.close();
        }
    }
}