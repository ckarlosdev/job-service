package com.hmbrandt.job_management_service.service;

import com.hmbrandt.job_management_service.dto.ChangeOrderResponseDTO;
import com.hmbrandt.job_management_service.dto.ChangeOrderUpdateDTO;
import com.hmbrandt.job_management_service.entity.ChangeOrder;

import java.util.List;

public interface ChangeOrderService {
    ChangeOrderResponseDTO save(ChangeOrder changeOrder);

    ChangeOrderResponseDTO update(Long id, ChangeOrderUpdateDTO dto);

    List<ChangeOrderResponseDTO> findAll();

    ChangeOrderResponseDTO findById(Long id);

    void delete(Long id);
}
