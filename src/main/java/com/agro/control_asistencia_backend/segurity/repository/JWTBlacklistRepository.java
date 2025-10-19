package com.agro.control_asistencia_backend.segurity.repository;

public class JWTBlacklistRepository {

    boolean existsByToken(String token); 
}
