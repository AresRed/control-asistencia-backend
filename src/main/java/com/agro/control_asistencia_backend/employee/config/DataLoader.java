package com.agro.control_asistencia_backend.employee.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.repository.RoleRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner{

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección de dependencias por constructor
    public DataLoader(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // Ejecutar solo si no hay roles en la base de datos
        if (roleRepository.count() == 0) {
            loadRoles();
            loadAdminUser();
        }
    }

    private void loadRoles() {
        System.out.println("-> Inicializando Roles...");

        // 1. Cargar Rol ADMIN
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);

        // 2. Cargar Rol RRHH
        Role rrhhRole = new Role();
        rrhhRole.setName(ERole.ROLE_RRHH);
        roleRepository.save(rrhhRole);

        // 3. Cargar Rol EMPLOYEE
        Role employeeRole = new Role();
        employeeRole.setName(ERole.ROLE_EMPLOYEE);
        roleRepository.save(employeeRole);

        System.out.println("-> Roles cargados exitosamente.");
    }

    private void loadAdminUser() {
        System.out.println("-> Creando Usuario Administrador...");
        
        // 1. Buscar el rol ADMIN
        Optional<Role> adminRoleOpt = roleRepository.findByName(ERole.ROLE_ADMIN);

        if (adminRoleOpt.isPresent() && !userRepository.existsByUsername("Ares")) {
            
            User adminUser = new User();
            adminUser.setUsername("Ares");
            
            
            adminUser.setPassword(passwordEncoder.encode("Ares123")); // Contraseña: admin123
            adminUser.setRole(adminRoleOpt.get());

            
            userRepository.save(adminUser);
            System.out.println("-> Usuario 'Ares' creado con contraseña: Ares123");
        }
    }

}
