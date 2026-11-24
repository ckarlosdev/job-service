package com.hmbrandt.job_management_service.dto.schedule;

import java.util.List;

public record CreateAbsenceResponse(
        AbsenceResponse absence,
        List<UpdatedScheduleResponse> updatedSchedules
) {
}
