package com.hmbrandt.job_management_service.entity.schedule;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_schedule_employees",
        indexes = {
                @Index(name = "idx_jse_employee", columnList = "employee_id")
        }
)
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobScheduleEmployee {
    @EmbeddedId
    private JobScheduleEmployeeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobScheduleId")
    @JoinColumn(
            name = "job_schedule_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_jse_schedule")
    )
    private JobSchedule jobSchedule;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
