package vn.edu.ute.carsalesms.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import vn.edu.ute.carsalesms.config.JpaUtil;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.model.entity.Brand;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.CarCategory;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Lớp triển khai cho CarDao, sử dụng JPA (Java Persistence API) để tương tác với cơ sở dữ liệu.
 * Lớp này chịu trách nhiệm thực thi các truy vấn liên quan đến xe, thương hiệu, và danh mục xe.
 */
public class CarDaoImpl implements CarDao {

    /**
     * Triển khai phương thức tìm kiếm xe.
     * Xây dựng câu lệnh truy vấn JPQL động dựa trên các tham số đầu vào.
     */
    @Override
    public List<Car> findCars(String keyword, Status statusFilter) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            // Sử dụng StringBuilder để xây dựng câu truy vấn một cách linh hoạt.
            StringBuilder jpql = new StringBuilder(
                    "select c from Car c " +
                            // "join fetch" để tải ngay lập tức các thực thể liên quan, tránh vấn đề N+1 query.
                            "join fetch c.brand b " +
                            "join fetch c.category cat " +
                            "join fetch c.branch br where 1=1");

            // Thêm điều kiện tìm kiếm theo từ khóa nếu có.
            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(c.carCode) like :keyword or lower(c.carName) like :keyword)");
            }
            // Thêm điều kiện lọc theo trạng thái nếu có.
            if (statusFilter != null) {
                jpql.append(" and c.status = :status");
            }
            // Sắp xếp kết quả để đảm bảo thứ tự nhất quán.
            jpql.append(" order by c.updatedAt desc, c.id desc");

            TypedQuery<Car> query = entityManager.createQuery(jpql.toString(), Car.class);
            // Gán giá trị cho các tham số trong câu truy vấn.
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (statusFilter != null) {
                query.setParameter("status", statusFilter);
            }
            return query.getResultList();
        } finally {
            // Luôn đóng EntityManager sau khi sử dụng để giải phóng tài nguyên.
            entityManager.close();
        }
    }

    /**
     * Tìm xe theo ID và tải kèm các thông tin liên quan.
     */
    @Override
    public Optional<Car> findById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            Car car = entityManager.createQuery(
                            "select c from Car c " +
                                    "join fetch c.brand " +
                                    "join fetch c.category " +
                                    "join fetch c.branch " +
                                    "where c.id = :id", Car.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(car);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm xe theo mã xe, không phân biệt chữ hoa chữ thường.
     */
    @Override
    public Optional<Car> findByCode(String carCode) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            Car car = entityManager.createQuery(
                            "select c from Car c where lower(c.carCode) = :carCode", Car.class)
                    .setParameter("carCode", carCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(car);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm kiếm danh sách thương hiệu với các điều kiện lọc.
     */
    @Override
    public List<Brand> findBrands(String keyword, Status statusFilter) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select b from Brand b where 1=1");
            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(b.brandCode) like :keyword or lower(b.brandName) like :keyword)");
            }
            if (statusFilter != null) {
                jpql.append(" and b.status = :status");
            }
            jpql.append(" order by b.updatedAt desc, b.id desc");

            TypedQuery<Brand> query = entityManager.createQuery(jpql.toString(), Brand.class);
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (statusFilter != null) {
                query.setParameter("status", statusFilter);
            }
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm thương hiệu theo mã, không phân biệt chữ hoa chữ thường.
     */
    @Override
    public Optional<Brand> findBrandByCode(String brandCode) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            Brand brand = entityManager.createQuery(
                            "select b from Brand b where lower(b.brandCode) = :brandCode", Brand.class)
                    .setParameter("brandCode", brandCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(brand);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm thương hiệu theo ID. Sử dụng entityManager.find() cho hiệu quả khi tìm theo khóa chính.
     */
    @Override
    public Optional<Brand> findBrandById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            Brand brand = entityManager.find(Brand.class, id);
            return Optional.ofNullable(brand);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một thương hiệu.
     * Sử dụng transaction để đảm bảo tính toàn vẹn dữ liệu.
     */
    @Override
    public Brand saveBrand(Brand brand) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            // merge() sẽ tạo mới nếu entity chưa có trong persistence context, hoặc cập nhật nếu đã có.
            Brand merged = entityManager.merge(brand);
            entityManager.flush(); // Đẩy các thay đổi vào DB
            entityManager.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            // Nếu có lỗi, rollback transaction để tránh dữ liệu không nhất quán.
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex; // Ném lại exception để lớp gọi xử lý.
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm kiếm danh sách danh mục xe với các điều kiện lọc.
     */
    @Override
    public List<CarCategory> findCategories(String keyword, Status statusFilter) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select c from CarCategory c where 1=1");
            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(c.categoryCode) like :keyword or lower(c.categoryName) like :keyword)");
            }
            if (statusFilter != null) {
                jpql.append(" and c.status = :status");
            }
            jpql.append(" order by c.updatedAt desc, c.id desc");

            TypedQuery<CarCategory> query = entityManager.createQuery(jpql.toString(), CarCategory.class);
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("keyword", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (statusFilter != null) {
                query.setParameter("status", statusFilter);
            }
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm danh mục theo mã, không phân biệt chữ hoa chữ thường.
     */
    @Override
    public Optional<CarCategory> findCategoryByCode(String categoryCode) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            CarCategory category = entityManager.createQuery(
                            "select c from CarCategory c where lower(c.categoryCode) = :categoryCode", CarCategory.class)
                    .setParameter("categoryCode", categoryCode.trim().toLowerCase())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(category);
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm danh mục theo ID.
     */
    @Override
    public Optional<CarCategory> findCategoryById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(CarCategory.class, id));
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một danh mục xe.
     */
    @Override
    public CarCategory saveCategory(CarCategory category) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            CarCategory merged = entityManager.merge(category);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Tìm chi nhánh theo ID.
     */
    @Override
    public Optional<Branch> findBranchById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(Branch.class, id));
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lấy danh sách các thương hiệu đang hoạt động, sắp xếp theo tên.
     */
    @Override
    public List<Brand> findActiveBrands() {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select b from Brand b where b.status = :status order by b.brandName", Brand.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lấy danh sách các danh mục đang hoạt động, sắp xếp theo tên.
     */
    @Override
    public List<CarCategory> findActiveCategories() {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select c from CarCategory c where c.status = :status order by c.categoryName", CarCategory.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lấy danh sách các chi nhánh đang hoạt động, sắp xếp theo tên.
     */
    @Override
    public List<Branch> findActiveBranches() {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return entityManager.createQuery(
                            "select b from Branch b where b.status = :status order by b.branchName", Branch.class)
                    .setParameter("status", Status.ACTIVE)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Vô hiệu hóa hàng loạt các xe thuộc một thương hiệu.
     * Đây là một "bulk update", hiệu quả hơn việc tải từng xe và cập nhật.
     */
    @Override
    public int deactivateCarsByBrandId(Long brandId) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            int updated = entityManager.createQuery(
                            "update Car c set c.status = :inactive where c.brand.id = :brandId and c.status <> :inactive")
                    .setParameter("inactive", Status.INACTIVE)
                    .setParameter("brandId", brandId)
                    .executeUpdate();
            entityManager.getTransaction().commit();
            return updated;
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Vô hiệu hóa hàng loạt các xe thuộc một danh mục.
     */
    @Override
    public int deactivateCarsByCategoryId(Long categoryId) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            int updated = entityManager.createQuery(
                            "update Car c set c.status = :inactive where c.category.id = :categoryId and c.status <> :inactive")
                    .setParameter("inactive", Status.INACTIVE)
                    .setParameter("categoryId", categoryId)
                    .executeUpdate();
            entityManager.getTransaction().commit();
            return updated;
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }

    /**
     * Lưu (thêm mới hoặc cập nhật) một xe.
     */
    @Override
    public Car save(Car car) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            Car merged = entityManager.merge(car);
            entityManager.flush();
            // Chủ động tải các thuộc tính lazy-loaded để chúng không bị lỗi khi EntityManager đóng.
            merged.getBrand().getBrandName();
            merged.getCategory().getCategoryName();
            merged.getBranch().getBranchName();
            entityManager.getTransaction().commit();
            return merged;
        } catch (Exception ex) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw ex;
        } finally {
            entityManager.close();
        }
    }
}
