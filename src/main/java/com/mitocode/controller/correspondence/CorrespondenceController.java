package com.mitocode.controller.correspondence;

import com.mitocode.dto.request.correspondence.CorrespondenceRequest;
import com.mitocode.dto.response.correspondence.AttachmentResponse;
import com.mitocode.dto.response.correspondence.CorrespondenceResponse;
import com.mitocode.service.correspondence.ICorrespondenceService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/correspondence")
@RequiredArgsConstructor
public class CorrespondenceController {

    private final ICorrespondenceService correspondenceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCorrespondence(
            @ModelAttribute CorrespondenceRequest request
    ) throws IOException {

        CorrespondenceResponse response = correspondenceService.createCorrespondence(request);

        return ResponseEntity.ok(
                new ApiResponseUtil(true, "Correspondencia creada exitosamente", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<Optional<CorrespondenceResponse>>> getById(@PathVariable Long id) {
        Optional<CorrespondenceResponse> correspondence = correspondenceService.getCorrespondenceById(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondence found", correspondence));
    }

    // 📄 Listar Correspondencias por Departamento
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> getByDepartment(@PathVariable Long departmentId) {
        List<CorrespondenceResponse> responses = correspondenceService.getByDepartment(departmentId);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondencias encontradas", responses));
    }

    // 📌 Listar Correspondencias por Fecha de Envío
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> getByDate(@PathVariable String date) {
        List<CorrespondenceResponse> responses = correspondenceService.getByDate(date);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondencias encontradas", responses));
    }

    // 📌 Listar Correspondencias expiradas
    @GetMapping("/expireDate")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> getExpireCorrespondence() {
        List<CorrespondenceResponse> responses = correspondenceService.getExpiredCorrespondences();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondencias encontradas", responses));
    }

    /*
    // 📥 Ver Correspondencias Recibidas por Usuario Autenticado
    @GetMapping("/received")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> getReceived() {
        List<CorrespondenceResponse> responses = correspondenceService.getReceivedCorrespondences();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondencias recibidas", responses));
    }
     */

    // 📤 Ver Correspondencias Enviadas por Usuario Autenticado
    @GetMapping("/sent")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> getSent() {
        List<CorrespondenceResponse> responses = correspondenceService.getSentCorrespondences();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Correspondencias enviadas", responses));
    }

    // 📊 Dashboard de Correspondencias (Resumen de estado)
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseUtil<Map<String, Long>>> getDashboard() {
        Map<String, Long> summary = correspondenceService.getCorrespondenceSummary();
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Resumen de correspondencias", summary));
    }

    // 🔍 Buscar Correspondencias por Texto
    @GetMapping("/search")
    public ResponseEntity<ApiResponseUtil<List<CorrespondenceResponse>>> searchCorrespondences(@RequestParam String query) {
        List<CorrespondenceResponse> responses = correspondenceService.searchCorrespondences(query);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Resultados de búsqueda", responses));
    }

    // 📎 Descargar Adjuntos de una Correspondencia
    @GetMapping("/{id}/attachments")
    public ResponseEntity<ApiResponseUtil<List<AttachmentResponse>>> getAttachments(@PathVariable Long id) {
        List<AttachmentResponse> attachments = correspondenceService.getAttachmentsByCorrespondence(id);
        return ResponseEntity.ok(new ApiResponseUtil<>(true, "Adjuntos obtenidos", attachments));
    }

}
