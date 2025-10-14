package com.agro.control_asistencia_backend.scheduling.model.dto;


import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;


@Data 
@Builder
public class ScheduleResponseDTO {

     private Long assignmentId;
    private Long employeeId;
    private String employeeCode;
    
    private String scheduleName;
    private LocalTime startTime;
    private LocalTime endTime;
    
    private LocalDate validFrom;
    private LocalDate validTo;
    private String workingDays;
}
