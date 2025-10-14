package com.agro.control_asistencia_backend.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.document.model.entity.RequestType;

public interface RequestTypeRepository extends JpaRepository<RequestType, Long> {

    
}
