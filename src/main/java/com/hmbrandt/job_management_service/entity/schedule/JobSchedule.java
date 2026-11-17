package com.hmbrandt.job_management_service.entity.schedule;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "job_schedules",
        indexes = {
                @Index(name = "idx_job_schedules_date", columnList = "scheduled_date, start_time")
        }
)
@SQLDelete(sql = "UPDATE job_schedules SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED")
    private Long id;

    // ID externo (probablemente de otro microservicio de Jobs)
    @Column(name = "job_id", nullable = false)
    private Integer jobId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "schedule_comment", columnDefinition = "TEXT")
    private String scheduleComment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Mapea directamente la tabla job_schedule_employees como una colección de IDs
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_schedule_employees",
            joinColumns = @JoinColumn(
                    name = "job_schedule_id",
                    foreignKey = @ForeignKey(name = "fk_jse_schedule")
            ),
            indexes = {
                    @Index(name = "idx_jse_employee", columnList = "employee_id")
            }
    )
    @Column(name = "employee_id", nullable = false)
    @Builder.Default
    private Set<Integer> employeeIds = new HashSet<>();
}
