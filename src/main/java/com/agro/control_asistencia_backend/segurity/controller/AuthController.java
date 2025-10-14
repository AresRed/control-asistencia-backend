package com.agro.control_asistencia_backend.segurity.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agro.control_asistencia_backend.segurity.jwt.JwtUtils;
import com.agro.control_asistencia_backend.segurity.model.JwtResponseDTO;
import com.agro.control_asistencia_backend.segurity.model.MessageResponse; // Asegúrate que la importación apunte al paquete 'model'
import com.agro.control_asistencia_backend.segurity.model.LoginRequestDTO;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager; // Componente para validar credenciales

    @Autowired
    JwtUtils jwtUtils; // Para generar el token

    @PostMapping("/login") // Endpoint: POST /api/auth/login
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {

        System.out.println("\n\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!! INTENTO DE LOGIN RECIBIDO EN EL BACKEND !!!");
        System.out.println("!!! Usuario: " + loginRequest.getUsername());
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        // Quitar el prefijo "ROLE_" para la respuesta al frontend
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return ResponseEntity.ok(new JwtResponseDTO(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                role));
    }

    
    @PostMapping("/logout")
    // Spring inyecta automáticamente los detalles del usuario autenticado aquí
    public ResponseEntity<MessageResponse> logout(
        HttpServletRequest request, 
        @AuthenticationPrincipal UserDetailsImpl userDetails) { // Capturamos el UserDetails
        
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7); 
            
            // Llama a invalidateToken, pasando los detalles del usuario
            jwtUtils.invalidateToken(token, userDetails.getId()); 
            
            return ResponseEntity.ok(new MessageResponse("Logout exitoso para el usuario " + userDetails.getUsername()));
        }
        
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Token no encontrado en el encabezado."));
    }

}
