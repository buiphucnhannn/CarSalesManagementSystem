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

public class CarDaoImpl implements CarDao {

    @Override
    public List<Car> findCars(String keyword, Status statusFilter) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select c from Car c " +
                            "join fetch c.brand b " +
                            "join fetch c.category cat " +
                            "join fetch c.branch br where 1=1");

            if (keyword != null && !keyword.isBlank()) {
                jpql.append(" and (lower(c.carCode) like :keyword or lower(c.carName) like :keyword)");
            }
            if (statusFilter != null) {
                jpql.append(" and c.status = :status");
            }
            jpql.append(" order by c.updatedAt desc, c.id desc");

            TypedQuery<Car> query = entityManager.createQuery(jpql.toString(), Car.class);
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

    @Override
    public Brand saveBrand(Brand brand) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            Brand merged = entityManager.merge(brand);
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

    @Override
    public Optional<CarCategory> findCategoryById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(CarCategory.class, id));
        } finally {
            entityManager.close();
        }
    }

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

    @Override
    public Optional<Branch> findBranchById(Long id) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(Branch.class, id));
        } finally {
            entityManager.close();
        }
    }

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

    @Override
    public Car save(Car car) {
        EntityManager entityManager = JpaUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            Car merged = entityManager.merge(car);
            entityManager.flush();
            // Ensure relationships are initialized before EntityManager closes.
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

