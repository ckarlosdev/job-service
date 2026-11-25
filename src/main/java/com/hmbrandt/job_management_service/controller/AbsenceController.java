package com.hmbrandt.job_management_service.controller;

import com.hmbrandt.job_management_service.dto.schedule.AbsenceResponse;
import com.hmbrandt.job_management_service.dto.schedule.CreateAbsenceRequest;
import com.hmbrandt.job_management_service.dto.schedule.CreateAbsenceResponse;
import com.hmbrandt.job_management_service.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/job-management/absences")
@RequiredArgsConstructor
public class AbsenceController {
    private final AbsenceService absenceService;

    @GetMapping
    public ResponseEntity<List<AbsenceResponse>> getAbsences(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        return ResponseEntity.ok(absenceService.getAbsences(from, to));
    }

    @PostMapping
    public ResponseEntity<CreateAbsenceResponse> createAbsence(
            @RequestBody CreateAbsenceRequest request) {
        CreateAbsenceResponse created = absenceService.createAbsence(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAbsence(@PathVariable Long id) {
        absenceService.deleteAbsence(id);
        return ResponseEntity.noContent().build();
    }
}
