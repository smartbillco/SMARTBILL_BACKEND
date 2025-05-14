package com.mitocode.controller.file;

import com.mitocode.dto.request.file.FileRequest;
import com.mitocode.dto.response.FileResponse;
import com.mitocode.dto.response.GroupedInvoiceResponse;
import com.mitocode.model.file.FileEntity;
import com.mitocode.service.file.IFileUploadService;
import com.mitocode.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final IFileUploadService fileUploadService;

    // Metodo para manejar la subida de un archivo
    @PostMapping("/upload")
    public ResponseEntity<ApiResponseUtil<List<String>>> uploadFile(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> response = fileUploadService.uploadFiles(files);
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(true, "Archivos procesados exitosamente", response);
            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponseUtil<List<String>> apiResponse = new ApiResponseUtil<>(false, "Error al subir archivos", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // Endpoint para buscar archivos por tipo y usuario
    @GetMapping("/search")
    public ResponseEntity<ApiResponseUtil<?>> searchFiles(@RequestParam String fileType) {
        try {
            Object searchResult = fileUploadService.searchFiles(fileType);

            if (searchResult instanceof Map) { // Si es un Map, significa que es de tipo invoice (agrupado)
                Map<String, List<FileEntity>> groupedFiles = (Map<String, List<FileEntity>>) searchResult;

                if (groupedFiles.isEmpty()) {
                    ApiResponseUtil<List<GroupedInvoiceResponse>> apiResponse = new ApiResponseUtil<>(false, "No se encontraron archivos agrupados", Collections.emptyList());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
                }

                List<GroupedInvoiceResponse> groupedInvoiceDTOs = groupedFiles.entrySet().stream()
                        .map(entry -> new GroupedInvoiceResponse(
                                entry.getKey(), // Código de la factura
                                entry.getValue().stream()
                                        .map(file -> new FileResponse(file.getId(), file.getFileName(), file.getStorageType(), file.getFileType(), file.getFileUrl()))
                                        .collect(Collectors.toList())))
                        .collect(Collectors.toList());

                ApiResponseUtil<List<GroupedInvoiceResponse>> apiResponse = new ApiResponseUtil<>(true, "Archivos agrupados encontrados", groupedInvoiceDTOs);
                return ResponseEntity.ok(apiResponse);

            } else { // Si es una lista normal de archivos
                List<FileEntity> fileEntities = (List<FileEntity>) searchResult;

                if (fileEntities.isEmpty()) {
                    ApiResponseUtil<List<FileRequest>> apiResponse = new ApiResponseUtil<>(false, "No se encontraron archivos", Collections.emptyList());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
                }

                List<FileRequest> fileDTOs = fileEntities.stream()
                        .map(file -> new FileRequest(file.getFileName(), file.getStorageType(), file.getFileType(), file.getFileUrl()))
                        .collect(Collectors.toList());

                ApiResponseUtil<List<FileRequest>> apiResponse = new ApiResponseUtil<>(true, "Archivos encontrados", fileDTOs);
                return ResponseEntity.ok(apiResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponseUtil<Object> apiResponse = new ApiResponseUtil<>(false, "Error al buscar archivos", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseUtil<String>> deleteFile(@PathVariable("id") Long fileId) {
        try {
            String message = fileUploadService.deleteFileById(fileId);
            return ResponseEntity.ok(new ApiResponseUtil<>(true, "Eliminación exitosa", message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseUtil<>(false, "Error al eliminar archivo", e.getMessage()));
        }
    }
}
