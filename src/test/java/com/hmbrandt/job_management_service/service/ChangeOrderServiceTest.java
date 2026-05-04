package com.hmbrandt.job_management_service.service;

import com.hmbrandt.job_management_service.dto.ChangeOrderResponseDTO;
import com.hmbrandt.job_management_service.dto.ChangeOrderUpdateDTO;
import com.hmbrandt.job_management_service.entity.ChangeOrder;
import com.hmbrandt.job_management_service.exception.ResourceNotFoundException;
import com.hmbrandt.job_management_service.repository.ChangeOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeOrderServiceTest {
    @Mock
    private ChangeOrderRepository repository;

    @InjectMocks
    private ChangeOrderServiceImpl service;

    private ChangeOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new ChangeOrder();
        sampleOrder.setId(1L);
        sampleOrder.setJobId(1010L);
        sampleOrder.setEmployeeId(42L);
        sampleOrder.setOrderDate(LocalDate.of(2026, 4, 29));
        sampleOrder.setOrderNumber(1010);
        sampleOrder.setContractDate(LocalDate.of(2026, 4, 29));
        sampleOrder.setChangeDescription("test description");
        sampleOrder.setAmount(new BigDecimal("315.6"));
        sampleOrder.setCreatedBy("test user");
        sampleOrder.setCreatedAt(LocalDateTime.of(2026, 4, 29, 14, 52, 24));
    }

    // --- SAVE TEST ---
    @Test
    @DisplayName("Debe guardar una orden exitosamente")
    void save_ShouldReturnDto_WhenSuccessful() {
        when(repository.save(any(ChangeOrder.class))).thenReturn(sampleOrder);

        ChangeOrderResponseDTO result = service.save(new ChangeOrder());

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(ChangeOrder.class));
    }

    // --- TESTS DE UPDATE ---

    @Test
    @DisplayName("Debe actualizar la orden cuando existe")
    void update_ShouldReturnUpdatedDto_WhenOrderExists() {
        ChangeOrderUpdateDTO updateDto = new ChangeOrderUpdateDTO(
                LocalDate.now(),
                200,
                "Updated Desc",
                new BigDecimal("500.6"),
                "Admin",
                LocalDateTime.now()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(repository.save(any(ChangeOrder.class))).thenAnswer(i -> i.getArguments()[0]);

        ChangeOrderResponseDTO result = service.update(1L, updateDto);

        assertThat(result.orderNumber()).isEqualTo(200);
        assertThat(result.changeDescription()).isEqualTo("Updated Desc");
        verify(repository).save(any(ChangeOrder.class));
    }

    @Test
    @DisplayName("Update debe lanzar ResourceNotFoundException cuando no existe")
    void update_ShouldThrowException_WhenOrderDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, mock(ChangeOrderUpdateDTO.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Debe retornar DTO cuando el ID existe")
    void findById_ShouldReturnDto_WhenIdExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        ChangeOrderResponseDTO result = service.findById(1L);

        assertThat(result.orderNumber()).isEqualTo(1010);
    }

    @Test
    @DisplayName("Debe lanzar EntityNotFoundException cuando el ID no existe")
    void findById_ShouldThrowException_WhenIdDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Order not fount with id: 1");
    }

    // --- TESTS DE DELETE ---

    @Test
    @DisplayName("Debe eliminar cuando el ID existe")
    void delete_ShouldCallDelete_WhenIdExists() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete debe lanzar excepción cuando el ID no existe")
    void delete_ShouldThrowException_WhenIdDoesNotExist() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- TESTS DE FIND ALL ---

    @Test
    void shouldReturnChangeOrders() {
        when(repository.findAll()).thenReturn(List.of(new ChangeOrder()));

        List<ChangeOrderResponseDTO> result = service.findAll();

        assertEquals(1, result.size());
    }

}
