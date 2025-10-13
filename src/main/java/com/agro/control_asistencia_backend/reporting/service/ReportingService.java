package com.agro.control_asistencia_backend.reporting.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;

@Service
public class ReportingService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ReportingService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Calcula el resumen de horas trabajadas para un empleado en un rango de fechas.
     */
    public List<DailyWorkSummary> getEmployeeWorkSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found for reporting."));
        
        // 1. Obtener todos los registros en el rango de fechas
        List<AttendanceRecord> records = attendanceRepository
                .findAllByEmployeeAndDeviceTimestampBetweenOrderByDeviceTimestampAsc(
                        employee,
                        startDate.atStartOfDay(),
                        endDate.atTime(LocalTime.MAX)
                );

        // 2. Agrupar registros por fecha
        Map<LocalDate, List<AttendanceRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getDeviceTimestamp().toLocalDate()
                ));

        // 3. Procesar cada día y calcular la duración
        return recordsByDate.entrySet().stream()
                .map(entry -> calculateDailySummary(employee, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyWorkSummary::getDate))
                .collect(Collectors.toList());
    }

    /**
     * Lógica para calcular la duración total de la jornada para un día específico.
     */
    private DailyWorkSummary calculateDailySummary(Employee employee, LocalDate date, List<AttendanceRecord> dailyRecords) {
        
        long totalMinutes = 0;
        
        // Aseguramos que los registros estén ordenados por tiempo
        dailyRecords.sort(Comparator.comparing(AttendanceRecord::getDeviceTimestamp));
        
        LocalDateTime checkIn = null;
        LocalDateTime checkOut = null;
        
        // Iterar para encontrar pares IN/OUT
        for (AttendanceRecord record : dailyRecords) {
            if ("IN".equals(record.getRecordType())) {
                checkIn = record.getDeviceTimestamp();
            } else if ("OUT".equals(record.getRecordType()) && checkIn != null) {
                // Par completo encontrado (IN seguido de OUT)
                checkOut = record.getDeviceTimestamp();
                totalMinutes += ChronoUnit.MINUTES.between(checkIn, checkOut);
                
                // Reiniciar para el siguiente ciclo (si hay pausas o más entradas/salidas)
                checkIn = null; 
            }
        }
        
        // La jornada está completa si el último registro fue OUT y hubo un par (o si el último IN fue balanceado)
        boolean isComplete = dailyRecords.stream()
                .anyMatch(r -> r.getRecordType().equals("OUT")) && checkIn == null; // Si checkIn es null, el último IN fue emparejado

        // Obtener la primera entrada y la última salida para el resumen visual
        LocalTime firstIn = dailyRecords.stream()
                .filter(r -> "IN".equals(r.getRecordType()))
                .findFirst()
                .map(r -> r.getDeviceTimestamp().toLocalTime())
                .orElse(null);

        LocalTime lastOut = dailyRecords.stream()
                .filter(r -> "OUT".equals(r.getRecordType()))
                .reduce((a, b) -> b) // Obtener el último elemento
                .map(r -> r.getDeviceTimestamp().toLocalTime())
                .orElse(null);

        return DailyWorkSummary.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .date(date)
                .checkInTime(firstIn)
                .checkOutTime(lastOut)
                .totalDuration(formatDuration(totalMinutes))
                .totalMinutes(totalMinutes)
                .isComplete(isComplete)
                .build();
    }
    
    /**
     * Convierte los minutos totales a un formato legible (Ej: 480 -> "8h 0m").
     */
    private String formatDuration(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }
}
