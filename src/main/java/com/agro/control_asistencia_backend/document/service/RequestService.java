package com.agro.control_asistencia_backend.document.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agro.control_asistencia_backend.document.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.document.model.dto.RequestResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.document.model.entity.RequestType;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.repository.RequestTypeRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;

import jakarta.transaction.Transactional;

@Service
public class RequestService {

    private final EmployeeRequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final EmployeeRepository employeeRepository;

    // Asumiremos que tu entidad User tiene una relación OneToOne con Employee
    // o que Employee tiene un campo userId. Usaremos findByUserId.

    @Autowired
    public RequestService(EmployeeRequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            EmployeeRepository employeeRepository) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Crea una nueva solicitud de empleado.
     * 
     * @param requestDTO Datos de la solicitud.
     * @param userId     ID del usuario autenticado (extraído del Token JWT).
     * @return La solicitud creada.
     */
    @Transactional
    public RequestResponseDTO createRequest(EmployeeRequestDTO requestDTO, Long userId) {

        // 1. Encontrar el Empleado a partir del ID de Usuario (CRÍTICO)
        // **Asegúrate de que tu EmployeeRepository tenga este método: findByUserId(Long
        // userId)**
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Error: Empleado no encontrado para el usuario autenticado.");
        }
        Employee employee = employeeOpt.get();

        // 2. Encontrar el Tipo de Solicitud
        Optional<RequestType> typeOpt = requestTypeRepository.findById(requestDTO.getRequestTypeId());

        if (typeOpt.isEmpty()) {
            throw new RuntimeException(
                    "Error: Tipo de solicitud con ID " + requestDTO.getRequestTypeId() + " no encontrado.");
        }
        RequestType requestType = typeOpt.get();

        // 3. Mapear y Guardar la Solicitud
        EmployeeRequest newRequest = new EmployeeRequest();
        newRequest.setEmployee(employee);
        newRequest.setRequestType(requestType);
        newRequest.setDetails(requestDTO.getDetails());
        newRequest.setStartDate(requestDTO.getStartDate());
        newRequest.setEndDate(requestDTO.getEndDate());
        // El estado por defecto es "PENDING" (definido en la entidad)
        EmployeeRequest savedRequest = requestRepository.save(newRequest); // Guardar

        return RequestResponseDTO.builder()
                .id(savedRequest.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .requestType(requestType.getName())
                .details(savedRequest.getDetails())
                .requestedDate(savedRequest.getRequestedDate())
                .startDate(savedRequest.getStartDate())
                .endDate(savedRequest.getEndDate())
                .status(savedRequest.getStatus())
                .build();

    }

    @Transactional
    public RequestResponseDTO updateRequestStatus(Long requestId, String status, String comment) {

        EmployeeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + requestId));

        // Validar el estado entrante
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new RuntimeException("Estado inválido. Use 'APPROVED' o 'REJECTED'.");
        }

        request.setStatus(status);
        request.setManagerComment(comment);

        EmployeeRequest savedRequest = requestRepository.save(request);

        // NOTA: Aquí iría la lógica para notificar al empleado sobre el cambio de
        // estado.

        // Mapear la entidad guardada al DTO de Respuesta
        return RequestResponseDTO.builder()
        .id(savedRequest.getId())
        .employeeId(savedRequest.getEmployee().getId())
        .employeeName(savedRequest.getEmployee().getFirstName() + " " + savedRequest.getEmployee().getLastName())
        .requestType(savedRequest.getRequestType().getName())
        
        // ¡AGREGAR ESTOS CAMPOS FALTANTES!
        .details(savedRequest.getDetails())
        .requestedDate(savedRequest.getRequestedDate())
        .startDate(savedRequest.getStartDate())
        .endDate(savedRequest.getEndDate())
        
        .status(savedRequest.getStatus())
        .build(); // Usaremos un método de mapeo auxiliar
    }

    // Método auxiliar (debe ser implementado en tu servicio)
    private RequestResponseDTO mapToRequestResponseDTO(EmployeeRequest request) {
        // ... Lógica de mapeo a RequestResponseDTO (igual que en createRequest) ...
        // Necesario para devolver JSON limpio y evitar errores de proxy.
        return RequestResponseDTO.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName())
                .requestType(request.getRequestType().getName())
                .status(request.getStatus())
                // ... otros campos
                .build();
    }

}
