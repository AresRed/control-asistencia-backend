package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class RequestResponseDTO {

    private Long id;
    private Long employeeId; // ID del empleado
    private String employeeName; // Nombre para mostrar en el frontend
    private String requestType; // Nombre del tipo de solicitud
    private String details;
    private LocalDateTime requestedDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // PENDING, APPROVED, REJECTED
}
