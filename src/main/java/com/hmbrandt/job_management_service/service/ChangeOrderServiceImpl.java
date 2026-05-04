package com.hmbrandt.job_management_service.service;


import com.hmbrandt.job_management_service.dto.ChangeOrderResponseDTO;
import com.hmbrandt.job_management_service.dto.ChangeOrderUpdateDTO;
import com.hmbrandt.job_management_service.entity.ChangeOrder;
import com.hmbrandt.job_management_service.exception.ResourceNotFoundException;
import com.hmbrandt.job_management_service.repository.ChangeOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeOrderServiceImpl implements ChangeOrderService {

    private final ChangeOrderRepository repository;

    @Override
    @Transactional
    public ChangeOrderResponseDTO save(ChangeOrder changeOrder) {
        ChangeOrder newChangeOrder = repository.save(changeOrder);
        return mapToDto(newChangeOrder);
    }

    @Override
    @Transactional
    public ChangeOrderResponseDTO update(Long id, ChangeOrderUpdateDTO dto) {
        // 1. Buscar la entidad existente o lanzar error si no existe
        ChangeOrder order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("order not found with id: " + id));

        order.setOrderDate(dto.orderDate());
        order.setOrderNumber(dto.orderNumber());
        order.setChangeDescription(dto.changeDescription());
        order.setAmount(dto.amount());
        order.setUpdatedBy(dto.updatedBy());
        order.setUpdatedAt(dto.updatedAt());

        ChangeOrder savedOrder = repository.save(order);

        return mapToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeOrderResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto) // Uso de Method Reference para limpiar el código
                .orElseThrow(() -> new EntityNotFoundException("Order not fount with id: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("ID not found.");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChangeOrderResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    private ChangeOrderResponseDTO mapToDto(ChangeOrder entity) {
        return new ChangeOrderResponseDTO(
                entity.getId(),
                entity.getJobId(),
                entity.getEmployeeId(),
                entity.getOrderDate(),
                entity.getOrderNumber(),
                entity.getContractDate(),
                entity.getChangeDescription(),
                entity.getAmount(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }

}
