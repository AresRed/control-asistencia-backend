package com.agro.control_asistencia_backend.scheduling.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.scheduling.model.dto.EmployeeScheduleAssignmentDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.ScheduleResponseDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.WorkScheduleDTO;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.repository.EmployeeScheduleRepository;
import com.agro.control_asistencia_backend.scheduling.repository.WorkScheduleRepository;

import jakarta.transaction.Transactional;

@Service
public class ScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ScheduleService(WorkScheduleRepository workScheduleRepository, 
                           EmployeeScheduleRepository employeeScheduleRepository, 
                           EmployeeRepository employeeRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.employeeRepository = employeeRepository;
    }

    // ---------------------------------------------------------------------
    // Lógica para crear turnos (WorkSchedule)
    // ---------------------------------------------------------------------

    @Transactional
    public WorkSchedule createWorkSchedule(WorkScheduleDTO dto) {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setName(dto.getName());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setToleranceMinutes(dto.getToleranceMinutes());
        
        return workScheduleRepository.save(schedule);
    }

    // ---------------------------------------------------------------------
    // Lógica para asignar turnos a empleados (EmployeeSchedule)
    // ---------------------------------------------------------------------
    
    @Transactional
    public ScheduleResponseDTO assignScheduleToEmployee(EmployeeScheduleAssignmentDTO dto) {
        
        // 1. Validar existencia del Empleado
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + dto.getEmployeeId()));

        // 2. Validar existencia del Turno
        WorkSchedule schedule = workScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Turno de trabajo no encontrado con ID: " + dto.getScheduleId()));
        
        // 3. Crear Asignación
        EmployeeSchedule assignment = new EmployeeSchedule();
        assignment.setEmployee(employee);
        assignment.setWorkSchedule(schedule);
        assignment.setValidFrom(dto.getValidFrom());
        assignment.setValidTo(dto.getValidTo());
        assignment.setWorkingDays(dto.getWorkingDays());
        
        EmployeeSchedule savedAssignment = employeeScheduleRepository.save(assignment);

        // NOTA: En un sistema completo, aquí iría la lógica para verificar
        // que no se solapen con otras asignaciones activas para el mismo empleado.

        return ScheduleResponseDTO.builder()
            .assignmentId(savedAssignment.getId())
            .employeeId(employee.getId())
            .employeeCode(employee.getEmployeeCode())
            .scheduleName(schedule.getName())
            .startTime(schedule.getStartTime())
            .endTime(schedule.getEndTime())
            .validFrom(savedAssignment.getValidFrom())
            .validTo(savedAssignment.getValidTo())
            .workingDays(savedAssignment.getWorkingDays())
            .build();
    }
}
