package com.hmbrandt.job_management_service.controller;


import com.hmbrandt.job_management_service.dto.schedule.*;
import com.hmbrandt.job_management_service.service.JobScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v2/job-management/schedules")
@RequiredArgsConstructor
public class JobScheduleController {
    private final JobScheduleService scheduleService;

    // Arrastrar trabajo del catálogo a un día
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request
    ) {
        ScheduleResponse created = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Mover tarjeta a otro día ({date, start_time}) o cambiar solo la hora ({start_time})
    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleResponse> patchSchedule(
            @PathVariable Long id,
            @RequestBody PatchScheduleRequest request
    ) {
        return ResponseEntity.ok(scheduleService.patchSchedule(id, request));
    }

    // Quitar tarjeta (soltar sobre el catálogo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    // Asignar o quitar empleados (enviando el conjunto completo)
    @PutMapping("/{id}/employees")
    public ResponseEntity<ScheduleResponse> updateEmployees(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeesRequest request
    ) {
        return ResponseEntity.ok(
                scheduleService.updateEmployees(id, request.employeeIds())
        );
    }

    // Opcional recomendado: Mover un empleado entre tarjetas atómicamente
    @PostMapping("/move-employee")
    public ResponseEntity<List<ScheduleResponse>> moveEmployee(
            @Valid @RequestBody MoveEmployeeRequest request
    ) {
        return ResponseEntity.ok(scheduleService.moveEmployeeBetweenSchedules(request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(scheduleService.getSchedulesByDateRange(from, to));
    }

}
