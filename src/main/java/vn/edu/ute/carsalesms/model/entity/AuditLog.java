package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Lớp Entity, đại diện cho bảng `audit_logs` trong cơ sở dữ liệu.
 * Bảng này có vai trò như một cuốn nhật ký, ghi lại lịch sử các thao tác quan trọng
 * của nhân viên trên hệ thống, phục vụ cho việc kiểm tra và giám sát.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * Múi giờ Việt Nam, dùng để đảm bảo thời gian được ghi lại một cách nhất quán.
     */
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Khóa chính của bảng, tự động tăng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mối quan hệ Nhiều-Một với thực thể Staff.
     * Nhiều bản ghi nhật ký có thể được thực hiện bởi cùng một nhân viên.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    /**
     * Tên hành động được thực hiện (ví dụ: "CREATE", "UPDATE", "LOGIN_FAILED").
     */
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * Tên của thực thể bị tác động (ví dụ: "CAR", "CUSTOMER", "SALE_ORDER").
     */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /**
     * ID của bản ghi cụ thể trong bảng của thực thể bị tác động.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Giá trị cũ của dữ liệu trước khi thay đổi (thường ở dạng chuỗi JSON hoặc mô tả).
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * Giá trị mới của dữ liệu sau khi thay đổi.
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Thời điểm hành động được thực hiện.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors, Getters, and Setters...
    public AuditLog() {
    }

    public AuditLog(Staff staff, String action, String entityName, Long entityId,
                    String oldValue, String newValue) {
        this.staff = staff;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() {
        return id;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Hàm callback của JPA, được gọi tự động trước khi một entity được lưu lần đầu.
     * Dùng để gán giá trị thời gian hiện tại (theo múi giờ Việt Nam) cho `createdAt`.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(VIETNAM_ZONE);
        }
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", action='" + action + '\'' +
                ", entityName='" + entityName + '\'' +
                ", entityId=" + entityId +
                ", createdAt=" + createdAt +
                '}';
    }
}
