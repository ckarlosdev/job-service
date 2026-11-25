package com.hmbrandt.job_management_service.entity.schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "employee_absences")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeAbsence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_absences_id", columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(name = "employees_id", nullable = false)
    private Integer employeesId;

    @Column(name = "absence_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "absence_type",
            nullable = false,
            columnDefinition = "ENUM('Off','Vacation','Sick','Other')"
    )
    private AbsenceType absenceType;

    @Column(name = "absence_comment", columnDefinition = "TEXT")
    private String absenceComment;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
