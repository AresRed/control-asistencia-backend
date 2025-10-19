package com.agro.control_asistencia_backend.scheduling.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.scheduling.model.dto.EmployeeScheduleAssignmentDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.ScheduleResponseDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.WorkScheduleDTO;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.service.ScheduleService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedules")
@PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Endpoint: POST /api/schedules/turn (Crea una plantilla de turno)
     */
    @PostMapping("/turn")
    public ResponseEntity<WorkSchedule> createWorkSchedule(@Valid @RequestBody WorkScheduleDTO dto) {
        WorkSchedule schedule = scheduleService.createWorkSchedule(dto);
        return new ResponseEntity<>(schedule, HttpStatus.CREATED);
    }

    /**
     * Endpoint: POST /api/schedules/assign (Asigna un turno a un empleado)
     */
    @PostMapping("/assign")
    public ResponseEntity<ScheduleResponseDTO> assignSchedule(@Valid @RequestBody EmployeeScheduleAssignmentDTO dto) {
        ScheduleResponseDTO  assignment = scheduleService.assignScheduleToEmployee(dto);
        return  ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @GetMapping("/me")
@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN') or hasRole('RRHH')")
public ResponseEntity<EmployeeSchedule> getMySchedule(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    
    // NOTA: Asumiremos que el servicio puede obtener el EmployeeSchedule activo
    Optional<EmployeeSchedule> scheduleOpt = scheduleService.getEmployeeScheduleByUserId(userDetails.getId(), LocalDate.now());

    if (scheduleOpt.isEmpty()) {
        // Devuelve un 404 si no hay horario activo asignado
        return ResponseEntity.notFound().build(); 
    }
    
    // NOTA CRÍTICA: Devolvemos la Entidad aquí, lo cual puede dar ByteBuddyInterceptor.
    // Lo ideal es devolver un DTO simple para evitar errores de serialización.
    return ResponseEntity.ok(scheduleOpt.get());
}
}
