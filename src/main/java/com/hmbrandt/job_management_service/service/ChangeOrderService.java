package com.hmbrandt.job_management_service.service;

import com.hmbrandt.job_management_service.dto.ChangeOrderResponseDTO;
import com.hmbrandt.job_management_service.dto.ChangeOrderUpdateDTO;
import com.hmbrandt.job_management_service.dto.create.ChangeOrderCreateDto;
import com.hmbrandt.job_management_service.entity.ChangeOrder;

import java.util.List;

public interface ChangeOrderService {
    ChangeOrderResponseDTO save(ChangeOrderCreateDto changeOrder);

    ChangeOrderResponseDTO update(Long id, ChangeOrderUpdateDTO dto);

    ChangeOrderResponseDTO finalizeOrder(Long id);

    ChangeOrderResponseDTO approveOrder(Long id);

    List<ChangeOrderResponseDTO> findAll();

    ChangeOrderResponseDTO findById(Long id);

    List<ChangeOrderResponseDTO> findByJobId(Long jobId);

    void delete(Long id);
}
