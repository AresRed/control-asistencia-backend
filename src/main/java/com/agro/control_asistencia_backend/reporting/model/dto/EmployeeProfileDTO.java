package com.agro.control_asistencia_backend.reporting.model.dto;

import java.math.BigDecimal;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class EmployeeProfileDTO {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String position;
    private String username;
    
    // --- Datos de Nómina y Horas Extra (CRÍTICO) ---
    private BigDecimal fixedSalary;
    private Long totalOvertimeMinutes;
    private String totalOvertimeDuration;
    
    // Constructor de mapeo para poblar el DTO fácilmente
    public EmployeeProfileDTO(Employee employee, EmployeeHourSummaryDTO summary) {
        // Mapeo de la Entidad Employee
        this.id = employee.getId();
        this.employeeCode = employee.getEmployeeCode();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.position = employee.getPosition();
        this.fixedSalary = employee.getFixedSalary();
        this.username = employee.getUser().getUsername(); // Asume que la relación está cargada

        // Mapeo del Resumen de Horas Extra
        this.totalOvertimeMinutes = summary.getTotalOvertimeMinutes();
        this.totalOvertimeDuration = summary.getTotalOvertimeDuration();
    }
}
