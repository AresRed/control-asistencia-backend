package com.agro.control_asistencia_backend.document.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO;
import com.agro.control_asistencia_backend.document.model.dto.FileUploadDTO;
import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ---------------------------------------------------------------------
    // Endpoint para Cargar Archivos (Upload)
    // ---------------------------------------------------------------------

    // Endpoint: POST /api/documents/upload
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    // Usamos @RequestPart para recibir JSON y el archivo binario
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @RequestPart("metadata") @Valid FileUploadDTO metadata,
            @RequestPart("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl uploader) throws IOException {

        DocumentResponseDTO response = documentService.uploadDocument(
                metadata.getEmployeeId(),
                metadata.getDocumentType(),
                file,uploader);

        return ResponseEntity.ok(response); // Devuelve JSON de los metadatos guardados
    }

    // ---------------------------------------------------------------------
    // Endpoint para Descargar Archivos (Download)
    // ---------------------------------------------------------------------

    // Endpoint: GET /api/documents/{id}/download
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH') or @documentAuthorization.canView(#id, principal.username)")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws MalformedURLException {

        // 1. Obtener los metadatos del documento
        Document document = documentService.getDocumentById(id);

        // 2. Cargar el archivo físico
        Path filePath = Paths.get(document.getStoragePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new RuntimeException("Archivo no encontrado: " + document.getFileName());
        }

        // 3. Construir la respuesta HTTP para descarga
        String contentType = document.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }

    @Data
    class GenerationRequestDTO {
        @NotNull
        private Long employeeId;
        @NotNull
        private Long templateId;
    }

    // Endpoint: POST /api/documents/generate
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<DocumentResponseDTO> generateDocument(
            @RequestBody GenerationRequestDTO request,
            @AuthenticationPrincipal UserDetailsImpl user) throws IOException {

        // Llamar al servicio de generación
        DocumentResponseDTO response = documentService.generateDocument(
                request.getEmployeeId(),
                request.getTemplateId(),
                user);

        return ResponseEntity.ok(response); // Devuelve el JSON del documento creado
    }
}
