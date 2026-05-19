package com.hmbrandt.job_management_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="order_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrderTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_task_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_order_id")
    private ChangeOrder changeOrder;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "foreman")
    private Integer foreman;

    @Column(name = "labor")
    private Integer labor;

    @Column(name = "other")
    private Integer other;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "equipment_comments", columnDefinition = "TEXT")
    private String equipmentComments;

    @Column(name = "tool_comments", columnDefinition = "TEXT")
    private String toolComments;

    @Column(name = "dumpster_comments", columnDefinition = "TEXT")
    private String dumpsterComments;

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

    @OneToMany(mappedBy = "orderTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskEquipment> equipments = new ArrayList<>();

    @OneToMany(mappedBy = "orderTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTool> tools = new ArrayList<>();

    @OneToMany(mappedBy = "orderTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskDumpster> dumpsters = new ArrayList<>();
}
