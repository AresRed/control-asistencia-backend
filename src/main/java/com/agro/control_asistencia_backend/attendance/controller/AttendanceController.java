package com.agro.control_asistencia_backend.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceRequestDTO;
import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceResponseDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.service.AttendanceAdminService;
import com.agro.control_asistencia_backend.attendance.service.AttendanceService;
import com.agro.control_asistencia_backend.document.model.dto.EmployeeStatusDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

   
    private final AttendanceService attendanceService;
    private final AttendanceAdminService adminService;

    @Autowired
        public AttendanceController(AttendanceService attendanceService,AttendanceAdminService adminService) {
            this.attendanceService = attendanceService;
            this.adminService=adminService;
        }

    @PostMapping("/register")
    public ResponseEntity<AttendanceResponseDTO> registerAttendance(
            @Valid @RequestBody AttendanceRequestDTO requestDTO) {
        AttendanceResponseDTO record = attendanceService.processAttendanceRecord(requestDTO);
        return new ResponseEntity<>(record, HttpStatus.CREATED);

    }

    @GetMapping("/status/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<List<EmployeeStatusDTO>> getDailyStatus(@RequestParam LocalDate date) {
        List<EmployeeStatusDTO> statusList = adminService.getDailyAttendanceStatus(date);
        return ResponseEntity.ok(statusList);
    }

    @GetMapping("/user")
@PreAuthorize("hasRole('USER')")
public ResponseEntity<List<AttendanceRecord>> getUserAttendance(Authentication authentication) {
    // 💡 El nombre de usuario (código de empleado) se obtiene del token JWT
    String employeeCode = authentication.getName(); 
    List<AttendanceRecord> userAttendance = attendanceService.getAttendanceByEmployeeCode(employeeCode); 
    return ResponseEntity.ok(userAttendance);
}
}
