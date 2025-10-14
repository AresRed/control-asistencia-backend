package com.agro.control_asistencia_backend.document.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.document.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.document.model.dto.RequestResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.document.service.RequestService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/requests")

public class RequestController {

    private final RequestService requestService;
    private final EmployeeRequestRepository requestRepository;

    @Autowired
    public RequestController(RequestService requestService, EmployeeRequestRepository requestRepository) {
        this.requestService = requestService;
        this.requestRepository = requestRepository;
    }

    /**
     * Endpoint para que un empleado cree una nueva solicitud.
     * Solo requiere que el usuario esté autenticado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequestResponseDTO> createEmployeeRequest(
            @Valid @RequestBody EmployeeRequestDTO requestDTO,
            // Captura el ID del usuario autenticado directamente del token
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();

        RequestResponseDTO createdRequest = requestService.createRequest(requestDTO, userId);

        return new ResponseEntity<RequestResponseDTO>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<List<EmployeeRequest>> getAllRequests() {
        // NOTA: Implementar findByStatus("PENDING") en el servicio y mapear a DTO List.
        return ResponseEntity.ok(requestRepository.findAll());
    }


    @Data
    @NoArgsConstructor 
    static class StatusUpdateDTO {
        private String status;
        private String comment;
    }

    // Endpoint 2: Aprobar o Rechazar una solicitud
    @PutMapping("/{requestId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<RequestResponseDTO> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestBody StatusUpdateDTO statusDTO) {

        RequestResponseDTO updatedRequest = requestService.updateRequestStatus(
                requestId, statusDTO.getStatus(), statusDTO.getComment());

        return ResponseEntity.ok(updatedRequest);
    }
}
