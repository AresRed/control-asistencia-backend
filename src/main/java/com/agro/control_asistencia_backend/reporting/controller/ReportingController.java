package com.agro.control_asistencia_backend.reporting.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;

    @Autowired
    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    // Endpoint: GET
    // /api/reports/employee/{employeeId}?start=2024-01-01&end=2024-01-31
    // Permite a ADMIN y RRHH solicitar el reporte de un empleado.
        
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<List<DailyWorkSummary>> getEmployeeReport(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<DailyWorkSummary> summary = reportingService.getEmployeeWorkSummary(employeeId, start, end);
        return ResponseEntity.ok(summary);
    }
}
