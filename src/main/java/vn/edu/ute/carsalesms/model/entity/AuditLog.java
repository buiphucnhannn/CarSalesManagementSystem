package vn.edu.ute.carsalesms.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng audit_logs.
 * Lưu lịch sử thao tác của nhân viên trên hệ thống.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Quan hệ nhiều - một:
     * Nhiều bản ghi log có thể do cùng một nhân viên thực hiện.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

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