package com.agro.control_asistencia_backend.employee.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRequestDTO {

    @NotBlank
    @Size(max = 10, message = "El código de empleado no puede exceder los 10 caracteres.")
    private String employeeCode; 

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(max = 30)
    private String position; 
    
    //Simulation Biomedic
    @NotBlank
    @Size(min = 32, max = 64, message = "El hash biométrico debe tener un tamaño válido.")
    private String biometricHash; 

    // Data them user
    @NotBlank
    @Size(min = 4, max = 50)
    private String username; 
    
    // auto password
    private String password; 

    //Rol name
    @NotBlank
    private String roleName; 
}
