package com.agro.control_asistencia_backend.scheduling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
