package com.agro.control_asistencia_backend.employee.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.document.model.entity.RequestType;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.repository.RequestTypeRepository;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.RoleRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;
import com.agro.control_asistencia_backend.employee.repository.WorkPositionRepository;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.repository.EmployeeScheduleRepository;
import com.agro.control_asistencia_backend.scheduling.repository.WorkScheduleRepository;

import jakarta.transaction.Transactional;

@Component
public class DataLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository; 
    private final WorkPositionRepository positionRepository; 
    private final AttendanceRepository attendanceRepository; 
    private final WorkScheduleRepository scheduleRepository; 
    private final EmployeeScheduleRepository employeeScheduleRepository; 
    private final EmployeeRequestRepository employeeRequestRepository; 
    private final RequestTypeRepository requestTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(RoleRepository roleRepository, UserRepository userRepository,
                      EmployeeRepository employeeRepository, WorkPositionRepository positionRepository,
                      AttendanceRepository attendanceRepository, WorkScheduleRepository scheduleRepository,
                      EmployeeScheduleRepository employeeScheduleRepository, EmployeeRequestRepository employeeRequestRepository,
                      RequestTypeRepository requestTypeRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.attendanceRepository = attendanceRepository;
        this.scheduleRepository = scheduleRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.employeeRequestRepository = employeeRequestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            loadRolesAndPositions();
        }
        if (userRepository.count() < 2) { // Evita recargar si ya hay usuarios básicos
            loadUsersAndData();
            loadSchedulesAndAttendance();
        }
    }

    // ---------------------------------------------------------------------
    // AUXILIARES DE CARGA DE ESTRUCTURA
    // ---------------------------------------------------------------------

    private void loadRolesAndPositions() {
        // 1. CARGAR ROLES
        for (ERole roleName : ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
        // 2. CARGAR CARGOS (WorkPositions)
        createPosition("Gerente General");
        createPosition("Subgerente de Operaciones");
        createPosition("Especialista RRHH");
        createPosition("Supervisor de Campo");
        createPosition("Supervisor de Mantenimiento");
        createPosition("Cosechador");
        createPosition("Operador de Maquinaria");
        createPosition("Técnico de Mantenimiento");
        createPosition("Asistente Administrativo");
        createPosition("Contador");
        
        // 3. CARGAR TIPOS DE SOLICITUD
        createRequestType("Permiso Personal");
        createRequestType("Solicitud de Vacaciones");
        createRequestType("Solicitud de Constancia de Trabajo");
        createRequestType("Solicitud de Permiso Médico");
        createRequestType("Solicitud de Cambio de Turno");
        createRequestType("Solicitud de Capacitación");
    }
    
    private void createPosition(String name) {
        if (positionRepository.findByName(name).isEmpty()) {
            WorkPosition pos = new WorkPosition();
            pos.setName(name);
            positionRepository.save(pos);
        }
    }
    
    private void createRequestType(String name) {
        if (requestTypeRepository.findByName(name).isEmpty()) {
            RequestType type = new RequestType();
            type.setName(name);
            requestTypeRepository.save(type);
        }
    }

    // -------------------------------------------------------------
    // FUNCIÓN CENTRAL PARA CREAR USUARIO Y EMPLEADO
    // -------------------------------------------------------------
    private Employee createUserAndEmployee(String username, String password, String code, String first, String last, String dni, String email, String positionName, double fixed, double hourly, Role role, WorkPosition position) {
        
        if (userRepository.findByUsername(username).isPresent()) return null; // Salir si ya existe
        
        // Buscar el cargo por su nombre (ya debe existir)
        WorkPosition posEntity = positionRepository.findByName(positionName).orElseThrow(() -> new RuntimeException("Cargo no encontrado: " + positionName));
        
        // Crear User
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user = userRepository.save(user);

        // Crear Employee
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setDni(dni);
        employee.setEmail(email);
        employee.setPhoneNumber("9" + dni.substring(2)); 
        employee.setAddress("Calle Falsa 123, Ica"); 
        employee.setBiometricHash("HASH-" + code + "-UNIQUE-ID-78901234567890123456"); 
        
        employee.setFixedSalary(new BigDecimal(fixed)); 
        employee.setHourlyRate(new BigDecimal(hourly)); 
        
        employee.setUser(user);
        employee.setPosition(posEntity);
        
        return employeeRepository.save(employee);
    }
    
    // ---------------------------------------------------------------------
    // CARGAR USUARIOS, HORARIOS, Y ASISTENCIA DE PRUEBA
    // ---------------------------------------------------------------------

    private void loadUsersAndData() {
        
        // Obtener roles y cargos
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).get();
        Role rrhhRole = roleRepository.findByName(ERole.ROLE_RRHH).get();
        Role employeeRole = roleRepository.findByName(ERole.ROLE_EMPLOYEE).get();
        
        WorkPosition gerencia = positionRepository.findByName("Gerente General").get();
        WorkPosition subgerencia = positionRepository.findByName("Subgerente de Operaciones").get();
        WorkPosition rrhhPos = positionRepository.findByName("Especialista RRHH").get();
        WorkPosition supervisorCampo = positionRepository.findByName("Supervisor de Campo").get();
        WorkPosition supervisorMantenimiento = positionRepository.findByName("Supervisor de Mantenimiento").get();
        WorkPosition cosechador = positionRepository.findByName("Cosechador").get();
        WorkPosition operadorMaquinaria = positionRepository.findByName("Operador de Maquinaria").get();
        WorkPosition tecnicoMantenimiento = positionRepository.findByName("Técnico de Mantenimiento").get();
        WorkPosition asistenteAdmin = positionRepository.findByName("Asistente Administrativo").get();
        WorkPosition contador = positionRepository.findByName("Contador").get();
        
        // =================================================================
        // ADMINISTRADORES (2)
        // =================================================================
        
        // 1. ADMINISTRADOR PRINCIPAL (admin / admin123)
        createUserAndEmployee("admin", "admin123", "ADM-001", "Carlos", "Mendoza", "12345678", "carlos.mendoza@agrocyt.com", "Gerente General", 6000.00, 60.00, adminRole, gerencia);

        // 2. SUBGERENTE ADMINISTRADOR (subadmin / subadmin123)
        createUserAndEmployee("subadmin", "subadmin123", "ADM-002", "Roberto", "Silva", "87654321", "roberto.silva@agrocyt.com", "Subgerente de Operaciones", 4500.00, 45.00, adminRole, subgerencia);
        
        // =================================================================
        // ESPECIALISTA RRHH
        // =================================================================
        
        // 3. ESPECIALISTA RRHH (rrhh / rrhh123)
        Employee rrhhEmployee = createUserAndEmployee("rrhh", "rrhh123", "RRHH-001", "Ana", "Gómez", "88776655", "ana.gomez@agrocyt.com", "Especialista RRHH", 3500.00, 35.00, rrhhRole, rrhhPos);
        
        // =================================================================
        // SUPERVISORES
        // =================================================================
        
        // 4. SUPERVISOR DE CAMPO (supervisor1 / sup123)
        Employee supervisor1 = createUserAndEmployee("supervisor1", "sup123", "SUP-001", "Miguel", "Torres", "11223344", "miguel.torres@agrocyt.com", "Supervisor de Campo", 2800.00, 28.00, employeeRole, supervisorCampo);
        
        // 5. SUPERVISOR DE MANTENIMIENTO (supervisor2 / sup123)
        Employee supervisor2 = createUserAndEmployee("supervisor2", "sup123", "SUP-002", "Elena", "Vargas", "55667788", "elena.vargas@agrocyt.com", "Supervisor de Mantenimiento", 2800.00, 28.00, employeeRole, supervisorMantenimiento);
        
        // =================================================================
        // TRABAJADORES (5)
        // =================================================================
        
        // 6. COSECHADOR 1 (maria / pass123)
        Employee maria = createUserAndEmployee("maria", "pass123", "EMP-001", "María", "López", "45454545", "maria.lopez@agrocyt.com", "Cosechador", 1200.00, 15.00, employeeRole, cosechador);
        
        // 7. COSECHADOR 2 (juan / pass123)
        Employee juan = createUserAndEmployee("juan", "pass123", "EMP-002", "Juan", "Pérez", "32323232", "juan.perez@agrocyt.com", "Cosechador", 1200.00, 15.00, employeeRole, cosechador);
        
        // 8. OPERADOR DE MAQUINARIA (pedro / pass123)
        Employee pedro = createUserAndEmployee("pedro", "pass123", "EMP-003", "Pedro", "Ramírez", "99887766", "pedro.ramirez@agrocyt.com", "Operador de Maquinaria", 1500.00, 18.00, employeeRole, operadorMaquinaria);
        
        // 9. TÉCNICO DE MANTENIMIENTO (lucia / pass123)
        Employee lucia = createUserAndEmployee("lucia", "pass123", "EMP-004", "Lucía", "Fernández", "55443322", "lucia.fernandez@agrocyt.com", "Técnico de Mantenimiento", 1400.00, 17.00, employeeRole, tecnicoMantenimiento);
        
        // 10. ASISTENTE ADMINISTRATIVO (carmen / pass123)
        Employee carmen = createUserAndEmployee("carmen", "pass123", "EMP-005", "Carmen", "Ruiz", "77665544", "carmen.ruiz@agrocyt.com", "Asistente Administrativo", 1300.00, 16.00, employeeRole, asistenteAdmin);
        
        // 11. CONTADOR (diego / pass123)
        Employee diego = createUserAndEmployee("diego", "pass123", "EMP-006", "Diego", "Morales", "33445566", "diego.morales@agrocyt.com", "Contador", 2000.00, 25.00, employeeRole, contador);
        
        System.out.println("-> Usuarios completos creados: 2 Administradores, 1 RRHH, 2 Supervisores, 6 Trabajadores");
    }

    private void loadSchedulesAndAttendance() {
        
        // Obtener empleados
        Employee maria = employeeRepository.findByEmployeeCode("EMP-001").get();
        Employee juan = employeeRepository.findByEmployeeCode("EMP-002").get();
        Employee pedro = employeeRepository.findByEmployeeCode("EMP-003").get();
        Employee lucia = employeeRepository.findByEmployeeCode("EMP-004").get();
        Employee carmen = employeeRepository.findByEmployeeCode("EMP-005").get();
        Employee diego = employeeRepository.findByEmployeeCode("EMP-006").get();
        Employee supervisor1 = employeeRepository.findByEmployeeCode("SUP-001").get();
        Employee supervisor2 = employeeRepository.findByEmployeeCode("SUP-002").get();
        
        // =================================================================
        // CREAR HORARIOS DE TRABAJO
        // =================================================================
        
        // Turno Mañana (7:00 - 15:00) - Para cosechadores
        WorkSchedule turnoManana = new WorkSchedule();
        turnoManana.setName("Turno Cosecha Mañana");
        turnoManana.setStartTime(LocalTime.of(7, 0));
        turnoManana.setEndTime(LocalTime.of(15, 0));
        turnoManana.setToleranceMinutes(15);
        turnoManana = scheduleRepository.save(turnoManana);
        
        // Turno Tarde (15:00 - 23:00) - Para operadores de maquinaria
        WorkSchedule turnoTarde = new WorkSchedule();
        turnoTarde.setName("Turno Operación Tarde");
        turnoTarde.setStartTime(LocalTime.of(15, 0));
        turnoTarde.setEndTime(LocalTime.of(23, 0));
        turnoTarde.setToleranceMinutes(15);
        turnoTarde = scheduleRepository.save(turnoTarde);
        
        // Turno Administrativo (8:00 - 17:00) - Para personal administrativo
        WorkSchedule turnoAdmin = new WorkSchedule();
        turnoAdmin.setName("Turno Administrativo");
        turnoAdmin.setStartTime(LocalTime.of(8, 0));
        turnoAdmin.setEndTime(LocalTime.of(17, 0));
        turnoAdmin.setToleranceMinutes(10);
        turnoAdmin = scheduleRepository.save(turnoAdmin);
        
        // Turno Mantenimiento (6:00 - 14:00) - Para técnicos
        WorkSchedule turnoMantenimiento = new WorkSchedule();
        turnoMantenimiento.setName("Turno Mantenimiento");
        turnoMantenimiento.setStartTime(LocalTime.of(6, 0));
        turnoMantenimiento.setEndTime(LocalTime.of(14, 0));
        turnoMantenimiento.setToleranceMinutes(15);
        turnoMantenimiento = scheduleRepository.save(turnoMantenimiento);
        
        // =================================================================
        // ASIGNAR HORARIOS A EMPLEADOS
        // =================================================================
        
        // María y Juan - Turno Mañana (Cosechadores)
        assignScheduleToEmployee(maria, turnoManana, "LUN,MAR,MIE,JUE,VIE");
        assignScheduleToEmployee(juan, turnoManana, "LUN,MAR,MIE,JUE,VIE");
        
        // Pedro - Turno Tarde (Operador de Maquinaria)
        assignScheduleToEmployee(pedro, turnoTarde, "LUN,MAR,MIE,JUE,VIE");
        
        // Lucía - Turno Mantenimiento (Técnico)
        assignScheduleToEmployee(lucia, turnoMantenimiento, "LUN,MAR,MIE,JUE,VIE");
        
        // Carmen y Diego - Turno Administrativo
        assignScheduleToEmployee(carmen, turnoAdmin, "LUN,MAR,MIE,JUE,VIE");
        assignScheduleToEmployee(diego, turnoAdmin, "LUN,MAR,MIE,JUE,VIE");
        
        // Supervisores - Turno Administrativo
        assignScheduleToEmployee(supervisor1, turnoAdmin, "LUN,MAR,MIE,JUE,VIE");
        assignScheduleToEmployee(supervisor2, turnoAdmin, "LUN,MAR,MIE,JUE,VIE");
        
        // =================================================================
        // CREAR REGISTROS DE ASISTENCIA DE EJEMPLO
        // =================================================================
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // María - Registros completos (con horas extra)
        createAttendanceRecord(maria, "IN", LocalDateTime.of(today, LocalTime.of(7, 5)));
        createAttendanceRecord(maria, "OUT", LocalDateTime.of(today, LocalTime.of(15, 30))); // 30 min extra
        
        // Juan - Registro solo entrada (empleado que no salió)
        createAttendanceRecord(juan, "IN", LocalDateTime.of(today, LocalTime.of(7, 10)));
        
        // Pedro - Registros completos turno tarde
        createAttendanceRecord(pedro, "IN", LocalDateTime.of(today, LocalTime.of(14, 55)));
        createAttendanceRecord(pedro, "OUT", LocalDateTime.of(today, LocalTime.of(22, 45)));
        
        // Lucía - Registros mantenimiento
        createAttendanceRecord(lucia, "IN", LocalDateTime.of(today, LocalTime.of(6, 5)));
        createAttendanceRecord(lucia, "OUT", LocalDateTime.of(today, LocalTime.of(14, 10)));
        
        // Carmen - Registros administrativos
        createAttendanceRecord(carmen, "IN", LocalDateTime.of(today, LocalTime.of(8, 5)));
        createAttendanceRecord(carmen, "OUT", LocalDateTime.of(today, LocalTime.of(17, 15))); // 15 min extra
        
        // Diego - Registros administrativos
        createAttendanceRecord(diego, "IN", LocalDateTime.of(today, LocalTime.of(7, 55)));
        createAttendanceRecord(diego, "OUT", LocalDateTime.of(today, LocalTime.of(17, 5)));
        
        // Registros del día anterior
        createAttendanceRecord(maria, "IN", LocalDateTime.of(yesterday, LocalTime.of(7, 0)));
        createAttendanceRecord(maria, "OUT", LocalDateTime.of(yesterday, LocalTime.of(15, 0)));
        createAttendanceRecord(juan, "IN", LocalDateTime.of(yesterday, LocalTime.of(7, 15)));
        createAttendanceRecord(juan, "OUT", LocalDateTime.of(yesterday, LocalTime.of(15, 10)));
        
        // =================================================================
        // CREAR SOLICITUDES DE EJEMPLO
        // =================================================================
        
        RequestType permisoType = requestTypeRepository.findByName("Permiso Personal").get();
        RequestType vacacionesType = requestTypeRepository.findByName("Solicitud de Vacaciones").get();
        RequestType constanciaType = requestTypeRepository.findByName("Solicitud de Constancia de Trabajo").get();
        RequestType medicoType = requestTypeRepository.findByName("Solicitud de Permiso Médico").get();
        RequestType cambioTurnoType = requestTypeRepository.findByName("Solicitud de Cambio de Turno").get();
        
        // María - Permiso personal pendiente
        createEmployeeRequest(maria, permisoType, "Cita médica de emergencia", "PENDING", 
                            LocalDate.now().plusDays(2), LocalDate.now().plusDays(2));
        
        // Juan - Solicitud de vacaciones aprobada
        createEmployeeRequest(juan, vacacionesType, "Vacaciones familiares", "APPROVED", 
                            LocalDate.now().plusDays(10), LocalDate.now().plusDays(17));
        
        // Pedro - Constancia de trabajo aprobada
        createEmployeeRequest(pedro, constanciaType, "Para trámite bancario", "APPROVED", 
                            LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
        
        // Lucía - Permiso médico pendiente
        createEmployeeRequest(lucia, medicoType, "Cirugía programada", "PENDING", 
                            LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));
        
        // Carmen - Cambio de turno rechazado
        createEmployeeRequest(carmen, cambioTurnoType, "Por motivos familiares", "REJECTED", 
                            LocalDate.now().plusDays(3), LocalDate.now().plusDays(3));
        
        // Diego - Solicitud de vacaciones pendiente
        createEmployeeRequest(diego, vacacionesType, "Vacaciones de fin de año", "PENDING", 
                            LocalDate.now().plusDays(20), LocalDate.now().plusDays(30));
        
        System.out.println("-> Datos completos cargados: Horarios, Asistencia y Solicitudes");
    }
    
    // =================================================================
    // MÉTODOS AUXILIARES
    // =================================================================
    
    private void assignScheduleToEmployee(Employee employee, WorkSchedule schedule, String workingDays) {
        EmployeeSchedule empSchedule = new EmployeeSchedule();
        empSchedule.setEmployee(employee);
        empSchedule.setWorkSchedule(schedule);
        empSchedule.setValidFrom(LocalDate.now().minusMonths(1));
        empSchedule.setWorkingDays(workingDays);
        employeeScheduleRepository.save(empSchedule);
    }
    
    private void createAttendanceRecord(Employee employee, String recordType, LocalDateTime timestamp) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setRecordType(recordType);
        record.setDeviceTimestamp(timestamp);
        attendanceRepository.save(record);
    }
    
    private void createEmployeeRequest(Employee employee, RequestType requestType, String details, 
                                     String status, LocalDate startDate, LocalDate endDate) {
        EmployeeRequest request = new EmployeeRequest();
        request.setEmployee(employee);
        request.setRequestType(requestType);
        request.setDetails(details);
        request.setRequestedDate(LocalDateTime.now().minusDays(1));
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setStatus(status);
        employeeRequestRepository.save(request);
    }
}
