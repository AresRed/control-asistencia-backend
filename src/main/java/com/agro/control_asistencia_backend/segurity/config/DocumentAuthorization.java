package com.agro.control_asistencia_backend.segurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.document.repository.DocumentRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;

@Component("documentAuthorization") 
public class DocumentAuthorization {


    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public DocumentAuthorization(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public boolean canView(Long documentId, String username) {
        
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;
        
        
        Long userId = userRepository.findByUsername(username)
                            .map(user -> user.getId()).orElse(0L);
        
        if (doc.getEmployee() != null && doc.getEmployee().getUser() != null) {
            return doc.getEmployee().getUser().getId().equals(userId);
        }
        
        return false;
    }
}
