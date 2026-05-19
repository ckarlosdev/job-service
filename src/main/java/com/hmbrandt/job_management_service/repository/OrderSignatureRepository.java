package com.hmbrandt.job_management_service.repository;

import com.hmbrandt.job_management_service.entity.OrderSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSignatureRepository extends JpaRepository<OrderSignature, Long> {
}
