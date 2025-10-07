package com.agro.control_asistencia_backend.employee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.RoleRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;

import jakarta.transaction.Transactional;

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

    @Transactional // CRÍTICO: Asegura que User y Employee se guarden correctamente o nada se
                   // guarde
    public Employee createEmployee(EmployeeRequestDTO requestDTO) {

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
        // Cifrar la contraseña
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

        // Vincular el User recién creado
        employee.setUser(user);

        return employeeRepository.save(employee);
    }

    // Método simple para leer todos los empleados
    public List

    <Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
}
