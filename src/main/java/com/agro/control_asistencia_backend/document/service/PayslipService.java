package com.agro.control_asistencia_backend.document.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.repository.PayslipRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;

@Service
public class PayslipService {

    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private ReportingService reportingService;
    @Autowired
    private PayslipRepository payslipRepo;
    @Autowired
    private DocumentService documentService;

   
    
    public List<Payslip> getPayslipsByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
        return payslipRepo.findByEmployee(employee);
    }

    /**
     * Método principal para generar una Boleta de Pago para un período.
     * Este método es el que debes usar.
     */
    @Transactional
    public Payslip generatePayslip(Long employeeId, LocalDate start, LocalDate end) {

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para generar boleta."));

        // 1. Acumular Horas de Asistencia
        long totalRegularMins = 0;
        long totalOvertimeMins = 0;

        // Itera sobre el rango de fechas
        for (LocalDate date = start; date.isBefore(end.plusDays(1)); date = date.plusDays(1)) {
            // Usa el nuevo método de ReportingService
            DailyWorkSummary summary = reportingService.getDailySummaryByEmployeeAndDate(employeeId, date);

            totalRegularMins += summary.getTotalMinutes();
            totalOvertimeMins += summary.getOvertimeMinutes();
        }

        // 2. Calcular Pago Monetario
        // Tarifa por minuto: (HourlyRate / 60)
        BigDecimal ratePerMinute = employee.getHourlyRate().divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        // Tarifa por minuto para la hora extra (ej: 50% extra)
        BigDecimal overtimeRatePerMinute = ratePerMinute.multiply(BigDecimal.valueOf(1.5));

        // Cálculo del pago
        BigDecimal regularPay = ratePerMinute.multiply(BigDecimal.valueOf(totalRegularMins)).setScale(2,
                RoundingMode.HALF_UP);
        BigDecimal overtimePay = overtimeRatePerMinute.multiply(BigDecimal.valueOf(totalOvertimeMins)).setScale(2,
                RoundingMode.HALF_UP);

        // Sueldo Fijo + Pago por Horas Trabajadas (Normales + Extra)
        BigDecimal netSalary = employee.getFixedSalary().add(regularPay).add(overtimePay).setScale(2,
                RoundingMode.HALF_UP);

        // 3. Crear y SETEAR la Entidad Payslip (¡Corrección!)
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setPayPeriodStart(start);
        payslip.setPayPeriodEnd(end);

        // Setear los valores calculados:
        payslip.setTotalRegularMinutes(totalRegularMins);
        payslip.setTotalOvertimeMinutes(totalOvertimeMins);
        payslip.setRegularPay(regularPay);
        payslip.setOvertimePay(overtimePay);
        payslip.setNetSalary(netSalary); // Salario Neto final

        // 4. Generar y Almacenar el PDF (Simulado)
        String documentPath = documentService.generatePayslipPdf(payslip);
        payslip.setDocumentPath(documentPath);

        return payslipRepo.save(payslip);
    }

    @Transactional(readOnly = true)
    public List<Payslip> getAllPayslips() {
        return payslipRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payslip> getPayslipsByEmployeeCode(String employeeCode) {
    // 1. Obtener el objeto Employee
    Employee employee = employeeRepo.findByEmployeeCode(employeeCode)
        .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
        
    // 2. Usar el repositorio de Payslips para filtrar
    return payslipRepo.findByEmployee(employee);
}
}
