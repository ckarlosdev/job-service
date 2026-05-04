package com.hmbrandt.job_management_service.controller;

import com.hmbrandt.job_management_service.dto.ChangeOrderResponseDTO;
import com.hmbrandt.job_management_service.dto.ChangeOrderUpdateDTO;
import com.hmbrandt.job_management_service.entity.ChangeOrder;
import com.hmbrandt.job_management_service.service.ChangeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/job-management/change-order")
@RequiredArgsConstructor
@Tag(name = "Change Orders", description = "Change Order Management for Projects")
public class ChangeOrderController {

    private final ChangeOrderService service;

    @PostMapping
    public ResponseEntity<ChangeOrderResponseDTO> create(@RequestBody ChangeOrder order) {
        return new ResponseEntity<>(service.save(order), HttpStatus.CREATED);
    }

    @Operation(summary = "Update change order", description = "Finds an order by ID and updates its fields with the data from the DTO.")
    @PutMapping("/{id}")
    public ResponseEntity<ChangeOrderResponseDTO> udpate(
            @PathVariable Long id,
            @RequestBody ChangeOrderUpdateDTO updateDto
    ){
        ChangeOrderResponseDTO updatedOrder = service.update(id, updateDto);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChangeOrderResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChangeOrderResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
