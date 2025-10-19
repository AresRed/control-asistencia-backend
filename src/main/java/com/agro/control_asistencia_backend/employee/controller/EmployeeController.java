package com.agro.control_asistencia_backend.employee.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeResponseDTO;
import com.agro.control_asistencia_backend.employee.model.dto.ManagerContactDTO;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.service.EmployeeService;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeHourSummaryDTO;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeProfileDTO;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    // 1. Inyección de servicios FINAL
    private final EmployeeService employeeService;
    private final ReportingService reportingService; // <--- ¡CRÍTICO: Inyección de Reportes!

    @Autowired
    public EmployeeController(EmployeeService employeeService, ReportingService reportingService) {
        this.employeeService = employeeService;
        this.reportingService = reportingService;
    }

    // -------------------------------------------------------------------------
    // ENDPOINTS DE CREACIÓN Y LISTADO GENERAL (ADMIN / RRHH)
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    // ⚠️ Mejor práctica: Devolver el DTO de respuesta, no la Entidad JPA
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeDTO) {
        // Asumimos que employeeService.createEmployee devuelve EmployeeResponseDTO
        EmployeeResponseDTO newEmployee = employeeService.createEmployee(employeeDTO); 
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    // ⚠️ Mejor práctica: Devolver la lista de DTOs
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        // Asumimos que employeeService.getAllEmployees ya devuelve List<EmployeeResponseDTO>
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees(); 
        return ResponseEntity.ok(employees);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE PERFIL DEL EMPLEADO (MÓDULO DE NÓMINA Y DATOS PERSONALES)
    // Se elimina el método duplicado y se mantiene el DTO
    // -------------------------------------------------------------------------

    @GetMapping("/me") // ÚNICA RUTA /me en este controlador
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<EmployeeProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);

        Employee employee = employeeService.getEmployeeByUserId(userId);

        // Llamada al ReportingService para obtener las Horas Extra
        EmployeeHourSummaryDTO summary = reportingService.getEmployeeHourSummary(employee.getId(), startDate, endDate);

        // Combina los datos de Employee y el Resumen en el DTO final
        EmployeeProfileDTO profileDTO = new EmployeeProfileDTO(employee, summary); 

        return ResponseEntity.ok(profileDTO);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE ACTUALIZACIÓN (PUT)
    // -------------------------------------------------------------------------
    
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileDTO> updateMyProfile( // ⚠️ Devolver el DTO actualizado
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody EmployeeRequestDTO profileUpdateDTO) { 

        EmployeeProfileDTO updatedEmployee = employeeService.updateProfile(userDetails.getId(), profileUpdateDTO);

        return ResponseEntity.ok(updatedEmployee);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE LISTADO DE MANAGERS (PARA SOLICITUDES)
    // -------------------------------------------------------------------------

    @GetMapping("/managers")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<List<ManagerContactDTO>> getManagers(@RequestParam List<String> roles) { 

        String[] roleArray = roles.toArray(new String[0]);

        List<ManagerContactDTO> managers = employeeService.getManagersByRoles(roleArray);
        return ResponseEntity.ok(managers);
    }
}