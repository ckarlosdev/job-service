package com.hmbrandt.job_management_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "change_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE change_orders SET deleted_at = NOW() WHERE change_order_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChangeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_order_id")
    private Long id;

    @Column(name = "jobs_id", nullable = false)
    private Long jobId;

    @Column(name = "employees_id", nullable = false)
    private Long employeeId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "order_status", nullable = false)
    private String orderStatus;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "changeOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderSignature> signatures = new ArrayList<>();

    @OneToMany(mappedBy = "changeOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderTask> tasks = new ArrayList<>();
}
