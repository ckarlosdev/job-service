package com.hmbrandt.job_management_service.entity.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JobScheduleEmployeeId implements Serializable{
    @Column(name = "job_schedule_id", columnDefinition = "INT UNSIGNED")
    private Long jobScheduleId;

    @Column(name = "employee_id")
    private Integer employeeId;
}
