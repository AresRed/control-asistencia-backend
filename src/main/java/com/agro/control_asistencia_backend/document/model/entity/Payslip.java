package com.agro.control_asistencia_backend.document.model.entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "payslips")
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate payPeriodStart;
    private LocalDate payPeriodEnd;
    private Long totalRegularMinutes; 
    private Long totalOvertimeMinutes; 
    private BigDecimal regularPay;
    private BigDecimal overtimePay;
    private BigDecimal netSalary; 
    private String documentPath;

}
