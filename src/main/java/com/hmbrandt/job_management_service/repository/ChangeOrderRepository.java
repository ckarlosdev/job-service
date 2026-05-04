package com.hmbrandt.job_management_service.repository;

import com.hmbrandt.job_management_service.entity.ChangeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeOrderRepository extends JpaRepository<ChangeOrder, Long> {
    List<ChangeOrder> findByJobId(Long jobId);

    List<ChangeOrder> findByEmployeeId(Long employeeId);
}
