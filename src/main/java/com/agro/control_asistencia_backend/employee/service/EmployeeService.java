package com.agro.control_asistencia_backend.employee.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeResponseDTO;
import com.agro.control_asistencia_backend.employee.model.dto.ManagerContactDTO;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.RoleRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeProfileDTO;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        // Asegúrate de que User y Role estén cargados (EAGER o @Transactional)
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFirstName() + " " + employee.getLastName())
                .position(employee.getPosition())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .roleName(employee.getUser().getRole().getName().name())
                .build();
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) { // 💡 Cambiar a DTO de retorno

        // 1. Validaciones
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (employeeRepository.findByBiometricHash(requestDTO.getBiometricHash()).isPresent()) {
            throw new RuntimeException("Error: Biometric Hash is already registered!");
        }

        // 2. Buscar Rol
        ERole roleEnum = ERole.valueOf(requestDTO.getRoleName());
        Role employeeRole = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));

        // 3. Crear y guardar el User (credenciales)
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(employeeRole);
        user = userRepository.save(user);

        // 4. Crear y guardar el Employee (datos de campo)
        Employee employee = new Employee();
        employee.setEmployeeCode(requestDTO.getEmployeeCode());
        employee.setFirstName(requestDTO.getFirstName());
        employee.setLastName(requestDTO.getLastName());
        employee.setPosition(requestDTO.getPosition());
        employee.setBiometricHash(requestDTO.getBiometricHash());

        // Asignar Salario (Nota: Estos campos deben venir del DTO si se editan en el
        // formulario)
        // Ejemplo simplificado:
        employee.setFixedSalary(new BigDecimal("1200.00"));
        employee.setHourlyRate(new BigDecimal("10.00"));

        // Vincular el User recién creado
        employee.setUser(user);

        Employee savedEmployee = employeeRepository.save(employee); // Guardar

        // 5. Devolver el DTO para el frontend
        return mapToResponseDTO(savedEmployee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() { // 💡 Devolver List<DTO>
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado."));
    }

    @Transactional
    public EmployeeProfileDTO updateProfile(Long userId, EmployeeRequestDTO updateDTO) {
        // 1. Buscar el empleado existente
        Employee existingEmployee = getEmployeeByUserId(userId);

        // 2. Aplicar las actualizaciones solo a campos editables
        existingEmployee.setFirstName(updateDTO.getFirstName());
        existingEmployee.setLastName(updateDTO.getLastName());

        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        throw new RuntimeException(
                "Error de lógica: El método updateProfile debe combinarse con ReportingService y devolver EmployeeProfileDTO.");

    }

    @Transactional(readOnly = true)
    public List<ManagerContactDTO> getManagersByRoles(String... roleNames) {

        List<ERole> roles = Arrays.stream(roleNames)
                .map(ERole::valueOf)
                .collect(Collectors.toList());

        return userRepository.findAll().stream()
                .filter(user -> roles.contains(user.getRole().getName()))
                .map(user -> {
                    Employee employee = employeeRepository.findByUserId(user.getId())
                            .orElse(null);

                    return ManagerContactDTO.builder()
                            .id(user.getId())
                            .fullName(employee != null ? employee.getFirstName() + " " + employee.getLastName()
                                    : user.getUsername())
                            .position(employee != null ? employee.getPosition() : "N/A")
                            .roleName(user.getRole().getName().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

}
